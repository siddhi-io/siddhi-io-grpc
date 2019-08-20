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
import io.grpc.ServerBuilder;
import io.grpc.ServerInterceptors;
import io.grpc.netty.shaded.io.grpc.netty.GrpcSslContexts;
import io.grpc.netty.shaded.io.grpc.netty.NettyServerBuilder;
import io.grpc.netty.shaded.io.netty.handler.ssl.ClientAuth;
import io.grpc.netty.shaded.io.netty.handler.ssl.SslContext;
import io.grpc.netty.shaded.io.netty.handler.ssl.SslContextBuilder;
import io.grpc.stub.StreamObserver;
import org.apache.log4j.Logger;
import org.wso2.grpc.Event;
import org.wso2.grpc.EventServiceGrpc;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.TrustManagerFactory;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.concurrent.TimeUnit;

public class TestTLSServer { //todo: follow http in setting the certificates
    private static final Logger logger = Logger.getLogger(TestServer.class.getName());
    private Server server;
    private TestServerInterceptor testInterceptor = new TestServerInterceptor();
    private int port;
    private String certChainFilePath = "/Users/niruhan/wso2/source_codes/siddhi-io-grpc-1/component/src/test/resources/certs/server2.pem";
    private String privateKeyFilePath = "/Users/niruhan/wso2/source_codes/siddhi-io-grpc-1/component/src/test/resources/certs/server2.key";
    private String trustCertCollectionFilePath;
    private KeyStore keyStore;

    public TestTLSServer(int port) throws KeyStoreException {
        this.port = port;
        keyStore = KeyStore.getInstance("JKS");
    }

    private SslContextBuilder getSslContextBuilder() {
        SslContextBuilder sslClientContextBuilder = SslContextBuilder.forServer(new File(certChainFilePath),
                new File(privateKeyFilePath));
        if (trustCertCollectionFilePath != null) {
            sslClientContextBuilder.trustManager(new File(trustCertCollectionFilePath));
            sslClientContextBuilder.clientAuth(ClientAuth.REQUIRE);
        }
        return GrpcSslContexts.configure(sslClientContextBuilder);
    }

    private SslContext getCarbonSslContext() {
        char[] passphrase = "wso2carbon".toCharArray();

        try {
            keyStore.load(new FileInputStream(System.getProperty("carbon.home") + "/resources/security/wso2carbon.jks"),
                    passphrase);
            KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
            kmf.init(keyStore, passphrase);
            TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
            tmf.init(keyStore);
            SslContextBuilder sslClientContextBuilder = SslContextBuilder.forServer(kmf);
    //        SslContext ssl = SslContext.defaultServerProvider().;
            sslClientContextBuilder = GrpcSslContexts.configure(sslClientContextBuilder);
            SslContext sslContext = sslClientContextBuilder.build();
            return sslContext;
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (CertificateException e) {
            e.printStackTrace();
        } catch (UnrecoverableKeyException e) {
            e.printStackTrace();
        } catch (KeyStoreException e) {
            e.printStackTrace();
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
                                    logger.debug("Server process hit with payload = " + request.getPayload() + " and Headers = {"
                                            + request.getHeadersMap().toString() + "}");
                                }
                                Event.Builder responseBuilder = Event.newBuilder();
                                String json = "{ \"message\": \"Hello from Server!\"}";
                                responseBuilder.setPayload(json);
                                Event response = responseBuilder.build();
                                responseObserver.onNext(response);
                                responseObserver.onCompleted();
                            }

                            @Override
                            public void consume(Event request,
                                                StreamObserver<Empty> responseObserver) {
                                if (logger.isDebugEnabled()) {
                                    logger.debug("Server consume hit with " + request.getPayload());
                                }
                                responseObserver.onNext(Empty.getDefaultInstance());
                                responseObserver.onCompleted();
                            }
                        })
                .sslContext(getCarbonSslContext())
//                .useTransportSecurity(new File(certChainFilePath), new File(privateKeyFilePath))
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
