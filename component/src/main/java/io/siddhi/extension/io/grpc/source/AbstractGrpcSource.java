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
import io.siddhi.core.util.transport.Option;
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
 * This is an abstract class extended by GrpcSource and GrpcServiceSource. This provides most of initialization
 * implementations common for both sources
 */
public abstract class AbstractGrpcSource extends Source {
    private static final Logger logger = Logger.getLogger(GrpcCallResponseSource.class.getName());
    protected SiddhiAppContext siddhiAppContext;
    protected SourceEventListener sourceEventListener;
    private String url;
    protected Server server;
    private String serviceName;
//    private String methodName;
    protected boolean isDefaultMode;
    private int port;
    protected Option headersOption;

    @Override
    protected ServiceDeploymentInfo exposeServiceDeploymentInfo() {
        return null;
    }

    /**
     * The initialization method for {@link Source}, will be called before other methods. It used to validate
     * all configurations and to get initial values.
     * @param sourceEventListener After receiving events, the source should trigger onEvent() of this listener.
     *                            Listener will then pass on the events to the appropriate mappers for processing .
     * @param optionHolder        Option holder containing static configuration related to the {@link Source}
     * @param configReader        ConfigReader is used to read the {@link Source} related system configuration.
     * @param siddhiAppContext    the context of the {@link io.siddhi.query.api.SiddhiApp} used to get Siddhi
     */
    @Override
    public StateFactory init(SourceEventListener sourceEventListener, OptionHolder optionHolder,
                             String[] requestedTransportPropertyNames, ConfigReader configReader,
                             SiddhiAppContext siddhiAppContext) {
        this.siddhiAppContext = siddhiAppContext;
        this.sourceEventListener = sourceEventListener;
        this.url = optionHolder.validateAndGetOption(GrpcConstants.PUBLISHER_URL).getValue();
        if (optionHolder.isOptionExists(GrpcConstants.HEADERS)) { //todo: what to do with headers?
            this.headersOption = optionHolder.validateAndGetOption(GrpcConstants.HEADERS);
        }
        List<String> urlParts = new ArrayList<>(Arrays.asList(url.split(GrpcConstants.PORT_SERVICE_SEPARATOR)));
        urlParts.removeAll(Collections.singletonList(GrpcConstants.EMPTY_STRING));

        if (!urlParts.get(GrpcConstants.URL_PROTOCOL_POSITION)
                .equalsIgnoreCase(GrpcConstants.GRPC_PROTOCOL_NAME + ":")) {
            throw new SiddhiAppValidationException(siddhiAppContext.getName() + ": The url must begin with \"" +
                    GrpcConstants.GRPC_PROTOCOL_NAME + "\" for all grpc source");
        }
        String[] fullyQualifiedServiceNameParts = urlParts.get(GrpcConstants.URL_SERVICE_NAME_POSITION).split("\\.");
        this.serviceName = fullyQualifiedServiceNameParts[fullyQualifiedServiceNameParts.length - 1];
//        this.methodName = urlParts.get(GrpcConstants.URL_METHOD_NAME_POSITION);
        this.port = Integer.parseInt(urlParts.get(GrpcConstants.URL_HOST_AND_PORT_POSITION).split(":")[1]);

        initSource(optionHolder);

        if (serviceName.equals(GrpcConstants.DEFAULT_SERVICE_NAME)
                && urlParts.size() == GrpcConstants.NUM_URL_PARTS_FOR_DEFAULT_MODE_SOURCE) {
                this.isDefaultMode = true;
                initializeGrpcServer(port);
        } else {
            //todo: handle generic grpc service
        }
        return null;
    }

    public abstract void initializeGrpcServer(int port);

    public abstract void initSource(OptionHolder optionHolder);

    /**
     * Returns the list of classes which this source can output.
     *
     * @return Array of classes that will be output by the source.
     * Null or empty array if it can produce any type of class.
     */
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

    /**
     * This method can be called when it is needed to disconnect from the end point.
     */
    @Override
    public void disconnect() {
        try {
            Server serverPointer = server;
            if (serverPointer == null) {
                if (logger.isDebugEnabled()) {
                    logger.debug(siddhiAppContext.getName() + ": Illegal state. Server already stopped.");
                }
                return;
            }
            serverPointer.shutdown();
            if (serverPointer.awaitTermination(1, TimeUnit.SECONDS)) {
                if (logger.isDebugEnabled()) {
                    logger.debug(siddhiAppContext.getName() + ": Server stopped");
                }
                return;
            }
            serverPointer.shutdownNow();
            if (serverPointer.awaitTermination(1, TimeUnit.SECONDS)) {
                return;
            }
            throw new SiddhiAppRuntimeException(siddhiAppContext.getName() + ": Unable to shutdown server");
        } catch (InterruptedException e) {
            throw new SiddhiAppRuntimeException(siddhiAppContext.getName() + ": " + e.getMessage());
        }
    }

    /**
     * Called at the end to clean all the resources consumed by the {@link Source}
     */
    @Override
    public void destroy() {
        server = null;
    }

    /**
     * Called to pause event consumption
     */
    @Override
    public void pause() {

    }

    /**
     * Called to resume event consumption
     */
    @Override
    public void resume() {

    }
}
