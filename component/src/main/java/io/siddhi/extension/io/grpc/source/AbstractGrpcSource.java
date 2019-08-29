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
import io.grpc.Server;
import io.grpc.netty.shaded.io.grpc.netty.GrpcSslContexts;
import io.grpc.netty.shaded.io.grpc.netty.NettyServerBuilder;
import io.grpc.netty.shaded.io.netty.handler.ssl.ClientAuth;
import io.grpc.netty.shaded.io.netty.handler.ssl.SslContextBuilder;
import io.siddhi.core.config.SiddhiAppContext;
import io.siddhi.core.exception.ConnectionUnavailableException;
import io.siddhi.core.exception.SiddhiAppCreationException;
import io.siddhi.core.exception.SiddhiAppRuntimeException;
import io.siddhi.core.stream.ServiceDeploymentInfo;
import io.siddhi.core.stream.input.source.Source;
import io.siddhi.core.stream.input.source.SourceEventListener;
import io.siddhi.core.util.config.ConfigReader;
import io.siddhi.core.util.snapshot.state.StateFactory;
import io.siddhi.core.util.transport.OptionHolder;
import io.siddhi.extension.io.grpc.util.GrpcConstants;
import io.siddhi.extension.io.grpc.util.GrpcDefaultServer;
import io.siddhi.extension.io.grpc.util.ServiceConfigs;
import io.siddhi.extension.io.grpc.util.SourceServerInterceptor;
import io.siddhi.query.api.exception.SiddhiAppValidationException;
import org.apache.log4j.Logger;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.BindException;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.TrustManagerFactory;

import static io.siddhi.extension.io.grpc.util.GrpcUtils.getServiceName;

/**
 * This is an abstract class extended by GrpcSource and GrpcServiceSource. This provides most of initialization
 * implementations common for both sources
 */
public abstract class AbstractGrpcSource extends Source { //todo: have thread workers and handover to the worker after receiving from source
    protected SiddhiAppContext siddhiAppContext;
    protected SourceEventListener sourceEventListener;
//    private String url;
//    private String serviceName;
    protected boolean isDefaultMode;
//    private int port;
    protected SourceServerInterceptor serverInterceptor;
    protected NettyServerBuilder serverBuilder;
    protected long serverShutdownWaitingTimeInMillis = -1L;
    protected String streamID;
    private ServiceDeploymentInfo serviceDeploymentInfo;
    public static ThreadLocal<Map<String, String>> metaDataMap = new ThreadLocal<>();
//    private ServiceConfigs serviceConfigs;
    private GrpcDefaultServer server;

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
        this.sourceEventListener = sourceEventListener;
        if (optionHolder.isOptionExists(GrpcConstants.SERVER_SHUTDOWN_WAITING_TIME)) {
            this.serverShutdownWaitingTimeInMillis = Long.parseLong(optionHolder.validateAndGetOption(
                    GrpcConstants.SERVER_SHUTDOWN_WAITING_TIME).getValue());
        }
//        this.serviceConfigs = new ServiceConfigs(optionHolder, siddhiAppContext, streamID);
        initSource(optionHolder, requestedTransportPropertyNames);
        this.serverInterceptor = new SourceServerInterceptor(siddhiAppContext, streamID);
        server =


//        String truststoreFilePath = null;
//        String truststorePassword = null;
//        String keystoreFilePath = null;
//        String keystorePassword = null;
//        String truststoreAlgorithm = null;
//        String keystoreAlgorithm = null;
//        String tlsStoreType = null;
//
//        if (optionHolder.isOptionExists(GrpcConstants.KEYSTORE_FILE)) {
//            keystoreFilePath = optionHolder.validateAndGetOption(GrpcConstants.KEYSTORE_FILE).getValue();
//            keystorePassword = optionHolder.validateAndGetOption(GrpcConstants.KEYSTORE_PASSWORD).getValue();
//            keystoreAlgorithm = optionHolder.validateAndGetOption(GrpcConstants.KEYSTORE_ALGORITHM).getValue();
//            tlsStoreType = optionHolder.getOrCreateOption(GrpcConstants.TLS_STORE_TYPE,
//                    GrpcConstants.DEFAULT_TLS_STORE_TYPE).getValue();
//        }
//
//        if (optionHolder.isOptionExists(GrpcConstants.TRUSTSTORE_FILE)) {
//            if (!optionHolder.isOptionExists(GrpcConstants.KEYSTORE_FILE)) {
//                throw new SiddhiAppCreationException(siddhiAppContext.getName() + ":" + streamID + ": Truststore " +
//                        "configurations given without keystore configurations. Please provide keystore");
//            }
//            truststoreFilePath = optionHolder.validateAndGetOption(GrpcConstants.TRUSTSTORE_FILE).getValue();
//            if (optionHolder.isOptionExists(GrpcConstants.TRUSTSTORE_PASSWORD)) {
//                truststorePassword = optionHolder.validateAndGetOption(GrpcConstants.TRUSTSTORE_PASSWORD).getValue();
//            }
//            truststoreAlgorithm = optionHolder.validateAndGetOption(GrpcConstants.TRUSTSTORE_ALGORITHM).getValue();
//            tlsStoreType = optionHolder.getOrCreateOption(GrpcConstants.TLS_STORE_TYPE,
//                    GrpcConstants.DEFAULT_TLS_STORE_TYPE).getValue();
//        }

