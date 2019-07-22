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
import io.siddhi.annotation.Parameter;
import io.siddhi.annotation.util.DataType;
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
import io.siddhi.extension.io.grpc.util.service.Event;
import org.apache.log4j.Logger;

/**
 * {@code GrpcSource} Handle receiving of responses for gRPC calls. Does not have connection logics as sink will add a
 * callback to inject responses into this source
 */

@Extension(
        name = "grpc",
        namespace = "source",
        description = "This grpc source receives responses received from gRPC server for requests sent from a gRPC " +
                "sink. The source will receive responses for sink with the same sink.id. For example if you have a " +
                "gRPC sink with sink.id 15 then we need to set the sink.id as 15 in the source to receives " +
                "responses. Sinks and sources have 1:1 mapping. When using the source to listen to responses from " +
                "Micro Integrator the optional parameter sequence should be given. Since the default Micro " +
                "Integrator connection service can provide access to many different sequences this should be " +
                "specified to separate and listen to only one sequence responses.",
        parameters = {
                @Parameter(name = "sink.id",
                        description = "a unique ID that should be set for each gRPC source. There is a 1:1 mapping " +
                                "between gRPC sinks and sources. Each sink has one particular source listening to " +
                                "the responses to requests published from that sink. So the same sink.id should be " +
                                "given when writing the sink also." ,
                        type = {DataType.INT}),
                @Parameter(name = "sequence",
                        description = "This is an optional parameter to be used when connecting to Micro Integrator " +
                                "sequences from Siddhi. Micro integrator will expose a service called EventService " +
                                "which has 2 rpc's as mentioned in the extension description. Both of these rpc can " +
                                "access many different sequences in Micro Integrator. This parameter is used to " +
                                "specify the sequence which we want to use. When this parameter is given gRPC source " +
                                "will listen to MI." ,
                        optional = true, defaultValue = "NA. When sequence is not given the service name and method " +
                        "name should be specified in the url",
                        type = {DataType.STRING}),
        },
        examples = {
                @Example(
                        syntax = "@source(type='grpc', sequence='mySeq', sink.id= '1') " +
                                "define stream BarStream (message String);",
                        description = "Here we are listening to responses from a sequence called mySeq. In addition, " +
                                "only the responses for requests sent from the sink with sink.id 1 will be received " +
                                "here. The results will be injected into BarStream"
                )
        }
)
public class GRPCSource extends Source {
    private static final Logger logger = Logger.getLogger(GRPCSource.class.getName());
    private SiddhiAppContext siddhiAppContext;
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
        return null;
    }

    public void onResponse(Event response) {
        sourceEventListener.onEvent(new Object[]{response.getPayload()}, new String[]{"1"});
        //todo: here I am sending the entire JSON string into the output event. Need to format and extract the data
        // and send as separate values in data array. Will do after some MI side implementation and finalizing the
        // JSON message format
    }

    /**
     * Returns the list of classes which this source can output.
     *
     * @return Array of classes that will be output by the source.
     * Null or empty array if it can produce any type of class.
     */
    @Override
    public Class[] getOutputEventClasses() {
        return new Class[]{io.siddhi.core.event.Event.class};
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
        sourceStaticHolder.removeGRPCSource(sinkID);
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
