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

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Metadata;
import io.grpc.netty.shaded.io.grpc.netty.GrpcSslContexts;
import io.grpc.netty.shaded.io.grpc.netty.NettyChannelBuilder;
import io.grpc.netty.shaded.io.netty.handler.ssl.SslContextBuilder;
import io.grpc.stub.AbstractStub;
import io.grpc.stub.MetadataUtils;
import io.siddhi.core.config.SiddhiAppContext;
import io.siddhi.core.exception.SiddhiAppCreationException;
import io.siddhi.core.stream.ServiceDeploymentInfo;
import io.siddhi.core.stream.output.sink.Sink;
import io.siddhi.core.util.config.ConfigReader;
import io.siddhi.core.util.snapshot.state.StateFactory;
import io.siddhi.core.util.transport.DynamicOptions;
import io.siddhi.core.util.transport.Option;
import io.siddhi.core.util.transport.OptionHolder;
import io.siddhi.extension.io.grpc.util.GrpcConstants;
import io.siddhi.extension.io.grpc.util.ServiceConfigs;
import io.siddhi.query.api.definition.StreamDefinition;
import org.apache.log4j.Logger;
import org.wso2.grpc.Event;

import java.io.FileInputStream;
import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLException;
import javax.net.ssl.TrustManagerFactory;

/**
 * {@code AbstractGrpcSink} is a super class extended by GrpcCallSink, and GrpcSink.
 * This provides most of the initialization implementations
 */
public abstract class AbstractGrpcSink extends Sink {
    private static final Logger logger = Logger.getLogger(AbstractGrpcSink.class.getName());
    protected String siddhiAppName;
    protected ManagedChannel channel;
    protected String streamID;
    protected Option headersOption;
    protected Option metadataOption;
    protected ManagedChannelBuilder managedChannelBuilder;
    protected long channelTerminationWaitingTimeInMillis = -1L;
    protected ServiceConfigs serviceConfigs;
    protected StreamDefinition streamDefinition;
    protected Map<String, String> headersMap;

    /**
     * Returns the list of classes which this sink can consume.
     * Based on the type of the sink, it may be limited to being able to publish specific type of classes.
     * For example, a sink of type file can only write objects of type String .
     * @return array of supported classes , if extension can support of any types of classes
     * then return empty array .
     */
    @Override
    public Class[] getSupportedInputEventClasses() {
        return new Class[]{com.google.protobuf.GeneratedMessageV3.class, String.class};
        // in default case json mapper will inject String. In custom gRPC service
        // case protobuf mapper will inject gRPC message class
    }

    @Override
    protected ServiceDeploymentInfo exposeServiceDeploymentInfo() {
        return null;
    }

    /**
     * Returns a list of supported dynamic options (that means for each event value of the option can change) by
     * the transport
     *
     * @return the list of supported dynamic option keys
     */
    @Override
    public String[] getSupportedDynamicOptions() {
        return new String[]{GrpcConstants.HEADERS, GrpcConstants.METADATA};
    }

