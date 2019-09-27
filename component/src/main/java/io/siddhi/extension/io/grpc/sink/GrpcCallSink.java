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

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.MoreExecutors;
import io.grpc.Channel;
import io.grpc.stub.AbstractStub;
import io.siddhi.annotation.Example;
import io.siddhi.annotation.Extension;
import io.siddhi.annotation.Parameter;
import io.siddhi.annotation.util.DataType;
import io.siddhi.core.exception.ConnectionUnavailableException;
import io.siddhi.core.exception.SiddhiAppRuntimeException;
import io.siddhi.core.util.snapshot.state.State;
import io.siddhi.core.util.transport.DynamicOptions;
import io.siddhi.core.util.transport.OptionHolder;
import io.siddhi.extension.io.grpc.util.GrpcConstants;
import io.siddhi.extension.io.grpc.util.GrpcSourceRegistry;
import io.siddhi.extension.io.grpc.util.ServiceConfigs;
import io.siddhi.query.api.definition.Attribute;
import io.siddhi.query.api.exception.SiddhiAppValidationException;
import org.apache.log4j.Logger;
import org.wso2.grpc.Event;
import org.wso2.grpc.EventServiceGrpc;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static io.siddhi.extension.io.grpc.util.GrpcUtils.getRpcMethodList;

/**
 * {@code GrpcCallSink} Handle the gRPC publishing tasks and injects response into grpc-call-response source.
 */
