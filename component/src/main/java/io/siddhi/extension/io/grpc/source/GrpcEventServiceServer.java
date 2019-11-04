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
import io.grpc.StatusRuntimeException;
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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.TrustManagerFactory;

/**
 * grpc server for default service. Sources can subscribe to this server with respective stream id
 */
public class GrpcEventServiceServer extends ServiceServer {
    private static final Logger logger = Logger.getLogger(GrpcEventServiceServer.class.getName());
    public static ThreadLocal<Map<String, String>> metaDataMap = new ThreadLocal<>();
    protected Server server;
    private NettyServerBuilder serverBuilder;
    private GrpcServerConfigs grpcServerConfigs;
    private SourceServerInterceptor serverInterceptor;
    private Map<String, GrpcSource> subscribersForConsume = new HashMap<>();
    private Map<String, GrpcServiceSource> subscribersForProcess = new HashMap<>();
    private int state = 0;
    private ExecutorService executorService;

    public GrpcEventServiceServer(GrpcServerConfigs grpcServerConfigs, SiddhiAppContext siddhiAppContext,
                                  String streamID) {
        this.serverInterceptor = new SourceServerInterceptor(grpcServerConfigs.getServiceConfigs().isDefaultService());
        this.grpcServerConfigs = grpcServerConfigs;
        super.lock = new ReentrantLock();
        super.condition = lock.newCondition();
        this.executorService = new ThreadPoolExecutor(grpcServerConfigs.getThreadPoolSize(),
                grpcServerConfigs.getThreadPoolSize(), 0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<>(grpcServerConfigs.getThreadPoolBufferSize()));
        setServerPropertiesToBuilder(siddhiAppContext.getName(), streamID);
        addServicesAndBuildServer(siddhiAppContext.getName(), streamID);
    }

    @Override
    protected void setServerPropertiesToBuilder(String siddhiAppName, String streamID) {
        serverBuilder = NettyServerBuilder.forPort(grpcServerConfigs.getServiceConfigs().getPort());
        if (grpcServerConfigs.getServiceConfigs().getKeystoreFilePath() != null) {
            try {
                SslContextBuilder sslContextBuilder = getSslContextBuilder(grpcServerConfigs.getServiceConfigs()
                                .getKeystoreFilePath(), grpcServerConfigs.getServiceConfigs().getKeystorePassword(),
                        grpcServerConfigs.getServiceConfigs().getKeystoreAlgorithm(), grpcServerConfigs
                                .getServiceConfigs().getTlsStoreType(), siddhiAppName, streamID);
                if (grpcServerConfigs.getServiceConfigs().getTruststoreFilePath() != null) {
                    sslContextBuilder = addTrustStore(grpcServerConfigs.getServiceConfigs().getTruststoreFilePath(),
                            grpcServerConfigs.getServiceConfigs().getTruststorePassword(), grpcServerConfigs
                                    .getServiceConfigs().getTruststoreAlgorithm(),
                            sslContextBuilder, grpcServerConfigs.getServiceConfigs().getTlsStoreType(),
                            siddhiAppName, streamID).clientAuth(ClientAuth.REQUIRE);
                }
                serverBuilder.sslContext(sslContextBuilder.build());
            } catch (IOException | CertificateException | NoSuchAlgorithmException | UnrecoverableKeyException |
                    KeyStoreException e) {
                throw new SiddhiAppCreationException(siddhiAppName + ": " + streamID + ": Error while " +
                        "creating SslContext. " + e.getMessage(), e);
            }
        }
        if (grpcServerConfigs.getMaxInboundMessageSize() != -1) {
            serverBuilder.maxInboundMessageSize(grpcServerConfigs.getMaxInboundMessageSize());
        }
        if (grpcServerConfigs.getMaxInboundMetadataSize() != -1) {
            serverBuilder.maxInboundMetadataSize(grpcServerConfigs.getMaxInboundMetadataSize());
        }
    }

