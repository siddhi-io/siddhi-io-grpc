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
import com.google.protobuf.Empty;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.siddhi.annotation.Example;
import io.siddhi.annotation.Extension;
import io.siddhi.annotation.Parameter;
import io.siddhi.annotation.util.DataType;
import io.siddhi.core.config.SiddhiAppContext;
import io.siddhi.core.exception.ConnectionUnavailableException;
import io.siddhi.core.exception.SiddhiAppRuntimeException;
import io.siddhi.core.stream.ServiceDeploymentInfo;
import io.siddhi.core.stream.output.sink.Sink;
import io.siddhi.core.util.config.ConfigReader;
import io.siddhi.core.util.snapshot.state.State;
import io.siddhi.core.util.snapshot.state.StateFactory;
import io.siddhi.core.util.transport.DynamicOptions;
import io.siddhi.core.util.transport.OptionHolder;
import io.siddhi.extension.io.grpc.util.GrpcConstants;
import io.siddhi.extension.io.grpc.util.SourceStaticHolder;
import io.siddhi.extension.io.grpc.util.service.EventServiceGrpc;
import io.siddhi.query.api.definition.StreamDefinition;
import io.siddhi.extension.io.grpc.util.service.Event;
import org.apache.log4j.Logger;

import javax.annotation.Nullable;
import java.util.concurrent.TimeUnit;

/**
 * {@code GrpcSink} Handle the gRPC publishing tasks.
 */
@Extension(
        name = "grpc", namespace = "sink",
        description = "This extension publishes event data encoded into GRPC Classes as defined in the user input " +
                "jar. This extension has a default gRPC service classes jar added. The default service is called " +
                "\"EventService\" and it has 2 rpc's. They are process and consume. Process sends a request of type " +
                "Event and receives a response of the same type. Consume sends a request of type Event and expects " +
                "no response from gRPC server. Please note that the Event type mentioned here is not " +
                "io.siddhi.core.event.Event but a type defined in the default service protobuf given in the readme.",
        parameters = {
                @Parameter(name = "url",
                        description = "The url to which the outgoing events should be published via this extension. " +
                                "This url should consist the host address, port, service name, method name in the " +
                                "following format. hostAddress:port/serviceName/methodName" ,
                        type = {DataType.STRING}),
                @Parameter(name = "sink.id",
                        description = "a unique ID that should be set for each gRPC sink. There is a 1:1 mapping " +
                                "between gRPC sinks and sources. Each sink has one particular source listening to " +
                                "the responses to requests published from that sink. So the same sink.id should be " +
                                "given when writing the source also." ,
                        type = {DataType.INT}),
                @Parameter(name = "sequence",
                        description = "This is an optional parameter to be used when connecting to Micro Integrator " +
                                "sequences from Siddhi. Micro integrator will expose a service called EventService " +
                                "which has 2 rpc's as mentioned in the extension description. Both of these rpc can " +
                                "access many different sequences in Micro Integrator. This parameter is used to " +
                                "specify the sequence which we want to use. When this parameter is given gRPC sink " +
                                "will comunicate with MI. Json map type should be used in this case to encode event " +
                                "data and send to MI" ,
                        optional = true, defaultValue = "NA. When sequence is not given the service name and method " +
                                "name should be specified in the url",
                        type = {DataType.STRING}),
        },
        examples = {
                @Example(
                        syntax = "@sink(type='grpc', " +
                                "url = '194.23.98.100:8080/EventService/process', " +
                                "sequence = 'mySeq', " +
                                "sink.id= '1', @map(type='json')) "
                                + "define stream FooStream (message String);",
                        description = "Here a stream named FooStream is defined with grpc sink. Since sequence is " +
                                "specified here sink will be in default mode. i.e communicating to MI. The " +
                                "MicroIntegrator should be running at 194.23.98.100 host and listening on port 8080. " +
                                "The sequence called mySeq will be accessed. sink.id is set to 1 here. So we can " +
                                "write a source with sink.id 1 so that it will listen to responses for requests " +
                                "published from this stream."
                        //todo: add an example for generic service access
                )
        }
)

public class GRPCSink extends Sink {
    private static final Logger logger = Logger.getLogger(GRPCSink.class.getName());
    private SiddhiAppContext siddhiAppContext;
    private ManagedChannel channel;
    private String serviceName;
    private String methodName;
    private String sequenceName;
    private EventServiceGrpc.EventServiceFutureStub futureStub;
    private boolean isMIConnect = false;
    private SourceStaticHolder sourceStaticHolder = SourceStaticHolder.getInstance();
    private String sinkID;
    private String url;
    private String streamID;

    /**
     * Returns the list of classes which this sink can consume.
     * Based on the type of the sink, it may be limited to being able to publish specific type of classes.
     * For example, a sink of type file can only write objects of type String .
     * @return array of supported classes , if extension can support of any types of classes
     * then return empty array .
     */
    @Override
    public Class[] getSupportedInputEventClasses() {
            return new Class[]{Object.class};
    }

    @Override
    protected ServiceDeploymentInfo exposeServiceDeploymentInfo() {
        return null;
    }