        //ServerBuilder parameters
//        serverBuilder = NettyServerBuilder.forPort(port);
//        if (keystoreFilePath != null) {
//            try {
//                SslContextBuilder sslContextBuilder = getSslContextBuilder(keystoreFilePath, keystorePassword,
//                        keystoreAlgorithm, tlsStoreType);
//                if (truststoreFilePath != null) {
//                    sslContextBuilder = addTrustStore(truststoreFilePath, truststorePassword, truststoreAlgorithm,
//                            sslContextBuilder, tlsStoreType).clientAuth(ClientAuth.REQUIRE);
//                }
//                serverBuilder.sslContext(sslContextBuilder.build());
//            } catch (IOException | CertificateException | NoSuchAlgorithmException | UnrecoverableKeyException |
//                    KeyStoreException e) {
//                throw new SiddhiAppCreationException(siddhiAppContext.getName() + ": " + streamID + ": Error while " +
//                        "creating SslContext. ", e);
//            }
//        }
//        serverBuilder.maxInboundMessageSize(Integer.parseInt(optionHolder.getOrCreateOption(
//                GrpcConstants.MAX_INBOUND_MESSAGE_SIZE, GrpcConstants.MAX_INBOUND_MESSAGE_SIZE_DEFAULT).getValue()));
//        serverBuilder.maxInboundMetadataSize(Integer.parseInt(optionHolder.getOrCreateOption(
//                GrpcConstants.MAX_INBOUND_METADATA_SIZE, GrpcConstants.MAX_INBOUND_METADATA_SIZE_DEFAULT).getValue()));

//        if (serviceName.equals(GrpcConstants.DEFAULT_SERVICE_NAME)) {
//                this.isDefaultMode = true;
//                initializeGrpcServer(port);
//        } else {
//
//        }
        this.serviceDeploymentInfo = new ServiceDeploymentInfo(port, truststoreFilePath != null ||
                keystoreFilePath != null);
        return null;
    }

    private SslContextBuilder getSslContextBuilder(String filePath, String password, String algorithm, String storeType)
            throws KeyStoreException, NoSuchAlgorithmException, CertificateException,
            UnrecoverableKeyException {
        char[] passphrase = password.toCharArray();
        KeyStore keyStore = KeyStore.getInstance(storeType);
        try (FileInputStream fis = new FileInputStream(filePath)) {
            keyStore.load(fis, passphrase);
        } catch (IOException e) {
            throw new SiddhiAppCreationException(siddhiAppContext.getName() + ": " + streamID + ": ", e);
        }
        KeyManagerFactory kmf = KeyManagerFactory.getInstance(algorithm);
        kmf.init(keyStore, passphrase);
        SslContextBuilder sslContextBuilder = SslContextBuilder.forServer(kmf);
        sslContextBuilder = GrpcSslContexts.configure(sslContextBuilder);
        return sslContextBuilder;
    }

    private SslContextBuilder addTrustStore(String filePath, String password, String algorithm,
                                            SslContextBuilder sslContextBuilder, String storeType)
            throws NoSuchAlgorithmException, KeyStoreException, CertificateException {
        char[] passphrase = password.toCharArray();
        KeyStore keyStore = KeyStore.getInstance(storeType);
        try (FileInputStream fis = new FileInputStream(filePath)) {
            keyStore.load(fis, passphrase);
        } catch (IOException e) {
            throw new SiddhiAppCreationException(siddhiAppContext.getName() + ": " + streamID + ": ", e);
        }
        TrustManagerFactory tmf = TrustManagerFactory.getInstance(algorithm);
        tmf.init(keyStore);
        return sslContextBuilder.trustManager(tmf).clientAuth(ClientAuth.REQUIRE);
    }

    public abstract void initializeGrpcServer(int port);

    public abstract void initSource(OptionHolder optionHolder, String[] requestedTransportPropertyNames);

    /**
     * Returns the list of classes which this source can output.
     *
     * @return Array of classes that will be output by the source.
     * Null or empty array if it can produce any type of class.
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

    public void connectGrpcServer(Server server, Logger logger, ConnectionCallback connectionCallback) {
        try {
            server.start();
            if (logger.isDebugEnabled()) {
                logger.debug(siddhiAppContext.getName() + ":" + streamID + ": gRPC Server started");
            }
        } catch (IOException e) {
            if (e.getCause() instanceof BindException) {
                throw new SiddhiAppValidationException(siddhiAppContext.getName() + ":" + streamID + ": Another " +
                        "server is already running on the port " + port + ". Please provide a different port");
            } else {
                connectionCallback.onError(new ConnectionUnavailableException(siddhiAppContext.getName() + ":" +
                        streamID + ": Error when starting the server. ", e));
            }
            throw new SiddhiAppRuntimeException(siddhiAppContext.getName() + ": " + streamID + ": ", e);
        }
    }

    public void disconnectGrpcServer(Server server, Logger logger) {
        try {
            if (server == null) {
                if (logger.isDebugEnabled()) {
                    logger.debug(siddhiAppContext.getName() + ":" + streamID + ": Illegal state. Server already " +
                            "stopped.");
                }
                return;
            }
            server.shutdown();
            if (serverShutdownWaitingTimeInMillis != -1L) {
                if (server.awaitTermination(serverShutdownWaitingTimeInMillis, TimeUnit.MILLISECONDS)) {
                    if (logger.isDebugEnabled()) {
                        logger.debug(siddhiAppContext.getName() + ": " + streamID + ": Server stopped");
                    }
                    return;
                }
                server.shutdownNow();
                if (server.awaitTermination(serverShutdownWaitingTimeInMillis, TimeUnit.SECONDS)) {
                    return;
                }
                throw new SiddhiAppRuntimeException(siddhiAppContext.getName() + ":" + streamID + ": Unable to " +
                        "shutdown server");
            }
        } catch (InterruptedException e) {
            throw new SiddhiAppRuntimeException(siddhiAppContext.getName() + ": " + streamID + ": ", e);
        }
    }
}
