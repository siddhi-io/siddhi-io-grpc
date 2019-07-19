package io.siddhi.extension.io.grpc;

import io.grpc.BindableService;
import io.grpc.ServerCallHandler;
import io.grpc.ServerServiceDefinition;
import io.grpc.stub.ServerCalls;
import io.grpc.stub.StreamObserver;
import io.siddhi.extension.io.grpc.sink.GRPCSink;
import io.siddhi.extension.io.grpc.util.GRPCService;
import io.siddhi.extension.io.grpc.util.GRPCService.EmptyResponse;

public class TestService implements BindableService {
    private String SERVICE_NAME = "TestService";
    @Override
    public ServerServiceDefinition bindService() {
        ServerServiceDefinition.Builder ssd = ServerServiceDefinition.builder(SERVICE_NAME);
        ServerCallHandler<GRPCService.Request, GRPCService.EmptyResponse> serverCallHandler = ServerCalls.asyncUnaryCall((request, responseObserver) -> create(request, responseObserver));
        ssd.addMethod(GRPCSink.CREATE_METHOD, serverCallHandler);
        return ssd.build();
    }

    public void create(GRPCService.Request request, StreamObserver<GRPCService.EmptyResponse> responseObserver) {
        System.out.println("server hit!");
        EmptyResponse emptyResponse = new EmptyResponse();
        responseObserver.onNext(emptyResponse);
        responseObserver.onCompleted();
    }

}
