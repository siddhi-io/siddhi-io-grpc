package io.siddhi.extension.io.grpc;

import com.google.protobuf.Empty;
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
    TestServerInterceptor testInterceptor = new TestServerInterceptor();

    public void start() throws IOException {
        if (server != null) {
            throw new IllegalStateException("Already started");
        }
        server = ServerBuilder
                .forPort(8888)
                .addService(
                        ServerInterceptors.intercept(new EventServiceGrpc.EventServiceImplBase() {
            @Override
            public void process(Event request,
                                StreamObserver<Event> responseObserver) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Server hit");
                }
                Event.Builder responseBuilder = Event.newBuilder();
                String json = "{ \"message\": \"Benjamin Watson\"}";
                responseBuilder.setPayload(json);
                Event response = responseBuilder.build();
                responseObserver.onNext(response);
                responseObserver.onCompleted();
            }

            @Override
            public void consume(Event request,
                                StreamObserver<Empty> responseObserver) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Server hit");
                }
                System.out.println("Server consume hit " + request.toString());
                responseObserver.onNext(Empty.getDefaultInstance());
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
