package io.siddhi.extension.io.grpc;

import com.google.protobuf.Empty;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;
import io.siddhi.extension.io.grpc.util.service.Event;
import io.siddhi.extension.io.grpc.util.service.EventServiceGrpc;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class TestServer {
    private static final Logger logger = Logger.getLogger(TestServer.class.getName());
    private Server server;

    public void start() throws IOException {
        if (server != null) {
            throw new IllegalStateException("Already started");
        }
        server = ServerBuilder.forPort(0).addService(new EventServiceGrpc.EventServiceImplBase() {
            @Override
            public void process(io.siddhi.extension.io.grpc.util.service.Event request,
                                StreamObserver<Event> responseObserver) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Server hit");
                }
                io.siddhi.extension.io.grpc.util.service.Event.Builder responseBuilder =
                        io.siddhi.extension.io.grpc.util.service.Event.newBuilder();
                responseBuilder.setPayload("server data");
                io.siddhi.extension.io.grpc.util.service.Event response = responseBuilder.build();
                responseObserver.onNext(response);
                responseObserver.onCompleted();
            }

            @Override
            public void consume(io.siddhi.extension.io.grpc.util.service.Event request,
                                StreamObserver<Empty> responseObserver) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Server hit");
                }
                responseObserver.onNext(Empty.getDefaultInstance());
                responseObserver.onCompleted();
            }
        }).build();
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
