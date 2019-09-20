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
package io.siddhi.extension.io.grpc.sink;

import com.google.protobuf.Empty;
import io.grpc.Channel;
import io.grpc.stub.AbstractStub;
import io.grpc.stub.StreamObserver;
import io.siddhi.annotation.Example;
import io.siddhi.annotation.Extension;
import io.siddhi.annotation.Parameter;
import io.siddhi.annotation.util.DataType;
import io.siddhi.core.exception.ConnectionUnavailableException;
import io.siddhi.core.util.snapshot.state.State;
import io.siddhi.core.util.transport.DynamicOptions;
import io.siddhi.core.util.transport.OptionHolder;
import io.siddhi.extension.io.grpc.util.GrpcConstants;
import io.siddhi.extension.io.grpc.util.ServiceConfigs;
import io.siddhi.query.api.exception.SiddhiAppValidationException;
import org.apache.log4j.Logger;
import org.wso2.grpc.Event;
import org.wso2.grpc.EventServiceGrpc;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;

import static io.siddhi.extension.io.grpc.util.GrpcUtils.getRpcMethodList;

/**
 * {@code GrpcSink} Handle the gRPC publishing tasks.
 */
@Extension(name = "grpc", namespace = "sink",
        description = "" +
                "This extension publishes event data encoded into GRPC Classes as defined in the user input " +
                "jar. This extension has a default gRPC service classes added. The default service is called " +
                "\"EventService\". Please find the protobuf definition [here](https://github.com/siddhi-io/" +
                "siddhi-io-grpc/tree/master/component/src/main/resources/EventService.proto). This grpc sink is " +
                "used for scenarios where we send a request and don't expect a response back. I.e getting a " +
                "google.protobuf.Empty response back.",
        parameters = {
                @Parameter(
                        name = "publisher.url",
                        description = "The url to which the outgoing events should be published via this extension. " +
                                "This url should consist the host hostPort, port, fully qualified service name, " +
                                "method name in the following format. `grpc://0.0.0.0:9763/<serviceName>/" +
                                "<methodName>`\n" +
                                "For example:\n" +
                                "grpc://0.0.0.0:9763/org.wso2.grpc.EventService/consume",
                        type = {DataType.STRING}),
                @Parameter(
                        name = "headers",
                        description = "GRPC Request headers in format `\"'<key>:<value>','<key>:<value>'\"`. " +
                                "If header parameter is not provided just the payload is sent",
                        type = {DataType.STRING},
                        optional = true,
                        defaultValue = "-"),
                @Parameter(
                        name = "idle.timeout",
                        description = "Set the duration in seconds without ongoing RPCs before going to idle mode.",
                        type = {DataType.LONG},
                        optional = true,
                        defaultValue = "1800"),
                @Parameter(
                        name = "keep.alive.time",
                        description = "Sets the time in seconds without read activity before sending a keepalive " +
                                "ping. Keepalives can increase the load on services so must be used with caution. By " +
                                "default set to Long.MAX_VALUE which disables keep alive pinging.",
                        type = {DataType.LONG},
                        optional = true,
                        defaultValue = "Long.MAX_VALUE"),
                @Parameter(
                        name = "keep.alive.timeout",
                        description = "Sets the time in seconds waiting for read activity after sending a keepalive " +
                                "ping.",
                        type = {DataType.LONG},
                        optional = true,
                        defaultValue = "20"),
                @Parameter(
                        name = "keep.alive.without.calls",
                        description = "Sets whether keepalive will be performed when there are no outstanding RPC " +
                                "on a connection.",
                        type = {DataType.BOOL},
                        optional = true,
                        defaultValue = "false"),
                @Parameter(
                        name = "enable.retry",
                        description = "Enables the retry mechanism provided by the gRPC library.",
                        type = {DataType.BOOL},
                        optional = true,
                        defaultValue = "false"),
                @Parameter(
                        name = "max.retry.attempts",
                        description = "Sets max number of retry attempts. The total number of retry attempts for " +
                                "each RPC will not exceed this number even if service config may allow a higher " +
                                "number.",
                        type = {DataType.INT},
                        optional = true,
                        defaultValue = "5"),
                @Parameter(
                        name = "retry.buffer.size",
                        description = "Sets the retry buffer size in bytes. If the buffer limit is exceeded, no " +
                                "RPC could retry at the moment, and in hedging case all hedges but one of the same " +
                                "RPC will cancel.",
                        type = {DataType.LONG},
                        optional = true,
                        defaultValue = "16777216"),
                @Parameter(
                        name = "per.rpc.buffer.size",
                        description = "Sets the per RPC buffer limit in bytes used for retry. The RPC is not " +
                                "retriable if its buffer limit is exceeded.",
                        type = {DataType.LONG},
                        optional = true,
                        defaultValue = "1048576"),
                @Parameter(
                        name = "channel.termination.waiting.time",
                        description = "The time in seconds to wait for the channel to become terminated, giving up " +
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
                                "be set to default carbon jks by default" ,
                        type = {DataType.BOOL},
                        optional = true,
                        defaultValue = "FALSE"),
        },
        examples = {
                @Example(syntax = "" +
                        "@sink(type='grpc',\n" +
                        "      publisher.url = 'grpc://134.23.43.35:8080/org.wso2.grpc.EventService/consume',\n" +
                        "      @map(type='json'))\n" +
                        "define stream FooStream (message String);",
                        description = "Here a stream named FooStream is defined with grpc sink. A grpc server " +
                                "should be running at 194.23.98.100 listening to port 8080. sink.id is set to 1 here." +
                                " So we can write a source with sink.id 1 so that it will listen to responses for " +
                                "requests published from this stream. Note that since we are using EventService/" +
                                "consume the sink will be operating in default mode"
                ),
                @Example(syntax = "" +
                        "@sink(type='grpc',\n" +
                        "      publisher.url = 'grpc://134.23.43.35:8080/org.wso2.grpc.EventService/consume',\n" +
                        "      headers='{{headers}}',\n" +
                        "      @map(type='json'),\n" +
                        "           @payload('{{message}}'))\n" +
                        "define stream FooStream (message String, headers String);",
                        description = "A similar example to above but with headers. Headers are also send into the " +
                                "stream as a data. In the sink headers dynamic property reads the value and sends " +
                                "it as MetaData with the request"
                ),
                @Example(syntax = "" +
                        "@sink(type='grpc',\n" +
                        "      publisher.url = 'grpc://134.23.43.35:8080/org.wso2.grpc.MyService/send',\n" +
                        "      @map(type='protobuf'),\n" +
                        "define stream FooStream (stringValue string, intValue int,longValue long,booleanValue bool," +
                        "floatValue float,doubleValue double);",
                        description = "Here a stream named FooStream is defined with grpc sink. A grpc server should" +
                                " be running at 134.23.43.35 listening to port 8080 since there is no mapper " +
                                "provided, attributes of stream definition should be as same as the attributes of " +
                                "protobuf message definition"
                ),
                @Example(syntax = "" +
                        "@sink(type='grpc',\n" +
                        "      publisher.url = 'grpc://134.23.43.35:8080/org.wso2.grpc.MyService/testMap',\n" +
                        "      @map(type='protobuf'),\n" +
                        "define stream FooStream (stringValue string, intValue int,map object);",
                        description = "Here a stream named FooStream is defined with grpc sink. A grpc server should " +
                                "be running at 134.23.43.35 listening to port 8080. The 'map object' in the stream " +
                                "definition defines that this stream is going to use Map object with grpc service. " +
                                "We can use any map object that extends 'java.util.AbstractMap' class."
                ),
                @Example(syntax = "" +
                        "@sink(type='grpc',\n" +
                        "      publisher.url = 'grpc://134.23.43.35:8080/org.wso2.grpc.MyService/testMap',\n" +
                        "      @map(type='protobuf', \n" +
                        "@payload(stringValue='a',longValue='b',intValue='c',booleanValue='d',floatValue = 'e', " +
                        "doubleValue = 'f'))) \n" +
                        "define stream FooStream (a string, b long, c int,d bool,e float,f double);",
                        description = "Here a stream named FooStream is defined with grpc sink. A grpc server should " +
                                "be running at 194.23.98.100 listening to port 8080. @payload is provided in this " +
                                "stream, therefore we can use any name for the attributes in the stream definition, " +
                                "but we should correctly map those names with protobuf message attributes. If we are " +
                                "planning to send metadata within a stream we should use @payload to map attributes " +
                                "to identify the metadata attribute and the protobuf attributes separately. "
                ),
                @Example(syntax = "" +
                        "@sink(type='grpc',\n" +
                        "      publisher.url = 'grpc://194.23.98.100:8888/org.wso2.grpc.test.StreamService/" +
                        "clientStream',\n" +
                        "      @map(type='protobuf')) \n" +
                        "define stream FooStream (stringValue string, intValue int,longValue long,booleanValue bool," +
                        "floatValue float,doubleValue double);",
                        description = "Here in the grpc sink, we are sending a stream of requests to the server that " +
                                "runs on 194.23.98.100 and port 8888. When we need to send a stream of requests from " +
                                "the grpc sink we have to define a client stream RPC method(look the sample proto " +
                                "file that provided in the resource folder [here](https://github.com/siddhi-io/siddhi" +
                                "-io-grpc/tree/master/component/src/main/resources)).Then the siddhi will identify " +
                                "whether it's a unary method or a stream method and send requests according to the " +
                                "method type."
                )
        }
)

