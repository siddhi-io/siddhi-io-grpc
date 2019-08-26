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
import io.siddhi.query.api.definition.Attribute;
import io.siddhi.query.api.exception.SiddhiAppValidationException;
import org.apache.log4j.Logger;
import org.wso2.grpc.Event;
import org.wso2.grpc.EventServiceGrpc;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * {@code GrpcCallSink} Handle the gRPC publishing tasks and injects response into grpc-call-response source.
 */
@Extension(name = "grpc-call", namespace = "sink",
        description = "This extension publishes event data encoded into GRPC Classes as defined in the user input " +
                "jar. This extension has a default gRPC service classes jar added. The default service is called " +
                "\"EventService\". Please find the protobuf definition here. This grpc-call sink is used for " +
                "scenarios where we send a request out and expect a response back. In default mode this will use " +
                "EventService process method. grpc-call-response source is used to receive the responses. A unique " +
                "sink.id is used to correlate between the sink and its corresponding source.",
        parameters = {
                @Parameter(
                        name = "publisherurl",
                        description = "The url to which the outgoing events should be published via this extension. " +
                                "This url should consist the host address, port, service name, method name in the " +
                                "following format. `grpc://0.0.0.0:9763/<serviceName>/<methodName>`" ,
                        type = {DataType.STRING}),
                @Parameter(
                        name = "sink.id",
                        description = "a unique ID that should be set for each grpc-call-sink. There is a 1:1 " +
                                "mapping between grpc-call sinks and grpc-call-response sources. Each sink has one " +
                                "particular source listening to the responses to requests published from that sink. " +
                                "So the same sink.id should be given when writing the source also." ,
                        type = {DataType.INT}),
                @Parameter(
                        name = "headers",
                        description = "GRPC Request headers in format `\"'<key>:<value>','<key>:<value>'\"`. " +
                                "If header parameter is not provided just the payload is sent" ,
                        type = {DataType.STRING},
                        optional = true,
                        defaultValue = "-"),
                @Parameter(
                        name = "idle.timeout",
                        description = "Set the duration in seconds without ongoing RPCs before going to idle mode." ,
                        type = {DataType.LONG},
                        optional = true,
                        defaultValue = "1800"),
                @Parameter(
                        name = "keep.alive.time",
                        description = "Sets the time in seconds without read activity before sending a keepalive " +
                                "ping. Keepalives can increase the load on services so must be used with caution. By " +
                                "default set to Long.MAX_VALUE which disables keep alive pinging." ,
                        type = {DataType.LONG},
                        optional = true,
                        defaultValue = "Long.MAX_VALUE"),
                @Parameter(
                        name = "keep.alive.timeout",
                        description = "Sets the time in seconds waiting for read activity after sending a keepalive " +
                                "ping." ,
                        type = {DataType.LONG},
                        optional = true,
                        defaultValue = "20"),
                @Parameter(
                        name = "keep.alive.without.calls",
                        description = "Sets whether keepalive will be performed when there are no outstanding RPC " +
                                "on a connection." ,
                        type = {DataType.BOOL},
                        optional = true,
                        defaultValue = "false"),
                @Parameter(
                        name = "enable.retry",
                        description = "Enables the retry and hedging mechanism provided by the gRPC library." ,
                        type = {DataType.BOOL},
                        optional = true,
                        defaultValue = "false"),
                @Parameter(
                        name = "max.retry.attempts",
                        description = "Sets max number of retry attempts. The total number of retry attempts for " +
                                "each RPC will not exceed this number even if service config may allow a higher " +
                                "number." ,
                        type = {DataType.INT},
                        optional = true,
                        defaultValue = "5"),
                @Parameter(
                        name = "retry.buffer.size",
                        description = "Sets the retry buffer size in bytes. If the buffer limit is exceeded, no " +
                                "RPC could retry at the moment, and in hedging case all hedges but one of the same " +
                                "RPC will cancel." ,
                        type = {DataType.LONG},
                        optional = true,
                        defaultValue = "16777216"),
                @Parameter(
                        name = "per.rpc.buffer.size",
                        description = "Sets the per RPC buffer limit in bytes used for retry. The RPC is not " +
                                "retriable if its buffer limit is exceeded." ,
                        type = {DataType.LONG},
                        optional = true,
                        defaultValue = "1048576"),
                @Parameter(
                        name = "channel.termination.waiting.time",
                        description = "The time in seconds to wait for the channel to become terminated, giving up " +
                                "if the timeout is reached." ,
                        type = {DataType.LONG},
                        optional = true,
                        defaultValue = "5"),
                @Parameter(
                        name = "max.inbound.message.size",
                        description = "Sets the maximum message size allowed to be received on the channel in bytes" ,
                        type = {DataType.LONG},
                        optional = true,
                        defaultValue = "4194304"),
                @Parameter(
                        name = "max.inbound.metadata.size",
                        description = "Sets the maximum size of metadata allowed to be received in bytes" ,
                        type = {DataType.LONG},
                        optional = true,
                        defaultValue = "8192"),
        },
        examples = {
                @Example(syntax = "" +
                        "@sink(type='grpc-call',\n" +
                        "      publisher.url = 'grpc://194.23.98.100:8080/EventService/process',\n" +
                        "      sink.id= '1', @map(type='json'))\n" +
                        "define stream FooStream (message String);\n",
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
                )
        }
)
public class GrpcCallSink extends AbstractGrpcSink {
    private static final Logger logger = Logger.getLogger(GrpcCallSink.class.getName());
    protected String sinkID;

