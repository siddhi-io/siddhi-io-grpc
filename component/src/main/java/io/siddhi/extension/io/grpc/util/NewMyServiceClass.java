package io.siddhi.extension.io.grpc.util;

import com.google.protobuf.Any;
import com.google.protobuf.Descriptors;
import com.google.protobuf.Empty;

import static io.grpc.MethodDescriptor.generateFullMethodName;
import static io.grpc.stub.ServerCalls.asyncUnaryCall;
import static io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall;


public class NewMyServiceClass {
    public static final String SERVICE_NAME = "MyService";
    public  static String nonEmptyResponseMethodName ="process";
    public static String emptyResponseMethodName = "send";


    private static abstract class AnyServiceBaseDescriptorSupplier
            implements io.grpc.protobuf.ProtoFileDescriptorSupplier, io.grpc.protobuf.ProtoServiceDescriptorSupplier {
        AnyServiceBaseDescriptorSupplier() {
        }

        @Override
        public Descriptors.FileDescriptor getFileDescriptor() {
            return null;
//            return package01.test.Sample.getDescriptor();
        }

        @Override
        public Descriptors.ServiceDescriptor getServiceDescriptor() {
            return getFileDescriptor().findServiceByName(SERVICE_NAME);//todo get service name from url
        }
    }

    private static final class AnyServiceFileDescriptorSupplier
            extends AnyServiceBaseDescriptorSupplier {
        AnyServiceFileDescriptorSupplier() {
        }
    }


    private static final class AnyServiceMethodDescriptorSupplier
            extends AnyServiceBaseDescriptorSupplier
            implements io.grpc.protobuf.ProtoMethodDescriptorSupplier {
        private final String methodName;

        AnyServiceMethodDescriptorSupplier(String methodName) {
            this.methodName = methodName;
        }

        @Override
        public Descriptors.MethodDescriptor getMethodDescriptor() {
            return null; //todo not relevent
        }
    }


    private static volatile io.grpc.ServiceDescriptor serviceDescriptor;

    public static io.grpc.ServiceDescriptor getServiceDescriptor() {
        io.grpc.ServiceDescriptor result = serviceDescriptor;
        if (result == null) {
            synchronized (NewMyServiceClass.class) {
                result = serviceDescriptor;

                if (result == null) {
                    serviceDescriptor = result = io.grpc.ServiceDescriptor.newBuilder(SERVICE_NAME)
                            .setSchemaDescriptor(new AnyServiceFileDescriptorSupplier())
                            .addMethod(getHandleEmptyResponse())
                            .addMethod(getHandleNonEmptyResponse())
                            .build();
                }
            }
        }
        return result;
    }


    private static volatile io.grpc.MethodDescriptor<Any,
            Empty> handleEmptyResponse;