    /**
     * Returns a list of supported dynamic options (that means for each event value of the option can change) by
     * the transport
     *
     * @return the list of supported dynamic option keys
     */
    @Override
    public String[] getSupportedDynamicOptions() {
            return null;
    }

    /**
     * The initialization method for {@link Sink}, will be called before other methods. It used to validate
     * all configurations and to get initial values.
     * @param streamDefinition  containing stream definition bind to the {@link Sink}
     * @param optionHolder            Option holder containing static and dynamic configuration related
     *                                to the {@link Sink}
     * @param configReader        to read the sink related system configuration.
     * @param siddhiAppContext        the context of the {@link io.siddhi.query.api.SiddhiApp} used to
     */
    @Override
    protected StateFactory init(StreamDefinition streamDefinition, OptionHolder optionHolder, ConfigReader configReader,
                                SiddhiAppContext siddhiAppContext) {
        this.siddhiAppContext = siddhiAppContext;
        this.url = optionHolder.validateAndGetOption(GrpcConstants.PUBLISHER_URL).getValue();
        String[] temp = url.split(GrpcConstants.PORT_SERVICE_SEPARATOR);
        StringBuilder target = new StringBuilder();
        for (int i = 0; i < temp.length - 2; i++) {
            target.append(GrpcConstants.PORT_SERVICE_SEPARATOR).append(temp[i]);
        }
        this.serviceName = temp[temp.length - 2];
        this.methodName = temp[temp.length - 1];
        this.sinkID = optionHolder.validateAndGetOption(GrpcConstants.SINK_ID).getValue();
        this.channel = ManagedChannelBuilder.forTarget(target.toString().substring(1))
                .usePlaintext(true)
                .build();
        this.streamID = siddhiAppContext.getName() + GrpcConstants.PORT_HOST_SEPARATOR + streamDefinition.toString();

        if (optionHolder.isOptionExists(GrpcConstants.SEQUENCE)) {
            isMIConnect = true;
            futureStub = EventServiceGrpc.newFutureStub(channel);
            sequenceName = optionHolder.validateAndGetOption(GrpcConstants.SEQUENCE).getValue();
        } else {
            //todo: handle generic grpc service
        }
        return null;
    }

    @Override
    public void publish(Object payload, DynamicOptions dynamicOptions, State state)
            throws ConnectionUnavailableException {
        if (isMIConnect) {
            if (!(payload instanceof String)) {
                throw new SiddhiAppRuntimeException(siddhiAppContext.getName() + ": Payload should be of type String " +
                        "for communicating with Micro Integrator but found " + payload.getClass().getName());
            }
            if (methodName.equalsIgnoreCase(GrpcConstants.DEFAULT_METHOD_NAME_WITH_RESPONSE)) {
                Event.Builder requestBuilder = Event.newBuilder();
                requestBuilder.setPayload((String) payload);
                Event sequenceCallRequest = requestBuilder.build();
                ListenableFuture<Event> futureResponse =
                        futureStub.process(sequenceCallRequest);
                Futures.addCallback(futureResponse, new FutureCallback<Event>() {
                    @Override
                    public void onSuccess(Event result) {
                        sourceStaticHolder.getGRPCSource(sinkID).onResponse(result);
                    }

                    @Override
                    public void onFailure(Throwable t) {
                        if (logger.isDebugEnabled()) {
                            logger.debug(siddhiAppContext.getName() + ": " + t.getMessage());
                        }
                        throw new SiddhiAppRuntimeException(t.getMessage());
                    }
                }, MoreExecutors.directExecutor());
            } else if (methodName.equalsIgnoreCase(GrpcConstants.DEFAULT_METHOD_NAME_WITHOUT_RESPONSE)) {
                Event.Builder requestBuilder = Event.newBuilder();
                requestBuilder.setPayload((String) payload);
                Event sequenceCallRequest = requestBuilder.build();
                ListenableFuture<Empty> futureResponse =
                        futureStub.consume(sequenceCallRequest);
                Futures.addCallback(futureResponse, new FutureCallback<Empty>() {
                    @Override
                    public void onSuccess(@Nullable Empty result) {

                    }

                    @Override
                    public void onFailure(Throwable t) {
                        if (logger.isDebugEnabled()) {
                            logger.debug(siddhiAppContext.getName() + ": " + t.getMessage());
                        }
                        throw new SiddhiAppRuntimeException(t.getMessage());
                    }
                }, MoreExecutors.directExecutor());
            }
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
        if (!channel.isShutdown()) {
            logger.info(streamID + " has successfully connected to " + url);
        }
    }

    /**
     * Called after all publishing is done, or when {@link ConnectionUnavailableException} is thrown
     * Implementation of this method should contain the steps needed to disconnect from the sink.
     */
    @Override
    public void disconnect() {
        try {
            channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            throw new SiddhiAppRuntimeException(siddhiAppContext.getName() + ": " + e.getMessage());
        }
        logger.info("Channel for url " + url + " shutdown.");
    }

    /**
     * The method can be called when removing an event receiver.
     * The cleanups that have to be done after removing the receiver could be done here.
     */
    @Override
    public void destroy() {
        channel = null;
        futureStub = null;
    }
}