public class GrpcSink extends AbstractGrpcSink {
    private static final Logger logger = Logger.getLogger(GrpcSink.class.getName());
    private StreamObserver responseObserver;
    private AbstractStub asyncStub;
    private StreamObserver requestObserver;
    private Method rpcMethod;


    @Override
    public void initSink(OptionHolder optionHolder) {
        if (serviceConfigs.isDefaultService()) {
            responseObserver = new StreamObserver<Empty>() {
                @Override
                public void onNext(Empty event) {
                }

                @Override
                public void onError(Throwable t) { //parent method doest have error in its signature. so cant throw
                    // from here
//                    if (((StatusRuntimeException) t).getStatus().getCode().equals(Status.UNAVAILABLE)) {
//                        throw new ConnectionUnavailableException(siddhiAppName.getName() + ": " + streamID + ": "
//                        + t.getMessage());
//                    }
                    logger.error(siddhiAppName + ":" + streamID + ": " + t.getMessage() + " caused by "
                            + t.getMessage(), t);
                }

                @Override
                public void onCompleted() {
                }
            };
            if (serviceConfigs.getMethodName() == null) {
                serviceConfigs.setMethodName(GrpcConstants.DEFAULT_METHOD_NAME_WITHOUT_RESPONSE);
            } else if (!serviceConfigs.getMethodName().equalsIgnoreCase(GrpcConstants
                    .DEFAULT_METHOD_NAME_WITHOUT_RESPONSE)) {
                throw new SiddhiAppValidationException(siddhiAppName + ": " + streamID + ": In default " +
                        "mode grpc-sink when using EventService the method name should be '" +
                        GrpcConstants.DEFAULT_METHOD_NAME_WITHOUT_RESPONSE + "' but given " + serviceConfigs
                        .getMethodName());
            }
            this.channel = managedChannelBuilder.build();
            this.asyncStub = EventServiceGrpc.newStub(channel);
            if (metadataOption != null) {
                if (metadataOption.isStatic()) {
                    asyncStub = attachMetaDataToStub(null, asyncStub);
                }
            }
            requestObserver = ((EventServiceGrpc.EventServiceStub) asyncStub).consume(responseObserver);
        } else {
            responseObserver = new StreamObserver<Object>() {
                @Override
                public void onNext(Object event) {
                }

                @Override
                public void onError(Throwable t) { //parent method doest have error in its signature. so cant throw
                    // from here
//                    if (((StatusRuntimeException) t).getStatus().getCode().equals(Status.UNAVAILABLE)) {
//                        throw new ConnectionUnavailableException(siddhiAppName.getName() + ": " + streamID + ": "
//                        + t.getMessage());
//                    }
                    logger.error(siddhiAppName + ":" + streamID + ": " + t.getMessage() + " caused by "
                            + t.getMessage(), t);
                }

                @Override
                public void onCompleted() {
                }
            };
            this.channel = managedChannelBuilder.build();
            rpcMethod = getRpcMethod(serviceConfigs, siddhiAppName, streamID);
            if (metadataOption != null) {
                if (metadataOption.isStatic()) {
                    asyncStub = attachMetaDataToStub(null, asyncStub);
                }
            }
            this.asyncStub = createStub(serviceConfigs);
            try {
                if (rpcMethod.getParameterCount() == 1) {
                    requestObserver = (StreamObserver) rpcMethod.invoke(asyncStub, responseObserver);
                }
            } catch (IllegalAccessException | InvocationTargetException e) { //throws from 'invoke'
                throw new SiddhiAppValidationException(siddhiAppName + ":" + streamID + ": Invalid method name " +
                        "provided in the url, provided method name: " + serviceConfigs.getMethodName() +
                        "expected one of these methods: " + getRpcMethodList(serviceConfigs, siddhiAppName,
                        streamID), e);
            }
        }
    }