    @io.grpc.stub.annotations.RpcMethod(
            fullMethodName = SERVICE_NAME + '/' + "emptyResponse",//not relevant the methodName
            requestType = Any.class,
            responseType = Empty.class,
            methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
    public static io.grpc.MethodDescriptor<Any,
            Empty> getHandleEmptyResponse() {
        io.grpc.MethodDescriptor<Any, Empty> getNoResponseMethod;
        if ((getNoResponseMethod = NewMyServiceClass.handleEmptyResponse) == null) {
            synchronized (NewMyServiceClass.class) {
                if ((getNoResponseMethod = NewMyServiceClass.handleEmptyResponse) == null) {
                    NewMyServiceClass.handleEmptyResponse = getNoResponseMethod =
                            io.grpc.MethodDescriptor.<Any, Empty>newBuilder()
                                    .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
                                    .setFullMethodName(generateFullMethodName(
                                            SERVICE_NAME, emptyResponseMethodName))//todo ====
                                    .setSampledToLocalTracing(true)
                                    .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(Any.getDefaultInstance()))
                                    .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                                            Empty.getDefaultInstance()))
                                    .setSchemaDescriptor(new AnyServiceMethodDescriptorSupplier(emptyResponseMethodName))
                                    .build();
                }
            }
        }
        return getNoResponseMethod;
    }


    private static volatile io.grpc.MethodDescriptor<Any,
            Any> getHandleNonEmptyResponse;

    @io.grpc.stub.annotations.RpcMethod(
            fullMethodName = SERVICE_NAME + '/' + "methodName",//not relevant the methodName
            requestType = Any.class,
            responseType = Empty.class,
            methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
    public static io.grpc.MethodDescriptor<Any,
            Any> getHandleNonEmptyResponse() {
        io.grpc.MethodDescriptor<Any, Any> getDetail;
        if ((getDetail = NewMyServiceClass.getHandleNonEmptyResponse) == null) {
            synchronized (NewMyServiceClass.class) {
                if ((getDetail = NewMyServiceClass.getHandleNonEmptyResponse) == null) {
                    NewMyServiceClass.getHandleNonEmptyResponse = getDetail =
                            io.grpc.MethodDescriptor.<Any, Any>newBuilder()
                                    .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
                                    .setFullMethodName(generateFullMethodName(
                                            SERVICE_NAME, nonEmptyResponseMethodName))//todo ====
                                    .setSampledToLocalTracing(true)
                                    .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(Any.getDefaultInstance()))
                                    .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                                            Any.getDefaultInstance()))
                                    .setSchemaDescriptor(new AnyServiceMethodDescriptorSupplier(nonEmptyResponseMethodName))
                                    .build();
                }
            }
        }
        return getDetail;
    }


    //=================================== ===========================


    private static final class MethodHandlers<Req, Resp> implements
            io.grpc.stub.ServerCalls.UnaryMethod<Req, Resp>,
            io.grpc.stub.ServerCalls.ServerStreamingMethod<Req, Resp>,
            io.grpc.stub.ServerCalls.ClientStreamingMethod<Req, Resp>,
            io.grpc.stub.ServerCalls.BidiStreamingMethod<Req, Resp> {
        private final NewMyServiceClass.AnyServiceImplBase serviceImpl;
        private final int methodId;

        MethodHandlers(NewMyServiceClass.AnyServiceImplBase serviceImpl, int methodId) {
            this.serviceImpl = serviceImpl;
            this.methodId = methodId;
        }

        @Override
        @SuppressWarnings("unchecked")
        public void invoke(Req request, io.grpc.stub.StreamObserver<Resp> responseObserver) {
            System.out.println("Here in invoke :"+request.getClass());
            switch (methodId) {

                case 2:
                    serviceImpl.handleEmptyResponse((Any)request,
                            (io.grpc.stub.StreamObserver<Empty>) responseObserver);
                    break;
                    case 3:
                    serviceImpl.handleNonEmptyResponse((Any)request,
                            (io.grpc.stub.StreamObserver<Any>) responseObserver);
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


    public static abstract class AnyServiceImplBase implements io.grpc.BindableService{

        public void handleEmptyResponse(Any request,
                                io.grpc.stub.StreamObserver<Empty> responseObserver) {
            asyncUnimplementedUnaryCall(getHandleEmptyResponse(), responseObserver);
        }

        public void handleNonEmptyResponse(Any request,
                                io.grpc.stub.StreamObserver<Any> responseObserver) {
            asyncUnimplementedUnaryCall(getHandleNonEmptyResponse(), responseObserver);
        }

        @Override
        public final io.grpc.ServerServiceDefinition bindService() {

            return io.grpc.ServerServiceDefinition.builder(getServiceDescriptor())
                    .addMethod(
                            getHandleEmptyResponse(),
                            asyncUnaryCall(
                                    new NewMyServiceClass.MethodHandlers<
                                            Any, Empty>(this, 2)))
                    .addMethod(
                            getHandleNonEmptyResponse(),
                            asyncUnaryCall(
                                    new NewMyServiceClass.MethodHandlers<
                                            Any, Any>(this, 3)))
                    .build();
        }
    }


}
