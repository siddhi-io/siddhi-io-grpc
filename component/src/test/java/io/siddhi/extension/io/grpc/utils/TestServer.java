/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package io.siddhi.extension.io.grpc.utils;

import com.google.protobuf.Empty;
import io.grpc.BindableService;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.ServerInterceptors;
import io.grpc.stub.StreamObserver;
import org.apache.log4j.Logger;
import org.wso2.grpc.Event;
import org.wso2.grpc.EventServiceGrpc;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class TestServer {
    private static final Logger logger = Logger.getLogger(TestServer.class.getName());
    private Server server;
    private TestServerInterceptor testInterceptor = new TestServerInterceptor();
    private int port;

    private BindableService myService = new EventServiceGrpc.EventServiceImplBase() {
        @Override
        public void process(Event request,
                            StreamObserver<Event> responseObserver) {
            if (logger.isDebugEnabled()) {
                logger.debug("Server process hit with payload = " + request.getPayload() + " and Headers = {"
                        + request.getHeadersMap().toString() + "}");
            }
            Event.Builder responseBuilder = Event.newBuilder();
            String json = "{ \"message\": \"Hello from Server!\"}";
            responseBuilder.setPayload(json);
            Event response = responseBuilder.build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        }

        @Override
        public StreamObserver<Event> consume(StreamObserver<Empty> responseObserver) {
            return new StreamObserver<Event>() {
                @Override
                public void onNext(Event request) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("Server consume hit with payload = " + request.getPayload() + " and Headers = {"
                                + request.getHeadersMap().toString() + "}");
                    }
                    System.out.println(request.getPayload());
                }

                @Override
                public void onError(Throwable t) {

                }

                @Override
                public void onCompleted() {
                    responseObserver.onNext(Empty.getDefaultInstance());
                    responseObserver.onCompleted();
                }
            };
        }
    };

    public TestServer(int port) {
        this.port = port;
    }

    public void start() throws IOException {
        if (server != null) {
            throw new IllegalStateException("Already started");
        }
        server = ServerBuilder
                .forPort(port)
                .addService(
                        ServerInterceptors.intercept(myService, testInterceptor)).build();
        server.start();
        if (logger.isDebugEnabled()) {
            logger.debug("Server started");
        }
    }

    public void stop() throws InterruptedException {
        Server s = server;
        if (s == null) {
            throw new IllegalStateException("Already stopped");
        }
        server = null;
        s.shutdown();
        if (s.awaitTermination(1, TimeUnit.SECONDS)) {

            if (logger.isDebugEnabled()) {
                logger.debug("Server stopped");
            }
            return;
        }
        s.shutdownNow();
        if (s.awaitTermination(1, TimeUnit.SECONDS)) {
            return;
        }
        throw new RuntimeException("Unable to shutdown server");
    }

    public int getPort() {
        return server.getPort();
    }
}
