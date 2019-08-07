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
import io.grpc.ServerInterceptors;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import io.siddhi.annotation.Example;
import io.siddhi.annotation.Extension;
import io.siddhi.annotation.Parameter;
import io.siddhi.annotation.util.DataType;
import io.siddhi.core.exception.SiddhiAppRuntimeException;
import io.siddhi.core.util.transport.OptionHolder;
import io.siddhi.extension.io.grpc.util.GrpcConstants;
import io.siddhi.extension.io.grpc.util.GrpcSourceRegistry;
import org.apache.log4j.Logger;
import org.wso2.grpc.Event;
import org.wso2.grpc.EventServiceGrpc;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * This extension handles receiving requests from grpc clients/stubs and sending back responses
 */
@Extension(
        name = "grpc-service",
        namespace = "source",
        description = "This extension implements a grpc server for receiving and responding to requests. During " +
                "initialization time a grpc server is started on the user specified port exposing the required " +
                "service as given in the url. This source also has a default mode and a user defined grpc service " +
                "mode. In the default mode this will use the EventService process method. This accepts grpc message " +
                "class Event as defined in the EventService proto. This uses GrpcServiceResponse sink to send " +
                "reponses back in the same Event message format.",
        parameters = {
                @Parameter(name = "url",
                        description = "The url which can be used by a client to access the grpc server in this " +
                                "extension. This url should consist the host address, port, service name, method " +
                                "name in the following format. grpc://hostAddress:port/serviceName/methodName" ,
                        type = {DataType.STRING}),
                @Parameter(name = "headers",
                        description = "GRPC Request headers in format `\"'<key>:<value>','<key>:<value>'\"`. " +
                                "If header parameter is not provided just the payload is sent" ,
                        type = {DataType.STRING},
                        optional = true,
                        defaultValue = "N/A"),
                @Parameter(name = "max.inbound.message.size",
                        description = "Sets the maximum message size in bytes allowed to be received on the server." ,
                        type = {DataType.INT},
                        optional = true,
                        defaultValue = "4194304"),
                @Parameter(name = "max.inbound.metadata.size",
                        description = "Sets the maximum size of metadata in bytes allowed to be received." ,
                        type = {DataType.INT},
                        optional = true,
                        defaultValue = "8192"),
                @Parameter(name = "service.timeout",
                        description = "The period of time in milliseconds to wait for siddhi to respond to a " +
                                "request received. After this time period of receiving a request it will be closed " +
                                "with an error message." ,
                        type = {DataType.INT},
                        optional = true,
                        defaultValue = "10000"),
        },
        examples = {
                @Example(
                        syntax = "@source(type='grpc-service', " +
                                "url='grpc://localhost:8888/org.wso2.grpc.EventService/process', source.id='1', " +
                                "@map(type='json', @attributes(messageId='trp:messageId', message='message'))) " +
                                "define stream FooStream (messageId String, message String);",
                        description = "Here a grpc server will be started at port 8888. The process method of " +
                                "EventService will be exposed for clients. source.id is set as 1. So a " +
                                "grpc-service-response sink with source.id = 1 will send responses back for " +
                                "requests received to this source. Note that it is required to specify the " +
                                "transport property messageId since we need to correlate the request message with " +
                                "the response."
                )
        }
)
public class GrpcServiceSource extends AbstractGrpcSource {
    private static final Logger logger = Logger.getLogger(GrpcServiceSource.class.getName());
    private Map<String, StreamObserver<Event>> streamObserverMap = new ConcurrentHashMap<>();
    private Map<String, Long> timestampOfMessageIds = new ConcurrentHashMap<>();
    private String sourceId;
    private String headerString;
    private long serviceTimeout;

    @Override
    public void initializeGrpcServer(int port) {
        if (isDefaultMode) {
            this.server = ServerBuilder.forPort(port).addService(ServerInterceptors.intercept(
                    new EventServiceGrpc.EventServiceImplBase() {
                @Override
                public void process(Event request,
                                    StreamObserver<Event> responseObserver) {
                    String messageId = UUID.randomUUID().toString();
                    streamObserverMap.put(messageId, responseObserver);
                    timestampOfMessageIds.put(messageId, siddhiAppContext.getTimestampGenerator().currentTime());
                    if (headerString != null) {
                        try {
                            sourceEventListener.onEvent(request.getPayload(), extractHeaders(headerString + ", '" +
                                    GrpcConstants.MESSAGE_ID + ":" + messageId + "'"));
                        } catch (SiddhiAppRuntimeException e) {
                            logger.error(siddhiAppContext.getName() + "Dropping request. " + e.getMessage());
                            responseObserver.onError(new io.grpc.StatusRuntimeException(Status.DATA_LOSS));
                        }
                    } else {
                        sourceEventListener.onEvent(request.getPayload(), new String[]{messageId});
                    }
                    streamObserverMap.put(messageId, responseObserver);
                }
            }, serverInterceptor)).build();
        }
    }

    class ServiceSourceTimeoutChecker implements Runnable {
        @Override
        public void run() {
            for (String messageId: timestampOfMessageIds.keySet()) {
                if (timestampOfMessageIds.get(messageId) < siddhiAppContext.getTimestampGenerator().currentTime() -
                        serviceTimeout) {
                    streamObserverMap.get(messageId).onError(new io.grpc.StatusRuntimeException(
                            Status.DEADLINE_EXCEEDED));
                }
            }
        }
    }

    @Override
    public void initSource(OptionHolder optionHolder) {
        this.sourceId = optionHolder.validateAndGetOption(GrpcConstants.SOURCE_ID).getValue();
        this.serviceTimeout = Long.parseLong(optionHolder.getOrCreateOption(GrpcConstants.SERVICE_TIMEOUT,
                GrpcConstants.SERVICE_TIMEOUT_DEFAULT).getValue());
        long timeoutCheckInterval;
        if (optionHolder.isOptionExists(GrpcConstants.TIMEOUT_CHECK_INTERVAL)) {
            timeoutCheckInterval = Long.parseLong(optionHolder.validateAndGetOption(
                    GrpcConstants.TIMEOUT_CHECK_INTERVAL).getValue());
        } else {
            timeoutCheckInterval = serviceTimeout;
        }
        GrpcSourceRegistry.getInstance().putGrpcServiceSource(sourceId, this);
        siddhiAppContext.getScheduledExecutorService().scheduleAtFixedRate(new ServiceSourceTimeoutChecker(), 0,
                timeoutCheckInterval, TimeUnit.MILLISECONDS);
    }

    @Override
    public void populateHeaderString(String headerString) {
        this.headerString = headerString;
    }

    public void handleCallback(String messageId, String responsePayload) {
        if (isDefaultMode) {
            Event.Builder responseBuilder = Event.newBuilder();
            responseBuilder.setPayload(responsePayload);
            Event response = responseBuilder.build();
            StreamObserver<Event> streamObserver = streamObserverMap.get(messageId);
            streamObserverMap.remove(messageId);
            streamObserver.onNext(response);
            streamObserver.onCompleted();
        }
    }

    @Override
    public void destroy() {
        GrpcSourceRegistry.getInstance().removeGrpcServiceSource(sourceId);
    }
}
