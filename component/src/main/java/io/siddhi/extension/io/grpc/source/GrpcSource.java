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
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;
import io.siddhi.annotation.Example;
import io.siddhi.annotation.Extension;
import io.siddhi.annotation.Parameter;
import io.siddhi.annotation.util.DataType;
import io.siddhi.core.config.SiddhiAppContext;
import io.siddhi.core.exception.ConnectionUnavailableException;
import io.siddhi.core.exception.SiddhiAppRuntimeException;
import io.siddhi.core.stream.ServiceDeploymentInfo;
import io.siddhi.core.stream.input.source.Source;
import io.siddhi.core.stream.input.source.SourceEventListener;
import io.siddhi.core.util.config.ConfigReader;
import io.siddhi.core.util.snapshot.state.State;
import io.siddhi.core.util.snapshot.state.StateFactory;
import io.siddhi.core.util.transport.OptionHolder;
import io.siddhi.extension.io.grpc.util.GrpcConstants;
import io.siddhi.query.api.exception.SiddhiAppValidationException;
import org.apache.log4j.Logger;
import org.wso2.grpc.Event;
import org.wso2.grpc.EventServiceGrpc;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 *
 */
@Extension(
        name = "grpc",
        namespace = "source",
        description = "sdfsdf",
        parameters = {
                @Parameter(name = "url",
                        description = "asdfa" ,
                        type = {DataType.STRING}),
        },
        examples = {
                @Example(
                        syntax = "@source(type='grpc', url='') " +
                                "define stream BarStream (message String);",
                        description = "asdfasdf"
                )
        }
)
public class GrpcSource extends Source {
    private static final Logger logger = Logger.getLogger(GrpcCallResponseSource.class.getName());
    private SiddhiAppContext siddhiAppContext;
    private SourceEventListener sourceEventListener;
    private String url;
    private Server server;
    private String serviceName;
    private String methodName;
    private boolean isDefaultMode;

    @Override
    protected ServiceDeploymentInfo exposeServiceDeploymentInfo() {
        return null;
    }

    @Override
    public StateFactory init(SourceEventListener sourceEventListener, OptionHolder optionHolder,
                             String[] requestedTransportPropertyNames, ConfigReader configReader,
                             SiddhiAppContext siddhiAppContext) {
        this.siddhiAppContext = siddhiAppContext;
        this.sourceEventListener = sourceEventListener;
        this.url = optionHolder.validateAndGetOption(GrpcConstants.PUBLISHER_URL).getValue();
        List<String> URLParts = new ArrayList<>(Arrays.asList(url.split(GrpcConstants.PORT_SERVICE_SEPARATOR)));
        URLParts.removeAll(Collections.singletonList(GrpcConstants.EMPTY_STRING));

        if (!URLParts.get(GrpcConstants.URL_PROTOCOL_POSITION)
                .equalsIgnoreCase(GrpcConstants.GRPC_PROTOCOL_NAME + ":")) {
            throw new SiddhiAppValidationException(siddhiAppContext.getName() + ": The url must begin with \"" +
                    GrpcConstants.GRPC_PROTOCOL_NAME + "\" for all grpc source");
        }
        String[] fullyQualifiedServiceNameParts = URLParts.get(GrpcConstants.URL_SERVICE_NAME_POSITION).split("\\.");
        this.serviceName = fullyQualifiedServiceNameParts[fullyQualifiedServiceNameParts.length - 1];
        this.methodName = URLParts.get(GrpcConstants.URL_METHOD_NAME_POSITION);
        if (serviceName.equals(GrpcConstants.DEFAULT_SERVICE_NAME)
                && URLParts.size() == GrpcConstants.NUM_URL_PARTS_FOR_DEFAULT_MODE_SOURCE) {
            if (methodName.equals(GrpcConstants.DEFAULT_METHOD_NAME_WITHOUT_RESPONSE)) {
                this.isDefaultMode = true;
                this.server = ServerBuilder.forPort(8888).addService(new EventServiceGrpc.EventServiceImplBase() {
                    @Override
                    public void consume(Event request,
                                        StreamObserver<Empty> responseObserver) {
                        if (logger.isDebugEnabled()) {
                            logger.debug(siddhiAppContext.getName() + ": Server hit");
                        }
                        sourceEventListener.onEvent(request.getPayload(), new String[]{"1"});
                        responseObserver.onNext(Empty.getDefaultInstance());
                        responseObserver.onCompleted();
                    }
                }).build();
            } else if (methodName.equals(GrpcConstants.DEFAULT_METHOD_NAME_WITH_RESPONSE)) {
                throw new SiddhiAppValidationException(siddhiAppContext.getName() + ": grpc source only accepts " +
                        "method consume in EventService when started in the default mode. If you want to use process " +
                        "method use grpc-service source");
            }
        } else {
            //todo: handle generic grpc service
        }
        return null;
    }

    public void start() throws IOException {

    }

    public void stop() throws InterruptedException {
        Server s = server;
        if (s == null) {
            throw new IllegalStateException("Already stopped");
        }
        server = null;
        s.shutdown();
        if (s.awaitTermination(1, TimeUnit.SECONDS)) {

            if (logger.isDebugEnabled()) {
                logger.debug("Server stopped");
            }
            return;
        }
        s.shutdownNow();
        if (s.awaitTermination(1, TimeUnit.SECONDS)) {
            return;
        }
        throw new RuntimeException("Unable to shutdown server");
    }

    @Override
    public Class[] getOutputEventClasses() {
        return new Class[0];
    }

    @Override
    public void connect(ConnectionCallback connectionCallback, State state) throws ConnectionUnavailableException {
        try {
            server.start();
            if (logger.isDebugEnabled()) {
                logger.debug(siddhiAppContext.getName() + ": Server started");
            }
        } catch (IOException e) {
            throw new SiddhiAppRuntimeException(siddhiAppContext.getName() + ": " + e.getMessage());
        }
    }

    @Override
    public void disconnect() {
        try {
            stop();
        } catch (InterruptedException e) {
            throw new SiddhiAppRuntimeException(siddhiAppContext.getName() + ": " + e.getMessage());
        }
    }

    @Override
    public void destroy() {

    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }
}
