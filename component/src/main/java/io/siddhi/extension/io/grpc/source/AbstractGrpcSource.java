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

import com.google.protobuf.GeneratedMessageV3;
import io.siddhi.core.config.SiddhiAppContext;
import io.siddhi.core.stream.ServiceDeploymentInfo;
import io.siddhi.core.stream.input.source.Source;
import io.siddhi.core.stream.input.source.SourceEventListener;
import io.siddhi.core.util.config.ConfigReader;
import io.siddhi.core.util.snapshot.state.StateFactory;
import io.siddhi.core.util.transport.OptionHolder;
import io.siddhi.extension.io.grpc.util.*;
import io.siddhi.query.api.exception.SiddhiAppValidationException;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * This is an abstract class extended by GrpcSource and GrpcServiceSource. This provides most of initialization
 * implementations common for both sources
 */
public abstract class AbstractGrpcSource extends Source {
    protected SiddhiAppContext siddhiAppContext;
    protected SourceEventListener sourceEventListener;
    private String[] requestedTransportPropertyNames;
    protected boolean isDefaultMode;
    protected String streamID;
    private ServiceDeploymentInfo serviceDeploymentInfo;
    protected GrpcServerConfigs grpcServerConfigs;

    @Override
    protected ServiceDeploymentInfo exposeServiceDeploymentInfo() {
        return serviceDeploymentInfo;
    }

    /**
     * The initialization method for {@link Source}, will be called before other methods. It used to validate
     * all configurations and to get initial values.
     * @param sourceEventListener After receiving events, the source should trigger onEvent() of this listener.
     *                            Listener will then pass on the events to the appropriate mappers for processing .
     * @param optionHolder        Option holder containing static configuration related to the {@link Source}
     * @param configReader        ConfigReader is used to read the {@link Source} related system configuration.
     * @param siddhiAppContext    the context of the {@link io.siddhi.query.api.SiddhiApp} used to get Siddhi
     */
    @Override
    public StateFactory init(SourceEventListener sourceEventListener, OptionHolder optionHolder,
                             String[] requestedTransportPropertyNames, ConfigReader configReader,
                             SiddhiAppContext siddhiAppContext) {
        this.streamID = sourceEventListener.getStreamDefinition().getId();
        this.siddhiAppContext = siddhiAppContext;
        this.sourceEventListener = sourceEventListener;
        this.requestedTransportPropertyNames = requestedTransportPropertyNames.clone();
        this.grpcServerConfigs = new GrpcServerConfigs(optionHolder, siddhiAppContext,streamID);
        initSource(optionHolder, requestedTransportPropertyNames);
        this.serviceDeploymentInfo = new ServiceDeploymentInfo(grpcServerConfigs.getServiceConfigs().getPort(),
                grpcServerConfigs.getServiceConfigs().getTruststoreFilePath() != null ||
                        grpcServerConfigs.getServiceConfigs().getKeystoreFilePath() != null);
//        ArrayList<String> attributeList = new ArrayList<>(Arrays.asList(sourceEventListener.getStreamDefinition()
//                .getAttributeNameArray())); // is req trp name not in strm def it doesnt come in the reqtrp array
//        for (String requestedTransportProperty: requestedTransportPropertyNames) {
//            if (!attributeList.contains(requestedTransportProperty)) {
//                throw new SiddhiAppValidationException(siddhiAppContext.getName() + ": " + streamID + ": The " +
//                        "requested transport property " + requestedTransportProperty + " not found in stream " +
//                        "definition.");
//            }
//        }
        return null;
    }

    public void handleInjection(String payload, String[] headers) {
        sourceEventListener.onEvent(payload, headers);
    }

    public abstract void initSource(OptionHolder optionHolder, String[] requestedTransportPropertyNames);

    /**
     * Returns the list of classes which this source can output.
     *
     * @return Array of classes that will be output by the source.
     * Null or empty array if it can produce any type of class.
     */
    @Override
    public Class[] getOutputEventClasses() {
        return new Class[]{String.class, GeneratedMessageV3.class};
    }

    /**
     * Called at the end to clean all the resources consumed by the {@link Source}
     */
    @Override
    public void destroy() {

    }

    /**
     * Called to pause event consumption
     */
    @Override
    public void pause() {

    }

    /**
     * Called to resume event consumption
     */
    @Override
    public void resume() {

    }

    public String[] getRequestedTransportPropertyNames() {
        return requestedTransportPropertyNames;
    }

    public String getStreamID() {
        return streamID;
    }
}
