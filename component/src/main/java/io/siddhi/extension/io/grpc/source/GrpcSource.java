/*
 * Copyright (c)  2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package io.siddhi.extension.io.grpc.source;

import io.siddhi.annotation.Example;
import io.siddhi.annotation.Extension;
import io.siddhi.annotation.Parameter;
import io.siddhi.annotation.SystemParameter;
import io.siddhi.annotation.util.DataType;
import io.siddhi.core.exception.ConnectionUnavailableException;
import io.siddhi.core.util.snapshot.state.State;
import io.siddhi.core.util.transport.OptionHolder;
import io.siddhi.extension.io.grpc.util.GenericService;
import io.siddhi.extension.io.grpc.util.GrpcConstants;
import io.siddhi.extension.io.grpc.util.ServiceConfigs;
import io.siddhi.query.api.exception.SiddhiAppValidationException;
import org.apache.log4j.Logger;

import java.lang.reflect.Method;

import static io.siddhi.extension.io.grpc.util.GrpcUtils.getRpcMethodList;

/**
 * This handles receiving requests from grpc clients and populating the stream
 */
@Extension(name = "grpc", namespace = "source",
        description = "This extension starts a grpc server during initialization time. The server listens to " +
                "requests from grpc stubs. This source has a default mode of operation and custom user defined grpc " +
                "service mode. By default this uses EventService. Please find the proto " +
                "definition [here]" +
                "(https://github.com/siddhi-io/siddhi-io-grpc/tree/master/component/src/main/resources/" +
                "EventService.proto). In the default mode this source will use " +
                "EventService consume method. If we want to use our " +
                "custom gRPC services, we have to  pack auto-generated gRPC service classes and  protobuf classes " +
                "into a jar file and add it into the project classpath (or to the `jars` folder in the `siddhi-" +
                "tooling` folder if we use it with `siddhi-tooling`). Please find the custom protobuf definition that" +
                " uses in examples [here](https://github.com/siddhi-io/siddhi-io-grpc/tree/master/component/src/main/" +
                "resources/sample.proto)." +
                " This method will receive requests and injects them into stream through a mapper.",
        parameters = {
                @Parameter(
                        name = "receiver.url",
                        description = "The url which can be used by a client to access the grpc server in this " +
                                "extension. This url should consist the host hostPort, port, fully qualified service " +
                                "name, method name in the following format. `grpc://0.0.0.0:9763/<serviceName>/" +
                                "<methodName>`\n" +
                                "For example:\n" +
                                "grpc://0.0.0.0:9763/org.wso2.grpc.EventService/consume",
                        type = {DataType.STRING}),
                @Parameter(
                        name = "max.inbound.message.size",
                        description = "Sets the maximum message size in bytes allowed to be received on the server.",
                        type = {DataType.INT},
                        optional = true,
                        defaultValue = "4194304"),
                @Parameter(
                        name = "max.inbound.metadata.size",
                        description = "Sets the maximum size of metadata in bytes allowed to be received.",
                        type = {DataType.INT},
                        optional = true,
                        defaultValue = "8192"),
                @Parameter(
                        name = "server.shutdown.waiting.time",
                        description = "The time in seconds to wait for the server to shutdown, giving up " +
                                "if the timeout is reached.",
                        type = {DataType.LONG},
                        optional = true,
                        defaultValue = "5"),
                @Parameter(
                        name = "truststore.file",
                        description = "the file path of truststore. If this is provided then server authentication " +
                                "is enabled",
                        type = {DataType.STRING},
                        optional = true,
                        defaultValue = "-"),
                @Parameter(
                        name = "truststore.password",
                        description = "the password of truststore. If this is provided then the integrity of the " +
                                "keystore is checked",
                        type = {DataType.STRING},
                        optional = true,
                        defaultValue = "-"),
                @Parameter(
                        name = "truststore.algorithm",
                        description = "the encryption algorithm to be used for server authentication",
                        type = {DataType.STRING},
                        optional = true,
                        defaultValue = "-"),
                @Parameter(
                        name = "tls.store.type",
                        description = "TLS store type",
                        type = {DataType.STRING},
                        optional = true,
                        defaultValue = "-"),
                @Parameter(
                        name = "keystore.file",
                        description = "the file path of keystore. If this is provided then client authentication " +
                                "is enabled",
                        type = {DataType.STRING},
                        optional = true,
                        defaultValue = "-"),
                @Parameter(
                        name = "keystore.password",
                        description = "the password of keystore",
                        type = {DataType.STRING},
                        optional = true,
                        defaultValue = "-"),
                @Parameter(
                        name = "keystore.algorithm",
                        description = "the encryption algorithm to be used for client authentication",
                        type = {DataType.STRING},
                        optional = true,
                        defaultValue = "-"),
                @Parameter(
                        name = "enable.ssl",
                        description = "to enable ssl. If set to true and truststore.file is not given then it will " +
                                "be set to default carbon jks by default",
                        type = {DataType.BOOL},
                        optional = true,
                        defaultValue = "FALSE"),
                @Parameter(
                        name = "threadpool.size",
                        description = "Sets the maximum size of threadpool dedicated to serve requests at the gRPC " +
                                "server",
                        type = {DataType.INT},
                        optional = true,
                        defaultValue = "100"),
                @Parameter(
                        name = "threadpool.buffer.size",
                        description = "Sets the maximum size of threadpool buffer " +
                                "server",
                        type = {DataType.INT},
                        optional = true,
                        defaultValue = "100"),
        },
        examples = {
                @Example(syntax = "" +
                        "@source(type='grpc',\n" +
                        "       receiver.url='grpc://localhost:8888/org.wso2.grpc.EventService/consume',\n" +
                        "       @map(type='json'))\n" +
                        "define stream BarStream (message String);",
                        description = "Here the port is given as 8888. So a grpc server will be started on port 8888 " +
                                "and the server will expose EventService. This is the default service packed with " +
                                "the source. In EventService the consume method is"
                ),
                @Example(syntax = "" +
                        "@source(type='grpc',\n" +
                        "       receiver.url='grpc://localhost:8888/org.wso2.grpc.EventService/consume',\n" +
                        "       @map(type='json', @attributes(name='trp:name', age='trp:age', message='message')))\n" +
                        "define stream BarStream (message String, name String, age int);",
                        description = "Here we are getting headers sent with the request as transport properties and " +
                                "injecting them into the stream. With each request a header will be sent in MetaData " +
                                "in the following format: 'Name:John', 'Age:23'"
                ),
                @Example(syntax = "" +
                        "@source(type='grpc',\n" +
                        "       receiver.url='grpc://localhost:8888/org.wso2.grpc.MyService/send',\n" +
                        "       @map(type='protobuf'))\n" +
                        "define stream BarStream (stringValue string, intValue int,longValue long,booleanValue bool," +
                        "floatValue float,doubleValue double);",
                        description = "Here the port is given as 8888. So a grpc server will be started on port 8888 " +
                                "and sever will keep listening to the 'send' RPC method in the 'MyService' service."
                ),
                @Example(syntax = "" +
                        "@source(type='grpc',\n" +
                        "       receiver.url='grpc://localhost:8888/org.wso2.grpc.MyService/send',\n" +
                        "       @map(type='protobuf', \n" +
                        "@attributes(a = 'stringValue', b = 'intValue', c = 'longValue',d = 'booleanValue', e " +
                        "='floatValue', f ='doubleValue'))) \n" +
                        "define stream BarStream (a string ,c long,b int, d bool,e float,f double);",
                        description = "Here the port is given as 8888. So a grpc server will be started on port 8888 " +
                                "and sever will keep listening to the 'send' method in the 'MyService' service. Since" +
                                " we provide mapping in the stream we can use any names for stream attributes, but we" +
                                " have to map those names with correct protobuf message attributes' names. If we want" +
                                " to send metadata, we should map the attributes."
                ),
                @Example(syntax = "" +
                        "@source(type='grpc',\n" +
                        "       receiver.url='grpc://localhost:8888/org.wso2.grpc.StreamService/" +
                        "clientStream',\n" +
                        "       @map(type='protobuf')) \n" +
                        "define stream BarStream (stringValue string, intValue int,longValue long,booleanValue bool," +
                        "floatValue float,doubleValue double);",
                        description = "Here we receive a stream of requests to the grpc source. Whenever we want to " +
                                "use streaming with grpc source, we have to define the RPC method as client streaming" +
                                " method (look at the sample proto file provided in the resource folder[here]" +
                                "(https://github.com/siddhi-io/siddhi-io-grpc/tree/master/component/src/main/" +
                                "resources)), when we define a stream method siddhi will identify it as a stream RPC" +
                                " method and ready to accept stream of request from the client."
                )
        },
        systemParameter = {
                @SystemParameter(name = "keyStoreFile",
                        description = "Path of the key store file",
                        defaultValue = "src/main/resources/security/wso2carbon.jks",
                        possibleParameters = "valid path for a key store file"),
                @SystemParameter(name = "keyStorePassword",
                        description = "This is the password used with key store file",
                        defaultValue = "wso2carbon",
                        possibleParameters = "valid password for the key store file"),
                @SystemParameter(name = "keyStoreAlgorithm",
                        description = "The encryption algorithm to be used for client authentication",
                        defaultValue = "SunX509",
                        possibleParameters = "-"),
                @SystemParameter(name = "trustStoreFile",
                        description = "This is the trust store file with the path",
                        defaultValue = "src/main/resources/security/client-truststore.jks",
                        possibleParameters = "-"),
                @SystemParameter(name = "trustStorePassword",
                        description = "This is the password used with trust store file",
                        defaultValue = "wso2carbon",
                        possibleParameters = "valid password for the trust store file"),
                @SystemParameter(name = "trustStoreAlgorithm",
                        description = "the encryption algorithm to be used for server authentication",
                        defaultValue = "SunX509",
                        possibleParameters = "-"),
                @SystemParameter(name = "receiver.url",
                        description = "This is the client side data receiving url ",
                        defaultValue = "grpc://0.0.0.0:9763/org.wso2.grpc.EventService/consume",
                        possibleParameters = "-")
        }
)
public class GrpcSource extends AbstractGrpcSource {
    private static final Logger logger = Logger.getLogger(GrpcSource.class.getName());
    private GenericServiceServer genericServiceServer;

