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

import io.grpc.Server;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import io.siddhi.annotation.Example;
import io.siddhi.annotation.Extension;
import io.siddhi.annotation.Parameter;
import io.siddhi.annotation.util.DataType;
import io.siddhi.core.exception.ConnectionUnavailableException;
import io.siddhi.core.exception.SiddhiAppRuntimeException;
import io.siddhi.core.util.snapshot.state.State;
import io.siddhi.core.util.transport.OptionHolder;
import io.siddhi.extension.io.grpc.util.GrpcConstants;
import io.siddhi.extension.io.grpc.util.GrpcSourceRegistry;
import org.apache.log4j.Logger;
import org.wso2.grpc.Event;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

/**
 * This extension handles receiving requests from grpc clients/stubs and sending back responses
 */
@Extension(name = "grpc-service", namespace = "source",
        description = "This extension implements a grpc server for receiving and responding to requests. During " +
                "initialization time a grpc server is started on the user specified port exposing the required " +
                "service as given in the url. This source also has a default mode and a user defined grpc service " +
                "mode. By default this uses EventService. Please find the io.siddhi.extension.io.grpc.proto definition [here](https://github.com/" +
                "siddhi-io/siddhi-io-grpc/tree/master/component/src/main/resources/EventService.io.siddhi.extension.io.grpc.proto) In the default" +
                " mode this will use the EventService process method. This accepts grpc message class Event as " +
                "defined in the EventService io.siddhi.extension.io.grpc.proto. This uses GrpcServiceResponse sink to send reponses back in the " +
                "same Event message format.",
        parameters = {
                @Parameter(
                        name = "receiver.url",
                        description = "The url which can be used by a client to access the grpc server in this " +
                                "extension. This url should consist the host hostPort, port, fully qualified service " +
                                "name, method name in the following format. `grpc://0.0.0.0:9763/<serviceName>/" +
                                "<methodName>`\n" +
                                "For example:\n" +
                                "grpc://0.0.0.0:9763/org.wso2.grpc.EventService/consume" ,
                        type = {DataType.STRING}),
                @Parameter(
                        name = "max.inbound.message.size",
                        description = "Sets the maximum message size in bytes allowed to be received on the server." ,
                        type = {DataType.INT},
                        optional = true,
                        defaultValue = "4194304"),
                @Parameter(
                        name = "max.inbound.metadata.size",
                        description = "Sets the maximum size of metadata in bytes allowed to be received." ,
                        type = {DataType.INT},
                        optional = true,
                        defaultValue = "8192"),
                @Parameter(
                        name = "service.timeout",
                        description = "The period of time in milliseconds to wait for siddhi to respond to a " +
                                "request received. After this time period of receiving a request it will be closed " +
                                "with an error message." ,
                        type = {DataType.INT},
                        optional = true,
                        defaultValue = "10000"),
                @Parameter(
                        name = "server.shutdown.waiting.time",
                        description = "The time in seconds to wait for the server to shutdown, giving up " +
                                "if the timeout is reached." ,
                        type = {DataType.LONG},
                        optional = true,
                        defaultValue = "5"),
                @Parameter(
                        name = "truststore.file",
                        description = "the file path of truststore. If this is provided then server authentication " +
                                "is enabled" ,
                        type = {DataType.STRING},
                        optional = true,
                        defaultValue = "-"),
                @Parameter(
                        name = "truststore.password",
                        description = "the password of truststore. If this is provided then the integrity of the " +
                                "keystore is checked" ,
                        type = {DataType.STRING},
                        optional = true,
                        defaultValue = "-"),
                @Parameter(
                        name = "truststore.algorithm",
                        description = "the encryption algorithm to be used for server authentication" ,
                        type = {DataType.STRING},
                        optional = true,
                        defaultValue = "-"),
                @Parameter(
                        name = "tls.store.type",
                        description = "TLS store type" ,
                        type = {DataType.STRING},
                        optional = true,
                        defaultValue = "-"),
                @Parameter(
                        name = "keystore.file",
                        description = "the file path of keystore. If this is provided then client authentication " +
                                "is enabled" ,
                        type = {DataType.STRING},
                        optional = true,
                        defaultValue = "-"),
                @Parameter(
                        name = "keystore.password",
                        description = "the password of keystore" ,
                        type = {DataType.STRING},
                        optional = true,
                        defaultValue = "-"),
                @Parameter(
                        name = "keystore.algorithm",
                        description = "the encryption algorithm to be used for client authentication" ,
                        type = {DataType.STRING},
                        optional = true,
                        defaultValue = "-"),
        },
        examples = {
                @Example(syntax = "" +
                        "@source(type='grpc-service',\n" +
                        "       receiver.url='grpc://localhost:8888/org.wso2.grpc.EventService/process',\n" +
                        "       source.id='1',\n" +
                        "       @map(type='json', @attributes(messageId='trp:messageId', message='message')))\n" +
                        "define stream FooStream (messageId String, message String);",
                        description = "Here a grpc server will be started at port 8888. The process method of " +
                                "EventService will be exposed for clients. source.id is set as 1. So a " +
                                "grpc-service-response sink with source.id = 1 will send responses back for requests " +
                                "received to this source. Note that it is required to specify the transport property " +
                                "messageId since we need to correlate the request message with the response."
                ),
                @Example(syntax = "" +
                        "@sink(type='grpc-service-response',\n" +
                        "      source.id='1',\n" +
                        "      @map(type='json'))\n" +
                        "define stream BarStream (messageId String, message String);\n" +
                        "\n" +
                        "@source(type='grpc-service',\n" +
                        "       receiver.url='grpc://134.23.43.35:8080/org.wso2.grpc.EventService/process',\n" +
                        "       source.id='1',\n" +
                        "       @map(type='json', @attributes(messageId='trp:messageId', message='message')))\n" +
                        "define stream FooStream (messageId String, message String);\n" +
                        "\n" +
                        "from FooStream\n" +
                        "select * \n" +
                        "insert into BarStream;",
                        description = "The grpc requests are received through the grpc-service sink. Each received " +
                                "event is sent back through grpc-service-source. This is just a passthrough through " +
                                "Siddhi as we are selecting everything from FooStream and inserting into BarStream."
                ),
                @Example(syntax = "" +
                        "@source(type='grpc-service', source.id='1' " +
                        "       receiver.url='grpc://locanhost:8888/org.wso2.grpc.EventService/consume', " +
                        "       @map(type='json', @attributes(name='trp:name', age='trp:age', message='message'))) " +
                        "define stream BarStream (message String, name String, age int);",
                        description = "Here we are getting headers sent with the request as transport properties and " +
                                "injecting them into the stream. With each request a header will be sent in MetaData " +
                                "in the following format: 'Name:John', 'Age:23'"
                )
        }
)
public class GrpcServiceSource extends AbstractGrpcSource {
    private static final Logger logger = Logger.getLogger(GrpcServiceSource.class.getName());
    private Map<String, StreamObserver<Event>> streamObserverMap = Collections.synchronizedMap(new HashMap<>());
    private String sourceId;
    private long serviceTimeout;
    protected Server server;
    private Timer timer;

