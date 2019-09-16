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

import com.google.protobuf.Any;
import com.google.protobuf.ByteString;
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
import io.siddhi.extension.io.grpc.util.GenericService;
import io.siddhi.extension.io.grpc.util.GrpcConstants;
import io.siddhi.extension.io.grpc.util.GrpcServerConfigs;
import io.siddhi.extension.io.grpc.util.SourceServerInterceptor;
import io.siddhi.query.api.exception.SiddhiAppValidationException;
import org.apache.log4j.Logger;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.TrustManagerFactory;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.BindException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static io.siddhi.extension.io.grpc.util.GrpcUtils.getRpcMethodList;

/**
 * grpc server for generic service, create separated servers for each sources.
 */
public class GenericServiceServer {
    private static final Logger logger = Logger.getLogger(GenericServiceServer.class.getName());
    public static ThreadLocal<Map<String, String>> metaDataMap = new ThreadLocal<>();
    private Server server;
    private NettyServerBuilder serverBuilder;
    private GrpcServerConfigs grpcServerConfigs;
    private SourceServerInterceptor serverInterceptor;
    private ExecutorService executorService;
    private AbstractGrpcSource relevantSource;
    private Class requestClass;

    public GenericServiceServer(GrpcServerConfigs grpcServerConfigs, AbstractGrpcSource relevantSource,
                                Class requestClass, String siddhiAppName, String streamID) {
        this.serverInterceptor = new SourceServerInterceptor(grpcServerConfigs.getServiceConfigs().isDefaultService());
        this.grpcServerConfigs = grpcServerConfigs;
        this.relevantSource = relevantSource;
        this.requestClass = requestClass;
        this.executorService = new ThreadPoolExecutor(grpcServerConfigs.getThreadPoolSize(),
                grpcServerConfigs.getThreadPoolSize(), 0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<>(grpcServerConfigs.getThreadPoolBufferSize()));
        setServerPropertiesToBuilder(siddhiAppName, streamID);
        addServicesAndBuildServer(siddhiAppName, streamID);
    }

    private void setServerPropertiesToBuilder(String siddhiAppName, String streamID) {
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

    private void addServicesAndBuildServer(String siddhiAppName, String streamID) {
        this.server = serverBuilder.addService(ServerInterceptors.intercept(
                new GenericService.AnyServiceImplBase() {
                    @Override
                    public void handleEmptyResponse(Any request, StreamObserver<Empty> responseObserver) {
                        try {
                            Object requestMessageObject = requestClass.getDeclaredMethod(GrpcConstants.
                                    PARSE_FROM_METHOD_NAME, ByteString.class).invoke(requestClass, request.
                                    toByteString());
                            executorService.execute(new GrpcWorkerThread(relevantSource, requestMessageObject,
                                    metaDataMap.get()));
                            responseObserver.onNext(Empty.getDefaultInstance());
                            responseObserver.onCompleted();
                        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                            throw new SiddhiAppValidationException(siddhiAppName + ":" + streamID + ": Invalid method" +
                                    " name provided in the url, provided method name: " + grpcServerConfigs.
                                    getServiceConfigs().getMethodName() + ", Expected one of these these methods: " +
                                    getRpcMethodList(grpcServerConfigs.getServiceConfigs(), siddhiAppName, streamID),
                                    e);
                        } catch (SiddhiAppRuntimeException e) {
                            logger.error(siddhiAppName + ":" + streamID + ": Dropping request. " + e.getMessage());
                            responseObserver.onError(new io.grpc.StatusRuntimeException(Status.DATA_LOSS));
                        }
                    }

                    @Override
                    public StreamObserver<Any> clientStream(StreamObserver<Empty> responseObserver) {
                        return new StreamObserver<Any>() {
                            @Override
                            public void onNext(Any value) {
                                try {
                                    Object requestMessageObject = requestClass.getDeclaredMethod(GrpcConstants.
                                            PARSE_FROM_METHOD_NAME, ByteString.class).invoke(requestClass, value.
                                            toByteString());
                                    executorService.execute(new GrpcWorkerThread(relevantSource, requestMessageObject,
                                            metaDataMap.get()));
                                } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                                    throw new SiddhiAppValidationException(siddhiAppName + ":" + streamID + ": Invalid method" +
                                            " name provided in the url, provided method name: " + grpcServerConfigs.
                                            getServiceConfigs().getMethodName() + ", Expected one of these these methods: " +
                                            getRpcMethodList(grpcServerConfigs.getServiceConfigs(), siddhiAppName, streamID),
                                            e);
                                } catch (SiddhiAppRuntimeException e) {
                                    logger.error(siddhiAppName + ":" + streamID + ": Dropping request. " + e.getMessage());
                                    responseObserver.onError(new io.grpc.StatusRuntimeException(Status.DATA_LOSS));
                                }
                            }

                            @Override
                            public void onError(Throwable t) {

                            }

                            @Override
                            public void onCompleted() {
                                responseObserver.onNext(Empty.getDefaultInstance());
                                responseObserver.onCompleted();
                            }
                        };
                    }
                }, serverInterceptor)).build();
    }

    public void connectServer(Logger logger, Source.ConnectionCallback connectionCallback,
                              String siddhiAppName, String streamID) {
        try {
            server.start();
            if (logger.isDebugEnabled()) {
                logger.debug(siddhiAppName + ":" + streamID + ": gRPC Server started");
            }
        } catch (IOException e) {
            if (e.getCause() instanceof BindException) {
                throw new SiddhiAppValidationException(siddhiAppName + ":" + streamID + ": Another " +
                        "server is already running on the port " + grpcServerConfigs.getServiceConfigs().getPort() +
                        ". Please provide a different port");
            } else {
                connectionCallback.onError(new ConnectionUnavailableException(siddhiAppName + ":" + streamID +
                        ": Error when starting the server. " + e.getMessage(), e));
            }
            throw new SiddhiAppRuntimeException(siddhiAppName + ": " + streamID + ": ", e);
        }
    }

    public void disconnectServer(Logger logger, String siddhiAppName, String streamID) {
        try {
            if (server == null) {
                if (logger.isDebugEnabled()) {
                    logger.debug(siddhiAppName + ":" + streamID + ": Illegal state. Server already stopped.");
                }
                return;
            }
            server.shutdown();
            if (grpcServerConfigs.getServerShutdownWaitingTimeInMillis() > 0) {
                if (server.awaitTermination(grpcServerConfigs.getServerShutdownWaitingTimeInMillis(),
                        TimeUnit.MILLISECONDS)) {
                    if (logger.isDebugEnabled()) {
                        logger.debug(siddhiAppName+ ": " + streamID + ": Server stopped");
                    }
                    return;
                }
                server.shutdownNow();
                if (server.awaitTermination(grpcServerConfigs.getServerShutdownWaitingTimeInMillis(),
                        TimeUnit.MILLISECONDS)) {
                    return;
                }
                throw new SiddhiAppRuntimeException(siddhiAppName + ":" + streamID + ": Unable to shutdown server");
            }
        } catch (InterruptedException e) {
            throw new SiddhiAppRuntimeException(siddhiAppName + ": " + streamID + ": " + e.getMessage(), e);
        }
    }

    private SslContextBuilder getSslContextBuilder(String filePath, String password, String algorithm, String storeType,
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

    private SslContextBuilder addTrustStore(String filePath, String password, String algorithm,
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


}
