/*
 * Copyright (c)  2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package io.siddhi.extension.io.grpc.util;

import com.google.protobuf.Any;
import com.google.protobuf.Descriptors;
import com.google.protobuf.Empty;

import static io.grpc.MethodDescriptor.generateFullMethodName;
import static io.grpc.stub.ServerCalls.asyncUnaryCall;
import static io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall;

/**
 * {@code GenericServiceClass} Work as a generic service to handle rpc calls that send non-empty responses and empty
 * responses.
 */
public class GenericServiceClass {
    private static String serviceName = "";
    private static String nonEmptyResponseMethodName = "";
    private static String emptyResponseMethodName = "";
    // if we have 2 empty responses
    private static volatile io.grpc.ServiceDescriptor serviceDescriptor;
    private static volatile io.grpc.MethodDescriptor<Any, Empty> handleEmptyResponse;
    private static volatile io.grpc.MethodDescriptor<Any, Any> getHandleNonEmptyResponse;

    public static void setServiceName(String serviceName) {
        GenericServiceClass.serviceName = serviceName;
    }

    public static void setNonEmptyResponseMethodName(String nonEmptyResponseMethodName) {
        GenericServiceClass.nonEmptyResponseMethodName = nonEmptyResponseMethodName;
    }

    public static void setEmptyResponseMethodName(String emptyResponseMethodName) {
        GenericServiceClass.emptyResponseMethodName = emptyResponseMethodName;
    }

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
                                            serviceName, emptyResponseMethodName)) //todo ====
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

    //todo remove all anotations
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
            return getServiceDescriptor().findMethodByName(methodName); //return  null
        }
    }


    //=================================== ===========================

    private static final class MethodHandlers<Req, Resp> implements io.grpc.stub.ServerCalls.UnaryMethod<Req, Resp>,
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
            switch (methodId) {

                case 1:
                    serviceImpl.handleEmptyResponse((Any) request,
                            (io.grpc.stub.StreamObserver<Empty>) responseObserver);
                    break;
                case 2:
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
                                            Any, Empty>(this, 1)))
                    .addMethod(
                            getHandleNonEmptyResponse(),
                            asyncUnaryCall(
                                    new GenericServiceClass.MethodHandlers<
                                            Any, Any>(this, 2)))
                    .build();
        }
    }
}
