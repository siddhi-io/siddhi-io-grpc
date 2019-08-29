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
import io.siddhi.query.api.definition.StreamDefinition;
import io.siddhi.query.api.exception.SiddhiAppValidationException;
import org.apache.log4j.Logger;
import org.wso2.grpc.Event;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.concurrent.TimeUnit;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.TrustManagerFactory;

import static io.siddhi.extension.io.grpc.util.GrpcUtils.*;
import static io.siddhi.extension.io.grpc.util.GrpcUtils.getRequestClass;

/**
 * {@code AbstractGrpcSink} is a super class extended by GrpcCallSink, and GrpcSink.
 * This provides most of the initialization implementations
 */
public abstract class AbstractGrpcSink extends Sink {
    private static final Logger logger = Logger.getLogger(AbstractGrpcSink.class.getName());
    protected String siddhiAppName;
    protected ManagedChannel channel;
    protected String methodName;
    protected String sequenceName;
    protected boolean isDefaultMode = false;
    protected String url;
    protected String streamID;
    protected String address;
    protected Option headersOption;
    protected Option metadataOption;
    protected ManagedChannelBuilder managedChannelBuilder;
    protected long channelTerminationWaitingTimeInMillis = -1L;
    protected StreamDefinition streamDefinition;

    protected Class requestClass;
    protected String serviceReference;

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
        this.streamDefinition = streamDefinition;
        this.url = optionHolder.validateAndGetOption(GrpcConstants.PUBLISHER_URL).getValue().trim();
        this.streamID = streamDefinition.getId();
        if (optionHolder.isOptionExists(GrpcConstants.HEADERS)) {
            this.headersOption = optionHolder.validateAndGetOption(GrpcConstants.HEADERS);
        }
        if (optionHolder.isOptionExists(GrpcConstants.METADATA)) {
            this.metadataOption = optionHolder.validateAndGetOption(GrpcConstants.METADATA);
        }
        if (!url.startsWith(GrpcConstants.GRPC_PROTOCOL_NAME)) {
            throw new SiddhiAppValidationException(siddhiAppContext.getName() + ":" + streamID +
                    ": The url must begin with \"" + GrpcConstants.GRPC_PROTOCOL_NAME + "\" for all grpc sinks");
        }
        URL aURL;
        try {
            aURL = new URL(GrpcConstants.DUMMY_PROTOCOL_NAME + url.substring(4));
        } catch (MalformedURLException e) {
            throw new SiddhiAppValidationException(siddhiAppContext.getName() + ":" + streamID +
                    ": Error in URL format. Expected format is `grpc://0.0.0.0:9763/<serviceName>/<methodName>` but " +
                    "the provided url is " + url + ". " + e.getMessage(), e);
        }
        String serviceName = getServiceName(aURL.getPath());
        this.methodName = getMethodName(aURL.getPath());
        this.address = aURL.getAuthority();
        if (optionHolder.isOptionExists(GrpcConstants.CHANNEL_TERMINATION_WAITING_TIME_MILLIS)) {
            this.channelTerminationWaitingTimeInMillis = Long.parseLong(optionHolder.validateAndGetOption(
                    GrpcConstants.CHANNEL_TERMINATION_WAITING_TIME_MILLIS).getValue());
        }

        String truststoreFilePath = null;
        String truststorePassword = null;
        String keystoreFilePath = null;
        String keystorePassword = null;
        String truststoreAlgorithm = null;
        String keystoreAlgorithm = null;
        String tlsStoreType = null;

        if (optionHolder.isOptionExists(GrpcConstants.TRUSTSTORE_FILE)) {
            truststoreFilePath = optionHolder.validateAndGetOption(GrpcConstants.TRUSTSTORE_FILE).getValue();
            if (optionHolder.isOptionExists(GrpcConstants.TRUSTSTORE_PASSWORD)) {
                truststorePassword = optionHolder.validateAndGetOption(GrpcConstants.TRUSTSTORE_PASSWORD).getValue();
            }
            truststoreAlgorithm = optionHolder.validateAndGetOption(GrpcConstants.TRUSTSTORE_ALGORITHM).getValue();
            tlsStoreType = optionHolder.getOrCreateOption(GrpcConstants.TLS_STORE_TYPE,
                    GrpcConstants.DEFAULT_TLS_STORE_TYPE).getValue();
        }

