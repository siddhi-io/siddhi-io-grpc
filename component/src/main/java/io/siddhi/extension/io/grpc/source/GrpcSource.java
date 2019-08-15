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

import com.google.protobuf.Empty;
import io.grpc.Server;
import io.grpc.ServerInterceptors;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import io.siddhi.annotation.Example;
import io.siddhi.annotation.Extension;
import io.siddhi.annotation.Parameter;
import io.siddhi.annotation.util.DataType;
import io.siddhi.core.exception.ConnectionUnavailableException;
import io.siddhi.core.exception.SiddhiAppRuntimeException;
import io.siddhi.core.util.snapshot.state.State;
import io.siddhi.core.util.transport.OptionHolder;
import org.apache.log4j.Logger;
import org.wso2.grpc.Event;
import org.wso2.grpc.EventServiceGrpc;

import static io.siddhi.extension.io.grpc.util.GrpcUtils.extractHeaders;

/**
 * This handles receiving requests from grpc clients and populating the stream
 */
@Extension(name = "grpc", namespace = "source", description = "This extension starts a grpc server during " +
        "initialization time. The server listens to requests from grpc stubs. This source has a default mode of " +
        "operation and custom user defined grpc service mode. In the default mode this source will use EventService " +
        "consume method. This method will receive requests and injects them into stream through a mapper.",
        parameters = {
                @Parameter(
                        name = "url",
                        description = "The url which can be used by a client to access the grpc server in this " +
                                "extension. This url should consist the host address, port, service name, method " +
                                "name in the following format. `grpc://0.0.0.0:9763/<serviceName>/<methodName>`" ,
                        type = {DataType.STRING}),
                @Parameter(
                        name = "max.inbound.message.size",
                        description = "Sets the maximum message size in bytes allowed to be received on the server." ,
                        type = {DataType.INT},
                        optional = true,
                        defaultValue = "4194304"),
                @Parameter(
                        name = "max.inbound.metadata.size",
                        description = "Sets the maximum size of metadata in bytes allowed to be received." ,
                        type = {DataType.INT},
                        optional = true,
                        defaultValue = "8192"),
                @Parameter(
                        name = "server.shutdown.waiting.time",
                        description = "The time in seconds to wait for the server to shutdown, giving up " +
                                "if the timeout is reached." ,
                        type = {DataType.LONG},
                        optional = true,
                        defaultValue = "5"),
        },
        examples = {
                @Example(syntax = "@source(type='grpc', " +
                        "url='grpc://locanhost:8888/org.wso2.grpc.EventService/consume', @map(type='json')) " +
                        "define stream BarStream (message String);",
                        description = "Here the port is given as 8888. So a grpc server will be started on port 8888 " +
                        "and the server will expose EventService. This is the default service packed with the source." +
                        " In EventService the consume method is"
                ),
                @Example(syntax = "@source(type='grpc', " +
                        "url='grpc://locanhost:8888/org.wso2.grpc.EventService/consume', " +
                        "@map(type='json', @attributes(name='trp:name', age='trp:age', message='message'))) " +
                        "define stream BarStream (message String, name String, age int);",
                        description = "Here we are getting headers sent with the request as transport properties and " +
                        "injecting them into the stream. With each request a header will be sent in MetaData in the " +
                        "following format: 'Name:John', 'Age:23'"
                )
        }
)
public class GrpcSource extends AbstractGrpcSource {
    private static final Logger logger = Logger.getLogger(GrpcSource.class.getName());
    protected String[] requestedTransportPropertyNames;
    protected Server server;

    @Override
    public void initializeGrpcServer(int port) {
        if (isDefaultMode) {
            this.server = serverBuilder.addService(ServerInterceptors.intercept(
                    new EventServiceGrpc.EventServiceImplBase() {
                @Override
                public void consume(Event request,
                                    StreamObserver<Empty> responseObserver) {
                    if (request.getPayload() == null) {
                        logger.error(siddhiAppContext.getName() + ":" + streamID + ": Dropping request due to " +
                                "missing payload ");
                        responseObserver.onError(new io.grpc.StatusRuntimeException(Status.DATA_LOSS));
                    } else {
                        try {
                            if (request.getHeadersMap().size() != 0) {
                                sourceEventListener.onEvent(request.getPayload(), extractHeaders(request.getHeadersMap(),
                                        requestedTransportPropertyNames));
                            } else {
                                sourceEventListener.onEvent(request.getPayload(), null);
                            }
                        } catch (SiddhiAppRuntimeException e) {
                            logger.error(siddhiAppContext.getName() + ":" + streamID + ": Dropping request. " +
                                    e.getMessage());
                        }
                        responseObserver.onNext(Empty.getDefaultInstance());
                        responseObserver.onCompleted();
                    }
                }
            }, serverInterceptor)).build();
        } else {

        }
    }

    @Override
    public void initSource(OptionHolder optionHolder, String[] requestedTransportPropertyNames) {
        this.requestedTransportPropertyNames = requestedTransportPropertyNames.clone();
    }

    @Override
    public void connect(ConnectionCallback connectionCallback, State state) throws ConnectionUnavailableException {
        connectGrpcServer(server, logger);
    }

    /**
     * This method can be called when it is needed to disconnect from the end point.
     */
    @Override
    public void disconnect() {
        disconnectGrpcServer(server, logger);
    }
}
