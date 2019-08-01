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

import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;
import io.siddhi.annotation.Example;
import io.siddhi.annotation.Extension;
import io.siddhi.annotation.Parameter;
import io.siddhi.annotation.util.DataType;
import io.siddhi.core.util.transport.OptionHolder;
import io.siddhi.extension.io.grpc.util.GrpcConstants;
import io.siddhi.extension.io.grpc.util.GrpcSourceRegistry;
import org.apache.log4j.Logger;
import org.wso2.grpc.Event;
import org.wso2.grpc.EventServiceGrpc;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 *
 */
@Extension(
        name = "grpc-service",
        namespace = "source",
        description = "sdfsdf",
        parameters = {
                @Parameter(name = "url",
                        description = "asdfa" ,
                        type = {DataType.STRING}),
        },
        examples = {
                @Example(
                        syntax = "@source(type='grpc', url='', source.id='1') " +
                                "define stream BarStream (message String);",
                        description = "asdfasdf"
                )
        }
)
public class GrpcServiceSource extends AbstractGrpcSource {
    private static final Logger logger = Logger.getLogger(GrpcCallResponseSource.class.getName());
    private Map<String, StreamObserver<Event>> streamObserverMap = new ConcurrentHashMap<>();
    protected GrpcSourceRegistry grpcSourceRegistry = GrpcSourceRegistry.getInstance();

    @Override
    public void initializeGrpcServer(int port) {
        this.server = ServerBuilder.forPort(port).addService(new EventServiceGrpc.EventServiceImplBase() {
            @Override
            public void process(Event request,
                                StreamObserver<Event> responseObserver) { //todo message id & another correlation id for iding source from sink
                System.out.println("received in server");
                if (logger.isDebugEnabled()) {
                    logger.debug(siddhiAppContext.getName() + ": Server hit");
                }
                String messageId = UUID.randomUUID().toString();
                streamObserverMap.put(messageId, responseObserver);
                sourceEventListener.onEvent(request.getPayload(), new String[]{messageId});
                System.out.println("");
            }
        }).build();
    }

    @Override
    public void initSource(OptionHolder optionHolder) {
        String sourceId = optionHolder.validateAndGetOption(GrpcConstants.SOURCE_ID).getValue();
        grpcSourceRegistry.putGrpcServiceSource(sourceId, this);
    }

    public void handleCallback(String messageId, String responsePayload) {
        Event.Builder responseBuilder = Event.newBuilder();
        responseBuilder.setPayload(responsePayload);
        Event response = responseBuilder.build();
        StreamObserver<Event> streamObserver = streamObserverMap.get(messageId);
        streamObserver.onNext(response);
        streamObserver.onCompleted();
    }
}
