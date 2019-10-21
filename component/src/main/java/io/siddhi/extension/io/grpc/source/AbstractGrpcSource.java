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

import com.google.protobuf.GeneratedMessageV3;
import io.siddhi.core.config.SiddhiAppContext;
import io.siddhi.core.stream.ServiceDeploymentInfo;
import io.siddhi.core.stream.input.source.Source;
import io.siddhi.core.stream.input.source.SourceEventListener;
import io.siddhi.core.util.config.ConfigReader;
import io.siddhi.core.util.snapshot.state.StateFactory;
import io.siddhi.core.util.transport.OptionHolder;
import io.siddhi.extension.io.grpc.util.GrpcConstants;
import io.siddhi.extension.io.grpc.util.GrpcServerConfigs;
import io.siddhi.query.api.exception.SiddhiAppValidationException;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;

import static io.siddhi.extension.io.grpc.util.GrpcUtils.getRpcMethodList;

/**
 * This is an abstract class extended by GrpcSource and GrpcServiceSource. This provides most of initialization
 * implementations common for both sources
 */
public abstract class AbstractGrpcSource extends Source {
    protected SiddhiAppContext siddhiAppContext;
    protected SourceEventListener sourceEventListener;
    private String[] requestedTransportPropertyNames;
    protected String streamID;
    private ServiceDeploymentInfo serviceDeploymentInfo;
    protected GrpcServerConfigs grpcServerConfigs;
    protected String siddhiAppName;
    protected Class requestClass;


    @Override
    protected ServiceDeploymentInfo exposeServiceDeploymentInfo() {
        return serviceDeploymentInfo;
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
        this.streamID = sourceEventListener.getStreamDefinition().getId();
        this.siddhiAppContext = siddhiAppContext;
        this.siddhiAppName = siddhiAppContext.getName();
        this.sourceEventListener = sourceEventListener;

        this.requestedTransportPropertyNames = requestedTransportPropertyNames.clone();
        this.grpcServerConfigs = new GrpcServerConfigs(optionHolder, siddhiAppContext, streamID , configReader);
        if (!grpcServerConfigs.getServiceConfigs().isDefaultService()) {
            requestClass = getRequestClass();
        }
        initSource(optionHolder, requestedTransportPropertyNames);
        this.serviceDeploymentInfo = new ServiceDeploymentInfo(grpcServerConfigs.getServiceConfigs().getPort(),
                grpcServerConfigs.getServiceConfigs().getTruststoreFilePath() != null ||
                        grpcServerConfigs.getServiceConfigs().getKeystoreFilePath() != null);
        return null;
    }

    public void handleInjection(Object payload, String[] headers) {
        sourceEventListener.onEvent(payload, headers);
    }

    private Class getRequestClass() {
        String camelCaseMethodName = grpcServerConfigs.getServiceConfigs().getMethodName().substring(0, 1).toUpperCase()
                + grpcServerConfigs.getServiceConfigs().getMethodName().substring(1);
        Field methodDescriptor;
        try {
            methodDescriptor = Class.forName(grpcServerConfigs.getServiceConfigs().getFullyQualifiedServiceName() +
                    GrpcConstants.GRPC_PROTOCOL_NAME_UPPERCAMELCASE).getDeclaredField(GrpcConstants.GETTER +
                    camelCaseMethodName + GrpcConstants.METHOD_NAME);
        } catch (ClassNotFoundException e) {
            throw new SiddhiAppValidationException(siddhiAppName + ":" + streamID + ": Invalid service name provided " +
                    "in the url, provided service name: " + grpcServerConfigs.getServiceConfigs()
                    .getFullyQualifiedServiceName(), e);
        } catch (NoSuchFieldException e) {
            throw new SiddhiAppValidationException(siddhiAppName + ":" + streamID + ": Invalid method name provided " +
                    "in the url, provided method name: " + grpcServerConfigs.getServiceConfigs()
                    .getMethodName() + ", Expected one of these these methods: " + getRpcMethodList(grpcServerConfigs
                    .getServiceConfigs(), siddhiAppName, streamID), e);
        }
        ParameterizedType parameterizedType = (ParameterizedType) methodDescriptor.getGenericType();
        return (Class) parameterizedType.getActualTypeArguments()[GrpcConstants.REQUEST_CLASS_POSITION];
    }

    public abstract void initSource(OptionHolder optionHolder, String[] requestedTransportPropertyNames);

    /**
     * Returns the list of classes which this source can output.
     */
    @Override
    public Class[] getOutputEventClasses() {
        return new Class[]{String.class, GeneratedMessageV3.class};
    }

    /**
     * Called at the end to clean all the resources consumed by the {@link Source}
     */
    @Override
    public void destroy() {

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

    public String[] getRequestedTransportPropertyNames() {
        return requestedTransportPropertyNames.clone();
    }

    public String getStreamID() {
        return streamID;
    }

    public abstract void logError(String message);
}
