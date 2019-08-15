package io.siddhi.extension.io.grpc.source;

import com.google.protobuf.Empty;
import io.grpc.HandlerRegistry;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.ServerInterceptors;
import io.grpc.stub.StreamObserver;
import io.grpc.util.MutableHandlerRegistry;
import org.testng.annotations.Test;
import org.wso2.grpc.Event;
import org.wso2.grpc.EventServiceGrpc;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class myTest {
    private Server server;
    private int port = 8080;
    @Test
    public void hotDeployment() throws IOException {
        HandlerRegistry serviceRegistry = new MutableHandlerRegistry();
        ((MutableHandlerRegistry) serviceRegistry).addService(new EventServiceGrpc.EventServiceImplBase() {
            @Override
            public void process(Event request,
                                StreamObserver<Event> responseObserver) {

                Event.Builder responseBuilder = Event.newBuilder();
                String json = "{ \"message\": \"Hello from Server!\"}";
                responseBuilder.setPayload(json);
                Event response = responseBuilder.build();
                responseObserver.onNext(response);
                responseObserver.onCompleted();
            }

            @Override
            public void consume(Event request,
                                StreamObserver<Empty> responseObserver) {

                responseObserver.onNext(Empty.getDefaultInstance());
                responseObserver.onCompleted();
            }
        });
        ServerBuilder serverBuilder = ServerBuilder.forPort(port);
        serverBuilder.fallbackHandlerRegistry(serviceRegistry);
        server = serverBuilder.build();
        server.start();
        List mutableserv = server.getMutableServices();
        System.out.println("Sdf");
    }

    public void start() throws IOException {
        if (server != null) {
            throw new IllegalStateException("Already started");
        }
        server = ServerBuilder
                .forPort(port)
                .addService(new EventServiceGrpc.EventServiceImplBase() {
                            @Override
                            public void process(Event request,
                                                StreamObserver<Event> responseObserver) {

                                Event.Builder responseBuilder = Event.newBuilder();
                                String json = "{ \"message\": \"Hello from Server!\"}";
                                responseBuilder.setPayload(json);
                                Event response = responseBuilder.build();
                                responseObserver.onNext(response);
                                responseObserver.onCompleted();
                            }

                            @Override
                            public void consume(Event request,
                                                StreamObserver<Empty> responseObserver) {

                                responseObserver.onNext(Empty.getDefaultInstance());
                                responseObserver.onCompleted();
                            }
                        }).build();
        server.start();
    }

    public void stop() throws InterruptedException {
        Server s = server;
        if (s == null) {
            throw new IllegalStateException("Already stopped");
        }
        server = null;
        s.shutdown();
        if (s.awaitTermination(1, TimeUnit.SECONDS)) {
            return;
        }
        s.shutdownNow();
        if (s.awaitTermination(1, TimeUnit.SECONDS)) {
            return;
        }
        throw new RuntimeException("Unable to shutdown server");
    }
}
