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
import io.siddhi.core.util.snapshot.state.StateFactory;
import io.siddhi.core.util.transport.Option;
import io.siddhi.core.util.transport.OptionHolder;
import io.siddhi.extension.io.grpc.util.GrpcConstants;
import io.siddhi.query.api.definition.StreamDefinition;
import io.siddhi.query.api.exception.SiddhiAppValidationException;
import org.apache.log4j.Logger;
import org.wso2.grpc.EventServiceGrpc;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * {@code AbstractGrpcSink} is a super class extended by GrpcCallSink, and GrpcSink.
 * This provides most of the initialization implementations
 */

public abstract class AbstractGrpcSink extends Sink {
    private static final Logger logger = Logger.getLogger(AbstractGrpcSink.class.getName());
    protected SiddhiAppContext siddhiAppContext;
    protected ManagedChannel channel;
    private String serviceName;
    protected String methodName;
    private String sequenceName;
    protected boolean isDefaultMode = false;
    private String url;
    private String streamID; //todo: no need. check if we need this for error throwing
    private String address;
    protected EventServiceGrpc.EventServiceFutureStub futureStub;
    protected Option headersOption;

    /**
     * Returns the list of classes which this sink can consume.
     * Based on the type of the sink, it may be limited to being able to publish specific type of classes.
     * For example, a sink of type file can only write objects of type String .
     * @return array of supported classes , if extension can support of any types of classes
     * then return empty array .
     */
    @Override
    public Class[] getSupportedInputEventClasses() {
        return new Class[]{com.google.protobuf.GeneratedMessageV3.class, String.class};
        // in default case json mapper will inject String. In custom gRPC service
        // case protobuf mapper will inject gRPC message class
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
        return new String[]{GrpcConstants.HEADERS};
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
        this.streamID = siddhiAppContext.getName() + GrpcConstants.PORT_HOST_SEPARATOR + streamDefinition.getId();
        if (optionHolder.isOptionExists(GrpcConstants.HEADERS)) {
            this.headersOption = optionHolder.validateAndGetOption(GrpcConstants.HEADERS);
        }

        List<String> urlParts = new ArrayList<>(Arrays.asList(url.split(GrpcConstants.PORT_SERVICE_SEPARATOR)));
        urlParts.removeAll(Collections.singletonList(GrpcConstants.EMPTY_STRING)); //todo: use java url validator. dont split by urself
        if (!urlParts.get(GrpcConstants.URL_PROTOCOL_POSITION)
                .equalsIgnoreCase(GrpcConstants.GRPC_PROTOCOL_NAME + ":")) {
            throw new SiddhiAppValidationException(siddhiAppContext.getName() + ": " + streamID +
                    "The url must begin with \"" + GrpcConstants.GRPC_PROTOCOL_NAME + "\" for all grpc sinks");
        }
        String[] fullyQualifiedServiceNameParts = urlParts.get(GrpcConstants.URL_SERVICE_NAME_POSITION).split("\\.");
        this.serviceName = fullyQualifiedServiceNameParts[fullyQualifiedServiceNameParts.length - 1];
        this.methodName = urlParts.get(GrpcConstants.URL_METHOD_NAME_POSITION);
        this.address = urlParts.get(GrpcConstants.URL_HOST_AND_PORT_POSITION);
        initSink(optionHolder);

        if (serviceName.equals(GrpcConstants.DEFAULT_SERVICE_NAME)
                && (methodName.equals(GrpcConstants.DEFAULT_METHOD_NAME_WITH_RESPONSE)
                || methodName.equals(GrpcConstants.DEFAULT_METHOD_NAME_WITHOUT_RESPONSE))) {
            this.isDefaultMode = true;
            if (urlParts.size() == GrpcConstants.NUM_URL_PARTS_FOR_MI_MODE_SINK) {
                this.sequenceName = urlParts.get(GrpcConstants.URL_SEQUENCE_NAME_POSITION);
            }
        } else {
            //todo: handle generic grpc service
        }
        return null;
    }

    public abstract void initSink(OptionHolder optionHolder);

    /**
     * This method will be called before the processing method.
     * Intention to establish connection to publish event.
     * @throws ConnectionUnavailableException if end point is unavailable the ConnectionUnavailableException thrown
     *                                        such that the  system will take care retrying for connection
     */
    @Override
    public void connect() throws ConnectionUnavailableException {
        this.channel = ManagedChannelBuilder.forTarget(address).usePlaintext(true)
                .build();
        this.futureStub = EventServiceGrpc.newFutureStub(channel);
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
            channel.shutdown().awaitTermination(5, TimeUnit.SECONDS); //todo: check for optimal time. check if we want user to configure this
        } catch (InterruptedException e) {
            throw new SiddhiAppRuntimeException(siddhiAppContext.getName() + ": Error in shutting down the channel. "
                    + e.getMessage());
        }
    }

    /**
     * The method can be called when removing an event receiver.
     * The cleanups that have to be done after removing the receiver could be done here.
     */
    @Override
    public void destroy() {
        channel = null;
    }
}