    /**
     * The initialization method for {@link Sink}, will be called before other methods. It used to validate
     * all configurations and to get initial values.
     * @param streamDefinition  containing stream definition bind to the {@link Sink}
     * @param optionHolder            Option holder containing static and dynamic configuration related
     *                                to the {@link Sink}
     * @param configReader        to read the sink related system configuration.
     * @param siddhiAppContext        the context of the {@link io.siddhi.query.api.SiddhiApp} used to
     */
    @Override
    protected StateFactory init(StreamDefinition streamDefinition, OptionHolder optionHolder, ConfigReader configReader,
                                SiddhiAppContext siddhiAppContext) {
        this.siddhiAppName = siddhiAppContext.getName();
        this.streamID = streamDefinition.getId();
        this.streamDefinition = streamDefinition;
        if (optionHolder.isOptionExists(GrpcConstants.HEADERS)) {
            this.headersOption = optionHolder.validateAndGetOption(GrpcConstants.HEADERS);
        }
        if (optionHolder.isOptionExists(GrpcConstants.METADATA)) {
            this.metadataOption = optionHolder.validateAndGetOption(GrpcConstants.METADATA);
        }
        this.serviceConfigs = new ServiceConfigs(optionHolder, siddhiAppContext, streamID);

        managedChannelBuilder = NettyChannelBuilder.forTarget(serviceConfigs.getHostPort());

        if (serviceConfigs.getTruststoreFilePath() != null || serviceConfigs.getKeystoreFilePath() != null) {
            SslContextBuilder sslContextBuilder = GrpcSslContexts.forClient();
            if (serviceConfigs.getTruststoreFilePath() != null) {
                sslContextBuilder.trustManager(getTrustManagerFactory(serviceConfigs.getTruststoreFilePath(),
                        serviceConfigs.getTruststorePassword(), serviceConfigs.getTruststoreAlgorithm(),
                        serviceConfigs.getTlsStoreType()));
            }
            if (serviceConfigs.getKeystoreFilePath() != null) {
                sslContextBuilder.keyManager(getKeyManagerFactory(serviceConfigs.getKeystoreFilePath(),
                        serviceConfigs.getKeystorePassword(), serviceConfigs.getKeystoreAlgorithm(),
                        serviceConfigs.getTlsStoreType()));
            }
            try {
                managedChannelBuilder = ((NettyChannelBuilder) managedChannelBuilder).sslContext(sslContextBuilder
                        .build());
            } catch (SSLException e) {
                throw new SiddhiAppCreationException(siddhiAppContext.getName() + ": " + streamID + ": Error while " +
                        "creating gRPC channel. " + e.getMessage(), e);
            }
        } else {
                managedChannelBuilder = managedChannelBuilder.usePlaintext();
        }

        if (optionHolder.isOptionExists(GrpcConstants.IDLE_TIMEOUT_MILLIS)) {
            managedChannelBuilder.idleTimeout(Long.parseLong(optionHolder.validateAndGetOption(
                    GrpcConstants.IDLE_TIMEOUT_MILLIS).getValue()), TimeUnit.MILLISECONDS);
        }
        if (optionHolder.isOptionExists(GrpcConstants.KEEP_ALIVE_TIME_MILLIS)) {
            managedChannelBuilder.keepAliveTime(Long.parseLong(optionHolder.validateAndGetOption(
                    GrpcConstants.KEEP_ALIVE_TIME_MILLIS).getValue()), TimeUnit.MILLISECONDS);
        }
        if (optionHolder.isOptionExists(GrpcConstants.KEEP_ALIVE_TIMEOUT_MILLIS)) {
            managedChannelBuilder.keepAliveTimeout(Long.parseLong(optionHolder.validateAndGetOption(
                    GrpcConstants.KEEP_ALIVE_TIMEOUT_MILLIS).getValue()), TimeUnit.MILLISECONDS);
        }
        if (optionHolder.isOptionExists(GrpcConstants.KEEP_ALIVE_WITHOUT_CALLS)) {
            managedChannelBuilder.keepAliveWithoutCalls(Boolean.parseBoolean(optionHolder.validateAndGetOption(
                    GrpcConstants.KEEP_ALIVE_WITHOUT_CALLS).getValue()));
        }
        if (Boolean.parseBoolean(optionHolder.getOrCreateOption(GrpcConstants.ENABLE_RETRY,
                GrpcConstants.ENABLE_RETRY_DEFAULT).getValue())) {
            managedChannelBuilder.enableRetry();
            if (optionHolder.isOptionExists(GrpcConstants.MAX_RETRY_ATTEMPTS)) {
                managedChannelBuilder.maxRetryAttempts(Integer.parseInt(optionHolder.validateAndGetOption(
                        GrpcConstants.MAX_RETRY_ATTEMPTS).getValue()));
            }
            if (optionHolder.isOptionExists(GrpcConstants.RETRY_BUFFER_SIZE)) {
                managedChannelBuilder.retryBufferSize(Long.parseLong(optionHolder.validateAndGetOption(
                        GrpcConstants.RETRY_BUFFER_SIZE).getValue()));
            }
            if (optionHolder.isOptionExists(GrpcConstants.PER_RPC_BUFFER_SIZE)) {
                managedChannelBuilder.perRpcBufferLimit(Long.parseLong(optionHolder.validateAndGetOption(
                        GrpcConstants.PER_RPC_BUFFER_SIZE).getValue()));
            }
        }
        initSink(optionHolder);
        if (headersOption != null && headersOption.isStatic()) {
            headersMap = new HashMap<>();
            String headers = headersOption.getValue();
            headers = headers.replaceAll(GrpcConstants.INVERTED_COMMA_STRING, GrpcConstants.EMPTY_STRING);
            String[] headersArray = headers.split(GrpcConstants.COMMA_STRING);
            for (String headerKeyValue: headersArray) {
                String[] headerKeyValueArray = headerKeyValue.split(GrpcConstants.SEMI_COLON_STRING);
                headersMap.put(headerKeyValueArray[0], headerKeyValueArray[1]);
            }
            if (serviceConfigs.getSequenceName() != null) {
                headersMap.put(GrpcConstants.SEQUENCE_HEADER_KEY, serviceConfigs.getSequenceName());
            }
        }
        return null;
    }