        if (optionHolder.isOptionExists(GrpcConstants.KEYSTORE_FILE)) {
            keystoreFilePath = optionHolder.validateAndGetOption(GrpcConstants.KEYSTORE_FILE).getValue();
            keystorePassword = optionHolder.validateAndGetOption(GrpcConstants.KEYSTORE_PASSWORD).getValue();
            keystoreAlgorithm = optionHolder.validateAndGetOption(GrpcConstants.KEYSTORE_ALGORITHM).getValue();
            tlsStoreType = optionHolder.getOrCreateOption(GrpcConstants.TLS_STORE_TYPE,
                    GrpcConstants.DEFAULT_TLS_STORE_TYPE).getValue();
        }

        managedChannelBuilder = NettyChannelBuilder.forTarget(address);

        try {
            if (truststoreFilePath != null && keystoreFilePath != null) {
                managedChannelBuilder = ((NettyChannelBuilder) managedChannelBuilder).sslContext(GrpcSslContexts
                        .forClient().trustManager(getTrustManagerFactory(truststoreFilePath, truststorePassword,
                                truststoreAlgorithm, tlsStoreType))
                        .keyManager(getKeyManagerFactory(keystoreFilePath, keystorePassword, keystoreAlgorithm,
                                tlsStoreType))
                        .build());
            } else if (truststoreFilePath != null) {
                managedChannelBuilder = ((NettyChannelBuilder) managedChannelBuilder).sslContext(GrpcSslContexts
                        .forClient().trustManager(getTrustManagerFactory(truststoreFilePath, truststorePassword,
                                truststoreAlgorithm, tlsStoreType))
                        .build());
            } else if (keystoreFilePath != null) {
                managedChannelBuilder = ((NettyChannelBuilder) managedChannelBuilder).sslContext(GrpcSslContexts
                        .forClient().keyManager(getKeyManagerFactory(keystoreFilePath, keystorePassword,
                                keystoreAlgorithm, tlsStoreType))
                        .build());
            } else {
                managedChannelBuilder = managedChannelBuilder.usePlaintext();
            }
        } catch (KeyStoreException | IOException | NoSuchAlgorithmException | CertificateException |
                UnrecoverableKeyException e) {
            throw new SiddhiAppCreationException(siddhiAppContext.getName() + ": " + streamID + ": Error while " +
                    "creating gRPC channel. " + e.getMessage(), e);
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

        if (serviceName.equals(GrpcConstants.DEFAULT_SERVICE_NAME)) {
            this.isDefaultMode = true;
            if (isSequenceNamePresent(aURL.getPath())) {
                this.sequenceName = getSequenceName(aURL.getPath());
            }
        } else {
            this.serviceReference = getFullServiceName(aURL.getPath());
            try {
                this.requestClass = getRequestClass(serviceReference,methodName);
            } catch (ClassNotFoundException e) {
                throw new SiddhiAppCreationException(siddhiAppContext.getName() + ": " +
                        "Invalid service name provided in the url, provided service name : '" + serviceReference + "'", e);
            }
        }
        initSink(optionHolder);
        return null;
    }

    private TrustManagerFactory getTrustManagerFactory(String filePath, String password, String algorithm,
                                                       String storeType) throws
            KeyStoreException, NoSuchAlgorithmException, CertificateException {
        char[] passphrase = password.toCharArray();
        KeyStore keyStore = KeyStore.getInstance(storeType);
        try (FileInputStream fis = new FileInputStream(filePath)) {
            keyStore.load(fis, passphrase);
        } catch (IOException e) {
            throw new SiddhiAppCreationException(siddhiAppName + ": " + streamID + ": " + e.getMessage(), e);
        }
        TrustManagerFactory tmf = TrustManagerFactory.getInstance(algorithm);
        tmf.init(keyStore);
        return tmf;
    }

    private KeyManagerFactory getKeyManagerFactory(String filePath, String password, String algorithm,
                                                   String storeType) throws
            KeyStoreException, CertificateException, NoSuchAlgorithmException, UnrecoverableKeyException {
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

        if (sequenceName != null) {
            eventBuilder.putHeaders(GrpcConstants.SEQUENCE_HEADER_KEY, sequenceName);
        }
        return eventBuilder;
    }

    public AbstractStub attachMetaDataToStub(DynamicOptions dynamicOptions, AbstractStub stub) {
        Metadata metadata = new Metadata();
        String metadataString = metadataOption.getValue(dynamicOptions);
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