    @Override
    public void initSource(OptionHolder optionHolder, String[] requestedTransportPropertyNames) {
        if (grpcServerConfigs.getServiceConfigs().isDefaultService()) {
            GrpcServerManager.getInstance().registerSource(grpcServerConfigs, this,
                    GrpcConstants.DEFAULT_METHOD_NAME_WITHOUT_RESPONSE, siddhiAppContext, streamID);
        } else {
            GenericService.setServiceName(grpcServerConfigs.getServiceConfigs().getServiceName());
            if (isStreamMethod(grpcServerConfigs.getServiceConfigs())) {
                GenericService.setClientStreamMethodName(grpcServerConfigs.getServiceConfigs().getMethodName());
            } else {
                GenericService.setEmptyResponseMethodName(grpcServerConfigs.getServiceConfigs().getMethodName());
            }
            genericServiceServer = new GenericServiceServer(grpcServerConfigs, this, requestClass,
                    siddhiAppName, streamID);
        }
    }

    @Override
    public void logError(String message) {
        logger.error(siddhiAppContext.getName() + ": " + streamID + ": " + message);
    }

    @Override
    public void connect(ConnectionCallback connectionCallback, State state) throws ConnectionUnavailableException {
        if (grpcServerConfigs.getServiceConfigs().isDefaultService()) {
            if (GrpcServerManager.getInstance().getServer(grpcServerConfigs.getServiceConfigs().getPort())
                    .getState() == 0) {
                GrpcServerManager.getInstance().getServer(grpcServerConfigs.getServiceConfigs().getPort())
                        .connectServer(logger, connectionCallback, siddhiAppContext, streamID);
            }
        } else {
            genericServiceServer.connectServer(logger, connectionCallback, siddhiAppName, streamID);
        }
    }

