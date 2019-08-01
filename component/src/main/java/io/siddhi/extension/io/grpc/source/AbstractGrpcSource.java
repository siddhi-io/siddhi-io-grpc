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

import io.grpc.Server;
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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 *
 */
public abstract class AbstractGrpcSource extends Source {
    private static final Logger logger = Logger.getLogger(GrpcCallResponseSource.class.getName());
    protected SiddhiAppContext siddhiAppContext;
    protected SourceEventListener sourceEventListener;
    private String url;
    protected Server server;
    private String serviceName;
    private String methodName;
    private boolean isDefaultMode;
    private int port;
    private String hostAddress;
    protected String[] requestedTransportPropertyNames;

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
        this.requestedTransportPropertyNames = requestedTransportPropertyNames.clone();
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
        this.hostAddress = URLParts.get(GrpcConstants.URL_HOST_AND_PORT_POSITION).split(":")[0];
        this.port = Integer.parseInt(URLParts.get(GrpcConstants.URL_HOST_AND_PORT_POSITION).split(":")[1]);

        initSource(optionHolder);

        if (serviceName.equals(GrpcConstants.DEFAULT_SERVICE_NAME)
                && URLParts.size() == GrpcConstants.NUM_URL_PARTS_FOR_DEFAULT_MODE_SOURCE) {
//            if (methodName.equals(GrpcConstants.DEFAULT_METHOD_NAME_WITHOUT_RESPONSE)) {
                this.isDefaultMode = true;
                initializeGrpcServer(port);
//            } else if (methodName.equals(GrpcConstants.DEFAULT_METHOD_NAME_WITH_RESPONSE)) {
//                throw new SiddhiAppValidationException(siddhiAppContext.getName() + ": grpc source only accepts " +
//                        "method consume in EventService when started in the default mode. If you want to use process " +
//                        "method use grpc-service source");
//            }
        } else {
            //todo: handle generic grpc service
        }
        return null;
    }

    public abstract void initializeGrpcServer(int port);

    public abstract void initSource(OptionHolder optionHolder);

    private void stop() throws InterruptedException { //todo move into disconnect
        Server s = server; //todo: put meaningful names
        if (s == null) {
            throw new SiddhiAppRuntimeException(siddhiAppContext.getName() + ": Illeagal state. Server already stopped");
        }
        server = null;
        s.shutdown();
        if (s.awaitTermination(1, TimeUnit.SECONDS)) {

            if (logger.isDebugEnabled()) {
                logger.debug(siddhiAppContext.getName() + ": Server stopped");
            }
            return;
        }
        s.shutdownNow();
        if (s.awaitTermination(1, TimeUnit.SECONDS)) {
            return;
        }
        throw new SiddhiAppRuntimeException(siddhiAppContext.getName() + ": Unable to shutdown server");
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
