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
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.ServerInterceptors;
import io.grpc.stub.StreamObserver;
import org.apache.log4j.Logger;
import org.wso2.grpc.test.MyServiceGrpc;
import org.wso2.grpc.test.Request;
import org.wso2.grpc.test.Response;


import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class GenericTestServer {

    private static final Logger logger = Logger.getLogger(TestServer.class.getName());
    TestServerInterceptor testInterceptor = new TestServerInterceptor();
    private Server server;

    public void start() throws IOException {
        if (server != null) {
            throw new IllegalStateException("Already started");
        }
        server = ServerBuilder
                .forPort(8888)
                .addService(ServerInterceptors.intercept(new MyServiceGrpc.MyServiceImplBase() {
                    @Override
                    public void send(Request request, StreamObserver<Empty> responseObserver) {
                        responseObserver.onNext(Empty.getDefaultInstance());
                        responseObserver.onCompleted();
                    }

                    @Override
                    public void process(Request request, StreamObserver<Response> responseObserver) {
                        Response response = Response.newBuilder()
                                .setIntValue(request.getIntValue())
                                .setStringValue(request.getStringValue())
                                .setDoubleValue(request.getDoubleValue())
                                .setLongValue(request.getLongValue())
                                .setBooleanValue(request.getBooleanValue())
                                .setFloatValue(request.getFloatValue())
                                .build();

                        responseObserver.onNext(response);
                        responseObserver.onCompleted();

                    }
                }, testInterceptor)).build();
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
