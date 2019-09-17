package io.siddhi.extension.io.grpc.utils;

import com.google.protobuf.Any;
import com.google.protobuf.Empty;
import com.google.protobuf.InvalidProtocolBufferException;
import io.grpc.Channel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;
import io.siddhi.extension.io.grpc.proto.MyServiceGrpc;
import io.siddhi.extension.io.grpc.proto.Request;
import io.siddhi.extension.io.grpc.util.GenericService;

import java.io.IOException;

public class Test {
    Server server;
    public static void main(String[] args) throws InterruptedException {
        Test test = new Test();
        test.startServer(8000);
        Thread client = new Thread() {
            @Override
            public void run() {
                Channel channel = ManagedChannelBuilder.forTarget("localhost:8000").usePlaintext().build();
                MyServiceGrpc.MyServiceBlockingStub stub = MyServiceGrpc.newBlockingStub(channel);

                Request request = Request.newBuilder()
                        .setIntValue(100)
                        .setBooleanValue(true)
                        .setDoubleValue(100000.123)
                        .setStringValue("kjasfdkjhfsaf")
                        .setFloatValue(9906.54f)
                        .setLongValue(1000L)
                        .build();
                stub.send(request);
            }
        };

        client.start();
        Thread.sleep(1000);
    }

    public void startServer(int port) {
        GenericService.setServiceName("MyService");
        GenericService.setEmptyResponseMethodName("send");
        GenericService.setClientStreamMethodName("clientStream");
        this.server = ServerBuilder.forPort(port).addService(new Generic()).build();
        try {
            server.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private class Generic extends GenericService.AnyServiceImplBase {
        @Override
        public void handleEmptyResponse(Any request, StreamObserver<Empty> responseObserver) {
            try {
                Request request1 = Request.parseFrom(request.toByteString());
            } catch (InvalidProtocolBufferException e) {
                e.printStackTrace();
            }
        }
    }
}