@Extension(name = "grpc-call", namespace = "sink",
        description = "This extension publishes event data encoded into GRPC Classes as defined in the user input " +
                "jar. This extension has a default gRPC service classes jar added. The default service is called " +
                "\"EventService\". Please find the protobuf definition [here](https://github.com/siddhi-io/" +
                "siddhi-io-grpc/tree/master/component/src/main/resources/EventService.proto). If we want to use our " +
                "custom gRPC services, we have to  pack auto-generated gRPC service classes and  protobuf classes " +
                "into a jar file and add it into the project classpath (or to the `jars` folder in the `siddhi-" +
                "tooling` folder if we use it with `siddhi-tooling`). Please find the custom protobuf definition that" +
                " uses in examples [here](https://github.com/siddhi-io/siddhi-io-grpc/tree/master/component/src/main/" +
                "resources/sample.proto). This grpc-call sink is " +
                "used for scenarios where we send a request out and expect a response back. In default mode this " +
                "will use EventService process method. grpc-call-response source is used to receive the responses. " +
                "A unique sink.id is used to correlate between the sink and its corresponding source.",
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
                        name = "sink.id",
                        description = "a unique ID that should be set for each grpc-call-sink. There is a 1:1 " +
                                "mapping between grpc-call sinks and grpc-call-response sources. Each sink has one " +
                                "particular source listening to the responses to requests published from that sink. " +
                                "So the same sink.id should be given when writing the source also.",
                        type = {DataType.INT}),
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
                        description = "Enables the retry and hedging mechanism provided by the gRPC library.",
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
                        name = "max.inbound.message.size",
                        description = "Sets the maximum message size allowed to be received on the channel in bytes",
                        type = {DataType.LONG},
                        optional = true,
                        defaultValue = "4194304"),
                @Parameter(
                        name = "max.inbound.metadata.size",
                        description = "Sets the maximum size of metadata allowed to be received in bytes",
                        type = {DataType.LONG},
                        optional = true,
                        defaultValue = "8192"),
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
        },
        examples = {
                @Example(syntax = "" +
                        "@sink(type='grpc-call',\n" +
                        "      publisher.url = 'grpc://194.23.98.100:8080/EventService/process',\n" +
                        "      sink.id= '1', @map(type='json'))\n" +
                        "define stream FooStream (message String);\n" +
                        "@source(type='grpc-call-response', sink.id= '1')\n" +
                        "define stream BarStream (message String);",
                        description = "" +
                                "Here a stream named FooStream is defined with grpc sink. A grpc server " +
                                "should be running at 194.23.98.100 listening to port 8080. sink.id is set to 1 here." +
                                " So we can write a source with sink.id 1 so that it will listen to responses for " +
                                "requests published from this stream. Note that since we are using EventService/" +
                                "process the sink will be operating in default mode"
                ),
                @Example(syntax = "" +
                        "@sink(type='grpc-call',\n" +
                        "      publisher.url = 'grpc://194.23.98.100:8080/EventService/process',\n" +
                        "      sink.id= '1', @map(type='json'))\n" +
                        "define stream FooStream (message String);\n" +
                        "\n" +
                        "@source(type='grpc-call-response', sink.id= '1')\n" +
                        "define stream BarStream (message String);",
                        description = "Here with the same FooStream definition we have added a BarStream which has " +
                                "a grpc-call-response source with the same sink.id 1. So the responses for calls " +
                                "sent from the FooStream will be added to BarStream."
                ),
                @Example(syntax = "" +
                        "@sink(type='grpc-call',\n" +
                        "      publisher.url = 'grpc://194.23.98.100:8888/org.wso2.grpc.test.MyService/process',\n" +
                        "      sink.id= '1', @map(type='protobuf'))\n" +
                        "define stream FooStream (stringValue string, intValue int,longValue long,booleanValue " +
                        "bool,floatValue float,doubleValue double);\n" +
                        "\n" +
                        "@source(type='grpc-call-response', receiver.url = 'grpc://localhost:8888/org.wso2.grpc" +
                        ".MyService/process', sink.id= '1', \n" +
                        "@map(type='protobuf'))" +
                        "define stream FooStream (stringValue string, intValue int,longValue long,booleanValue bool," +
                        "floatValue float,doubleValue double);",
                        description = "Here a stream named FooStream is defined with grpc sink. A grpc server should " +
                                "be running at 194.23.98.100 listening to port 8080. We have added another stream " +
                                "called  BarStream which is a grpc-call-response source with the same sink.id 1 and " +
                                "as same as FooStream definition. So the responses for calls sent from the FooStream " +
                                "will be added to BarStream. Since there is no mapping available in the stream " +
                                "definition attributes names should be as same as the attributes of the protobuf " +
                                "message definition. (Here the only reason we provide receiver.url in the grpc-call" +
                                "-response source is for protobuf mapper to map Response into a siddhi event, we can " +
                                "give any address and any port number in the URL, but we should provide the service " +
                                "name and the method name correctly)"
                ),
                @Example(syntax = "" +
                        "@sink(type='grpc-call',\n" +
                        "      publisher.url = 'grpc://194.23.98.100:8888/org.wso2.grpc.test.MyService/process',\n" +
                        "      sink.id= '1', @map(type='protobuf', \n" +
                        "@payload(stringValue='a',longValue='c',intValue='b',booleanValue='d',floatValue = 'e', " +
                        "doubleValue = 'f')))" +
                        "define stream FooStream (a string, b int,c long,d bool,e float,f double);\n" +
                        "\n" +
                        "@source(type='grpc-call-response', receiver.url = 'grpc://localhost:8888/org.wso2.grpc.test" +
                        ".MyService/process', sink.id= '1', \n" +
                        "@map(type='protobuf'," +
                        "@attributes(a = 'stringValue', b = 'intValue', c = 'longValue',d = 'booleanValue', " +
                        "e ='floatValue', f ='doubleValue')))" +
                        "define stream FooStream (a string, b int,c long,d bool,e float,f double);",
                        description = "Here with the same FooStream definition we have added a BarStream which has " +
                                "a grpc-call-response source with the same sink.id 1. So the responses for calls " +
                                "sent from the FooStream will be added to BarStream. In this stream we provided " +
                                "mapping for both the sink and the source. so we can use any name for the " +
                                "attributes in the stream definition, but we have to map those attributes with " +
                                "correct protobuf attributes. As same as the grpc-sink, if we are planning to use  " +
                                "metadata we should map the attributes."
                )
        }
)
public class GrpcCallSink extends AbstractGrpcSink {
    private static final Logger logger = Logger.getLogger(GrpcCallSink.class.getName());
    protected String sinkID;
    protected AbstractStub futureStub;

