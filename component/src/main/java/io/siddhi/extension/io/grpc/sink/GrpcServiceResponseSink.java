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

import io.siddhi.annotation.Example;
import io.siddhi.annotation.Extension;
import io.siddhi.annotation.Parameter;
import io.siddhi.annotation.util.DataType;
import io.siddhi.core.config.SiddhiAppContext;
import io.siddhi.core.exception.ConnectionUnavailableException;
import io.siddhi.core.stream.ServiceDeploymentInfo;
import io.siddhi.core.stream.output.sink.Sink;
import io.siddhi.core.util.config.ConfigReader;
import io.siddhi.core.util.snapshot.state.State;
import io.siddhi.core.util.snapshot.state.StateFactory;
import io.siddhi.core.util.transport.DynamicOptions;
import io.siddhi.core.util.transport.Option;
import io.siddhi.core.util.transport.OptionHolder;
import io.siddhi.extension.io.grpc.util.GrpcConstants;
import io.siddhi.extension.io.grpc.util.GrpcSourceRegistry;
import io.siddhi.query.api.definition.StreamDefinition;
import io.siddhi.query.api.exception.SiddhiAppValidationException;
import org.apache.log4j.Logger;

import java.net.MalformedURLException;
import java.net.URL;

import static io.siddhi.extension.io.grpc.util.GrpcUtils.getMethodName;
import static io.siddhi.extension.io.grpc.util.GrpcUtils.getServiceName;

/**
 * {@code GrpcServiceResponseSink} Handle sending responses for requests received via grpc-service source.
 */
@Extension(
        name = "grpc-service-response", namespace = "sink",
        description = "This extension is used to send responses back to a gRPC client after receiving requests " +
                "through grpc-service source. This calls a callback in grpc-service source to put response back in " +
                "StreamObserver.",
        parameters = {
                @Parameter(name = "url",
                        description = "The url to which the outgoing events should be published via this extension. " +
                                "This url should consist the host address, port, service name, method name in the " +
                                "following format. grpc://hostAddress:port/serviceName/methodName/sequenceName" ,
                        type = {DataType.STRING}),
                @Parameter(name = "source.id",
                        description = "A unique id to identify the coorect source to which this sink is mapped. " +
                                "There is a 1:1 mapping between source and sink" ,
                        type = {DataType.INT}),
        },
        examples = {
                @Example(
                        syntax = "@sink(type='grpc-service-response', " +
                                "url = 'grpc://134.23.43.35:8080/org.wso2.grpc.EventService/consume/mySequence', " +
                                "source.id='1'" +
                                "@map(type='json')) "
                                + "define stream FooStream (messageId String, message String);",
                        description = "asdf"
                        //todo: add an example for generic service access
                )
        }
)
public class GrpcServiceResponseSink extends Sink {
    private static final Logger logger = Logger.getLogger(GrpcServiceResponseSink.class.getName());
    protected GrpcSourceRegistry grpcSourceRegistry = GrpcSourceRegistry.getInstance();
    private String sourceId;
    private Option messageIdOption;
    private SiddhiAppContext siddhiAppContext;
    private String url;
    private String serviceName;
    private String methodName;
    private String sinkID;
    private String streamID;

    @Override
    protected StateFactory init(StreamDefinition outputStreamDefinition, OptionHolder optionHolder,
                                ConfigReader sinkConfigReader, SiddhiAppContext siddhiAppContext) {
        this.siddhiAppContext = siddhiAppContext;
        this.streamID = siddhiAppContext.getName() + GrpcConstants.PORT_HOST_SEPARATOR + outputStreamDefinition.getId();
        this.url = optionHolder.validateAndGetOption(GrpcConstants.PUBLISHER_URL).getValue();
        if (!url.substring(0,4).equalsIgnoreCase(GrpcConstants.GRPC_PROTOCOL_NAME)) {
            throw new SiddhiAppValidationException(streamID + "The url must begin with \"" +
                    GrpcConstants.GRPC_PROTOCOL_NAME + "\" for all grpc sinks");
        }
        URL aURL;
        try {
            aURL = new URL("http" + url.substring(4));
        } catch (MalformedURLException e) {
            throw new SiddhiAppValidationException(siddhiAppContext.getName() + ": MalformedURLException. "
                    + e.getMessage());
        }
        this.serviceName = getServiceName(aURL.getPath());
        this.methodName = getMethodName(aURL.getPath());
        if (optionHolder.isOptionExists(GrpcConstants.SINK_ID)) {
            this.sinkID = optionHolder.validateAndGetOption(GrpcConstants.SINK_ID).getValue();
        } else {
            if (optionHolder.validateAndGetOption(GrpcConstants.SINK_TYPE_OPTION)
                    .getValue().equalsIgnoreCase(GrpcConstants.GRPC_CALL_SINK_NAME)) {
                throw new SiddhiAppValidationException(siddhiAppContext.getName() + ": For grpc-call sink the " +
                        "parameter sink.id is mandatory for receiving responses. Please provide a sink.id");
            }
        }
        this.sourceId = optionHolder.validateAndGetOption(GrpcConstants.SOURCE_ID).getValue();
        this.messageIdOption = optionHolder.validateAndGetOption(GrpcConstants.MESSAGE_ID);
        return null;
    }

    @Override
    public void publish(Object payload, DynamicOptions dynamicOptions, State state) {
        String messageId = messageIdOption.getValue(dynamicOptions);
        grpcSourceRegistry.getGrpcServiceSource(sourceId).handleCallback(messageId, (String) payload);
    }

    @Override
    public void connect() throws ConnectionUnavailableException {
    }
    @Override
    public void disconnect() {
    }

    @Override
    public void destroy() {

    }

    @Override
    public Class[] getSupportedInputEventClasses() {
        return new Class[]{Object.class}; // in default case json mapper will inject String. In custom gRPC service
         // case protobuf mapper will inject gRPC message class
    }

    @Override
    protected ServiceDeploymentInfo exposeServiceDeploymentInfo() {
        return null;
    }

    @Override
    public String[] getSupportedDynamicOptions() {
        return new String[]{GrpcConstants.MESSAGE_ID};
    }
}
