package io.siddhi.extension.io.grpc.utils;

import com.google.protobuf.Empty;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.ServerInterceptors;
import io.grpc.stub.StreamObserver;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import io.siddhi.extension.io.grpc.proto.MyServiceGrpc;
import io.siddhi.extension.io.grpc.proto.Request;
import io.siddhi.extension.io.grpc.proto.RequestWithMap;
import io.siddhi.extension.io.grpc.proto.Response;
import io.siddhi.extension.io.grpc.proto.ResponseWithMap;
import io.siddhi.extension.io.grpc.proto.StreamServiceGrpc;

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
                        if (logger.isDebugEnabled()) {
                            logger.debug("Server hits with request :\n" + request);
                        }
                        responseObserver.onNext(Empty.getDefaultInstance());
                        responseObserver.onCompleted();
                    }

                    @Override
                    public void process(Request request, StreamObserver<Response> responseObserver) {
                        if (logger.isDebugEnabled()) {
                            logger.debug("Server hits with request :\n" + request);
                        }
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

                    @Override
                    public void testMap(RequestWithMap request, StreamObserver<ResponseWithMap> responseObserver) {
                        if (logger.isDebugEnabled()) {
                            logger.debug("Server hits with request :\n" + request);
                        }
                        ResponseWithMap response = ResponseWithMap.newBuilder()
                                .setIntValue(request.getIntValue())
                                .setStringValue(request.getStringValue())
                                .putAllMap(request.getMapMap()).build();
                        responseObserver.onNext(response);
                        responseObserver.onCompleted();
                    }
                }, testInterceptor)).addService(ServerInterceptors.intercept(
                        new StreamServiceGrpc.StreamServiceImplBase() {
                    @Override
                    public StreamObserver<Request> clientStream(StreamObserver<Empty> responseObserver) {
                        return new StreamObserver<Request>() {
                            @Override
                            public void onNext(Request value) {
                                logger.log(Level.INFO, "Request : \n" + value);
                            }

                            @Override
                            public void onError(Throwable t) {

                            }

                            @Override
                            public void onCompleted() {
                                logger.info("Done Streaming");
                                responseObserver.onNext(Empty.getDefaultInstance());
                                responseObserver.onCompleted();
                            }
                        };
                    }

                    @Override
                    public StreamObserver<RequestWithMap> clientStreamWithMap(StreamObserver<Empty> responseObserver) {
                        return new StreamObserver<RequestWithMap>() {
                            @Override
                            public void onNext(RequestWithMap value) {
                                logger.log(Level.INFO, "Request : \n" + value);
                            }

                            @Override
                            public void onError(Throwable t) {

                            }

                            @Override
                            public void onCompleted() {
                                logger.info("Done Streaming");
                                responseObserver.onNext(Empty.getDefaultInstance());
                                responseObserver.onCompleted();
                            }
                        };
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
