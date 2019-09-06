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
import io.grpc.ManagedChannel;
import io.grpc.netty.shaded.io.grpc.netty.GrpcSslContexts;
import io.grpc.netty.shaded.io.grpc.netty.NettyChannelBuilder;
import io.siddhi.core.SiddhiAppRuntime;
import io.siddhi.core.SiddhiManager;
import io.siddhi.core.query.output.callback.QueryCallback;
import io.siddhi.core.util.EventPrinter;
import io.siddhi.extension.io.grpc.util.GrpcConstants;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.FileInputStream;
import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.concurrent.atomic.AtomicInteger;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.TrustManagerFactory;

public class GrpcSourceAuthTestCase {
    private static final Logger logger = Logger.getLogger(GrpcSourceAuthTestCase.class.getName());
    private AtomicInteger eventCount = new AtomicInteger(0);

    @Test
    public void testGrpcSourceWithServerAuth() throws Exception {
        logger.info("Test case to call process");
        logger.setLevel(Level.DEBUG);
        SiddhiManager siddhiManager = new SiddhiManager();

        String stream2 = "@source(type='grpc', " +
                "receiver.url = 'grpc://localhost:8888/org.wso2.grpc.EventService/consume', " +
                "keystore.file = 'src/test/resources/security/wso2carbon.jks'," +
                "keystore.password = 'wso2carbon', " +
                "keystore.algorithm = 'SunX509', " +
                "@map(type='json')) " +
                "define stream BarStream (message String);";
        String query = "@info(name = 'query') "
                + "from BarStream "
                + "select *  "
                + "insert into outputStream;";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(stream2 + query);
        siddhiAppRuntime.addCallback("query", new QueryCallback() {
            @Override
            public void receive(long timeStamp, io.siddhi.core.event.Event[] inEvents,
                                io.siddhi.core.event.Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                for (int i = 0; i < inEvents.length; i++) {
                    eventCount.incrementAndGet();
                    switch (i) {
                        case 0:
                            Assert.assertEquals((String) inEvents[i].getData()[0], "Benjamin Watson");
                            break;
                        default:
                            Assert.fail();
                    }
                }
            }
        });

        Event.Builder requestBuilder = Event.newBuilder();

        String json = "{ \"message\": \"Benjamin Watson\"}";

        String truststoreFilePath = "src/test/resources/security/wso2carbon.jks";
        String truststorePassword = "wso2carbon";
        String truststoreAlgorithm = "SunX509";

        requestBuilder.setPayload(json);
        Event sequenceCallRequest = requestBuilder.build();
        ManagedChannel channel = NettyChannelBuilder.forTarget("localhost:8888").sslContext(GrpcSslContexts
                .forClient().trustManager(getTrustManagerFactory(truststoreFilePath, truststorePassword,
                        truststoreAlgorithm)).build()).build();
        EventServiceGrpc.EventServiceBlockingStub blockingStub = EventServiceGrpc.newBlockingStub(channel);

        siddhiAppRuntime.start();
        Empty emptyResponse = blockingStub.consume(sequenceCallRequest);
        Thread.sleep(1000);
        siddhiAppRuntime.shutdown();
    }

