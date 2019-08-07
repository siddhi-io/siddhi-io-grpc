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

import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.TimeUnit;

import static io.siddhi.extension.io.grpc.util.GrpcUtils.getMethodName;
import static io.siddhi.extension.io.grpc.util.GrpcUtils.getSequenceName;
import static io.siddhi.extension.io.grpc.util.GrpcUtils.getServiceName;
import static io.siddhi.extension.io.grpc.util.GrpcUtils.isSequenceNamePresent;

/**
 * {@code AbstractGrpcSink} is a super class extended by GrpcCallSink, and GrpcSink.
 * This provides most of the initialization implementations
 */

public abstract class AbstractGrpcSink extends Sink {
    private static final Logger logger = Logger.getLogger(AbstractGrpcSink.class.getName());
    protected SiddhiAppContext siddhiAppContext;
    protected ManagedChannel channel;
    protected String methodName;
    protected String sequenceName;
    protected boolean isDefaultMode = false;
    protected String url;
    protected String streamID;
    protected String address;
    protected EventServiceGrpc.EventServiceFutureStub futureStub;
    protected Option headersOption;
    protected ManagedChannelBuilder managedChannelBuilder;
    protected long channelTerminationWaitingTime;

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
        if (!url.substring(0, 4).equalsIgnoreCase(GrpcConstants.GRPC_PROTOCOL_NAME)) {
            throw new SiddhiAppValidationException(streamID + "The url must begin with \"" +
                    GrpcConstants.GRPC_PROTOCOL_NAME + "\" for all grpc sinks");
        }
        URL aURL;
        try {
            aURL = new URL(GrpcConstants.DUMMY_PROTOCOL_NAME + url.substring(4));
        } catch (MalformedURLException e) {
            throw new SiddhiAppValidationException(siddhiAppContext.getName() + ": MalformedURLException. "
                    + e.getMessage());
        }
        String serviceName = getServiceName(aURL.getPath());
        this.methodName = getMethodName(aURL.getPath());
        this.address = aURL.getAuthority();
        this.channelTerminationWaitingTime = Integer.parseInt(optionHolder.getOrCreateOption(
                GrpcConstants.CHANNEL_TERMINATION_WAITING_TIME, GrpcConstants.CHANNEL_TERMINATION_WAITING_TIME_DEFAULT)
                .getValue());

        //ManagedChannelBuilder Properties. i.e gRPC connection parameters
        this.managedChannelBuilder = ManagedChannelBuilder.forTarget(address).usePlaintext();
        managedChannelBuilder.idleTimeout(Long.parseLong(optionHolder.getOrCreateOption(GrpcConstants.IDLE_TIMEOUT,
                GrpcConstants.IDLE_TIMEOUT_DEFAULT).getValue()), TimeUnit.SECONDS);
        managedChannelBuilder.keepAliveTime(Long.parseLong(optionHolder.getOrCreateOption(GrpcConstants.KEEP_ALIVE_TIME,
                GrpcConstants.KEEP_ALIVE_TIME_DEFAULT).getValue()), TimeUnit.SECONDS);
        managedChannelBuilder.keepAliveTimeout(Long.parseLong(optionHolder.getOrCreateOption(
                GrpcConstants.KEEP_ALIVE_TIMEOUT, GrpcConstants.KEEP_ALIVE_TIMEOUT_DEFAULT).getValue()),
                TimeUnit.SECONDS);
        managedChannelBuilder.keepAliveWithoutCalls(Boolean.parseBoolean(optionHolder.getOrCreateOption(
                GrpcConstants.KEEP_ALIVE_WITHOUT_CALLS, GrpcConstants.KEEP_ALIVE_WITHOUT_CALLS_DEFAULT).getValue()));
        managedChannelBuilder.maxRetryAttempts(Integer.parseInt(optionHolder.getOrCreateOption(
                GrpcConstants.MAX_RETRY_ATTEMPTS, GrpcConstants.MAX_RETRY_ATTEMPTS_DEFAULT).getValue()));
        managedChannelBuilder.maxHedgedAttempts(Integer.parseInt(optionHolder.getOrCreateOption(
                GrpcConstants.MAX_HEDGED_ATTEMPTS, GrpcConstants.MAX_HEDGED_ATTEMPTS_DEFAULT).getValue()));
        if (Boolean.parseBoolean(optionHolder.getOrCreateOption(GrpcConstants.ENABLE_RETRY,
                GrpcConstants.ENABLE_RETRY_DEFAULT).getValue())) {
            managedChannelBuilder.enableRetry();
            managedChannelBuilder.retryBufferSize(Long.parseLong(optionHolder.getOrCreateOption(
                    GrpcConstants.RETRY_BUFFER_SIZE, GrpcConstants.RETRY_BUFFER_SIZE_DEFAULT).getValue()));
            managedChannelBuilder.perRpcBufferLimit(Long.parseLong(optionHolder.getOrCreateOption(
                    GrpcConstants.PER_RPC_BUFFER_SIZE, GrpcConstants.PER_RPC_BUFFER_SIZE_DEFAULT).getValue()));
        }
        initSink(optionHolder);

        if (serviceName.equals(GrpcConstants.DEFAULT_SERVICE_NAME)
                && (methodName.equals(GrpcConstants.DEFAULT_METHOD_NAME_WITH_RESPONSE)
                || methodName.equals(GrpcConstants.DEFAULT_METHOD_NAME_WITHOUT_RESPONSE))) {
            this.isDefaultMode = true;
            if (isSequenceNamePresent(aURL.getPath())) {
                this.sequenceName = getSequenceName(aURL.getPath());
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
    public void connect() throws ConnectionUnavailableException {}

    /**
     * Called after all publishing is done, or when {@link ConnectionUnavailableException} is thrown
     * Implementation of this method should contain the steps needed to disconnect from the sink.
     */
    @Override
    public void disconnect() {}

    /**
     * The method can be called when removing an event receiver.
     * The cleanups that have to be done after removing the receiver could be done here.
     */
    @Override
    public void destroy() {
        channel = null;
    }
}
