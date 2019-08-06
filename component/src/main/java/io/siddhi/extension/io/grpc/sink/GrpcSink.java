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
package io.siddhi.extension.io.grpc.sink;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.protobuf.Empty;
import io.grpc.Channel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Metadata;
import io.grpc.stub.MetadataUtils;
import io.grpc.stub.StreamObserver;
import io.siddhi.annotation.Example;
import io.siddhi.annotation.Extension;
import io.siddhi.annotation.Parameter;
import io.siddhi.annotation.util.DataType;
import io.siddhi.core.exception.ConnectionUnavailableException;
import io.siddhi.core.exception.SiddhiAppRuntimeException;
import io.siddhi.core.util.snapshot.state.State;
import io.siddhi.core.util.transport.DynamicOptions;
import io.siddhi.core.util.transport.OptionHolder;
import io.siddhi.extension.io.grpc.util.GrpcConstants;
import org.apache.log4j.Logger;
import org.wso2.grpc.Event;
import org.wso2.grpc.EventServiceGrpc;

import javax.annotation.Nullable;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * {@code GrpcSink} Handle the gRPC publishing tasks.
 */
@Extension(
        name = "grpc", namespace = "sink",
        description = "This extension publishes event data encoded into GRPC Classes as defined in the user input " +
                "jar. This extension has a default gRPC service classes jar added. The default service is called " +
                "\"EventService\" and it has 2 rpc's. They are process and consume. Process sends a request of type " +
                "Event and receives a response of the same type. Consume sends a request of type Event and expects " +
                "no response from gRPC server. Please note that the Event type mentioned here is not " +
                "io.siddhi.core.event.Event but a type defined in the default service protobuf given in the readme.",
        parameters = {
                @Parameter(name = "url",
                        description = "The url to which the outgoing events should be published via this extension. " +
                                "This url should consist the host address, port, service name, method name in the " +
                                "following format. grpc://hostAddress:port/serviceName/methodName/sequenceName",
                        type = {DataType.STRING}),
                @Parameter(name = "sink.id",
                        description = "a unique ID that should be set for each gRPC sink. There is a 1:1 mapping " +
                                "between gRPC sinks and sources. Each sink has one particular source listening to " +
                                "the responses to requests published from that sink. So the same sink.id should be " +
                                "given when writing the source also.",
                        type = {DataType.INT}),
        },
        examples = {
                @Example(
                        syntax = "@sink(type='grpc', " +
                                "url = 'grpc://134.23.43.35:8080/org.wso2.grpc.EventService/consume/mySequence', " +
                                "@map(type='json')) "
                                + "define stream FooStream (message String);",
                        description = "Here a stream named FooStream is defined with grpc sink. Since sequence is " +
                                "specified here sink will be in default mode. i.e communicating to MI. The " +
                                "MicroIntegrator should be running at 134.23.43.35 host and listening on port 8080. " +
                                "The sequence called mySeq will be accessed."
                        //todo: add an example for generic service access
                )
        }
)

public class GrpcSink extends AbstractGrpcSink {
    private static final Logger logger = Logger.getLogger(GrpcSink.class.getName());
    EventServiceGrpc.EventServiceStub asyncStub;


    @Override
    public void initSink(OptionHolder optionHolder) {


    }

    @Override
    public void publish(Object payload, DynamicOptions dynamicOptions, State state)
            throws ConnectionUnavailableException {
        StreamObserver<Empty> responseObserver = new StreamObserver<Empty>() {
            @Override
            public void onNext(Empty event) {
            }

            @Override
            public void onError(Throwable t) {
                if (logger.isDebugEnabled()) {
                    logger.debug(siddhiAppContext.getName() + ": " + t.getMessage());
                }
            }

            @Override
            public void onCompleted() {
                System.out.println("Empty Done.....");
            }
        };
        if (isDefaultMode) {
            Event.Builder requestBuilder = Event.newBuilder();
            requestBuilder.setPayload((String) payload);
            Event sequenceCallRequest = requestBuilder.build();
            EventServiceGrpc.EventServiceStub currentAsyncStub = asyncStub;

            if (headersOption != null) {
                Metadata header = new Metadata();
                String headers = headersOption.getValue(dynamicOptions);
                Metadata.Key<String> key =
                        Metadata.Key.of(GrpcConstants.HEADERS, Metadata.ASCII_STRING_MARSHALLER);
                header.put(key, headers);
                currentAsyncStub = MetadataUtils.attachHeaders(asyncStub, header);// TODO: 8/5/19 can we put this outside the IF condition???
            }

            /*StreamObserver<Empty> responseObserver = new StreamObserver<Empty>() {
                @Override
                public void onNext(Empty event) {}

                @Override
                public void onError(Throwable t) {
                    if (logger.isDebugEnabled()) {
                        logger.debug(siddhiAppContext.getName() + ": " + t.getMessage());
                    }
                }

                @Override
                public void onCompleted() {}
            };*/
            currentAsyncStub.consume(sequenceCallRequest, responseObserver);
        } else {
            //todo: handle publishing to generic service
            try {


                Class[] parameter = new Class[]{requestClass, StreamObserver.class};
                Object[] params = new Object[]{payload, responseObserver};
                Method m = super.stubClass.getDeclaredMethod(super.methodName, parameter);

                m.invoke(stubObject, params);
//                ListenableFuture<Empty> genericResponse = (ListenableFuture<Empty>) m.invoke(super.stubObject,payload);
//                Futures.addCallback(genericResponse, new FutureCallback<Empty>() {
//                    @Override
//                    public void onSuccess(@Nullable Empty o) {
//
//                    }
//
//                    @Override
//                    public void onFailure(Throwable t) {
//                        if (logger.isDebugEnabled()) {
//                            logger.debug(siddhiAppContext.getName() + ": " + t.getMessage());
//                        }
//                        throw new SiddhiAppRuntimeException(t.getMessage());
//                    }
//                },MoreExecutors.directExecutor());


            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }

        }
    }

    /**
     * This method will be called before the processing method.
     * Intention to establish connection to publish event.
     *
     * @throws ConnectionUnavailableException if end point is unavailable the ConnectionUnavailableException thrown
     *                                        such that the  system will take care retrying for connection
     */
    @Override
    public void connect() throws ConnectionUnavailableException {
        this.channel = ManagedChannelBuilder.forTarget(address).usePlaintext()
                .build();
        if (isDefaultMode) {
            this.asyncStub = EventServiceGrpc.newStub(channel);
            if (!channel.isShutdown()) {
                logger.info(streamID + " has successfully connected to " + url);
            }
        } else {

            String futureStubName = this.packageName + this.serviceName + "Grpc$" + this.serviceName + "Stub";

            try {
                stubClass = Class.forName(futureStubName);
                Constructor constructor = stubClass.getDeclaredConstructor(Channel.class);
                constructor.setAccessible(true);
                this.stubObject = constructor.newInstance(this.channel);


            } catch (ClassNotFoundException e) {

            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }

}