    private TrustManagerFactory getTrustManagerFactory(String filePath, String password, String algorithm,
                                                       String storeType)  {
        char[] passphrase = password.toCharArray();
        try {
            KeyStore keyStore = KeyStore.getInstance(storeType);
            try (FileInputStream fis = new FileInputStream(filePath)) {
                keyStore.load(fis, passphrase);
            } catch (IOException e) {
                throw new SiddhiAppCreationException(siddhiAppName + ": " + streamID + ": " + e.getMessage(), e);
            }
            TrustManagerFactory tmf = TrustManagerFactory.getInstance(algorithm);
            tmf.init(keyStore);
            return tmf;
        } catch (CertificateException | NoSuchAlgorithmException | KeyStoreException e) {
           throw new SiddhiAppCreationException(siddhiAppName + ": " + streamID + ": Error while reading truststore " +
                   e.getMessage(), e);
        }
    }

    private KeyManagerFactory getKeyManagerFactory(String filePath, String password, String algorithm,
                                                   String storeType) {
        try {
            KeyStore keyStore = KeyStore.getInstance(storeType);
            char[] passphrase = password.toCharArray();
            try (FileInputStream fis = new FileInputStream(filePath)) {
                keyStore.load(fis, passphrase);
            } catch (IOException e) {
                throw new SiddhiAppCreationException(siddhiAppName + ": " + streamID + ": " + e.getMessage(), e);
            }
            KeyManagerFactory kmf = KeyManagerFactory.getInstance(algorithm);
            kmf.init(keyStore, passphrase);
            return kmf;
        } catch (CertificateException | UnrecoverableKeyException | NoSuchAlgorithmException | KeyStoreException e) {
            throw new SiddhiAppCreationException(siddhiAppName + ": " + streamID + ": Error while reading keystore " +
                    e.getMessage(), e);
        }
    }

    public abstract void initSink(OptionHolder optionHolder);

    /**
     * The method can be called when removing an event receiver.
     * The cleanups that have to be done after removing the receiver could be done here.
     */
    @Override
    public void destroy() {
        channel = null;
    }

    public Event.Builder addHeadersToEventBuilder(DynamicOptions dynamicOptions, Event.Builder eventBuilder) {
        if (headersOption != null) {
            String headers = headersOption.getValue(dynamicOptions);
            headers = headers.replaceAll(GrpcConstants.INVERTED_COMMA_STRING, GrpcConstants.EMPTY_STRING);
            String[] headersArray = headers.split(GrpcConstants.COMMA_STRING);
            for (String headerKeyValue: headersArray) {
                String[] headerKeyValueArray = headerKeyValue.split(GrpcConstants.SEMI_COLON_STRING);
                eventBuilder.putHeaders(headerKeyValueArray[0], headerKeyValueArray[1]);
            }
        }
        if (serviceConfigs.getSequenceName() != null) {
            eventBuilder.putHeaders(GrpcConstants.SEQUENCE_HEADER_KEY, serviceConfigs.getSequenceName());
        }
        return eventBuilder;
    }

    public AbstractStub attachMetaDataToStub(DynamicOptions dynamicOptions, AbstractStub stub) {
        Metadata metadata = new Metadata();
        String metadataString;
        if (metadataOption.isStatic()) {
            metadataString = metadataOption.getValue();
        } else {
            metadataString = metadataOption.getValue(dynamicOptions);
        }
        metadataString = metadataString.replaceAll(GrpcConstants.INVERTED_COMMA_STRING, GrpcConstants.EMPTY_STRING);
        String[] metadataArray = metadataString.split(GrpcConstants.COMMA_STRING);
        for (String metadataKeyValue: metadataArray) {
            String[] headerKeyValueArray = metadataKeyValue.split(GrpcConstants.SEMI_COLON_STRING);
            metadata.put(Metadata.Key.of(headerKeyValueArray[0], Metadata.ASCII_STRING_MARSHALLER),
                    headerKeyValueArray[1]);
        }
        return MetadataUtils.attachHeaders(stub, metadata);
    }
}
