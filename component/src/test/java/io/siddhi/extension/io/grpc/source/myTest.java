package io.siddhi.extension.io.grpc.source;

import com.google.protobuf.Empty;
import io.grpc.*;
import io.grpc.stub.StreamObserver;
import io.grpc.util.MutableHandlerRegistry;
import io.siddhi.extension.io.grpc.utils.TestServerInterceptor;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.wso2.grpc.Event;
import org.wso2.grpc.EventServiceGrpc;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class myTest {
//    private Server server;
    private TestServerInterceptor testInterceptor = new TestServerInterceptor();
    private int port = 8899;

    @Test
    public void test1() throws IOException, InterruptedException {
        Server server = ServerBuilder
                .forPort(port)
                .addService(myService).build();
        server.start();

        Server server2 = ServerBuilder
                .forPort(port)
                .addService(myService2).build();
        server2.start();

        Thread.sleep(100);

//        Thread client = new Thread() {
//            public void run() {
//                Event.Builder requestBuilder = Event.newBuilder();
//
//                String json = "{ \"message\": \"Benjamin Watson\"}";
//
//                requestBuilder.setPayload(json);
//                requestBuilder.putHeaders("name", "john");
//                requestBuilder.putHeaders("age", "24");
//                Event sequenceCallRequest = requestBuilder.build();
//                ManagedChannel channel = ManagedChannelBuilder.forTarget("localhost:8888")
//                        .usePlaintext(true)
//                        .build();
//                EventServiceGrpc.EventServiceBlockingStub blockingStub = EventServiceGrpc.newBlockingStub(channel);
//
//                Event response = blockingStub.process(sequenceCallRequest);
//                Assert.assertNotNull(response);
//            }
//        };

        Thread.sleep(10000);

        server.shutdownNow();
        server2.shutdownNow();
    }

    private BindableService myService = new EventServiceGrpc.EventServiceImplBase() {
        @Override
        public void process(Event request,
                            StreamObserver<Event> responseObserver) {
//            request.
            System.out.println("server 1");
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
    };

    private BindableService myService2 = new EventServiceGrpc.EventServiceImplBase() {
        @Override
        public void process(Event request,
                            StreamObserver<Event> responseObserver) {
//            request.
            System.out.println("server 2");
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
    };


//    public void start() throws IOException {
//        if (server != null) {
//            throw new IllegalStateException("Already started");
//        }
//        server = ServerBuilder
//                .forPort(port)
//                .addService(
//                        ServerInterceptors.intercept(myService, testInterceptor)).addService(myService).build();
//        server.start();
//    }
//
//    public void stop() throws InterruptedException {
//        Server s = server;
//        if (s == null) {
//            throw new IllegalStateException("Already stopped");
//        }
//        server = null;
//        s.shutdown();
//        if (s.awaitTermination(1, TimeUnit.SECONDS)) {
//
//            return;
//        }
//        s.shutdownNow();
//        if (s.awaitTermination(1, TimeUnit.SECONDS)) {
//            return;
//        }
//        throw new RuntimeException("Unable to shutdown server");
//    }
}