    @Test
    public void testGrpcSourceWithMutualAuth() throws Exception {
        logger.info("Test case to call process");
        logger.setLevel(Level.DEBUG);
        SiddhiManager siddhiManager = new SiddhiManager();

        String stream2 = "@source(type='grpc', " +
                "receiver.url = 'grpc://localhost:8888/org.wso2.grpc.EventService/consume', " +
                "keystore.file = 'src/test/resources/security/wso2carbon.jks'," +
                "keystore.password = 'wso2carbon', " +
                "keystore.algorithm = 'SunX509', " +
                "truststore.file = 'src/test/resources/security/wso2carbon.jks'," +
                "truststore.password = 'wso2carbon', " +
                "truststore.algorithm = 'SunX509', " +
                "@map(type='json')) " +
                "define stream BarStream (message String);";
        String query = "@info(name = 'query') "
                + "from BarStream "
                + "select *  "
                + "insert into outputStream;";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(stream2 + query);
        siddhiAppRuntime.addCallback("query", new QueryCallback() {
            @Override
            public void receive(long timeStamp, io.siddhi.core.event.Event[] inEvents,
                                io.siddhi.core.event.Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                for (int i = 0; i < inEvents.length; i++) {
                    eventCount.incrementAndGet();
                    switch (i) {
                        case 0:
                            Assert.assertEquals((String) inEvents[i].getData()[0], "Benjamin Watson");
                            break;
                        default:
                            Assert.fail();
                    }
                }
            }
        });

        Event.Builder requestBuilder = Event.newBuilder();

        String json = "{ \"message\": \"Benjamin Watson\"}";

        String truststoreFilePath = "src/test/resources/security/wso2carbon.jks";
        String truststorePassword = "wso2carbon";
        String truststoreAlgorithm = "SunX509";

        requestBuilder.setPayload(json);
        Event sequenceCallRequest = requestBuilder.build();
        ManagedChannel channel = NettyChannelBuilder.forTarget("localhost:8888").sslContext(GrpcSslContexts
                .forClient().trustManager(getTrustManagerFactory(truststoreFilePath, truststorePassword,
                        truststoreAlgorithm))
                .keyManager(getKeyManagerFactory(truststoreFilePath, truststorePassword, truststoreAlgorithm))
                .build()).build();
        EventServiceGrpc.EventServiceBlockingStub blockingStub = EventServiceGrpc.newBlockingStub(channel);

        siddhiAppRuntime.start();
        Empty emptyResponse = blockingStub.consume(sequenceCallRequest);
        Thread.sleep(1000);
        siddhiAppRuntime.shutdown();
    }

    @Test
    public void testGrpcServiceSourceWithServerAuth() throws Exception {
        logger.info("Test case to call process");
        logger.setLevel(Level.DEBUG);
        SiddhiManager siddhiManager = new SiddhiManager();

        String stream1 = "@source(type='grpc-service', " +
                "receiver.url = 'grpc://localhost:8888/org.wso2.grpc.EventService/process', source.id='1', " +
                "keystore.file = 'src/test/resources/security/wso2carbon.jks'," +
                "keystore.password = 'wso2carbon', " +
                "keystore.algorithm = 'SunX509', " +
                "@map(type='json', @attributes(messageId='trp:message.id', message='message'))) " +
                "define stream FooStream (messageId String, message String);";

        String stream2 = "@sink(type='grpc-service-response', " +
                "receiver.url = 'grpc://localhost:8888/org.wso2.grpc.EventService/process', source.id='1', " +
                "message.id='{{messageId}}', " +
                "@map(type='json')) " +
                "define stream BarStream (messageId String, message String);";
        String query = "@info(name = 'query') "
                + "from FooStream "
                + "select *  "
                + "insert into BarStream;";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(stream1 + stream2 + query);
        siddhiAppRuntime.addCallback("query", new QueryCallback() {
            @Override
            public void receive(long timeStamp, io.siddhi.core.event.Event[] inEvents,
                                io.siddhi.core.event.Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                for (int i = 0; i < inEvents.length; i++) {
                    eventCount.incrementAndGet();
                    switch (i) {
                        case 0:
                            Assert.assertEquals((String) inEvents[i].getData()[1], "Benjamin Watson");
                            break;
                        default:
                            Assert.fail();
                    }
                }
            }
        });

        Thread client = new Thread() {
            public void run() {
                Event.Builder requestBuilder = Event.newBuilder();
                String json = "{ \"message\": \"Benjamin Watson\"}";
                String truststoreFilePath = "src/test/resources/security/wso2carbon.jks";
                String truststorePassword = "wso2carbon";
                String truststoreAlgorithm = "SunX509";

                requestBuilder.setPayload(json);
                Event sequenceCallRequest = requestBuilder.build();
                ManagedChannel channel = null;
                try {
                    channel = NettyChannelBuilder.forTarget("localhost:8888").sslContext(GrpcSslContexts
                            .forClient().trustManager(getTrustManagerFactory(truststoreFilePath, truststorePassword,
                                    truststoreAlgorithm)).build()).build();
                } catch (KeyStoreException | IOException | NoSuchAlgorithmException | CertificateException e) {
                }
                EventServiceGrpc.EventServiceBlockingStub blockingStub = EventServiceGrpc.newBlockingStub(channel);
                Event response = blockingStub.process(sequenceCallRequest);
                Assert.assertNotNull(response);
            }
        };
        siddhiAppRuntime.start();
        client.start();
        Thread.sleep(1000);
        siddhiAppRuntime.shutdown();
    }