    @Override
    public void publish(Object payload, DynamicOptions dynamicOptions, State state)
            throws ConnectionUnavailableException {
        if (serviceConfigs.isDefaultService()) {
            Event.Builder eventBuilder = Event.newBuilder().setPayload(payload.toString());

            if (headersOption != null || serviceConfigs.getSequenceName() != null) {
                if (headersOption != null && headersOption.isStatic()) {
                    eventBuilder.putAllHeaders(headersMap);
                } else {
                    eventBuilder = addHeadersToEventBuilder(dynamicOptions, eventBuilder);
                }
            }
            requestObserver.onNext(eventBuilder.build());
        } else {
            if (requestObserver == null) { // check if it's a client stream method
                Object[] arguments = new Object[]{payload, responseObserver};
                try {
                    rpcMethod.invoke(asyncStub, arguments); //send as unary request
                } catch (IllegalAccessException | InvocationTargetException e) {
                    throw new SiddhiAppValidationException(siddhiAppName + ":" + streamID + ": Invalid method name " +
                            "provided in the url, provided method name: " + serviceConfigs.getMethodName() +
                            "expected one of these methods: " + getRpcMethodList(serviceConfigs, siddhiAppName,
                            streamID), e);
                }
            } else {
                requestObserver.onNext(payload); //send as stream of requests
            }
        }
    }

