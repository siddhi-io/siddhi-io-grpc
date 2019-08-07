package io.siddhi.extension.io.grpc;

import com.google.protobuf.Empty;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.ServerInterceptors;
import io.grpc.stub.StreamObserver;
import io.siddhi.extension.io.grpc.TestServer;
import io.siddhi.extension.io.grpc.TestServerInterceptor;
import org.apache.log4j.Logger;
import package01.test.MyServiceGrpc;
import package01.test.Request;
import package01.test.Response;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class GenericTestServer {

    private static final Logger logger = Logger.getLogger(TestServer.class.getName());
    private Server server;
    io.siddhi.extension.io.grpc.TestServerInterceptor testInterceptor = new TestServerInterceptor();
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