    /**
     * This method can be called when it is needed to disconnect from the end point.
     */
    @Override
    public void disconnect() {
        if (grpcServerConfigs.getServiceConfigs().isDefaultService()) {
            GrpcServerManager.getInstance().unregisterSource(grpcServerConfigs.getServiceConfigs().getPort(), streamID,
                    GrpcConstants.DEFAULT_METHOD_NAME_WITHOUT_RESPONSE, logger, siddhiAppContext);
        } else {
            genericServiceServer.disconnectServer(logger, siddhiAppName, streamID);
        }
    }

    private boolean isStreamMethod(ServiceConfigs serviceConfigs) {
        Method rpcMethod = null;
        String stubReference = serviceConfigs.getFullyQualifiedServiceName() + GrpcConstants.
                GRPC_PROTOCOL_NAME_UPPERCAMELCASE + GrpcConstants.DOLLAR_SIGN + serviceConfigs.getServiceName()
                + GrpcConstants.STUB;
        try {
            Method[] methodsInStub = Class.forName(stubReference).getMethods();
            for (Method method : methodsInStub) {
                if (method.getName().equals(serviceConfigs.getMethodName())) { //cant use equalIgnoreCase
                    rpcMethod = method;
                    break;
                }
            }
            if (rpcMethod == null) { //only if user has provided a wrong method name
                throw new SiddhiAppValidationException(siddhiAppName + ":" + streamID + ": Invalid method name " +
                        "provided in the url, provided method name: '" + serviceConfigs.getMethodName() +
                        "', expected one of these methods: " + getRpcMethodList(serviceConfigs, siddhiAppName,
                        streamID));
            }
        } catch (ClassNotFoundException e) {
            throw new SiddhiAppValidationException(siddhiAppName + ":" + streamID + ": Invalid service name " +
                    "provided in the url, provided service name: '" + serviceConfigs
                    .getFullyQualifiedServiceName() + "'", e);
        }
        if (rpcMethod.getParameterCount() == 1) {
            return true;
        }
        return false;
    }
}
