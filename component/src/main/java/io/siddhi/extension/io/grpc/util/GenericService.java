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
import io.grpc.MethodDescriptor;
import io.grpc.ServerServiceDefinition;
import io.grpc.ServiceDescriptor;

import static io.grpc.MethodDescriptor.generateFullMethodName;
import static io.grpc.stub.ServerCalls.asyncClientStreamingCall;
import static io.grpc.stub.ServerCalls.asyncUnaryCall;
import static io.grpc.stub.ServerCalls.asyncUnimplementedStreamingCall;
import static io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall;

/**
 * {@code GenericServiceClass} Work as a generic service to handle rpc calls that send non-empty responses and empty
 * responses.
 */
public class GenericService {
    private static volatile String serviceName = "";
    private static volatile String nonEmptyResponseMethodName = "nonEmptyResponse";
    private static volatile String emptyResponseMethodName = "emptyResponse";
    private static volatile String clientStreamMethodName = "clientStreaming";
    private static volatile MethodDescriptor<Any, Empty> emptyResponseHandle;
    private static volatile MethodDescriptor<Any, Any> nonEmptyResponseHandle;
    private static volatile MethodDescriptor<Any, Empty> getClientStreamMethod;

    public static void setServiceName(String serviceName) {
        GenericService.serviceName = serviceName;
    }

    public static void setNonEmptyResponseMethodName(String nonEmptyResponseMethodName) {
        GenericService.nonEmptyResponseMethodName = nonEmptyResponseMethodName;
    }

    public static void setEmptyResponseMethodName(String emptyResponseMethodName) {
        GenericService.emptyResponseMethodName = emptyResponseMethodName;
    }

    public static void setClientStreamMethodName(String clientStreamMethodName) {
        GenericService.clientStreamMethodName = clientStreamMethodName;
    }

    public static ServiceDescriptor getServiceDescriptor() { //service descriptor have to be refresh when each time
        // server object is created,otherwise method descriptors won't be changed even if they changed later. because
        // of that server can't be implemented for multiple methods
        ServiceDescriptor result;
        synchronized (GenericService.class) {
            result = io.grpc.ServiceDescriptor.newBuilder(serviceName)
                    .setSchemaDescriptor(new AnyServiceFileDescriptorSupplier())
                    .addMethod(getEmptyResponseHandle())
                    .addMethod(getHandleNonEmptyResponse())
                    .addMethod(getClientStreamMethod())
                    .build();

        }
        return result;
    }