    @Test
    public void testGrpcServiceSourceWithMutualAuth() throws Exception {
        logger.info("Test case to call process");
        logger.setLevel(Level.DEBUG);
        SiddhiManager siddhiManager = new SiddhiManager();

        String stream1 = "@source(type='grpc-service', " +
                "receiver.url = 'grpc://localhost:8888/org.wso2.grpc.EventService/process', source.id='1', " +
                "keystore.file = 'src/test/resources/security/wso2carbon.jks'," +
                "keystore.password = 'wso2carbon', " +
                "keystore.algorithm = 'SunX509', " +
                "truststore.file = 'src/test/resources/security/wso2carbon.jks'," +
                "truststore.password = 'wso2carbon', " +
                "truststore.algorithm = 'SunX509', " +
                "@map(type='json', @attributes(messageId='trp:message.id', message='message'))) " +
                "define stream FooStream (messageId String, message String);";

        String stream2 = "@sink(type='grpc-service-response', " +
                "receiver.url = 'grpc://localhost:8888/org.wso2.grpc.EventService/process', source.id='1', " +
                "message.id='{{messageId}}', " +
                "@map(type='json')) " +
                "define stream BarStream (messageId String, message String);";
        String query = "@info(name = 'query') "
                + "from FooStream "
                + "select *  "
                + "insert into BarStream;";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(stream1 + stream2 + query);
        siddhiAppRuntime.addCallback("query", new QueryCallback() {
            @Override
            public void receive(long timeStamp, io.siddhi.core.event.Event[] inEvents,
                                io.siddhi.core.event.Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                for (int i = 0; i < inEvents.length; i++) {
                    eventCount.incrementAndGet();
                    switch (i) {
                        case 0:
                            Assert.assertEquals((String) inEvents[i].getData()[1], "Benjamin Watson");
                            break;
                        default:
                            Assert.fail();
                    }
                }
            }
        });

        Thread client = new Thread() {
            public void run() {
                Event.Builder requestBuilder = Event.newBuilder();
                String json = "{ \"message\": \"Benjamin Watson\"}";
                String truststoreFilePath = "src/test/resources/security/wso2carbon.jks";
                String truststorePassword = "wso2carbon";
                String truststoreAlgorithm = "SunX509";

                requestBuilder.setPayload(json);
                Event sequenceCallRequest = requestBuilder.build();
                ManagedChannel channel = null;
                try {
                    channel = NettyChannelBuilder.forTarget("localhost:8888").sslContext(GrpcSslContexts
                            .forClient().trustManager(getTrustManagerFactory(truststoreFilePath, truststorePassword,
                                    truststoreAlgorithm))
                            .keyManager(getKeyManagerFactory(truststoreFilePath, truststorePassword,
                                    truststoreAlgorithm))
                            .build()).build();
                } catch (KeyStoreException | IOException | NoSuchAlgorithmException | CertificateException |
                        UnrecoverableKeyException e) {
                }
                EventServiceGrpc.EventServiceBlockingStub blockingStub = EventServiceGrpc.newBlockingStub(channel);
                try {
                    Event response = blockingStub.process(sequenceCallRequest);
                    Assert.assertNotNull(response);
                } catch (Exception e) {
                }
            }
        };
        siddhiAppRuntime.start();
        client.start();
        Thread.sleep(1000);
        siddhiAppRuntime.shutdown();
    }

    private TrustManagerFactory getTrustManagerFactory(String jksPath, String password, String algorithm)
            throws KeyStoreException, IOException, NoSuchAlgorithmException, CertificateException {
        char[] passphrase = password.toCharArray();
        KeyStore keyStore = KeyStore.getInstance(GrpcConstants.DEFAULT_TLS_STORE_TYPE);
        keyStore.load(new FileInputStream(jksPath),
                passphrase);
        TrustManagerFactory tmf = TrustManagerFactory.getInstance(algorithm);
        tmf.init(keyStore);
        return tmf;
    }

    private KeyManagerFactory getKeyManagerFactory(String jksPath, String password, String algorithm) throws
            KeyStoreException, IOException, CertificateException, NoSuchAlgorithmException, UnrecoverableKeyException {
        KeyStore keyStore = KeyStore.getInstance(GrpcConstants.DEFAULT_TLS_STORE_TYPE);
        char[] passphrase = password.toCharArray();
        keyStore.load(new FileInputStream(jksPath),
                passphrase);
        KeyManagerFactory kmf = KeyManagerFactory.getInstance(algorithm);
        kmf.init(keyStore, passphrase);
        return kmf;
    }
}
