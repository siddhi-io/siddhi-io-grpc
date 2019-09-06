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
import io.grpc.netty.shaded.io.grpc.netty.GrpcSslContexts;
import io.grpc.netty.shaded.io.grpc.netty.NettyServerBuilder;
import io.grpc.netty.shaded.io.netty.handler.ssl.ClientAuth;
import io.grpc.netty.shaded.io.netty.handler.ssl.SslContextBuilder;
import io.grpc.stub.StreamObserver;
import io.siddhi.core.config.SiddhiAppContext;
import io.siddhi.core.exception.ConnectionUnavailableException;
import io.siddhi.core.exception.SiddhiAppCreationException;
import io.siddhi.core.exception.SiddhiAppRuntimeException;
import io.siddhi.core.stream.input.source.Source;
import io.siddhi.extension.io.grpc.util.GrpcConstants;
import io.siddhi.extension.io.grpc.util.GrpcServerConfigs;
import io.siddhi.extension.io.grpc.util.SourceServerInterceptor;
import io.siddhi.query.api.exception.SiddhiAppValidationException;
import org.apache.log4j.Logger;
import org.wso2.grpc.Event;
import org.wso2.grpc.EventServiceGrpc;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.TrustManagerFactory;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.BindException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static io.siddhi.extension.io.grpc.util.GrpcUtils.extractHeaders;

public class GrpcEventServiceServer {
    private static final Logger logger = Logger.getLogger(GrpcEventServiceServer.class.getName());
    protected Server server;
    private NettyServerBuilder serverBuilder;
    private GrpcServerConfigs grpcServerConfigs;
    private SourceServerInterceptor serverInterceptor;
    public static ThreadLocal<Map<String, String>> metaDataMap = new ThreadLocal<>();
    private Map<String, GrpcSource> subscribersForConsume = new HashMap<>();
    private Map<String, GrpcServiceSource> subscribersForProcess = new HashMap<>();
    private int state = 0;

    public GrpcEventServiceServer(GrpcServerConfigs grpcServerConfigs, SiddhiAppContext siddhiAppContext,
                                  String streamID) {
        this.serverInterceptor = new SourceServerInterceptor();
        this.grpcServerConfigs = grpcServerConfigs;
        setServerPropertiesToBuilder(siddhiAppContext, streamID);
        addServicesAndBuildServer(siddhiAppContext, streamID);
    }

    public void setServerPropertiesToBuilder(SiddhiAppContext siddhiAppContext, String streamID) {
        serverBuilder = NettyServerBuilder.forPort(grpcServerConfigs.getServiceConfigs().getPort());
        if (grpcServerConfigs.getKeystoreFilePath() != null) {
            try {
                SslContextBuilder sslContextBuilder = getSslContextBuilder(grpcServerConfigs.getKeystoreFilePath(),
                        grpcServerConfigs.getKeystorePassword(),
                        grpcServerConfigs.getKeystoreAlgorithm(), grpcServerConfigs.getTlsStoreType(), siddhiAppContext,
                        streamID);
                if (grpcServerConfigs.getTruststoreFilePath() != null) {
                    sslContextBuilder = addTrustStore(grpcServerConfigs.getTruststoreFilePath(),
                            grpcServerConfigs.getTruststorePassword(), grpcServerConfigs.getTruststoreAlgorithm(),
                            sslContextBuilder, grpcServerConfigs.getTlsStoreType(), siddhiAppContext, streamID)
                            .clientAuth(ClientAuth.REQUIRE);
                }
                serverBuilder.sslContext(sslContextBuilder.build());
            } catch (IOException | CertificateException | NoSuchAlgorithmException | UnrecoverableKeyException |
                    KeyStoreException e) {
                throw new SiddhiAppCreationException(siddhiAppContext.getName() + ": " + streamID + ": Error while " +
                        "creating SslContext. ", e);
            }
        }
        if (grpcServerConfigs.getMaxInboundMessageSize() != -1) {
            serverBuilder.maxInboundMessageSize(grpcServerConfigs.getMaxInboundMessageSize());
        }
        if (grpcServerConfigs.getMaxInboundMetadataSize() != -1) {
            serverBuilder.maxInboundMetadataSize(grpcServerConfigs.getMaxInboundMetadataSize());
        }

    }

