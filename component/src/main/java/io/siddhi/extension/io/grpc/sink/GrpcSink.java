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
import io.grpc.Metadata;
import io.grpc.stub.MetadataUtils;
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
import org.apache.log4j.Logger;
import org.wso2.grpc.Event;
import org.wso2.grpc.EventServiceGrpc;
import org.wso2.grpc.EventServiceGrpc.EventServiceStub;

/**
 * {@code GrpcSink} Handle the gRPC publishing tasks.
 */
@Extension(
        name = "grpc", namespace = "sink",
        description = "This extension publishes event data encoded into GRPC Classes as defined in the user input " +
                "jar. This extension has a default gRPC service classes added. The default service is called " +
                "\"EventService\" and it has 2 rpc's. They are process and consume. Process sends a request of type " +
                "Event and receives a response of the same type. Consume sends a request of type Event and expects " +
                "no response from gRPC server. Please note that the Event type mentioned here is not " +
                "io.siddhi.core.event.Event but org.wso2.grpc.Event. This grpc sink is used for scenarios where we " +
                "send a request and don't expect a response back. I.e getting a google.protobuf.Empty response back.",
        parameters = {
                @Parameter(name = "url",
                        description = "The url to which the outgoing events should be published via this extension. " +
                                "This url should consist the host address, port, service name, method name in the " +
                                "following format. grpc://hostAddress:port/serviceName/methodName" ,
                        type = {DataType.STRING}),
                @Parameter(name = "headers",
                        description = "GRPC Request headers in format `\"'<key>:<value>','<key>:<value>'\"`. " +
                                "If header parameter is not provided just the payload is sent" ,
                        type = {DataType.STRING},
                        optional = true,
                        defaultValue = ""),
                @Parameter(name = "idle.timeout",
                        description = "Set the duration in seconds without ongoing RPCs before going to idle mode." ,
                        type = {DataType.LONG},
                        optional = true,
                        defaultValue = "1800"),
                @Parameter(name = "keep.alive.time",
                        description = "Sets the time in seconds without read activity before sending a keepalive " +
                                "ping. Keepalives can increase the load on services so must be used with caution. By " +
                                "default set to Long.MAX_VALUE which disables keep alive pinging." ,
                        type = {DataType.LONG},
                        optional = true,
                        defaultValue = "Long.MAX_VALUE"),
                @Parameter(name = "keep.alive.timeout",
                        description = "Sets the time in seconds waiting for read activity after sending a keepalive " +
                                "ping." ,
                        type = {DataType.LONG},
                        optional = true,
                        defaultValue = "20"),
                @Parameter(name = "keep.alive.without.calls",
                        description = "Sets whether keepalive will be performed when there are no outstanding RPC " +
                                "on a connection." ,
                        type = {DataType.BOOL},
                        optional = true,
                        defaultValue = "false"),
                @Parameter(name = "enable.retry",
                        description = "Enables the retry and hedging mechanism provided by the gRPC library." ,
                        type = {DataType.BOOL},
                        optional = true,
                        defaultValue = "false"),
                @Parameter(name = "max.retry.attempts",
                        description = "Sets max number of retry attempts. The total number of retry attempts for " +
                                "each RPC will not exceed this number even if service config may allow a higher " +
                                "number." ,
                        type = {DataType.INT},
                        optional = true,
                        defaultValue = "5"),
                @Parameter(name = "max.hedged.attempts",
                        description = "Sets max number of hedged attempts. The total number of hedged attempts for " +
                                "each RPC will not exceed this number even if service config may allow a higher " +
                                "number." ,
                        type = {DataType.INT},
                        optional = true,
                        defaultValue = "5"),
                @Parameter(name = "retry.buffer.size",
                        description = "Sets the retry buffer size in bytes. If the buffer limit is exceeded, no " +
                                "RPC could retry at the moment, and in hedging case all hedges but one of the same " +
                                "RPC will cancel." ,
                        type = {DataType.LONG},
                        optional = true,
                        defaultValue = "16777216"),
                @Parameter(name = "per.rpc.buffer.size",
                        description = "Sets the per RPC buffer limit in bytes used for retry. The RPC is not " +
                                "retriable if its buffer limit is exceeded." ,
                        type = {DataType.LONG},
                        optional = true,
                        defaultValue = "1048576"),
                @Parameter(name = "channel.termination.waiting.time",
                        description = "The time in seconds to wait for the channel to become terminated, giving up " +
                                "if the timeout is reached." ,
                        type = {DataType.LONG},
                        optional = true,
                        defaultValue = "5"),
        },
        examples = {
                @Example(
                        syntax = "@sink(type='grpc', " +
                                "url = 'grpc://134.23.43.35:8080/org.wso2.grpc.EventService/consume', " +
                                "@map(type='json')) "
                                + "define stream FooStream (message String);",
                        description = "Here a stream named FooStream is defined with grpc sink. A grpc server " +
                                "should be running at 194.23.98.100 listening to port 8080. sink.id is set to 1 " +
                                "here. So we can write a source with sink.id 1 so that it will listen to responses " +
                                "for requests published from this stream. Note that since we are using " +
                                "EventService/consume the sink will be operating in default mode"
                ),
                @Example(
                        syntax = "@sink(type='grpc', " +
                                "url = 'grpc://134.23.43.35:8080/org.wso2.grpc.EventService/consume', " +
                                "headers='{{headers}}', " +
                                "@map(type='json'), @payload('{{message}}')) "
                                + "define stream FooStream (message String, headers String);",
                        description = "A similar example to above but with headers. Headers are also send into the " +
                                "stream as a data. In the sink headers dynamic property reads the value and sends " +
                                "it as MetaData with the request"
                )
        }
)

public class GrpcSink extends AbstractGrpcSink {
    private static final Logger logger = Logger.getLogger(GrpcSink.class.getName());
    EventServiceStub asyncStub;

    @Override
    public void initSink(OptionHolder optionHolder) {}

    @Override
    public void publish(Object payload, DynamicOptions dynamicOptions, State state)
            throws ConnectionUnavailableException {
        if (isDefaultMode) {
            Event.Builder requestBuilder = Event.newBuilder();
            requestBuilder.setPayload((String) payload);
            Event sequenceCallRequest = requestBuilder.build();
            EventServiceStub currentAsyncStub = asyncStub;

            if (headersOption != null) {
                Metadata header = new Metadata();
                String headers = headersOption.getValue(dynamicOptions);
                Metadata.Key<String> key =
                        Metadata.Key.of(GrpcConstants.HEADERS, Metadata.ASCII_STRING_MARSHALLER);
                header.put(key, headers);
                currentAsyncStub = MetadataUtils.attachHeaders(asyncStub, header);
            }

            StreamObserver<Empty> responseObserver = new StreamObserver<Empty>() {
                @Override
                public void onNext(Empty event) {}

                @Override
                public void onError(Throwable t) {
                    if (logger.isDebugEnabled()) {
                        logger.debug(siddhiAppContext.getName() + ": " + t.getMessage());
                    }
                }

                @Override
                public void onCompleted() {}
            };
            currentAsyncStub.consume(sequenceCallRequest, responseObserver);
        } else {
            //todo: handle publishing to generic service
        }
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
        this.asyncStub = EventServiceGrpc.newStub(channel);
        if (!channel.isShutdown()) {
            logger.info(streamID + " has successfully connected to " + url);
        }
    }
}
