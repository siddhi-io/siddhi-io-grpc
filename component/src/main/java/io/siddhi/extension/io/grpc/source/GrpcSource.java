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
package io.siddhi.extension.io.grpc.source;

import com.google.protobuf.Any;
import com.google.protobuf.ByteString;
import com.google.protobuf.Empty;
import io.grpc.ServerBuilder;
import io.grpc.ServerInterceptors;
import io.grpc.netty.shaded.io.grpc.netty.NettyServerBuilder;
import io.grpc.stub.StreamObserver;
import io.siddhi.annotation.Example;
import io.siddhi.annotation.Extension;
import io.siddhi.annotation.Parameter;
import io.siddhi.annotation.util.DataType;
import io.siddhi.core.exception.SiddhiAppRuntimeException;
import io.siddhi.core.util.transport.OptionHolder;
import io.siddhi.extension.io.grpc.util.GenericServiceClass;
import org.apache.log4j.Logger;
import org.omg.CORBA.Request;
import org.wso2.grpc.Event;
import org.wso2.grpc.EventServiceGrpc;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * This handles receiving requests from grpc clients and populating the stream
 */
@Extension(
        name = "grpc",
        namespace = "source",
        description = "This extension starts a grpc server during initialization time. The server listens to " +
                "requests from grpc stubs. This source has a default mode of operation and custom user defined grpc " +
                "service mode. In the default mode this source will use EventService consume method. This method " +
                "will receive requests and injects them into stream through a mapper.",
        parameters = {
                @Parameter(name = "url",
                        description = "The url which can be used by a client to access the grpc server in this " +
                                "extension. This url should consist the host address, port, service name, method " +
                                "name in the following format. grpc://hostAddress:port/serviceName/methodName" ,
                        type = {DataType.STRING}),
        },
        examples = {
                @Example(
                        syntax = "@source(type='grpc', " +
                                "url='grpc://locanhost:8888/org.wso2.grpc.EventService/consume', @map(type='json')) " +
                                "define stream BarStream (message String);",
                        description = "Here the port is given as 8888. So a grpc server will be started on port 8888 " +
                                "and the server will expose EventService. This is the default service packed with " +
                                "the source. In EventService the consume method is"
                )
        }
)
public class GrpcSource extends AbstractGrpcSource {
    private static final Logger logger = Logger.getLogger(GrpcSource.class.getName());
    private String headerString;

    @Override
    public void initializeGrpcServer(int port) {
        if (isDefaultMode) {
            this.server = serverBuilder.addService(ServerInterceptors.intercept(new EventServiceGrpc.EventServiceImplBase() {
                @Override
                public void consume(Event request,
                                    StreamObserver<Empty> responseObserver) {
                    if (headerString != null) {
                        try {
                            sourceEventListener.onEvent(request.getPayload(), extractHeaders(headerString));
                        } catch (SiddhiAppRuntimeException e) {
                            logger.error(siddhiAppContext.getName() + ": Dropping request. " + e.getMessage());
                        }

                    } else {
                        sourceEventListener.onEvent(request.getPayload(), null);
                    }
                    responseObserver.onNext(Empty.getDefaultInstance());
                    responseObserver.onCompleted();
                }
            }, serverInterceptor)).build();
        } else {
            //todo: generic server logic here
            GenericServiceClass.setServiceName(this.serviceName);

            synchronized (this) { //in case of 2 server creates at the same time
                GenericServiceClass.setEmptyResponseMethodName(this.methodName); //doesn't affect if 'methodname' changed after creating the server
                GenericServiceClass.AnyServiceImplBase service = new GenericServiceClass.AnyServiceImplBase() {
                    @Override
                    public void handleEmptyResponse(Any request, StreamObserver<Empty> responseObserver) {
                        Object requestClass = null;
                        try {
                            Class className = Class.forName("package01.test.Request");//todo get the class name from url


                            Method parseFrom = className.getDeclaredMethod("parseFrom", ByteString.class);
                            requestClass = parseFrom.invoke(Request.class,request.toByteString());
                        } catch (ClassNotFoundException e) {
                            e.printStackTrace();
                        } catch (NoSuchMethodException e) {
                            e.printStackTrace();
                        } catch (IllegalAccessException e) {
                            e.printStackTrace();
                        } catch (InvocationTargetException e) {
                            e.printStackTrace();
                        }

                        sourceEventListener.onEvent(requestClass,new String[]{"1"});
                    }
                };
                this.server = ServerBuilder.forPort(port).addService(service).build();
            }
        }
    }

    @Override
    public void initSource(OptionHolder optionHolder) {}

    @Override
    public void populateHeaderString(String headerString) {
        this.headerString = headerString;
    }
}