    public static io.grpc.MethodDescriptor<Any,
            Empty> getEmptyResponseHandle() {
        io.grpc.MethodDescriptor<Any, Empty> nonResponseMethod = null;
        if ((nonResponseMethod = GenericService.emptyResponseHandle) == null ||
                !(nonResponseMethod.getFullMethodName().equals(serviceName + "/" + emptyResponseMethodName))) {
            synchronized (GenericService.class) {
                emptyResponseHandle = null;
                if ((nonResponseMethod = GenericService.emptyResponseHandle) == null ||
                        !(nonResponseMethod.getFullMethodName().equals(serviceName + "/" + emptyResponseMethodName))) {
                    GenericService.emptyResponseHandle = nonResponseMethod =
                            io.grpc.MethodDescriptor.<Any, Empty>newBuilder()
                                    .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
                                    .setFullMethodName(generateFullMethodName(
                                            serviceName, emptyResponseMethodName))
                                    .setSampledToLocalTracing(true)
                                    .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.
                                            marshaller(Any.getDefaultInstance()))
                                    .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                                            Empty.getDefaultInstance()))
                                    .setSchemaDescriptor(
                                            new AnyServiceMethodDescriptorSupplier(emptyResponseMethodName))
                                    .build();
                }
            }
        }
        return nonResponseMethod;
    }

    public static io.grpc.MethodDescriptor<Any,
            Any> getHandleNonEmptyResponse() {
        io.grpc.MethodDescriptor<Any, Any> responseMethod;
        if ((responseMethod = GenericService.nonEmptyResponseHandle) == null ||
                !(responseMethod.getFullMethodName().equals(serviceName + "/" + nonEmptyResponseMethodName))) {
            synchronized (GenericService.class) {
                nonEmptyResponseHandle = null;
                if ((responseMethod = GenericService.nonEmptyResponseHandle) == null ||
                        !(responseMethod.getFullMethodName().equals(serviceName + "/" + nonEmptyResponseMethodName))) {
                    GenericService.nonEmptyResponseHandle = responseMethod =
                            io.grpc.MethodDescriptor.<Any, Any>newBuilder()
                                    .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
                                    .setFullMethodName(generateFullMethodName(
                                            serviceName, nonEmptyResponseMethodName))
                                    .setSampledToLocalTracing(true)
                                    .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.
                                            marshaller(Any.getDefaultInstance()))
                                    .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                                            Any.getDefaultInstance()))
                                    .setSchemaDescriptor(
                                            new AnyServiceMethodDescriptorSupplier(nonEmptyResponseMethodName))
                                    .build();
                }
            }
        }
        return responseMethod;
    }

    public static io.grpc.MethodDescriptor<Any, Empty> getClientStreamMethod() {
        io.grpc.MethodDescriptor<Any, Empty> getClientStreamMethod;
        if ((getClientStreamMethod = GenericService.getClientStreamMethod) == null ||
                !(getClientStreamMethod.getFullMethodName().equals(serviceName + "/" + clientStreamMethodName))) {
            synchronized (GenericService.class) {
                GenericService.getClientStreamMethod = null;
                if ((getClientStreamMethod = GenericService.getClientStreamMethod) == null) {
                    GenericService.getClientStreamMethod = getClientStreamMethod =
                            io.grpc.MethodDescriptor.<Any, Empty>newBuilder()
                                    .setType(io.grpc.MethodDescriptor.MethodType.CLIENT_STREAMING)
                                    .setFullMethodName(generateFullMethodName(
                                            serviceName, clientStreamMethodName))
                                    .setSampledToLocalTracing(true)
                                    .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                                            Any.getDefaultInstance()))
                                    .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                                            Empty.getDefaultInstance()))
                                    .setSchemaDescriptor(new AnyServiceMethodDescriptorSupplier(clientStreamMethodName))
                                    .build();
                }
            }
        }
        return getClientStreamMethod;
    }

    private abstract static class AnyServiceBaseDescriptorSupplier
            implements io.grpc.protobuf.ProtoFileDescriptorSupplier, io.grpc.protobuf.ProtoServiceDescriptorSupplier {
        AnyServiceBaseDescriptorSupplier() {
        }

        @Override
        public Descriptors.FileDescriptor getFileDescriptor() {
            return null;
        }

        @Override
        public Descriptors.ServiceDescriptor getServiceDescriptor() {
            return getFileDescriptor().findServiceByName(serviceName);
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
            return getServiceDescriptor().findMethodByName(methodName);
        }
    }

    private static final class MethodHandlers<Req, Resp> implements io.grpc.stub.ServerCalls.UnaryMethod<Req, Resp>,
            io.grpc.stub.ServerCalls.ServerStreamingMethod<Req, Resp>,
            io.grpc.stub.ServerCalls.ClientStreamingMethod<Req, Resp>,
            io.grpc.stub.ServerCalls.BidiStreamingMethod<Req, Resp> {
        private final GenericService.AnyServiceImplBase serviceImpl;
        private final int methodId;

        MethodHandlers(GenericService.AnyServiceImplBase serviceImpl, int methodId) {
            this.serviceImpl = serviceImpl;
            this.methodId = methodId;
        }

        @Override
        @SuppressWarnings("unchecked")
        public void invoke(Req request, io.grpc.stub.StreamObserver<Resp> responseObserver) {
            switch (methodId) {

                case GrpcConstants.EMPTY_METHOD_ID:
                    serviceImpl.handleEmptyResponse((Any) request,
                            (io.grpc.stub.StreamObserver<Empty>) responseObserver);
                    break;
                case GrpcConstants.NON_EMPTY_METHOD_ID:
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
                case GrpcConstants.CLIENT_STREAM_METHOD_ID:
                    return (io.grpc.stub.StreamObserver<Req>) serviceImpl.clientStream(
                            (io.grpc.stub.StreamObserver<Empty>) responseObserver);
                default:
                    throw new AssertionError();
            }
        }
    }

    /**
     * Generic ImplBase class of the GenericServiceClass
     */
    public abstract static class AnyServiceImplBase implements io.grpc.BindableService {
        public void handleEmptyResponse(Any request, io.grpc.stub.StreamObserver<Empty> responseObserver) {
            asyncUnimplementedUnaryCall(getEmptyResponseHandle(), responseObserver);
        }

        public void handleNonEmptyResponse(Any request,
                                           io.grpc.stub.StreamObserver<Any> responseObserver) {
            asyncUnimplementedUnaryCall(getHandleNonEmptyResponse(), responseObserver);
        }

        public io.grpc.stub.StreamObserver<Any> clientStream(
                io.grpc.stub.StreamObserver<Empty> responseObserver) {
            return asyncUnimplementedStreamingCall(getClientStreamMethod(), responseObserver);
        }

        @Override
        public final ServerServiceDefinition bindService() {

            return ServerServiceDefinition.builder(getServiceDescriptor())
                    .addMethod(
                            getEmptyResponseHandle(),
                            asyncUnaryCall(
                                    new MethodHandlers<
                                            Any, Empty>(this, GrpcConstants.EMPTY_METHOD_ID)))
                    // integers
                    .addMethod(
                            getHandleNonEmptyResponse(),
                            asyncUnaryCall(
                                    new MethodHandlers<
                                            Any, Any>(this, GrpcConstants.NON_EMPTY_METHOD_ID)))
                    .addMethod(getClientStreamMethod(),
                            asyncClientStreamingCall(
                                    new GenericService.MethodHandlers<
                                            Any,
                                            Empty>(
                                            this, GrpcConstants.CLIENT_STREAM_METHOD_ID)))
                    .build();
        }
    }
}

