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

import com.google.protobuf.Any;
import com.google.protobuf.ByteString;
import com.google.protobuf.Empty;
import io.grpc.Server;
import io.grpc.ServerInterceptors;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import io.siddhi.annotation.Example;
import io.siddhi.annotation.Extension;
import io.siddhi.annotation.Parameter;
import io.siddhi.annotation.util.DataType;
import io.siddhi.core.exception.ConnectionUnavailableException;
import io.siddhi.core.exception.SiddhiAppCreationException;
import io.siddhi.core.exception.SiddhiAppRuntimeException;
import io.siddhi.core.util.snapshot.state.State;
import io.siddhi.core.util.transport.OptionHolder;
import io.siddhi.extension.io.grpc.util.GenericServiceClass;
import io.siddhi.extension.io.grpc.util.GrpcConstants;
import org.apache.log4j.Logger;
import org.wso2.grpc.Event;
import org.wso2.grpc.EventServiceGrpc;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static io.siddhi.extension.io.grpc.util.GrpcUtils.extractHeaders;
import static io.siddhi.extension.io.grpc.util.GrpcUtils.getRPCmethodList;

/**
 * This handles receiving requests from grpc clients and populating the stream
 */
@Extension(name = "grpc", namespace = "source",
        description = "This extension starts a grpc server during initialization time. The server listens to " +
                "requests from grpc stubs. This source has a default mode of operation and custom user defined grpc " +
                "service mode. By default this uses EventService. Please find the proto definition [here]" +
                "(https://github.com/siddhi-io/siddhi-io-grpc/tree/master/component/src/main/resources/" +
                "EventService.proto) In the default mode this source will use EventService consume method. This " +
                "method will receive requests and injects them into stream through a mapper.",
        parameters = {
                @Parameter(
                        name = "receiver.url",
                        description = "The url which can be used by a client to access the grpc server in this " +
                                "extension. This url should consist the host address, port, service name, method " +
                                "name in the following format. `grpc://0.0.0.0:9763/<serviceName>/<methodName>`",
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
        },
        examples = {
                @Example(syntax = "" +
                        "@source(type='grpc',\n" +
                        "       receiver.url='grpc://194.23.98.100:8888/org.wso2.grpc.EventService/consume',\n" +
                        "       @map(type='json'))\n" +
                        "define stream BarStream (message String);",
                        description = "Here the port is given as 8888. So a grpc server will be started on port 8888 " +
                                "and the server will expose EventService. This is the default service packed with " +
                                "the source. In EventService the consume method is"
                ),
                @Example(syntax = "" +
                        "@source(type='grpc',\n" +
                        "       receiver.url='grpc://194.23.98.100:8888/org.wso2.grpc.EventService/consume',\n" +
                        "       @map(type='json', @attributes(name='trp:name', age='trp:age', message='message')))\n" +
                        "define stream BarStream (message String, name String, age int);",
                        description = "Here we are getting headers sent with the request as transport properties and " +
                                "injecting them into the stream. With each request a header will be sent in MetaData " +
                                "in the following format: 'Name:John', 'Age:23'"
                ),
                @Example(syntax = "" +
                        "@source(type='grpc',\n" +
                        "       receiver.url='grpc://194.23.98.100:8888/org.wso2.grpc.test.MyService/send',\n" +
                        "       @map(type='protobuf', ))\n" +
                        "define stream BarStream (stringValue string, intValue int,longValue long,booleanValue bool," +
                        "floatValue float,doubleValue double);",
                        description = "Here the port is give as 8888. So a grpc server will be started on port 8888 " +
                                "and sever will be keep listening to the 'send method in the 'MyService' service."
                )
        }
)
public class GrpcSource extends AbstractGrpcSource {
    private static final Logger logger = Logger.getLogger(GrpcSource.class.getName());
    protected String[] requestedTransportPropertyNames;
    protected Server server;

    @Override
    public void initializeGrpcServer(int port) {
        if (isDefaultMode) {
            this.server = serverBuilder.addService(ServerInterceptors.intercept(
                    new EventServiceGrpc.EventServiceImplBase() {
                        @Override
                        public void consume(Event request,
                                            StreamObserver<Empty> responseObserver) {
                            if (request.getPayload() == null) {
                                logger.error(siddhiAppContext.getName() + ":" + streamID + ": Dropping request due to" +
                                        " missing payload ");
                                responseObserver.onError(new io.grpc.StatusRuntimeException(Status.DATA_LOSS));

                            } else {
                                try {
                                    sourceEventListener.onEvent(request.getPayload(),
                                            extractHeaders(request.getHeadersMap(),
                                            metaDataMap.get(), requestedTransportPropertyNames));
                                    responseObserver.onNext(Empty.getDefaultInstance());
                                    responseObserver.onCompleted();
                                } catch (SiddhiAppRuntimeException e) {
                                    logger.error(siddhiAppContext.getName() + ":" + streamID + ": Dropping request. "
                                            + e.getMessage(), e);
                                    responseObserver.onError(new io.grpc.StatusRuntimeException(Status.DATA_LOSS));
                                } finally {
                                    metaDataMap.remove();
                                }
                            }
                        }
                    }, serverInterceptor)).build();
        } else {

            GenericServiceClass.setServiceName(serviceName);
            GenericServiceClass.setEmptyResponseMethodName(super.methodName);
            GenericServiceClass.AnyServiceImplBase service = new GenericServiceClass.AnyServiceImplBase() {
                @Override
                public void handleEmptyResponse(Any request, StreamObserver<Empty> responseObserver) {
                    Object requestObject = null;
                    try {
                        Method parseFrom = requestClass.getDeclaredMethod(GrpcConstants.PARSE_FROM_METHOD_NAME,
                                ByteString.class);
                        requestObject = parseFrom.invoke(requestClass, request.toByteString());
                    } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                        throw new SiddhiAppCreationException(siddhiAppContext.getName() + ":" + streamID + ": Invalid" +
                                " method name provided in the url, provided method name : '" + methodName +
                                "' expected one of these methods : " + getRPCmethodList(serviceReference,
                                siddhiAppContext.getName()) + ". " + e.getMessage(), e);
                    }
                    sourceEventListener.onEvent(requestObject, null);
                    responseObserver.onNext(Empty.getDefaultInstance());
                    responseObserver.onCompleted();
                }
            };
            this.server = serverBuilder.addService(ServerInterceptors.intercept(service, serverInterceptor)).build();
        }
    }

    @Override
    public void initSource(OptionHolder optionHolder, String[] requestedTransportPropertyNames) {
        this.requestedTransportPropertyNames = requestedTransportPropertyNames.clone();
    }

    @Override
    public void connect(ConnectionCallback connectionCallback, State state) throws ConnectionUnavailableException {
        connectGrpcServer(server, logger, connectionCallback);
    }

    /**
     * This method can be called when it is needed to disconnect from the end point.
     */
    @Override
    public void disconnect() {
        disconnectGrpcServer(server, logger);
    }
}