    @Override
    public void initSink(OptionHolder optionHolder) {
        if (isDefaultMode) {
            if (methodName == null) {
                methodName = GrpcConstants.DEFAULT_METHOD_NAME_WITH_RESPONSE;
            } else if (!methodName.equalsIgnoreCase(GrpcConstants.DEFAULT_METHOD_NAME_WITH_RESPONSE)) {
                throw new SiddhiAppValidationException(siddhiAppContext.getName() + ": " + streamID + ": In default " +
                        "mode grpc-call-sink when using EventService the method name should be '" +
                        GrpcConstants.DEFAULT_METHOD_NAME_WITH_RESPONSE + "' but given " + methodName);
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
        if (isDefaultMode) {
            Event.Builder eventBuilder = Event.newBuilder().setPayload(payload.toString());
            EventServiceGrpc.EventServiceFutureStub currentFutureStub = futureStub;

            if (headersOption != null || sequenceName != null) {
                eventBuilder = addHeadersToEventBuilder(dynamicOptions, eventBuilder);
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
                    GrpcSourceRegistry.getInstance().getGrpcCallResponseSourceSource(sinkID).onResponse(result,
                            siddhiRequestEventData);
                }

                @Override
                public void onFailure(Throwable t) {
                    logger.error(siddhiAppContext.getName() + ":" + streamID + ": " + t.getMessage());
                }
            }, MoreExecutors.directExecutor());
        } else {

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
     * @throws ConnectionUnavailableException if end point is unavailable the ConnectionUnavailableException thrown
     *                                        such that the  system will take care retrying for connection
     */
    @Override
    public void connect() throws ConnectionUnavailableException {
        this.channel = managedChannelBuilder.build();
        this.futureStub = EventServiceGrpc.newFutureStub(channel);
        logger.info(siddhiAppContext.getName() + ": gRPC service on " + streamID + " has successfully connected to "
                + url);
        if (GrpcSourceRegistry.getInstance().getGrpcCallResponseSourceSource(sinkID) == null) {
            throw new SiddhiAppRuntimeException(siddhiAppContext.getName() + ": " + streamID + ": For grpc-call sink " +
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
            if (channelTerminationWaitingTimeInMillis != -1L) {
                channel.shutdown().awaitTermination(channelTerminationWaitingTimeInMillis, TimeUnit.MILLISECONDS);
            } else {
                if (channel != null) {
                    channel.shutdown();
                }
            }
            channel = null;
        } catch (InterruptedException e) {
            throw new SiddhiAppRuntimeException(siddhiAppContext.getName() + ":" + streamID + ": Error in shutting " +
                    "down the channel. ", e);
        }
    }
}
