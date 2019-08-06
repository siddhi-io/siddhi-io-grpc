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

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Metadata;
import io.grpc.stub.MetadataUtils;
import io.siddhi.core.SiddhiAppRuntime;
import io.siddhi.core.SiddhiManager;
import io.siddhi.core.exception.SiddhiAppRuntimeException;
import io.siddhi.core.query.output.callback.QueryCallback;
import io.siddhi.core.util.EventPrinter;
import io.siddhi.extension.io.grpc.util.GrpcConstants;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.wso2.grpc.Event;
import org.wso2.grpc.EventServiceGrpc;

import java.util.concurrent.atomic.AtomicInteger;

public class GrpcServiceSourceTestCase {
    private static final Logger logger = Logger.getLogger(GrpcServiceSourceTestCase.class.getName());
    private AtomicInteger eventCount = new AtomicInteger(0);

//    @Test
//    public void test1() throws Exception {
//        logger.info("Test case to call process");
//        logger.setLevel(Level.DEBUG);
//        SiddhiManager siddhiManager = new SiddhiManager();
//
//        String stream2 = "@source(type='grpc-service', " +
//                "url='grpc://localhost:8888/org.wso2.grpc.EventService/consume', source.id='1', " +
//                "@map(type='json', @attributes(messageId='trp:messageId', message='message'))) " +
//                "define stream BarStream (messageId String, message String);";
//        String query = "@info(name = 'query') "
//                + "from BarStream "
//                + "select *  "
//                + "insert into outputStream;";
//
//        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(stream2 + query);
//        siddhiAppRuntime.addCallback("query", new QueryCallback() {
//            @Override
//            public void receive(long timeStamp, io.siddhi.core.event.Event[] inEvents,
//                                io.siddhi.core.event.Event[] removeEvents) {
//                EventPrinter.print(timeStamp, inEvents, removeEvents);
//                for (int i = 0; i < inEvents.length; i++) {
//                    eventCount.incrementAndGet();
//                    switch (i) {
//                        case 0:
//                            Assert.assertEquals((String) inEvents[i].getData()[1], "Benjamin Watson");
//                            break;
//                        default:
//                            Assert.fail();
//                    }
//                }
//            }
//        });
//
//        Event.Builder requestBuilder = Event.newBuilder();
//
//        String json = "{ \"message\": \"Benjamin Watson\"}";
//
//        requestBuilder.setPayload(json);
//        Event sequenceCallRequest = requestBuilder.build();
//        ManagedChannel channel = ManagedChannelBuilder.forTarget("localhost:8888")
//                .usePlaintext(true)
//                .build();
//        EventServiceGrpc.EventServiceBlockingStub blockingStub = EventServiceGrpc.newBlockingStub(channel);
//
//        siddhiAppRuntime.start();
//        Event emptyResponse = blockingStub.process(sequenceCallRequest);
//        Thread.sleep(1000);
//        siddhiAppRuntime.shutdown();
//    }

    @Test
    public void test2() throws Exception {
        logger.info("Test case to call process");
        logger.setLevel(Level.DEBUG);
        SiddhiManager siddhiManager = new SiddhiManager();

        String stream1 = "@source(type='grpc-service', " +
                "url='grpc://localhost:8888/org.wso2.grpc.EventService/process', source.id='1', " +
                "@map(type='json', @attributes(messageId='trp:message.id', message='message'))) " +
                "define stream FooStream (messageId String, message String);";

        String stream2 = "@sink(type='grpc-service-response', " +
                "url='grpc://localhost:8888/org.wso2.grpc.EventService/process', source.id='1', " +
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

                requestBuilder.setPayload(json);
                Event sequenceCallRequest = requestBuilder.build();
                ManagedChannel channel = ManagedChannelBuilder.forTarget("localhost:8888")
                        .usePlaintext()
                        .build();
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
    public void testWithHeader() throws Exception {
        logger.info("Test case to call process");
        logger.setLevel(Level.DEBUG);
        SiddhiManager siddhiManager = new SiddhiManager();

        String stream1 = "@source(type='grpc-service', " +
                "url='grpc://localhost:8888/org.wso2.grpc.EventService/process', source.id='1', " +
                "@map(type='json', @attributes(messageId='trp:message.id', message='message', name='trp:name', age='trp:age'))) " +
                "define stream FooStream (messageId String, message String, name String, age int);";

        String stream2 = "@sink(type='grpc-service-response', " +
                "url='grpc://localhost:8888/org.wso2.grpc.EventService/process', source.id='1', " +
                "message.id='{{messageId}}', " +
                "@map(type='json')) " +
                "define stream BarStream (messageId String, message String, name String, age int);";
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

                requestBuilder.setPayload(json);
                Event sequenceCallRequest = requestBuilder.build();
                ManagedChannel channel = ManagedChannelBuilder.forTarget("localhost:8888")
                        .usePlaintext(true)
                        .build();
                EventServiceGrpc.EventServiceBlockingStub blockingStub = EventServiceGrpc.newBlockingStub(channel);

                Metadata header = new Metadata();
                String headers = "'Name:John', 'Age:23'";
                Metadata.Key<String> key =
                        Metadata.Key.of(GrpcConstants.HEADERS, Metadata.ASCII_STRING_MARSHALLER);
                header.put(key, headers);
                blockingStub = MetadataUtils.attachHeaders(blockingStub, header);

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
    public void testWithIncompleteHeader() throws Exception {
        logger.info("Test case to call process");
        logger.setLevel(Level.DEBUG);
        SiddhiManager siddhiManager = new SiddhiManager();

        String stream1 = "@source(type='grpc-service', " +
                "url='grpc://localhost:8888/org.wso2.grpc.EventService/process', source.id='1', " +
                "@map(type='json', @attributes(messageId='trp:message.id', message='message', name='trp:name', age='trp:age'))) " +
                "define stream FooStream (messageId String, message String, name String, age int);";

        String stream2 = "@sink(type='grpc-service-response', " +
                "url='grpc://localhost:8888/org.wso2.grpc.EventService/process', source.id='1', " +
                "message.id='{{messageId}}', " +
                "@map(type='json')) " +
                "define stream BarStream (messageId String, message String, name String, age int);";
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

                requestBuilder.setPayload(json);
                Event sequenceCallRequest = requestBuilder.build();
                ManagedChannel channel = ManagedChannelBuilder.forTarget("localhost:8888")
                        .usePlaintext(true)
                        .build();
                EventServiceGrpc.EventServiceBlockingStub blockingStub = EventServiceGrpc.newBlockingStub(channel);

                Metadata header = new Metadata();
                String headers = "'Name:John'";
                Metadata.Key<String> key =
                        Metadata.Key.of(GrpcConstants.HEADERS, Metadata.ASCII_STRING_MARSHALLER);
                header.put(key, headers);
                blockingStub = MetadataUtils.attachHeaders(blockingStub, header);

                Event response = blockingStub.process(sequenceCallRequest);
                Assert.assertNotNull(response);
            }
        };
        siddhiAppRuntime.start();
        client.start();
        Thread.sleep(1000);
        siddhiAppRuntime.shutdown();
    }
}
