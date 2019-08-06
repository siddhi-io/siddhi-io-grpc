package io.siddhi.extension.io.grpc.util;

import com.google.protobuf.Any;
import com.google.protobuf.Descriptors;
import com.google.protobuf.Empty;

import static io.grpc.MethodDescriptor.generateFullMethodName;
import static io.grpc.stub.ServerCalls.asyncUnaryCall;
import static io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall;


public class GenericServiceClass {
    private static  String serviceName = "";
    private static String nonEmptyResponseMethodName = "";
    private static String emptyResponseMethodName = ""; //todo will there be a problem if we have 2 different sources if we have 2 empty responses

    public static void setServiceName(String serviceName) {
        GenericServiceClass.serviceName = serviceName;
    }

    public static void setNonEmptyResponseMethodName(String nonEmptyResponseMethodName) {
//        synchronized(GenericServiceClass.class) //in case of 2 server get create at the same time
//        {
            GenericServiceClass.nonEmptyResponseMethodName = nonEmptyResponseMethodName;
//        }
    }

    public static void setEmptyResponseMethodName(String emptyResponseMethodName) {
        GenericServiceClass.emptyResponseMethodName = emptyResponseMethodName;
    }


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
            return getFileDescriptor().findServiceByName(serviceName);//todo get service name from url
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
            synchronized (GenericServiceClass.class) {
                result = serviceDescriptor;

                if (result == null) {
                    serviceDescriptor = result = io.grpc.ServiceDescriptor.newBuilder(serviceName)
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
            fullMethodName = "serviceName" + '/' + "emptyResponse",//not relevant the methodName
            requestType = Any.class,
            responseType = Empty.class,
            methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
    public static io.grpc.MethodDescriptor<Any,
            Empty> getHandleEmptyResponse() {
        io.grpc.MethodDescriptor<Any, Empty> getNoResponseMethod;
        if ((getNoResponseMethod = GenericServiceClass.handleEmptyResponse) == null) {
            synchronized (GenericServiceClass.class) {
                if ((getNoResponseMethod = GenericServiceClass.handleEmptyResponse) == null) {
                    GenericServiceClass.handleEmptyResponse = getNoResponseMethod =
                            io.grpc.MethodDescriptor.<Any, Empty>newBuilder()
                                    .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
                                    .setFullMethodName(generateFullMethodName(
                                            serviceName, emptyResponseMethodName))//todo ====
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
            fullMethodName = "serviceName" + '/' + "methodName",//not relevant the methodName
            requestType = Any.class,
            responseType = Empty.class,
            methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
    public static io.grpc.MethodDescriptor<Any,
            Any> getHandleNonEmptyResponse() {
        io.grpc.MethodDescriptor<Any, Any> getDetail;
        if ((getDetail = GenericServiceClass.getHandleNonEmptyResponse) == null) {
            synchronized (GenericServiceClass.class) {
                if ((getDetail = GenericServiceClass.getHandleNonEmptyResponse) == null) {
                    GenericServiceClass.getHandleNonEmptyResponse = getDetail =
                            io.grpc.MethodDescriptor.<Any, Any>newBuilder()
                                    .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
                                    .setFullMethodName(generateFullMethodName(
                                            serviceName, nonEmptyResponseMethodName))//todo ====
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
        private final GenericServiceClass.AnyServiceImplBase serviceImpl;
        private final int methodId;

        MethodHandlers(GenericServiceClass.AnyServiceImplBase serviceImpl, int methodId) {
            this.serviceImpl = serviceImpl;
            this.methodId = methodId;
        }

        @Override
        @SuppressWarnings("unchecked")
        public void invoke(Req request, io.grpc.stub.StreamObserver<Resp> responseObserver) {
            System.out.println("Here in invoke :" + request.getClass());
            switch (methodId) {

                case 2:
                    serviceImpl.handleEmptyResponse((Any) request,
                            (io.grpc.stub.StreamObserver<Empty>) responseObserver);
                    break;
                case 3:
                    serviceImpl.handleNonEmptyResponse((Any) request,
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


    public static abstract class AnyServiceImplBase implements io.grpc.BindableService {

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
                                    new GenericServiceClass.MethodHandlers<
                                            Any, Empty>(this, 2)))
                    .addMethod(
                            getHandleNonEmptyResponse(),
                            asyncUnaryCall(
                                    new GenericServiceClass.MethodHandlers<
                                            Any, Any>(this, 3)))
                    .build();
        }
    }


}