    @Override
    protected void addServicesAndBuildServer(String siddhiAppName, String streamID) {
        this.server = serverBuilder.addService(ServerInterceptors.intercept(
                new EventServiceGrpc.EventServiceImplBase() {
                    @Override
                    public StreamObserver<Event> consume(StreamObserver<Empty> responseObserver) {
                        handlePause(logger);
                        return new StreamObserver<Event>() {
                            @Override
                            public void onNext(Event request) {
                                if (request.getPayload() == null) {
                                    logger.error(siddhiAppName + ":" + streamID + ": Dropping request " +
                                            "due to missing payload ");
                                    responseObserver.onError(new StatusRuntimeException(Status.DATA_LOSS));

                                } else if (!request.getHeadersMap().containsKey(GrpcConstants.STREAM_ID)) {
                                    logger.error(siddhiAppName + ":" + streamID + ": Dropping request " +
                                            "due to missing stream.id ");
                                    responseObserver.onError(new StatusRuntimeException(Status.DATA_LOSS));
                                } else if (!subscribersForConsume.containsKey(request.getHeadersMap().get(GrpcConstants
                                        .STREAM_ID))) {
                                    logger.error(siddhiAppName + ":" + streamID + ": Dropping request " +
                                            "because requested stream with stream.id " + request.getHeadersMap()
                                            .get("streamID") + " not subcribed to the gRPC server on port " +
                                            grpcServerConfigs.getServiceConfigs().getPort());
                                    responseObserver.onError(new StatusRuntimeException(Status.DATA_LOSS));
                                } else {

                                    try {
                                        GrpcSource relevantSource = subscribersForConsume.get(request.getHeadersMap()
                                                .get(GrpcConstants.STREAM_ID));
                                        executorService.execute(new GrpcWorkerThread(relevantSource,
                                                request.getPayload(), request.getHeadersMap(), metaDataMap.get()));
                                        responseObserver.onNext(Empty.getDefaultInstance());
                                        responseObserver.onCompleted();
                                    } catch (SiddhiAppRuntimeException e) {
                                        logger.error(siddhiAppName + ":" + streamID + ": Dropping " +
                                                "request. " + e.getMessage());
                                        responseObserver.onError(new StatusRuntimeException(Status.DATA_LOSS));
                                    } finally {
                                        metaDataMap.remove();
                                    }
                                }
                            }

                            @Override
                            public void onError(Throwable t) {

                            }

                            @Override
                            public void onCompleted() {

                            }
                        };
                    }

                    @Override
                    public void process(Event request,
                                        StreamObserver<Event> responseObserver) {
                        handlePause(logger);
                        if (request.getPayload() == null) {
                            logger.error(siddhiAppName + ":" + streamID + ": Dropping request due to " +
                                    "missing payload ");
                            responseObserver.onError(new StatusRuntimeException(Status.DATA_LOSS));
                        } else if (!request.getHeadersMap().containsKey(GrpcConstants.STREAM_ID)) {
                            logger.error(siddhiAppName + ":" + streamID + ": Dropping request due to " +
                                    "missing stream.id ");
                            responseObserver.onError(new StatusRuntimeException(Status.DATA_LOSS));
                        } else if (!subscribersForProcess.containsKey(request.getHeadersMap().get(GrpcConstants
                                .STREAM_ID))) {
                            logger.error(siddhiAppName + ":" + streamID + ": Dropping request because " +
                                    "requested stream with stream.id " + request.getHeadersMap().get(GrpcConstants
                                    .STREAM_ID) + " not subcribed to the gRPC server on port " +
                                    grpcServerConfigs.getServiceConfigs().getPort());
                            responseObserver.onError(new StatusRuntimeException(Status.DATA_LOSS));
                        } else {
                            String messageId = UUID.randomUUID().toString();
                            Map<String, String> transportPropertyMap = new HashMap<>();
                            transportPropertyMap.put(GrpcConstants.MESSAGE_ID, messageId);
                            transportPropertyMap.putAll(request.getHeadersMap());
                            try {
                                GrpcServiceSource relevantSource = subscribersForProcess.get(request.getHeadersMap()
                                        .get(GrpcConstants.STREAM_ID));
                                executorService.execute(new GrpcWorkerThread(relevantSource,
                                        request.getPayload(), transportPropertyMap, metaDataMap.get()));
                                relevantSource.putStreamObserver(messageId, responseObserver);
                                relevantSource.scheduleServiceTimeout(messageId);
                            } catch (SiddhiAppRuntimeException e) {
                                logger.error(siddhiAppName + ":" + streamID + ": Dropping request. "
                                        + e.getMessage(), e);
                                responseObserver.onError(new StatusRuntimeException(Status.DATA_LOSS));
                            } finally {
                                metaDataMap.remove();
                            }
                        }
                    }
                }, serverInterceptor)).build();
    }

