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
import io.siddhi.core.exception.ConnectionUnavailableException;
import io.siddhi.core.util.snapshot.state.State;
import io.siddhi.core.util.transport.DynamicOptions;
import io.siddhi.core.util.transport.Option;
import io.siddhi.core.util.transport.OptionHolder;
import io.siddhi.extension.io.grpc.util.GrpcConstants;
import org.apache.log4j.Logger;

/**
 * {@code GrpcServiceResponseSink} Handle the gRPC publishing tasks.
 */
@Extension(
        name = "grpc-service-response", namespace = "sink",
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
                                "following format. grpc://hostAddress:port/serviceName/methodName/sequenceName" ,
                        type = {DataType.STRING}),
                @Parameter(name = "source.id",
                        description = "sadf" ,
                        type = {DataType.INT}),
        },
        examples = {
                @Example(
                        syntax = "@sink(type='grpc', " +
                                "url = 'grpc://134.23.43.35:8080/org.wso2.grpc.EventService/consume/mySequence', " +
                                "@map(type='json')) "
                                + "define stream FooStream (message String);",
                        description = "asdf"
                        //todo: add an example for generic service access
                )
        }
)
public class GrpcServiceResponseSink extends AbstractGrpcSink {
    private static final Logger logger = Logger.getLogger(GrpcServiceResponseSink.class.getName());
    private String sourceId;
    private Option messageIdOption;

    @Override
    void initSink(OptionHolder optionHolder) {
        sourceId = optionHolder.validateAndGetOption(GrpcConstants.SOURCE_ID).getValue();
        this.messageIdOption = optionHolder.validateAndGetOption(GrpcConstants.MESSAGE_ID);
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
    public String[] getSupportedDynamicOptions() {
        return new String[]{GrpcConstants.MESSAGE_ID};
    }
}
