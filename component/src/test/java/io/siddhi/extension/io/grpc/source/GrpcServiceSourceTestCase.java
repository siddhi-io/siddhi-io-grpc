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
import io.siddhi.core.query.output.callback.QueryCallback;
import io.siddhi.core.util.EventPrinter;
import io.siddhi.extension.io.grpc.util.GrpcConstants;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.wso2.grpc.Event;
import org.wso2.grpc.EventServiceGrpc;
import package01.test.MyServiceGrpc;
import package01.test.Request;
import package01.test.Response;

import java.util.concurrent.atomic.AtomicInteger;

public class GrpcServiceSourceTestCase {
    private static final Logger logger = Logger.getLogger(GrpcServiceSourceTestCase.class.getName());
    private AtomicInteger eventCount = new AtomicInteger(0);

    @Test
    public void testToCallProcess() throws Exception {
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
//                System.out.println("Response :"+response);
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
                        .usePlaintext()
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

    @Test
    public void testCaseForServiceTimeout() throws Exception {
        logger.info("Test case to call process");
        logger.setLevel(Level.DEBUG);
        SiddhiManager siddhiManager = new SiddhiManager();

        String stream1 = "@source(type='grpc-service', " +
                "url='grpc://localhost:8888/org.wso2.grpc.EventService/process', source.id='1', " +
                "service.timeout = '3000', " +
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
                + "insert into OutputStream;";

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
        Thread.sleep(10000);
        siddhiAppRuntime.shutdown();
    }















    @Test
    public void testToCallProcess_1() throws Exception {
        logger.info("Test case to call process");
        logger.setLevel(Level.DEBUG);
        SiddhiManager siddhiManager = new SiddhiManager();

        String stream1 = "@source(type='grpc-service', " +
                "url='grpc://localhost:8888/package01.test.MyService/process', source.id='1', " +
                "@map(type='protobuf' , " +
                "@attributes(messageId='trp:message.id', a = 'stringValue', b = 'intValue', c = 'longValue',d = 'booleanValue', e = 'floatValue', f ='doubleValue'))) " +
                "define stream FooStream (messageId String,a string, b int,c long,d bool,e float,f double);";

        String stream2 = "@sink(type='grpc-service-response', " +
                "url='grpc://localhost:8888/package01.test.MyService/process', source.id='1', " +
                "message.id='{{messageId}}', " +
                "@map(type='protobuf'," +
                "@payload(stringValue='a',intValue='b',longValue='c',booleanValue='d',floatValue = 'e', doubleValue = 'f'))) " +
                "define stream BarStream (messageId String, a string, b int,c long,d bool,e float,f double);";
        String query = "@info(name = 'query') "
                + "from FooStream "
                + "select messageId,a,b*2 as b, c*2 as c,d,e*100 as e,f*4 as f "
                + "insert into BarStream;";
// TODO: 8/8/19 either user have to define the messageId in the first field or user has to provide the payload
        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(stream1 + stream2 + query);
        siddhiAppRuntime.addCallback("query", new QueryCallback() {
            @Override
            public void receive(long timeStamp, io.siddhi.core.event.Event[] inEvents,
                                io.siddhi.core.event.Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                for (int i = 0; i < inEvents.length; i++) {
                    eventCount.incrementAndGet();
//                    switch (i) {
//                        case 0:
//                            Assert.assertEquals((String) inEvents[i].getData()[1], "Benjamin Watson");
//                            break;
//                        default:
//                            Assert.fail();
//                    }
                }
            }
        });

        Thread client = new Thread() {
            public void run() {



                Request request = Request.newBuilder()
                        .setStringValue("Test 01")
                        .setIntValue(100)
                        .setBooleanValue(true)
                        .setDoubleValue(168.4567)
                        .setFloatValue(45.345f)
                        .setLongValue(1000000L)
                        .build();
                ManagedChannel channel = ManagedChannelBuilder.forTarget("localhost:8888")
                        .usePlaintext()
                        .build();
                MyServiceGrpc.MyServiceBlockingStub blockingStub = MyServiceGrpc.newBlockingStub(channel);
                Response response = blockingStub.process(request);
                System.out.println("Request\n"+request+"\n");
                System.out.println("Response :\n"+response);
            }
        };
        siddhiAppRuntime.start();
        client.start();
        Thread.sleep(1000);
        siddhiAppRuntime.shutdown();
    }
}