    /**
     * This method will be called before the processing method.
     * Intention to establish connection to publish event.
     *
     * @throws ConnectionUnavailableException if end point is unavailable the ConnectionUnavailableException thrown
     *                                        such that the  system will take care retrying for connection
     */
    @Override
    public void connect() throws ConnectionUnavailableException {
        if (channel == null || channel.isShutdown()) {
            this.channel = managedChannelBuilder.build();
            if (serviceConfigs.isDefaultService()) {
                this.asyncStub = EventServiceGrpc.newStub(channel);
            } else {
                this.asyncStub = createStub(serviceConfigs);
            }
            if (!channel.isShutdown()) {
                logger.info(siddhiAppName + ": gRPC service on " + streamID + " has successfully connected to "
                        + serviceConfigs.getUrl());
            } else {
                throw new ConnectionUnavailableException(siddhiAppName + ": gRPC service on" + streamID +
                        " could not connect to " + serviceConfigs.getUrl());
            }
        }
    }

    /**
     * Called after all publishing is done, or when {@link ConnectionUnavailableException} is thrown
     * Implementation of this method should contain the steps needed to disconnect from the sink.
     */
    @Override
    public void disconnect() {
        try {
            if (requestObserver != null) {
                requestObserver.onCompleted();
            }
            if (channelTerminationWaitingTimeInMillis > 0L) {
                channel.shutdown().awaitTermination(channelTerminationWaitingTimeInMillis, TimeUnit.MILLISECONDS);
            } else {
                channel.shutdown();
            }
            channel = null;
        } catch (InterruptedException e) {
            logger.error(siddhiAppName + ": " + streamID + ": Error in shutting " + "down the channel. " +
                    e.getMessage(), e);
        }
    }

    /**
     * to get the rpc method from the service
     */
    private static Method getRpcMethod(ServiceConfigs serviceConfigs, String siddhiAppName, String streamID) {

        Method rpcMethod = null;
        String stubReference = serviceConfigs.getFullyQualifiedServiceName() + GrpcConstants.
                GRPC_PROTOCOL_NAME_UPPERCAMELCASE + GrpcConstants.DOLLAR_SIGN + serviceConfigs.getServiceName()
                + GrpcConstants.STUB;
        try {
            Method[] methodsInStub = Class.forName(stubReference).getMethods();
            for (Method method : methodsInStub) {
                if (method.getName().equalsIgnoreCase(serviceConfigs.getMethodName())) {
                    rpcMethod = method;
                    break;
                }
            }
            if (rpcMethod == null) { //only if user has provided a wrong method name
                throw new SiddhiAppValidationException(siddhiAppName + ":" + streamID + ": Invalid method name " +
                        "provided in the url, provided method name: " + serviceConfigs.getMethodName() +
                        "expected one of these methods: " + getRpcMethodList(serviceConfigs, siddhiAppName,
                        streamID));
            }
        } catch (ClassNotFoundException e) {
            throw new SiddhiAppValidationException(siddhiAppName + ":" + streamID + ": Invalid service name " +
                    "provided in the url, provided service name: '" + serviceConfigs
                    .getFullyQualifiedServiceName() + "'", e);
        }
        return rpcMethod;
    }

    /**
     * to create Stub object in generic way
     */
    private AbstractStub createStub(ServiceConfigs serviceConfigs) {
        try {
            Class serviceClass = Class.forName(serviceConfigs.getFullyQualifiedServiceName() + GrpcConstants
                    .GRPC_PROTOCOL_NAME_UPPERCAMELCASE);
            Method newStub = serviceClass.getDeclaredMethod(GrpcConstants.NEW_STUB_NAME, Channel.class);
            return (AbstractStub) newStub.invoke(serviceClass, this.channel);
        } catch (ClassNotFoundException e) {
            throw new SiddhiAppValidationException(siddhiAppName + ":" + streamID + ": Invalid service name " +
                    "provided in the url, provided service name: '" + serviceConfigs
                    .getFullyQualifiedServiceName() + "'", e);
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            throw new SiddhiAppValidationException(siddhiAppName + ":" + streamID + ": Invalid method name " +
                    "provided in the url, provided method name: " + serviceConfigs.getMethodName() +
                    "expected one of these methods: " + getRpcMethodList(serviceConfigs, siddhiAppName, streamID));
        }
    }
}
