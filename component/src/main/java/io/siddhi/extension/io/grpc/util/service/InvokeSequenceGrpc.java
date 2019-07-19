package io.siddhi.extension.io.grpc.util.service;

import static io.grpc.MethodDescriptor.generateFullMethodName;
import static io.grpc.stub.ClientCalls.asyncUnaryCall;
import static io.grpc.stub.ClientCalls.blockingUnaryCall;
import static io.grpc.stub.ClientCalls.futureUnaryCall;
import static io.grpc.stub.ServerCalls.asyncUnaryCall;
import static io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall;

/**
 */
@javax.annotation.Generated(
    value = "by gRPC proto compiler (version 1.21.0)",
    comments = "Source: siddhi_MI_connect.proto")
public final class InvokeSequenceGrpc {

  private InvokeSequenceGrpc() {}

  public static final String SERVICE_NAME = "invokesequence.InvokeSequence";

  // Static method descriptors that strictly reflect the proto.
  private static volatile io.grpc.MethodDescriptor<SequenceCallRequest,
      SequenceCallResponse> getCallSequenceWithResponseMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "CallSequenceWithResponse",
      requestType = SequenceCallRequest.class,
      responseType = SequenceCallResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<SequenceCallRequest,
      SequenceCallResponse> getCallSequenceWithResponseMethod() {
    io.grpc.MethodDescriptor<SequenceCallRequest, SequenceCallResponse> getCallSequenceWithResponseMethod;
    if ((getCallSequenceWithResponseMethod = InvokeSequenceGrpc.getCallSequenceWithResponseMethod) == null) {
      synchronized (InvokeSequenceGrpc.class) {
        if ((getCallSequenceWithResponseMethod = InvokeSequenceGrpc.getCallSequenceWithResponseMethod) == null) {
          InvokeSequenceGrpc.getCallSequenceWithResponseMethod = getCallSequenceWithResponseMethod =
              io.grpc.MethodDescriptor.<SequenceCallRequest, SequenceCallResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(
                  "invokesequence.InvokeSequence", "CallSequenceWithResponse"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  SequenceCallRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  SequenceCallResponse.getDefaultInstance()))
                  .setSchemaDescriptor(new InvokeSequenceMethodDescriptorSupplier("CallSequenceWithResponse"))
                  .build();
          }
        }
     }
     return getCallSequenceWithResponseMethod;
  }

  private static volatile io.grpc.MethodDescriptor<SequenceCallRequest,
      EmptyResponse> getCallSequenceWithoutResponseMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "CallSequenceWithoutResponse",
      requestType = SequenceCallRequest.class,
      responseType = EmptyResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<SequenceCallRequest,
      EmptyResponse> getCallSequenceWithoutResponseMethod() {
    io.grpc.MethodDescriptor<SequenceCallRequest, EmptyResponse> getCallSequenceWithoutResponseMethod;
    if ((getCallSequenceWithoutResponseMethod = InvokeSequenceGrpc.getCallSequenceWithoutResponseMethod) == null) {
      synchronized (InvokeSequenceGrpc.class) {
        if ((getCallSequenceWithoutResponseMethod = InvokeSequenceGrpc.getCallSequenceWithoutResponseMethod) == null) {
          InvokeSequenceGrpc.getCallSequenceWithoutResponseMethod = getCallSequenceWithoutResponseMethod =
              io.grpc.MethodDescriptor.<SequenceCallRequest, EmptyResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(
                  "invokesequence.InvokeSequence", "CallSequenceWithoutResponse"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  SequenceCallRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  EmptyResponse.getDefaultInstance()))
                  .setSchemaDescriptor(new InvokeSequenceMethodDescriptorSupplier("CallSequenceWithoutResponse"))
                  .build();
          }
        }
     }
     return getCallSequenceWithoutResponseMethod;
  }

  /**
   * Creates a new async stub that supports all call types for the service
   */
  public static InvokeSequenceStub newStub(io.grpc.Channel channel) {
    return new InvokeSequenceStub(channel);
  }

  /**
   * Creates a new blocking-style stub that supports unary and streaming output calls on the service
   */
  public static InvokeSequenceBlockingStub newBlockingStub(
      io.grpc.Channel channel) {
    return new InvokeSequenceBlockingStub(channel);
  }

  /**
   * Creates a new ListenableFuture-style stub that supports unary calls on the service
   */
  public static InvokeSequenceFutureStub newFutureStub(
      io.grpc.Channel channel) {
    return new InvokeSequenceFutureStub(channel);
  }

  /**
   */
  public static abstract class InvokeSequenceImplBase implements io.grpc.BindableService {

    /**
     */
    public void callSequenceWithResponse(SequenceCallRequest request,
                                         io.grpc.stub.StreamObserver<SequenceCallResponse> responseObserver) {
      asyncUnimplementedUnaryCall(getCallSequenceWithResponseMethod(), responseObserver);
    }

    /**
     */
    public void callSequenceWithoutResponse(SequenceCallRequest request,
                                            io.grpc.stub.StreamObserver<EmptyResponse> responseObserver) {
      asyncUnimplementedUnaryCall(getCallSequenceWithoutResponseMethod(), responseObserver);
    }

    @Override public final io.grpc.ServerServiceDefinition bindService() {
      return io.grpc.ServerServiceDefinition.builder(getServiceDescriptor())
          .addMethod(
            getCallSequenceWithResponseMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                SequenceCallRequest,
                SequenceCallResponse>(
                  this, METHODID_CALL_SEQUENCE_WITH_RESPONSE)))
          .addMethod(
            getCallSequenceWithoutResponseMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                SequenceCallRequest,
                EmptyResponse>(
                  this, METHODID_CALL_SEQUENCE_WITHOUT_RESPONSE)))
          .build();
    }
  }

  /**
   */
  public static final class InvokeSequenceStub extends io.grpc.stub.AbstractStub<InvokeSequenceStub> {
    private InvokeSequenceStub(io.grpc.Channel channel) {
      super(channel);
    }

    private InvokeSequenceStub(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @Override
    protected InvokeSequenceStub build(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      return new InvokeSequenceStub(channel, callOptions);
    }

    /**
     */
    public void callSequenceWithResponse(SequenceCallRequest request,
                                         io.grpc.stub.StreamObserver<SequenceCallResponse> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getCallSequenceWithResponseMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void callSequenceWithoutResponse(SequenceCallRequest request,
                                            io.grpc.stub.StreamObserver<EmptyResponse> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getCallSequenceWithoutResponseMethod(), getCallOptions()), request, responseObserver);
    }
  }

  /**
   */
  public static final class InvokeSequenceBlockingStub extends io.grpc.stub.AbstractStub<InvokeSequenceBlockingStub> {
    private InvokeSequenceBlockingStub(io.grpc.Channel channel) {
      super(channel);
    }

    private InvokeSequenceBlockingStub(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @Override
    protected InvokeSequenceBlockingStub build(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      return new InvokeSequenceBlockingStub(channel, callOptions);
    }

    /**
     */
    public SequenceCallResponse callSequenceWithResponse(SequenceCallRequest request) {
      return blockingUnaryCall(
          getChannel(), getCallSequenceWithResponseMethod(), getCallOptions(), request);
    }

    /**
     */
    public EmptyResponse callSequenceWithoutResponse(SequenceCallRequest request) {
      return blockingUnaryCall(
          getChannel(), getCallSequenceWithoutResponseMethod(), getCallOptions(), request);
    }
  }

  /**
   */
  public static final class InvokeSequenceFutureStub extends io.grpc.stub.AbstractStub<InvokeSequenceFutureStub> {
    private InvokeSequenceFutureStub(io.grpc.Channel channel) {
      super(channel);
    }

    private InvokeSequenceFutureStub(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @Override
    protected InvokeSequenceFutureStub build(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      return new InvokeSequenceFutureStub(channel, callOptions);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<SequenceCallResponse> callSequenceWithResponse(
        SequenceCallRequest request) {
      return futureUnaryCall(
          getChannel().newCall(getCallSequenceWithResponseMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<EmptyResponse> callSequenceWithoutResponse(
        SequenceCallRequest request) {
      return futureUnaryCall(
          getChannel().newCall(getCallSequenceWithoutResponseMethod(), getCallOptions()), request);
    }
  }

  private static final int METHODID_CALL_SEQUENCE_WITH_RESPONSE = 0;
  private static final int METHODID_CALL_SEQUENCE_WITHOUT_RESPONSE = 1;

  private static final class MethodHandlers<Req, Resp> implements
      io.grpc.stub.ServerCalls.UnaryMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ServerStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ClientStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.BidiStreamingMethod<Req, Resp> {
    private final InvokeSequenceImplBase serviceImpl;
    private final int methodId;

    MethodHandlers(InvokeSequenceImplBase serviceImpl, int methodId) {
      this.serviceImpl = serviceImpl;
      this.methodId = methodId;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void invoke(Req request, io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        case METHODID_CALL_SEQUENCE_WITH_RESPONSE:
          serviceImpl.callSequenceWithResponse((SequenceCallRequest) request,
              (io.grpc.stub.StreamObserver<SequenceCallResponse>) responseObserver);
          break;
        case METHODID_CALL_SEQUENCE_WITHOUT_RESPONSE:
          serviceImpl.callSequenceWithoutResponse((SequenceCallRequest) request,
              (io.grpc.stub.StreamObserver<EmptyResponse>) responseObserver);
          break;
        default:
          throw new AssertionError();
      }
    }

    @Override
    @SuppressWarnings("unchecked")
    public io.grpc.stub.StreamObserver<Req> invoke(
        io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        default:
          throw new AssertionError();
      }
    }
  }

  private static abstract class InvokeSequenceBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoFileDescriptorSupplier, io.grpc.protobuf.ProtoServiceDescriptorSupplier {
    InvokeSequenceBaseDescriptorSupplier() {}

    @Override
    public com.google.protobuf.Descriptors.FileDescriptor getFileDescriptor() {
      return SiddhiMicroIntegratorProto.getDescriptor();
    }

    @Override
    public com.google.protobuf.Descriptors.ServiceDescriptor getServiceDescriptor() {
      return getFileDescriptor().findServiceByName("InvokeSequence");
    }
  }

  private static final class InvokeSequenceFileDescriptorSupplier
      extends InvokeSequenceBaseDescriptorSupplier {
    InvokeSequenceFileDescriptorSupplier() {}
  }

  private static final class InvokeSequenceMethodDescriptorSupplier
      extends InvokeSequenceBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoMethodDescriptorSupplier {
    private final String methodName;

    InvokeSequenceMethodDescriptorSupplier(String methodName) {
      this.methodName = methodName;
    }

    @Override
    public com.google.protobuf.Descriptors.MethodDescriptor getMethodDescriptor() {
      return getServiceDescriptor().findMethodByName(methodName);
    }
  }

  private static volatile io.grpc.ServiceDescriptor serviceDescriptor;

  public static io.grpc.ServiceDescriptor getServiceDescriptor() {
    io.grpc.ServiceDescriptor result = serviceDescriptor;
    if (result == null) {
      synchronized (InvokeSequenceGrpc.class) {
        result = serviceDescriptor;
        if (result == null) {
          serviceDescriptor = result = io.grpc.ServiceDescriptor.newBuilder(SERVICE_NAME)
              .setSchemaDescriptor(new InvokeSequenceFileDescriptorSupplier())
              .addMethod(getCallSequenceWithResponseMethod())
              .addMethod(getCallSequenceWithoutResponseMethod())
              .build();
        }
      }
    }
    return result;
  }
}
