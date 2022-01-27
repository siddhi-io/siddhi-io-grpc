/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package io.siddhi.extension.io.grpc.utils;

import com.google.protobuf.Empty;
import io.grpc.Server;
import io.grpc.netty.shaded.io.grpc.netty.GrpcSslContexts;
import io.grpc.netty.shaded.io.grpc.netty.NettyServerBuilder;
import io.grpc.netty.shaded.io.netty.handler.ssl.ClientAuth;
import io.grpc.netty.shaded.io.netty.handler.ssl.SslContext;
import io.grpc.netty.shaded.io.netty.handler.ssl.SslContextBuilder;
import io.grpc.stub.StreamObserver;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.wso2.grpc.Event;
import org.wso2.grpc.EventServiceGrpc;

import java.io.FileInputStream;
import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.concurrent.TimeUnit;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.TrustManagerFactory;

/**
 * TLS server that required to authentication test cases.
 */
public class TestTLSServer {
    private static final Logger logger = LogManager.getLogger(TestTLSServer.class);
    private Server server;
    private TestServerInterceptor testInterceptor = new TestServerInterceptor();
    private int port;
    private KeyStore keyStore;
    private boolean isMutualAuth;

    public TestTLSServer(int port, boolean isMutualAuth) throws KeyStoreException {
        this.port = port;
        keyStore = KeyStore.getInstance("JKS");
        this.isMutualAuth = isMutualAuth;
    }

    private SslContext getCarbonSslContext() {
        char[] passphrase = "wso2carbon".toCharArray();

        try {
            keyStore.load(new FileInputStream(System.getProperty("carbon.home") + "/resources/security/wso2carbon.jks"),
                    passphrase);
            KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
            kmf.init(keyStore, passphrase);
            SslContextBuilder sslClientContextBuilder = SslContextBuilder.forServer(kmf);
            sslClientContextBuilder = GrpcSslContexts.configure(sslClientContextBuilder);
            if (isMutualAuth) {
                TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
                tmf.init(keyStore);
                sslClientContextBuilder = sslClientContextBuilder.trustManager(tmf).clientAuth(ClientAuth.REQUIRE);
            }
            SslContext sslContext = sslClientContextBuilder.build();
            return sslContext;
        } catch (IOException | NoSuchAlgorithmException | CertificateException | UnrecoverableKeyException |
                KeyStoreException e) {
        }
        return null;
    }

    public void start() throws IOException {
        if (server != null) {
            throw new IllegalStateException("Already started");
        }
        server = NettyServerBuilder
                .forPort(port)
                .addService(new EventServiceGrpc.EventServiceImplBase() {
                            @Override
                            public void process(Event request,
                                                StreamObserver<Event> responseObserver) {
                                if (logger.isDebugEnabled()) {
                                    logger.debug("Server process hit with payload = " + request.getPayload() +
                                            " and Headers = {" + request.getHeadersMap().toString() + "}");
                                }
                                Event.Builder responseBuilder = Event.newBuilder();
                                String json = "{ \"message\": \"Hello from Server!\"}";
                                responseBuilder.setPayload(json);
                                Event response = responseBuilder.build();
                                responseObserver.onNext(response);
                                responseObserver.onCompleted();
                            }

                    @Override
                    public StreamObserver<Event> consume(StreamObserver<Empty> responseObserver) {
                        return new StreamObserver<Event>() {
                            @Override
                            public void onNext(Event request) {
                                if (logger.isDebugEnabled()) {
                                    logger.debug("Server consume hit with payload = " + request.getPayload() +
                                            " and Headers = {" + request.getHeadersMap().toString() + "}");
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
                        })
                .sslContext(getCarbonSslContext())
                .build();
        server.start();
        if (logger.isDebugEnabled()) {
            logger.debug("Server started");
        }
    }

    public void stop() throws InterruptedException {
        Server s = server;
        if (s == null) {
            throw new IllegalStateException("Already stopped");
        }
        server = null;
        s.shutdown();
        if (s.awaitTermination(1, TimeUnit.SECONDS)) {

            if (logger.isDebugEnabled()) {
                logger.debug("Server stopped");
            }
            return;
        }
        s.shutdownNow();
        if (s.awaitTermination(1, TimeUnit.SECONDS)) {
            return;
        }
        throw new RuntimeException("Unable to shutdown server");
    }

    public int getPort() {
        return server.getPort();
    }
}
