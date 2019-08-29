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
    private Server server;
    TestServerInterceptor testInterceptor = new TestServerInterceptor();
    public void start() throws IOException {
        if (server != null) {
            throw new IllegalStateException("Already started");
        }
        server = ServerBuilder
                .forPort(8888)
                .addService( ServerInterceptors.intercept( new MyServiceGrpc.MyServiceImplBase() {
                    @Override
                    public void send(Request request, StreamObserver<Empty> responseObserver) {
                        System.out.println("Request :::::::::::::::::::::::::::::::::::");
                        System.out.println(request);
                        System.out.println(":::::::::::::::::::::::::::::::::::::::::::");
                        System.out.println("Server hit ....");
                        responseObserver.onNext(Empty.getDefaultInstance());
                        responseObserver.onCompleted();
                    }

                    @Override
                    public void process(Request request, StreamObserver<Response> responseObserver) {
                        System.out.println("Request :::::::::::::::::::::::::::::::::::");
                        System.out.println(request);
                        System.out.println(":::::::::::::::::::::::::::::::::::::::::::");
                        System.out.println("Server hit ....");
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
                },testInterceptor)).build();
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