    public void addServicesAndBuildServer(SiddhiAppContext siddhiAppContext, String streamID) {
        this.server = serverBuilder.addService(ServerInterceptors.intercept(
                new EventServiceGrpc.EventServiceImplBase() {
                    @Override
                    public void consume(Event request,
                                        StreamObserver<Empty> responseObserver) {
                        if (request.getPayload() == null) {
                            logger.error(siddhiAppContext.getName() + ":" + streamID + ": Dropping request due to " +
                                    "missing payload ");
                            responseObserver.onError(new io.grpc.StatusRuntimeException(Status.DATA_LOSS));

                        } else if (!request.getHeadersMap().containsKey(GrpcConstants.STREAM_ID)) {
                            logger.error(siddhiAppContext.getName() + ":" + streamID + ": Dropping request due to " +
                                    "missing stream.id ");
                            responseObserver.onError(new io.grpc.StatusRuntimeException(Status.DATA_LOSS));
                        } else if (!subscribersForConsume.containsKey(request.getHeadersMap().get(GrpcConstants
                                .STREAM_ID))) {
                            logger.error(siddhiAppContext.getName() + ":" + streamID + ": Dropping request because " +
                                    "requested stream with stream.id " + request.getHeadersMap().get("streamID") +
                                    " not subcribed to the gRPC server on port " +
                                    grpcServerConfigs.getServiceConfigs().getPort());
                            responseObserver.onError(new io.grpc.StatusRuntimeException(Status.DATA_LOSS));
                        } else {
                            logger.error("server thread is: " + Thread.currentThread().getId());
                            try {
                                GrpcSource relevantSource = subscribersForConsume.get(request.getHeadersMap()
                                        .get(GrpcConstants.STREAM_ID));
                                GrpcWorkerThread sourceWorker = new GrpcWorkerThread(relevantSource, request, metaDataMap.get(), responseObserver);
                                sourceWorker.run();
//                                relevantSource.handleInjection(request.getPayload(), extractHeaders(request
//                                                .getHeadersMap(), metaDataMap.get(),
//                                        relevantSource.getRequestedTransportPropertyNames())); //todo: do this onEvent in a worker thread. user set threadpool parameter and buffer size
                                responseObserver.onNext(Empty.getDefaultInstance());
                                responseObserver.onCompleted();
                            } catch (SiddhiAppRuntimeException e) {
                                logger.error(siddhiAppContext.getName() + ":" + streamID + ": Dropping request. "
                                        + e.getMessage());
                                responseObserver.onError(new io.grpc.StatusRuntimeException(Status.DATA_LOSS));
                            } finally {
                                metaDataMap.remove();
                            }
                        }
                    }

                    @Override
                    public void process(Event request,
                                        StreamObserver<Event> responseObserver) {
                        if (request.getPayload() == null) {
                            logger.error(siddhiAppContext.getName() + ":" + streamID + ": Dropping request due to " +
                                    "missing payload ");
                            responseObserver.onError(new io.grpc.StatusRuntimeException(Status.DATA_LOSS));
                        } else if (!request.getHeadersMap().containsKey(GrpcConstants.STREAM_ID)) {
                            logger.error(siddhiAppContext.getName() + ":" + streamID + ": Dropping request due to " +
                                    "missing stream.id ");
                            responseObserver.onError(new io.grpc.StatusRuntimeException(Status.DATA_LOSS));
                        } else if (!subscribersForProcess.containsKey(request.getHeadersMap().get(GrpcConstants
                                .STREAM_ID))) {
                            logger.error(siddhiAppContext.getName() + ":" + streamID + ": Dropping request because " +
                                    "requested stream with stream.id " + request.getHeadersMap().get(GrpcConstants
                                    .STREAM_ID) + " not subcribed to the gRPC server on port " +
                                    grpcServerConfigs.getServiceConfigs().getPort());
                            responseObserver.onError(new io.grpc.StatusRuntimeException(Status.DATA_LOSS));
                        } else {
                            String messageId = UUID.randomUUID().toString();
                            Map<String, String> transportPropertyMap = new HashMap<>();
                            transportPropertyMap.put(GrpcConstants.MESSAGE_ID, messageId);
                            transportPropertyMap.putAll(request.getHeadersMap());
                            try {
                                GrpcServiceSource relevantSource = subscribersForProcess.get(request.getHeadersMap()
                                        .get(GrpcConstants.STREAM_ID));
                                relevantSource.handleInjection(request.getPayload(), extractHeaders(
                                        transportPropertyMap, metaDataMap.get(), relevantSource
                                                .getRequestedTransportPropertyNames()));
                                relevantSource.putStreamObserver(messageId, responseObserver);
                                relevantSource.scheduleServiceTimeout(messageId);
                            } catch (SiddhiAppRuntimeException e) {
                                logger.error(siddhiAppContext.getName() + ":" + streamID + ": Dropping request. "
                                        + e.getMessage());
                                responseObserver.onError(new io.grpc.StatusRuntimeException(Status.DATA_LOSS));
                            } finally {
                                metaDataMap.remove();
                            }
                        }
                    }
                }, serverInterceptor)).build();
    }