    private static Method getRpcMethod(ServiceConfigs serviceConfigs, String siddhiAppName, String streamID) {

        Method rpcMethod = null;
        String stubReference = serviceConfigs.getFullyQualifiedServiceName() + GrpcConstants.
                GRPC_PROTOCOL_NAME_UPPERCAMELCASE + GrpcConstants.DOLLAR_SIGN + serviceConfigs.getServiceName()
                + GrpcConstants.FUTURE_STUB;
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
                        "provided in the url, provided method name: '" + serviceConfigs.getMethodName() +
                        "', expected one of these methods: " + getRpcMethodList(serviceConfigs, siddhiAppName,
                        streamID));
            }
        } catch (ClassNotFoundException e) {
            throw new SiddhiAppValidationException(siddhiAppName + ": " + streamID + ": Invalid service name " +
                    "provided in the url, provided service name: '" + serviceConfigs
                    .getFullyQualifiedServiceName() + "'", e);
        }
        return rpcMethod;
    }

    @Override
    public void initSink(OptionHolder optionHolder) {
        if (serviceConfigs.isDefaultService()) {
            if (serviceConfigs.getMethodName() == null) {
                serviceConfigs.setMethodName(GrpcConstants.DEFAULT_METHOD_NAME_WITH_RESPONSE);
            } else if (!serviceConfigs.getMethodName().equalsIgnoreCase(GrpcConstants
                    .DEFAULT_METHOD_NAME_WITH_RESPONSE)) {
                throw new SiddhiAppValidationException(siddhiAppName + ": " + streamID + ": In default " +
                        "mode grpc-call-sink when using EventService the method name should be '" +
                        GrpcConstants.DEFAULT_METHOD_NAME_WITH_RESPONSE + "' but given " + serviceConfigs
                        .getMethodName());
            }
        }
        if (optionHolder.isOptionExists(GrpcConstants.MAX_INBOUND_MESSAGE_SIZE)) {
            managedChannelBuilder.maxInboundMessageSize(Integer.parseInt(optionHolder.validateAndGetOption(
                    GrpcConstants.MAX_INBOUND_MESSAGE_SIZE).getValue()));
        }
        if (optionHolder.isOptionExists(GrpcConstants.MAX_INBOUND_METADATA_SIZE)) {
            managedChannelBuilder.maxInboundMetadataSize(Integer.parseInt(optionHolder.validateAndGetOption(
                    GrpcConstants.MAX_INBOUND_METADATA_SIZE).getValue()));
        }
        this.sinkID = optionHolder.validateAndGetOption(GrpcConstants.SINK_ID).getValue();
    }

    @Override
    public void publish(Object payload, DynamicOptions dynamicOptions, State state)
            throws ConnectionUnavailableException {
        if (serviceConfigs.isDefaultService()) {
            Event.Builder eventBuilder = Event.newBuilder().setPayload(payload.toString());
            EventServiceGrpc.EventServiceFutureStub currentFutureStub = (EventServiceGrpc.EventServiceFutureStub)
                    futureStub;

            if (headersOption != null || serviceConfigs.getSequenceName() != null) {
                if (headersOption != null && headersOption.isStatic()) {
                    eventBuilder.putAllHeaders(headersMap);
                } else {
                    eventBuilder = addHeadersToEventBuilder(dynamicOptions, eventBuilder);
                }
            }

            if (metadataOption != null) {
                currentFutureStub = (EventServiceGrpc.EventServiceFutureStub) attachMetaDataToStub(dynamicOptions,
                        currentFutureStub);
            }

            ListenableFuture<Event> futureResponse = currentFutureStub.process(eventBuilder.build());
            Futures.addCallback(futureResponse, new FutureCallback<Event>() {
                Map<String, String> siddhiRequestEventData = getRequestEventDataMap(dynamicOptions);

                @Override
                public void onSuccess(Event result) {
                    GrpcSourceRegistry.getInstance().getGrpcCallResponseSource(sinkID).onResponse(result,
                            siddhiRequestEventData);
                }

                @Override
                public void onFailure(Throwable t) {
                    logger.error(siddhiAppName + ": " + streamID + ": " + t.getMessage());
                }
            }, MoreExecutors.directExecutor());
        } else {
            AbstractStub currentStub = futureStub;
            Method rpcMethod = getRpcMethod(serviceConfigs, siddhiAppName, streamID);
            if (metadataOption != null) {
                currentStub = attachMetaDataToStub(dynamicOptions, currentStub);
            }
            ListenableFuture genericFutureResponse;
            try {
                genericFutureResponse = (ListenableFuture) rpcMethod.invoke(currentStub, payload);
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new SiddhiAppValidationException(siddhiAppName + ":" + streamID + ": Invalid method name " +
                        "provided in the url, provided method name: '" + serviceConfigs.getMethodName() +
                        "', expected one of these methods: " + getRpcMethodList(serviceConfigs, siddhiAppName,
                        streamID), e);
            }
            Futures.addCallback(genericFutureResponse, new FutureCallback<Object>() {
                Map<String, String> siddhiRequestEventData = getRequestEventDataMap(dynamicOptions);

                @Override
                public void onSuccess(Object o) {
                    GrpcSourceRegistry.getInstance().getGrpcCallResponseSource(sinkID).onResponse(o,
                            siddhiRequestEventData);
                }

                @Override
                public void onFailure(Throwable t) {
                    logger.error(siddhiAppName + ":" + streamID + ": " + t.getMessage());
                }
            }, MoreExecutors.directExecutor());
        }
    }

    private Map<String, String> getRequestEventDataMap(DynamicOptions dynamicOptions) {
        io.siddhi.core.event.Event event = dynamicOptions.getEvent();
        Object[] data = event.getData();
        List<Attribute> attributes = streamDefinition.getAttributeList();
        Map<String, String> requestEventDataMap = new HashMap<>();
        for (int i = 0; i < attributes.size(); i++) {
            requestEventDataMap.put(attributes.get(i).getName(), data[i].toString());
        }
        return requestEventDataMap;
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
        this.channel = managedChannelBuilder.build();
        if (serviceConfigs.isDefaultService()) {
            this.futureStub = EventServiceGrpc.newFutureStub(channel);
        } else {
            this.futureStub = createFutureStub(serviceConfigs, siddhiAppName, streamID);
        }
        if (!channel.isShutdown()) {
            logger.info(siddhiAppName + ": gRPC service on " + streamID + " has successfully connected to "
                    + serviceConfigs.getUrl());
        } else {
            throw new ConnectionUnavailableException(siddhiAppName + ": gRPC service on" + streamID + " could not " +
                    "connect to " + serviceConfigs.getUrl());
        }
        if (GrpcSourceRegistry.getInstance().getGrpcCallResponseSource(sinkID) == null) {
            throw new SiddhiAppRuntimeException(siddhiAppName + ": " + streamID + ": For grpc-call sink " +
                    "to work a grpc-call-response source should be available with the same sink.id. In this case " +
                    "sink.id is " + sinkID + ". Please provide a grpc-call-response source with the sink.id " + sinkID);
        }
    }

    /**
     * Called after all publishing is done, or when {@link ConnectionUnavailableException} is thrown
     * Implementation of this method should contain the steps needed to disconnect from the sink.
     */
    @Override
    public void disconnect() {
        try {
            if (channelTerminationWaitingTimeInMillis > 0L) {
                channel.shutdown().awaitTermination(channelTerminationWaitingTimeInMillis, TimeUnit.MILLISECONDS);
            } else {
                if (channel != null) {
                    channel.shutdown();
                }
            }
            channel = null;
        } catch (InterruptedException e) {
            logger.error(siddhiAppName + ":" + streamID + ": Error in shutting " + "down the channel. " +
                    e.getMessage(), e);
        }
    }

    private AbstractStub createFutureStub(ServiceConfigs serviceConfigs, String siddhiAppName, String streamID) {
        try {
            Class serviceClass = Class.forName(serviceConfigs.getFullyQualifiedServiceName() + GrpcConstants
                    .GRPC_PROTOCOL_NAME_UPPERCAMELCASE);
            Method newStub = serviceClass.getDeclaredMethod(GrpcConstants.FUTURE_STUB_METHOD_NAME, Channel.class);
            return (AbstractStub) newStub.invoke(serviceClass, this.channel);
        } catch (ClassNotFoundException e) {
            throw new SiddhiAppValidationException(siddhiAppName + ":" + streamID + ": Invalid service name " +
                    "provided in the url, provided service name: '" + serviceConfigs
                    .getFullyQualifiedServiceName() + "'", e);
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            throw new SiddhiAppValidationException(siddhiAppName + ":" + streamID + ": Invalid method name " +
                    "provided in the url, provided method name: '" + serviceConfigs.getMethodName() +
                    "', expected one of these methods: " + getRpcMethodList(serviceConfigs, siddhiAppName, streamID)
                    , e);
        }
    }
}