    @Override
    protected void connectServer(Logger logger, Source.ConnectionCallback connectionCallback,
                                 String siddhiAppName, String streamID) {
        try {
            server.start();
            state = 1;
            if (logger.isDebugEnabled()) {
                logger.debug(siddhiAppName + ":" + streamID + ": gRPC Server started");
            }
        } catch (IOException e) {
            if (e.getCause() instanceof BindException) {
                throw new SiddhiAppValidationException(siddhiAppName + ":" + streamID + ": Another " +
                        "server is already running on the port " + grpcServerConfigs.getServiceConfigs().getPort() +
                        ". Please provide a different port");
            } else {
                connectionCallback.onError(new ConnectionUnavailableException(siddhiAppName + ":" +
                        streamID + ": Error when starting the server. " + e.getMessage(), e));
            }
            throw new SiddhiAppRuntimeException(siddhiAppName + ": " + streamID + ": ", e);
        }
    }

    @Override
    public void disconnectServer(Logger logger, String siddhiAppName, String streamID) {
        try {
            if (server == null) {
                if (logger.isDebugEnabled()) {
                    logger.debug(siddhiAppName + ":" + streamID + ": Illegal state. Server already " +
                            "stopped.");
                }
                return;
            }
            server.shutdown();
            if (getGrpcServerConfigs().getServerShutdownWaitingTimeInMillis() > 0) {
                if (server.awaitTermination(getGrpcServerConfigs().getServerShutdownWaitingTimeInMillis(),
                        TimeUnit.MILLISECONDS)) {
                    if (logger.isDebugEnabled()) {
                        logger.debug(siddhiAppName + ": " + streamID + ": Server stopped");
                    }
                    return;
                }
                server.shutdownNow();
                if (server.awaitTermination(getGrpcServerConfigs().getServerShutdownWaitingTimeInMillis(),
                        TimeUnit.MILLISECONDS)) {
                    return;
                }
                throw new SiddhiAppRuntimeException(siddhiAppName + ":" + streamID + ": Unable to " +
                        "shutdown server");
            }
            state = 2;
        } catch (InterruptedException e) {
            throw new SiddhiAppRuntimeException(siddhiAppName + ": " + streamID + ": " + e.getMessage(),
                    e);
        }
    }

    @Override
    protected SslContextBuilder getSslContextBuilder(String filePath, String password, String algorithm,
                                                     String storeType,
                                                     String siddhiAppName, String streamID)
            throws KeyStoreException, NoSuchAlgorithmException, CertificateException,
            UnrecoverableKeyException {
        char[] passphrase = password.toCharArray();
        KeyStore keyStore = KeyStore.getInstance(storeType);
        try (FileInputStream fis = new FileInputStream(filePath)) {
            keyStore.load(fis, passphrase);
        } catch (IOException e) {
            throw new SiddhiAppCreationException(siddhiAppName + ": " + streamID + ": " + e.getMessage(),
                    e);
        }
        KeyManagerFactory kmf = KeyManagerFactory.getInstance(algorithm);
        kmf.init(keyStore, passphrase);
        SslContextBuilder sslContextBuilder = SslContextBuilder.forServer(kmf);
        sslContextBuilder = GrpcSslContexts.configure(sslContextBuilder);
        return sslContextBuilder;
    }

    @Override
    protected SslContextBuilder addTrustStore(String filePath, String password, String algorithm,
                                              SslContextBuilder sslContextBuilder, String storeType,
                                              String siddhiAppName, String streamID)
            throws NoSuchAlgorithmException, KeyStoreException, CertificateException {
        char[] passphrase = password.toCharArray();
        KeyStore keyStore = KeyStore.getInstance(storeType);
        try (FileInputStream fis = new FileInputStream(filePath)) {
            keyStore.load(fis, passphrase);
        } catch (IOException e) {
            throw new SiddhiAppCreationException(siddhiAppName + ": " + streamID + ": " + e.getMessage(),
                    e);
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