    public void connectServer(Logger logger, Source.ConnectionCallback connectionCallback,
                              SiddhiAppContext siddhiAppContext, String streamID) {
        try {
            server.start();
            state = 1;
            if (logger.isDebugEnabled()) {
                logger.debug(siddhiAppContext.getName() + ":" + streamID + ": gRPC Server started");
            }
        } catch (IOException e) {
            if (e.getCause() instanceof BindException) {
                throw new SiddhiAppValidationException(siddhiAppContext.getName() + ":" + streamID + ": Another " +
                        "server is already running on the port " + grpcServerConfigs.getServiceConfigs().getPort() +
                        ". Please provide a different port");
            } else {
                connectionCallback.onError(new ConnectionUnavailableException(siddhiAppContext.getName() + ":" +
                        streamID + ": Error when starting the server. ", e));
            }
            throw new SiddhiAppRuntimeException(siddhiAppContext.getName() + ": " + streamID + ": ", e);
        }
    }

    public void disconnectServer(Logger logger, SiddhiAppContext siddhiAppContext, String streamID) {
        try {
            if (server == null) {
                if (logger.isDebugEnabled()) {
                    logger.debug(siddhiAppContext.getName() + ":" + streamID + ": Illegal state. Server already " +
                            "stopped.");
                }
                return;
            }
            server.shutdown();
            if (getGrpcServerConfigs().getServerShutdownWaitingTimeInMillis() > 0) {
                if (server.awaitTermination(getGrpcServerConfigs().getServerShutdownWaitingTimeInMillis(),
                        TimeUnit.MILLISECONDS)) {
                    if (logger.isDebugEnabled()) {
                        logger.debug(siddhiAppContext.getName() + ": " + streamID + ": Server stopped");
                    }
                    return;
                }
                server.shutdownNow();
                if (server.awaitTermination(getGrpcServerConfigs().getServerShutdownWaitingTimeInMillis(),
                        TimeUnit.MILLISECONDS)) {
                    return;
                }
                throw new SiddhiAppRuntimeException(siddhiAppContext.getName() + ":" + streamID + ": Unable to " +
                        "shutdown server");
            }
            state = 2;
        } catch (InterruptedException e) {
            throw new SiddhiAppRuntimeException(siddhiAppContext.getName() + ": " + streamID + ": ", e);
        }
    }

    private SslContextBuilder getSslContextBuilder(String filePath, String password, String algorithm, String storeType,
                                                   SiddhiAppContext siddhiAppContext, String streamID)
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
                                            SslContextBuilder sslContextBuilder, String storeType,
                                            SiddhiAppContext siddhiAppContext, String streamID)
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

    public GrpcServerConfigs getGrpcServerConfigs() {
        return grpcServerConfigs;
    }

    public void subscribe(String streamID, AbstractGrpcSource source, String methodName,
                          SiddhiAppContext siddhiAppContext) {
        if (methodName.equalsIgnoreCase(GrpcConstants.DEFAULT_METHOD_NAME_WITHOUT_RESPONSE)) {
            if (source instanceof GrpcSource) {
                subscribersForConsume.putIfAbsent(streamID, (GrpcSource) source);
            }
        } else if (methodName.equalsIgnoreCase(GrpcConstants.DEFAULT_METHOD_NAME_WITH_RESPONSE)) {
            if (source instanceof GrpcServiceSource) {
                subscribersForProcess.putIfAbsent(streamID, (GrpcServiceSource) source);
            }
        } else {
            throw new SiddhiAppValidationException(siddhiAppContext.getName() + ": " + streamID + ": method name " +
                    "should be either process or consume but given as " + methodName);
        }
    }

    public void unsubscribe(String streamID, String methodName, SiddhiAppContext siddhiAppContext) {
        if (methodName.equalsIgnoreCase(GrpcConstants.DEFAULT_METHOD_NAME_WITHOUT_RESPONSE)) {
            subscribersForConsume.remove(streamID);
        } else if (methodName.equalsIgnoreCase(GrpcConstants.DEFAULT_METHOD_NAME_WITH_RESPONSE)) {
            subscribersForProcess.remove(streamID);
        } else {
            throw new SiddhiAppValidationException(siddhiAppContext.getName() + ": " + streamID + ": method name " +
                    "should be either process or consume but given as " + methodName);
        }
    }

    public boolean isShutDown() {
        return server.isShutdown();
    }

    public int getState() {
        return state;
    }

    public int getNumSubscribers() {
        return subscribersForConsume.size() + subscribersForProcess.size();
    }
}
