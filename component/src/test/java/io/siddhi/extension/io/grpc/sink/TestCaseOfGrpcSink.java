package io.siddhi.extension.io.grpc.sink;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;
import io.siddhi.core.SiddhiAppRuntime;
import io.siddhi.core.SiddhiManager;
import io.siddhi.core.event.Event;
import io.siddhi.core.query.output.callback.QueryCallback;
import io.siddhi.core.stream.input.InputHandler;
import io.siddhi.core.util.EventPrinter;
import io.siddhi.extension.io.grpc.util.service.InvokeSequenceGrpc;
import io.siddhi.extension.io.grpc.util.service.SequenceCallRequest;
import io.siddhi.extension.io.grpc.util.service.SequenceCallResponse;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class TestCaseOfGrpcSink {
    private Server server;
        @Test
        public void test1() throws Exception {
            SiddhiManager siddhiManager = new SiddhiManager();

            startServer();
            String port = String.valueOf(server.getPort());
            String inStreamDefinition = ""
                    + "@sink(type='grpc', " +
                    "host = 'dns:///localhost', " +
                    "port = '" + port + "', " +
                    "sequence = 'mySeq', " +
                    "response = 'true', " +
                    "sink.id= '1') "
                    + "define stream FooStream (message String);";

            String stream2 = "@source(type='grpc', sequence='mySeq', response='true', sink.id= '1') " +
                    "define stream BarStream (message String);";
            String query = "@info(name = 'query') "
                    + "from BarStream "
                    + "select *  "
                    + "insert into outputStream;";

            SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(inStreamDefinition + stream2 + query);
            siddhiAppRuntime.addCallback("query", new QueryCallback() {
                @Override
                public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                    EventPrinter.print(timeStamp, inEvents, removeEvents);
                }
            });
            InputHandler fooStream = siddhiAppRuntime.getInputHandler("FooStream");


            System.out.println(server.getPort());
            try {
                siddhiAppRuntime.start();

                fooStream.send(new Object[]{"niruhan"});
                fooStream.send(new Object[]{"niruhan2"});

                Thread.sleep(5000);
                siddhiAppRuntime.shutdown();
            } finally {
                stopServer();
            }
        }

    private void startServer() throws IOException {
        if (server != null) {
            throw new IllegalStateException("Already started");
        }
        server = ServerBuilder.forPort(0).addService(new InvokeSequenceGrpc.InvokeSequenceImplBase() {
            @Override
            public void callSequenceWithResponse(SequenceCallRequest request, StreamObserver<SequenceCallResponse> responseObserver) {
                System.out.println("Server hit");
                SequenceCallResponse.Builder responseBuilder = SequenceCallResponse.newBuilder();
                responseBuilder.setResponseAsJSON("server data");
                SequenceCallResponse response = responseBuilder.build();
                responseObserver.onNext(response);
                responseObserver.onCompleted();
            }
        }).build();
        server.start();
        System.out.println("Server started");
    }

    private void stopServer() throws InterruptedException {
        Server s = server;
        if (s == null) {
            throw new IllegalStateException("Already stopped");
        }
        server = null;
        s.shutdown();
        if (s.awaitTermination(1, TimeUnit.SECONDS)) {
            System.out.println("Server stopped");
            return;
        }
        s.shutdownNow();
        if (s.awaitTermination(1, TimeUnit.SECONDS)) {
            return;
        }
        throw new RuntimeException("Unable to shutdown server");
    }
}
