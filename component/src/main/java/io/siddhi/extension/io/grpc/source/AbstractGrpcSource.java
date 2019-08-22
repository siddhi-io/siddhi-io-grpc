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
import io.siddhi.core.exception.SiddhiAppCreationException;
import io.siddhi.core.exception.SiddhiAppRuntimeException;
import io.siddhi.core.stream.ServiceDeploymentInfo;
import io.siddhi.core.stream.input.source.Source;
import io.siddhi.core.stream.input.source.SourceEventListener;
import io.siddhi.core.util.config.ConfigReader;
import io.siddhi.core.util.snapshot.state.StateFactory;
import io.siddhi.core.util.transport.OptionHolder;
import io.siddhi.extension.io.grpc.util.GrpcConstants;
import io.siddhi.extension.io.grpc.util.SourceServerInterceptor;
import io.siddhi.query.api.exception.SiddhiAppValidationException;
import org.apache.log4j.Logger;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.TrustManagerFactory;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static io.siddhi.extension.io.grpc.util.GrpcUtils.getServiceName;

/**
 * This is an abstract class extended by GrpcSource and GrpcServiceSource. This provides most of initialization
 * implementations common for both sources
 */
public abstract class AbstractGrpcSource extends Source { //todo: one source url for multiple streams by keeping a map of dummy sources
    protected SiddhiAppContext siddhiAppContext;
    protected SourceEventListener sourceEventListener;
    private String url;
    private String serviceName;
    protected boolean isDefaultMode;
    private int port;
    protected SourceServerInterceptor serverInterceptor;
    protected NettyServerBuilder serverBuilder;
    protected long serverShutdownWaitingTimeInMillis = -1L;
    protected String streamID;
    private ServiceDeploymentInfo serviceDeploymentInfo;
    private String truststoreFilePath;
    private String truststorePassword;
    private String keystoreFilePath;
    private String keystorePassword;
    private String truststoreAlgorithm;
    private String keystoreAlgorithm;
    public static ThreadLocal<Map<String, String>> metaDataMap = new ThreadLocal<>();

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
        this.url = optionHolder.validateAndGetOption(GrpcConstants.PUBLISHER_URL).getValue();
        if (!url.substring(0, 4).equalsIgnoreCase(GrpcConstants.GRPC_PROTOCOL_NAME)) {
            throw new SiddhiAppValidationException(siddhiAppContext.getName() + ":" + streamID + ": The url must " +
                    "begin with \"" + GrpcConstants.GRPC_PROTOCOL_NAME + "\" for all grpc sinks");
        }
        URL aURL;
        try {
            aURL = new URL(GrpcConstants.DUMMY_PROTOCOL_NAME + url.substring(4));
        } catch (MalformedURLException e) {
            throw new SiddhiAppValidationException(siddhiAppContext.getName() + ":" + streamID +
                    ": Error in URL format. Expected format is `grpc://0.0.0.0:9763/<serviceName>/<methodName>` but " +
                    "the provided url is " + url + ". " + e.getMessage());
        }
        this.serviceName = getServiceName(aURL.getPath());
        this.port = aURL.getPort();
        initSource(optionHolder, requestedTransportPropertyNames);
        this.serverInterceptor = new SourceServerInterceptor(siddhiAppContext, streamID);

        if (optionHolder.isOptionExists(GrpcConstants.KEYSTORE_FILE)) {
            this.keystoreFilePath = optionHolder.validateAndGetOption(GrpcConstants.KEYSTORE_FILE).getValue();
            this.keystorePassword = optionHolder.validateAndGetOption(GrpcConstants.KEYSTORE_PASSWORD).getValue();
            this.keystoreAlgorithm = optionHolder.validateAndGetOption(GrpcConstants.KEYSTORE_ALGORITHM).getValue();
        }

