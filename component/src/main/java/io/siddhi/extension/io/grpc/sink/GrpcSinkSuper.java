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

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
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
import io.siddhi.query.api.definition.StreamDefinition;
import io.siddhi.query.api.exception.SiddhiAppValidationException;
import org.apache.log4j.Logger;
import org.wso2.grpc.EventServiceGrpc;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * {@code GrpcSink} Handle the gRPC publishing tasks. This is a super class that will be extended by Grpc Sink and Grpc
 * Call sink to implement fire and forget and send request with response cases
 */

public class GrpcSinkSuper extends Sink {
    private static final Logger logger = Logger.getLogger(GrpcSinkSuper.class.getName());
    protected SiddhiAppContext siddhiAppContext;
    private ManagedChannel channel;
    private String serviceName;
    protected String methodName;
    private String sequenceName;
    protected EventServiceGrpc.EventServiceFutureStub futureStub;
    protected boolean isMIConnect = false;
    protected SourceStaticHolder sourceStaticHolder = SourceStaticHolder.getInstance();
    protected String sinkID;
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
        return new String[0];
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
        List<String> URLParts = new ArrayList<>(Arrays.asList(url.split(GrpcConstants.PORT_SERVICE_SEPARATOR)));
        URLParts.removeAll(Arrays.asList(GrpcConstants.EMPTY_STRING));

        if (!URLParts.get(GrpcConstants.URL_PROTOCOL_POSITION)
                .equalsIgnoreCase(GrpcConstants.GRPC_PROTOCOL_NAME + ":")) {
            throw new SiddhiAppValidationException(siddhiAppContext.getName() + ": The url must begin with \"" +
                    GrpcConstants.GRPC_PROTOCOL_NAME + "\" for all grpc sinks");
        }

        this.serviceName = URLParts.get(GrpcConstants.URL_SERVICE_NAME_POSITION);
        this.methodName = URLParts.get(GrpcConstants.URL_METHOD_NAME_POSITION);
        if (optionHolder.isOptionExists(GrpcConstants.SINK_ID)) {
            this.sinkID = optionHolder.validateAndGetOption(GrpcConstants.SINK_ID).getValue();
        } else {
            if (optionHolder.validateAndGetOption(GrpcConstants.SINK_TYPE_OPTION)
                    .getValue().equalsIgnoreCase(GrpcConstants.GRPC_CALL_SINK_NAME)) {
                throw new SiddhiAppValidationException(siddhiAppContext.getName() + ": For grpc-call sink the " +
                        "parameter sink.id is mandatory for receiving responses. Please provide a sink.id");
            }
        }
        this.channel = ManagedChannelBuilder.forTarget(URLParts.get(GrpcConstants.URL_HOST_AND_PORT_POSITION))
                .usePlaintext(true)
                .build();
        this.streamID = siddhiAppContext.getName() + GrpcConstants.PORT_HOST_SEPARATOR + streamDefinition.toString();

        if (serviceName.equals(GrpcConstants.DEFAULT_SERVICE_NAME)
                && (methodName.equals(GrpcConstants.DEFAULT_METHOD_NAME_WITH_RESPONSE)
                || methodName.equals(GrpcConstants.DEFAULT_METHOD_NAME_WITHOUT_RESPONSE))
                && URLParts.size() == GrpcConstants.NUM_URL_PARTS_FOR_MI_CONNECT) {
            isMIConnect = true;
            futureStub = EventServiceGrpc.newFutureStub(channel);
            sequenceName = URLParts.get(GrpcConstants.URL_SEQUENCE_NAME_POSITION);
        } else {
            //todo: handle generic grpc service
        }
        return null;
    }

    @Override
    public void publish(Object payload, DynamicOptions dynamicOptions, State state)
            throws ConnectionUnavailableException {

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
