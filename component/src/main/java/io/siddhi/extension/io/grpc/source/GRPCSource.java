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

import io.siddhi.annotation.Example;
import io.siddhi.annotation.Extension;
import io.siddhi.core.config.SiddhiAppContext;
import io.siddhi.core.exception.ConnectionUnavailableException;
import io.siddhi.core.stream.ServiceDeploymentInfo;
import io.siddhi.core.stream.input.source.Source;
import io.siddhi.core.stream.input.source.SourceEventListener;
import io.siddhi.core.util.config.ConfigReader;
import io.siddhi.core.util.snapshot.state.State;
import io.siddhi.core.util.snapshot.state.StateFactory;
import io.siddhi.core.util.transport.OptionHolder;
import io.siddhi.extension.io.grpc.util.SourceStaticHolder;
import io.siddhi.extension.io.grpc.util.service.SequenceCallResponse;
import org.apache.log4j.Logger;

/**
 * This is a sample class-level comment, explaining what the extension class does.
 */

@Extension(
        name = "grpc",
        namespace = "source",
        description = " ",
        parameters = {
                /*@Parameter(name = " ",
                        description = " " ,
                        dynamic = false/true,
                        optional = true/false, defaultValue = " ",
                        type = {DataType.INT, DataType.BOOL, DataType.STRING, DataType.DOUBLE, }),
                        type = {DataType.INT, DataType.BOOL, DataType.STRING, DataType.DOUBLE, }),*/
        },
        examples = {
                @Example(
                        syntax = " ",
                        description = " "
                )
        }
)
public class GRPCSource extends Source {
    private static final Logger logger = Logger.getLogger(GRPCSource.class.getName());
    private SiddhiAppContext siddhiAppContext;
    private static String serviceName;
    private static String methodName;
    private String sequenceName;
    private boolean isMIConnect = false;
    private SourceStaticHolder sourceStaticHolder = SourceStaticHolder.getInstance();
    private String sinkID;
    private SourceEventListener sourceEventListener;

    @Override
    protected ServiceDeploymentInfo exposeServiceDeploymentInfo() {
        return null;
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
        this.siddhiAppContext = siddhiAppContext;
        this.sourceEventListener = sourceEventListener;
        sinkID = optionHolder.validateAndGetOption("sink.id").getValue();
        sourceStaticHolder.putSource(sinkID, this);

        if (!optionHolder.isOptionExists("service")) {
            isMIConnect = true;
            serviceName = "InvokeSequence";
            sequenceName = optionHolder.validateAndGetOption("sequence").getValue();
            boolean isResponseExpected = optionHolder.validateAndGetOption("response").getValue().equalsIgnoreCase("True");
            if (isResponseExpected) {
                methodName = "CallSequenceWithResponse";
            } else {
                methodName = "CallSequenceWithoutResponse";
            }
        } else {
            serviceName = optionHolder.validateAndGetOption("service").getValue();
            methodName = optionHolder.validateAndGetOption("method").getValue();
        }
        return null;
    }

    public void onResponse(SequenceCallResponse response) {
        sourceEventListener.onEvent(new Object[]{response.getResponseAsJSON()}, new String[]{"1"});
    }

    /**
     * Returns the list of classes which this source can output.
     *
     * @return Array of classes that will be output by the source.
     * Null or empty array if it can produce any type of class.
     */
    @Override
    public Class[] getOutputEventClasses() {
        return new Class[0];
    }

    @Override
    public void connect(ConnectionCallback connectionCallback, State state) throws ConnectionUnavailableException {

    }

    /**
     * This method can be called when it is needed to disconnect from the end point.
     */
    @Override
    public void disconnect() {

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
}