        if (optionHolder.isOptionExists(GrpcConstants.TRUSTSTORE_FILE)) {
            if (!optionHolder.isOptionExists(GrpcConstants.KEYSTORE_FILE)) {
                throw new SiddhiAppCreationException(siddhiAppContext.getName() + ":" + streamID + ": Truststore " +
                        "configurations given without keystore configurations. Please provide keystore");
            }
            this.truststoreFilePath = optionHolder.validateAndGetOption(GrpcConstants.TRUSTSTORE_FILE).getValue();
            this.truststorePassword = optionHolder.validateAndGetOption(GrpcConstants.TRUSTSTORE_PASSWORD).getValue();
            this.truststoreAlgorithm = optionHolder.validateAndGetOption(GrpcConstants.TRUSTSTORE_ALGORITHM).getValue();
        }

        //ServerBuilder parameters
        serverBuilder = NettyServerBuilder.forPort(port);
        if (keystoreFilePath != null) {
            try {
                SslContextBuilder sslContextBuilder = getSslContextBuilder(keystoreFilePath, keystorePassword,
                        keystoreAlgorithm);
                if (truststoreFilePath != null) {
                    sslContextBuilder = addTrustStore(truststoreFilePath, truststorePassword, truststoreAlgorithm,
                            sslContextBuilder).clientAuth(ClientAuth.REQUIRE);
                }
                serverBuilder.sslContext(sslContextBuilder.build());
            } catch (IOException | CertificateException | NoSuchAlgorithmException | UnrecoverableKeyException | KeyStoreException e) {
                throw new SiddhiAppCreationException(siddhiAppContext.getName() + ": " + streamID + ": Error while " +
                        "creating SslContext. " + e.getMessage());
            }
        }
        serverBuilder.maxInboundMessageSize(Integer.parseInt(optionHolder.getOrCreateOption(
                GrpcConstants.MAX_INBOUND_MESSAGE_SIZE, GrpcConstants.MAX_INBOUND_MESSAGE_SIZE_DEFAULT).getValue()));
        serverBuilder.maxInboundMetadataSize(Integer.parseInt(optionHolder.getOrCreateOption(
                GrpcConstants.MAX_INBOUND_METADATA_SIZE, GrpcConstants.MAX_INBOUND_METADATA_SIZE_DEFAULT).getValue()));

        if (serviceName.equals(GrpcConstants.DEFAULT_SERVICE_NAME)) {
                this.isDefaultMode = true;
                initializeGrpcServer(port);
        } else {

        }
        this.serviceDeploymentInfo = new ServiceDeploymentInfo(port, false);
        return null;
    }

    private SslContextBuilder getSslContextBuilder(String JKSPath, String password, String algorithm)
            throws KeyStoreException, IOException, NoSuchAlgorithmException, CertificateException,
            UnrecoverableKeyException {
        char[] passphrase = password.toCharArray();
        KeyStore keyStore = KeyStore.getInstance(GrpcConstants.DEFAULT_KEYSTORE_TYPE);
        keyStore.load(new FileInputStream(JKSPath), passphrase);
        KeyManagerFactory kmf = KeyManagerFactory.getInstance(algorithm);
        kmf.init(keyStore, passphrase);
        SslContextBuilder sslContextBuilder = SslContextBuilder.forServer(kmf);
        sslContextBuilder = GrpcSslContexts.configure(sslContextBuilder);
        return sslContextBuilder;
    }

    private SslContextBuilder addTrustStore(String JKSPath, String password, String algorithm,
                                            SslContextBuilder sslContextBuilder) throws NoSuchAlgorithmException,
            KeyStoreException, IOException, CertificateException {
        char[] passphrase = password.toCharArray();
        KeyStore keyStore = KeyStore.getInstance(GrpcConstants.DEFAULT_KEYSTORE_TYPE);
        keyStore.load(new FileInputStream(JKSPath), passphrase);
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

    public void connectGrpcServer(Server server, Logger logger) {
        try {
            server.start();
            if (logger.isDebugEnabled()) {
                logger.debug(siddhiAppContext.getName() + ":" + streamID + ": gRPC Server started");
            }
        } catch (IOException e) {
            throw new SiddhiAppRuntimeException(siddhiAppContext.getName() + ":" + streamID + ": " + e.getMessage());
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
                        logger.debug(siddhiAppContext.getName() + ":" + streamID + ": Server stopped");
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
            throw new SiddhiAppRuntimeException(siddhiAppContext.getName() + ":" + streamID + ": " + e.getMessage());
        }
    }
}
