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
import io.grpc.ServerBuilder;
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
import io.siddhi.extension.io.grpc.util.SourceServerInterceptor;
import io.siddhi.query.api.exception.SiddhiAppValidationException;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.TimeUnit;

import static io.siddhi.extension.io.grpc.util.GrpcUtils.*;

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
    protected String serviceName;//private --> protected
    protected String methodName; //private --> protected
    protected boolean isDefaultMode;
    private int port;
    protected Option headersOption;
    protected String[] requestedTransportPropertyNames;
    protected SourceServerInterceptor serverInterceptor;
    protected ServerBuilder serverBuilder;


    //-----------------------------------
//    protected String methodName;
    protected Class requestClass;

    @Override
    protected ServiceDeploymentInfo exposeServiceDeploymentInfo() {
        return null;
    }

    /**
     * The initialization method for {@link Source}, will be called before other methods. It used to validate
     * all configurations and to get initial values.
     *
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
        this.requestedTransportPropertyNames = requestedTransportPropertyNames;
        this.url = optionHolder.validateAndGetOption(GrpcConstants.PUBLISHER_URL).getValue();
        if (optionHolder.isOptionExists(GrpcConstants.HEADERS)) {
            this.headersOption = optionHolder.validateAndGetOption(GrpcConstants.HEADERS);
        }
        if (!url.substring(0, 4).equalsIgnoreCase(GrpcConstants.GRPC_PROTOCOL_NAME)) {
            throw new SiddhiAppValidationException(siddhiAppContext.getName() + "The url must begin with \""
                    + GrpcConstants.GRPC_PROTOCOL_NAME + "\" for all grpc sinks");
        }
        URL aURL;
        try {
            aURL = new URL("http" + url.substring(4));
        } catch (MalformedURLException e) {
            throw new SiddhiAppValidationException(siddhiAppContext.getName() + ": MalformedURLException. "
                    + e.getMessage());
        }
        this.serviceName = getServiceName(aURL.getPath());
        this.methodName = getMethodName(aURL.getPath());
        this.port = aURL.getPort();
        initSource(optionHolder);
        this.serverInterceptor = new SourceServerInterceptor(this);

        //ServerBuilder parameters
        serverBuilder = ServerBuilder.forPort(port);
        serverBuilder.maxInboundMessageSize(Integer.parseInt(optionHolder.getOrCreateOption(
                GrpcConstants.MAX_INBOUND_MESSAGE_SIZE, GrpcConstants.MAX_INBOUND_MESSAGE_SIZE_DEFAULT).getValue()));
        serverBuilder.maxInboundMetadataSize(Integer.parseInt(optionHolder.getOrCreateOption(
                GrpcConstants.MAX_INBOUND_METADATA_SIZE, GrpcConstants.MAX_INBOUND_METADATA_SIZE_DEFAULT).getValue()));

        if (serviceName.equals(GrpcConstants.DEFAULT_SERVICE_NAME)) {
            this.isDefaultMode = true;
            initializeGrpcServer(port);//todo  can move out of the if
        } else {
            //todo: handle generic grpc service
            initializeGrpcServer(port);
            try {
                requestClass = getRequestClass(aURL.getPath());
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }


        }
        return null;
    }

    public abstract void initializeGrpcServer(int port);

    public abstract void initSource(OptionHolder optionHolder);

    public abstract void populateHeaderString(String headerString);

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

    protected String[] extractHeaders(String headerString) {
        String[] headersArray = new String[requestedTransportPropertyNames.length];
        String[] headerParts = headerString.split(",");
        for (String headerPart : headerParts) {
            String cleanA = headerPart.replaceAll("'", "");
            cleanA = cleanA.replaceAll(" ", "");
            String[] keyValue = cleanA.split(":");
            for (int i = 0; i < requestedTransportPropertyNames.length; i++) {
                if (keyValue[0].equalsIgnoreCase(requestedTransportPropertyNames[i])) {
                    headersArray[i] = keyValue[1];
                }
            }
        }
        for (int i = 0; i < requestedTransportPropertyNames.length; i++) {
            if (headersArray[i] == null || headersArray[i].equalsIgnoreCase("")) {
                throw new SiddhiAppRuntimeException(siddhiAppContext.getName() + ":  Missing header " +
                        requestedTransportPropertyNames[i]);
            }
        }
        return headersArray;
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