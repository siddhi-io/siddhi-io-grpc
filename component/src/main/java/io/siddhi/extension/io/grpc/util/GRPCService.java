package io.siddhi.extension.io.grpc.util;

import io.grpc.MethodDescriptor;
import io.grpc.MethodDescriptor.MethodType;
import io.grpc.MethodDescriptor.Marshaller;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class GRPCService {

    private static final String SERVICE_NAME = "io.grpc.MIService";

    public static class Request {
        byte[] value;
        public Request(byte[] value) {
            this.value = value;
        }

        public byte[] getValue() {
            return value;
        }
    }

    public static class EmptyResponse{
        public byte[] getResponse() {
            return response;
        }

        byte[] response = "Any String you want".getBytes();
//        byte[] response;
    }

//    static final MethodDescriptor<Request, EmptyResponse> CREATE_METHOD =
//            MethodDescriptor.newBuilder(
//                    marshallerFor(Request.class),
//                    marshallerFor(EmptyResponse.class))
//                    .setFullMethodName(
//                            MethodDescriptor.generateFullMethodName(SERVICE_NAME, "Create"))
//                    .setType(MethodType.UNARY)
//                    .setSampledToLocalTracing(true)
//                    .build();
//
//    static <T> Marshaller<T> marshallerFor(Class<T> clz) {
//        return new Marshaller<T>() {
//            @Override
//            public InputStream stream(T value) {
//                return new ByteArrayInputStream((byte[]) value);
//            }
//
//            @Override
//            public T parse(InputStream stream) {
//                return null; //gson.fromJson(new InputStreamReader(stream, StandardCharsets.UTF_8), clz);
//                //todo: find way to get byte[] from inputstream
//            }
//        };
//    }
}
