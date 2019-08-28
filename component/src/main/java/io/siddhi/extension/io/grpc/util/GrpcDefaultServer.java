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
package io.siddhi.extension.io.grpc.util;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.netty.shaded.io.grpc.netty.GrpcSslContexts;
import io.grpc.netty.shaded.io.grpc.netty.NettyServerBuilder;
import io.grpc.netty.shaded.io.netty.handler.ssl.ClientAuth;
import io.grpc.netty.shaded.io.netty.handler.ssl.SslContextBuilder;
import io.siddhi.core.exception.SiddhiAppCreationException;
import org.apache.log4j.Logger;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.TrustManagerFactory;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

public class GrpcDefaultServer {
    private static final Logger logger = Logger.getLogger(GrpcDefaultServer.class.getName());
    protected String[] requestedTransportPropertyNames;
    protected Server server;
    private ServerBuilder serverBuilder;

    public void initServer() {
        serverBuilder = NettyServerBuilder.forPort(port);
        if (keystoreFilePath != null) {
            try {
                SslContextBuilder sslContextBuilder = getSslContextBuilder(keystoreFilePath, keystorePassword,
                        keystoreAlgorithm, tlsStoreType);
                if (truststoreFilePath != null) {
                    sslContextBuilder = addTrustStore(truststoreFilePath, truststorePassword, truststoreAlgorithm,
                            sslContextBuilder, tlsStoreType).clientAuth(ClientAuth.REQUIRE);
                }
                serverBuilder.sslContext(sslContextBuilder.build());
            } catch (IOException | CertificateException | NoSuchAlgorithmException | UnrecoverableKeyException |
                    KeyStoreException e) {
                throw new SiddhiAppCreationException(siddhiAppContext.getName() + ": " + streamID + ": Error while " +
                        "creating SslContext. ", e);
            }
        }
        serverBuilder.maxInboundMessageSize(Integer.parseInt(optionHolder.getOrCreateOption(
                GrpcConstants.MAX_INBOUND_MESSAGE_SIZE, GrpcConstants.MAX_INBOUND_MESSAGE_SIZE_DEFAULT).getValue()));
        serverBuilder.maxInboundMetadataSize(Integer.parseInt(optionHolder.getOrCreateOption(
                GrpcConstants.MAX_INBOUND_METADATA_SIZE, GrpcConstants.MAX_INBOUND_METADATA_SIZE_DEFAULT).getValue()));

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
}