    public void scheduleServiceTimeout(String messageId) {
        timer.schedule(new GrpcServiceSource.ServiceSourceTimeoutChecker(messageId,
                siddhiAppContext.getTimestampGenerator().currentTime()), serviceTimeout);
    }

    class ServiceSourceTimeoutChecker extends TimerTask {
        private String messageId;
        private long requestReceivedTime;

        public ServiceSourceTimeoutChecker(String messageId, long requestReceivedTime) {
            this.messageId = messageId;
            this.requestReceivedTime = requestReceivedTime;
        }

        @Override
        public void run() {
            while (requestReceivedTime > siddhiAppContext.getTimestampGenerator().currentTime() - serviceTimeout) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    throw new SiddhiAppRuntimeException(siddhiAppContext.getName() + ": " + streamID + ": " +
                            e.getMessage(), e);
                }
            }
            StreamObserver streamObserver = streamObserverMap.remove(messageId);
            if (streamObserver != null) {
                streamObserver.onError(new io.grpc.StatusRuntimeException(
                        Status.DEADLINE_EXCEEDED));
            }
        }
    }

    //@Override
    public void initSource(OptionHolder optionHolder, String[] requestedTransportPropertyNames) {
        this.sourceId = optionHolder.validateAndGetOption(GrpcConstants.SOURCE_ID).getValue();
        this.serviceTimeout = Long.parseLong(optionHolder.getOrCreateOption(GrpcConstants.SERVICE_TIMEOUT,
                GrpcConstants.SERVICE_TIMEOUT_DEFAULT).getValue());
        this.timer = new Timer();
        GrpcSourceRegistry.getInstance().putGrpcServiceSource(sourceId, this);
        GrpcServerManager.getInstance().registerSource(grpcServerConfigs, this, GrpcConstants
                .DEFAULT_METHOD_NAME_WITH_RESPONSE, siddhiAppContext, streamID);
    }

    @Override
    public void connect(ConnectionCallback connectionCallback, State state) throws ConnectionUnavailableException {
        if (GrpcServerManager.getInstance().getServer(grpcServerConfigs.getServiceConfigs().getPort())
                .getState() == 0) {
            GrpcServerManager.getInstance().getServer(grpcServerConfigs.getServiceConfigs().getPort()).connectServer(
                    logger, connectionCallback, siddhiAppContext, streamID);
        }
    }

    /**
     * This method can be called when it is needed to disconnect from the end point.
     */
    @Override
    public void disconnect() {
        GrpcServerManager.getInstance().unregisterSource(grpcServerConfigs.getServiceConfigs().getPort(), streamID,
                GrpcConstants.DEFAULT_METHOD_NAME_WITH_RESPONSE, logger, siddhiAppContext);
    }

    public void handleCallback(String messageId, String responsePayload) {
        if (grpcServerConfigs.getServiceConfigs().isDefaultService()) {
            StreamObserver<Event> streamObserver = streamObserverMap.remove(messageId);
            if (streamObserver != null) {
                Event.Builder responseBuilder = Event.newBuilder();
                responseBuilder.setPayload(responsePayload);
                Event response = responseBuilder.build();
                streamObserver.onNext(response);
                streamObserver.onCompleted();
            }
        }
    }

    public void putStreamObserver(String messageID, StreamObserver streamObserver) {
        streamObserverMap.put(messageID, streamObserver);
    }

    @Override
    public void destroy() {
        GrpcSourceRegistry.getInstance().removeGrpcServiceSource(sourceId);
    }

    @Override
    public void logError(String message) {
        logger.error(siddhiAppContext.getName() + ": " + streamID + ": " + message);
    }
}
