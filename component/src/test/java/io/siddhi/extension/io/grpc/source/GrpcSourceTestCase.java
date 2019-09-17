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
import io.grpc.ManagedChannelBuilder;
import io.grpc.Metadata;
import io.grpc.stub.MetadataUtils;
import io.grpc.stub.StreamObserver;
import io.siddhi.core.SiddhiAppRuntime;
import io.siddhi.core.SiddhiManager;
import io.siddhi.core.query.output.callback.QueryCallback;
import io.siddhi.core.util.EventPrinter;
import io.siddhi.extension.io.grpc.proto.MyServiceGrpc;
import io.siddhi.extension.io.grpc.proto.Request;
import io.siddhi.extension.io.grpc.proto.RequestWithMap;
import io.siddhi.extension.io.grpc.proto.StreamServiceGrpc;
import io.siddhi.extension.io.grpc.utils.TestAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.spi.LoggingEvent;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.wso2.grpc.Event;
import org.wso2.grpc.EventServiceGrpc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class GrpcSourceTestCase {
    private static final Logger logger = Logger.getLogger(GrpcSourceTestCase.class.getName());
    private AtomicInteger eventCount = new AtomicInteger(0);
    private String port = "8181";
    private String packageName = "io.siddhi.extension.io.grpc.proto";

    @Test
    public void basicSourceTest() throws Exception {
        logger.info("Test case to call process");
        logger.setLevel(Level.DEBUG);
        SiddhiManager siddhiManager = new SiddhiManager();

        String stream2 = "@source(type='grpc', receiver.url = 'grpc://localhost:" + port +
                "/org.wso2.grpc.EventService/consume', " +
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

        requestBuilder.setPayload(json);
        requestBuilder.putHeaders("stream.id", "BarStream");
        Event sequenceCallRequest = requestBuilder.build();
        ManagedChannel channel = ManagedChannelBuilder.forTarget("localhost:" + port).usePlaintext().build();
        EventServiceGrpc.EventServiceStub asyncStub = EventServiceGrpc.newStub(channel);

        StreamObserver<Empty> responseObserver = new StreamObserver<Empty>() {
            @Override
            public void onNext(Empty event) {
            }

            @Override
            public void onError(Throwable t) {
            }

            @Override
            public void onCompleted() {
            }
        };

        siddhiAppRuntime.start();
        StreamObserver requestObserver  = asyncStub.consume(responseObserver);
        requestObserver.onNext(sequenceCallRequest);
        Thread.sleep(10);
        requestObserver.onCompleted();
        Thread.sleep(1000);
        siddhiAppRuntime.shutdown();
    }

    @Test
    public void testWithMetaData() throws Exception {
        logger.info("Test case to call process");
        logger.setLevel(Level.DEBUG);
        SiddhiManager siddhiManager = new SiddhiManager();

        String stream2 = "@source(type='grpc', receiver.url = 'grpc://localhost:" + port +
                "/org.wso2.grpc.EventService/consume', " +
                "@map(type='json', @attributes(name='trp:name', age='trp:age', message='message'))) " +
                "define stream BarStream (message String, name String, age int);";
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
                            Assert.assertEquals((String) inEvents[i].getData()[0], "Hello !");
                            break;
                        default:
                            Assert.fail();
                    }
                }
            }
        });

        Event.Builder requestBuilder = Event.newBuilder();

        String json = "{ \"message\": \"Hello !\"}";

        requestBuilder.setPayload(json);
        requestBuilder.putHeaders("stream.id", "BarStream");
        requestBuilder.putHeaders("name", "benjamin");
        requestBuilder.putHeaders("age", "24");
        Event sequenceCallRequest = requestBuilder.build();
        ManagedChannel channel = ManagedChannelBuilder.forTarget("localhost:" + port).usePlaintext().build();
        EventServiceGrpc.EventServiceStub asyncStub = EventServiceGrpc.newStub(channel);

        StreamObserver<Empty> responseObserver = new StreamObserver<Empty>() {
            @Override
            public void onNext(Empty event) {
            }

            @Override
            public void onError(Throwable t) {
            }

            @Override
            public void onCompleted() {
            }
        };

        siddhiAppRuntime.start();
        StreamObserver requestObserver  = asyncStub.consume(responseObserver);
        requestObserver.onNext(sequenceCallRequest);
        Thread.sleep(10);
        requestObserver.onCompleted();
        Thread.sleep(1000);
        siddhiAppRuntime.shutdown();
    }

    @Test
    public void testToCheckForAllReqTrpInStreamDef() throws Exception {
        logger.info("Test case to call process");
        logger.setLevel(Level.DEBUG);
        SiddhiManager siddhiManager = new SiddhiManager();

        String stream2 = "@source(type='grpc', receiver.url = 'grpc://localhost:" + port +
                "/org.wso2.grpc.EventService/consume', " +
                "@map(type='json', @attributes(name='trp:name', age='trp:age', message='message'))) " +
                "define stream BarStream (message String, name String);";
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
                            Assert.assertEquals((String) inEvents[i].getData()[0], "Hello !");
                            break;
                        default:
                            Assert.fail();
                    }
                }
            }
        });

        Event.Builder requestBuilder = Event.newBuilder();

        String json = "{ \"message\": \"Hello !\"}";

        requestBuilder.setPayload(json);
        requestBuilder.putHeaders("stream.id", "BarStream");
        requestBuilder.putHeaders("name", "benjamin");
        requestBuilder.putHeaders("age", "24");
        Event sequenceCallRequest = requestBuilder.build();
        ManagedChannel channel = ManagedChannelBuilder.forTarget("localhost:" + port).usePlaintext().build();
        EventServiceGrpc.EventServiceStub asyncStub = EventServiceGrpc.newStub(channel);

        StreamObserver<Empty> responseObserver = new StreamObserver<Empty>() {
            @Override
            public void onNext(Empty event) {
            }

            @Override
            public void onError(Throwable t) {
            }

            @Override
            public void onCompleted() {
            }
        };

        siddhiAppRuntime.start();
        StreamObserver requestObserver  = asyncStub.consume(responseObserver);
        requestObserver.onNext(sequenceCallRequest);
        Thread.sleep(10);
        requestObserver.onCompleted();
        Thread.sleep(1000);
        siddhiAppRuntime.shutdown();
    }

    @Test
    public void testWithIncompleteMetadata() throws Exception {
        logger.info("Test case to call process");
        final TestAppender appender = new TestAppender();
        final Logger rootLogger = Logger.getRootLogger();
        rootLogger.setLevel(Level.DEBUG);
        rootLogger.addAppender(appender);
        SiddhiManager siddhiManager = new SiddhiManager();

        String stream2 = "@source(type='grpc', receiver.url = 'grpc://localhost:" + port +
                "/org.wso2.grpc.EventService/consume', " +
                "@map(type='json', @attributes(name='trp:name', age='trp:age', message='message'))) " +
                "define stream BarStream (message String, name String, age int);";
        String query = "@info(name = 'query') "
                + "from BarStream "
                + "select *  "
                + "insert into outputStream;";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(stream2 + query);

        Event.Builder requestBuilder = Event.newBuilder();
        String json = "{ \"message\": \"Hello !\"}";
        requestBuilder.setPayload(json);
        requestBuilder.putHeaders("stream.id", "BarStream");
        Event sequenceCallRequest = requestBuilder.build();
        ManagedChannel channel = ManagedChannelBuilder.forTarget("localhost:" + port).usePlaintext().build();
        EventServiceGrpc.EventServiceBlockingStub blockingStub = EventServiceGrpc.newBlockingStub(channel);

        Metadata metadata = new Metadata();
        metadata.put(Metadata.Key.of("Name", Metadata.ASCII_STRING_MARSHALLER), "John");
        EventServiceGrpc.EventServiceStub asyncStub = EventServiceGrpc.newStub(channel);
        asyncStub = MetadataUtils.attachHeaders(asyncStub, metadata);

        StreamObserver<Empty> responseObserver = new StreamObserver<Empty>() {
            @Override
            public void onNext(Empty event) {
            }

            @Override
            public void onError(Throwable t) {
            }

            @Override
            public void onCompleted() {
            }
        };

        siddhiAppRuntime.start();
        StreamObserver requestObserver  = asyncStub.consume(responseObserver);
        requestObserver.onNext(sequenceCallRequest);
        Thread.sleep(10);
        requestObserver.onCompleted();
        Thread.sleep(1000);
        siddhiAppRuntime.shutdown();

        final List<LoggingEvent> log = appender.getLog();
        List<String> logMessages = new ArrayList<>();
        for (LoggingEvent logEvent : log) {
            String message = String.valueOf(logEvent.getMessage());
            if (message.contains("BarStream: ")) {
                message = message.split("BarStream: ")[1];
            }
            logMessages.add(message);
        }
//        Assert.assertTrue(logMessages.contains("Dropping request. Requested transport property 'age' not
//        present in " +
//                "received event"));
    }

    @Test
    public void testWithHeaders() throws Exception {
        logger.info("Test case to call process");
        logger.setLevel(Level.DEBUG);
        SiddhiManager siddhiManager = new SiddhiManager();

        String stream2 = "@source(type='grpc', receiver.url = 'grpc://localhost:" + port +
                "/org.wso2.grpc.EventService/consume', " +
                "@map(type='json', @attributes(name='trp:name', age='trp:age', message='message'))) " +
                "define stream BarStream (message String, name String, age int);";
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
                            Assert.assertEquals((String) inEvents[i].getData()[0], "Hello !");
                            break;
                        default:
                            Assert.fail();
                    }
                }
            }
        });

        Event.Builder requestBuilder = Event.newBuilder();
        String json = "{ \"message\": \"Hello !\"}";
        requestBuilder.setPayload(json);
        requestBuilder.putHeaders("stream.id", "BarStream");
        requestBuilder.putHeaders("name", "john");
        requestBuilder.putHeaders("age", "24");
        Event sequenceCallRequest = requestBuilder.build();
        ManagedChannel channel = ManagedChannelBuilder.forTarget("localhost:" + port).usePlaintext().build();
        EventServiceGrpc.EventServiceStub asyncStub = EventServiceGrpc.newStub(channel);

        StreamObserver<Empty> responseObserver = new StreamObserver<Empty>() {
            @Override
            public void onNext(Empty event) {
            }

            @Override
            public void onError(Throwable t) {
            }

            @Override
            public void onCompleted() {
            }
        };

        siddhiAppRuntime.start();
        StreamObserver requestObserver  = asyncStub.consume(responseObserver);
        requestObserver.onNext(sequenceCallRequest);
        Thread.sleep(10);
        requestObserver.onCompleted();
        Thread.sleep(1000);
        siddhiAppRuntime.shutdown();
    }

    @Test
    public void testWithIncompleteHeaders() throws Exception {
        logger.info("Test case to call process");
        final TestAppender appender = new TestAppender();
        final Logger rootLogger = Logger.getRootLogger();
        rootLogger.setLevel(Level.DEBUG);
        rootLogger.addAppender(appender);
        SiddhiManager siddhiManager = new SiddhiManager();

        String stream2 = "@source(type='grpc', receiver.url = 'grpc://localhost:" + port +
                "/org.wso2.grpc.EventService/consume', " +
                "@map(type='json', @attributes(name='trp:name', age='trp:age', message='message'))) " +
                "define stream BarStream (message String, name String, age int);";
        String query = "@info(name = 'query') "
                + "from BarStream "
                + "select *  "
                + "insert into outputStream;";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(stream2 + query);

        Event.Builder requestBuilder = Event.newBuilder();
        String json = "{ \"message\": \"Hello !\"}";
        requestBuilder.setPayload(json);
        requestBuilder.putHeaders("stream.id", "BarStream");
        requestBuilder.putHeaders("age", "24");
        Event sequenceCallRequest = requestBuilder.build();
        ManagedChannel channel = ManagedChannelBuilder.forTarget("localhost:" + port).usePlaintext().build();
        EventServiceGrpc.EventServiceStub asyncStub = EventServiceGrpc.newStub(channel);

        StreamObserver<Empty> responseObserver = new StreamObserver<Empty>() {
            @Override
            public void onNext(Empty event) {
            }

            @Override
            public void onError(Throwable t) {
            }

            @Override
            public void onCompleted() {
            }
        };

        siddhiAppRuntime.start();
        StreamObserver requestObserver  = asyncStub.consume(responseObserver);
        requestObserver.onNext(sequenceCallRequest);
        Thread.sleep(10);
        requestObserver.onCompleted();
        Thread.sleep(1000);
        siddhiAppRuntime.shutdown();

        final List<LoggingEvent> log = appender.getLog();
        List<String> logMessages = new ArrayList<>();
        for (LoggingEvent logEvent : log) {
            String message = String.valueOf(logEvent.getMessage());
            if (message.contains("BarStream: ")) {
                message = message.split("BarStream: ")[1];
            }
            logMessages.add(message);
        }
//        Assert.assertTrue(logMessages.contains("Dropping request. Requested transport property 'name' not
//        present in " +
//                "received event"));
    }

    @Test
    public void bindExceptionTest() throws Exception { //irrelevant now with subscriber model
        logger.info("Test case to call process");
        logger.setLevel(Level.DEBUG);
        SiddhiManager siddhiManager = new SiddhiManager();

        String stream1 = "@source(type='grpc', receiver.url = 'grpc://localhost:" + port +
                "/org.wso2.grpc.EventService/consume', " +
                "@map(type='json')) " +
                "define stream BarStream (message String);";
        String stream2 = "@source(type='grpc', receiver.url = 'grpc://localhost:" + port +
                "/org.wso2.grpc.EventService/consume', " +
                "@map(type='json')) " +
                "define stream FooStream (message String);";
        String query = "@info(name = 'query') "
                + "from BarStream "
                + "select *  "
                + "insert into outputStream;";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(stream1 + stream2 + query);
        siddhiAppRuntime.start();
    }




    @Test//(dependsOnMethods = {"bindExceptionTest"})
    public void genericTestCase01() throws Exception {
        logger.info("Test case to call process");
        logger.setLevel(Level.DEBUG);
        SiddhiManager siddhiManager = new SiddhiManager();

        String stream2 = "@source(type='grpc', receiver.url='grpc://localhost:8898/" + packageName +
                ".MyService/send', " +
                "@map(type='protobuf')) " +
                "define stream BarStream (stringValue string, intValue int,longValue long,booleanValue bool," +
                "floatValue float,doubleValue double);";
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
                            Assert.assertEquals((String) inEvents[i].getData()[0], "Test 01");
                            break;
                        default:
                            Assert.fail();
                    }
                }
            }
        });
        ManagedChannel channel = ManagedChannelBuilder.forTarget("localhost:8898")
                .usePlaintext().build();
        Request request = Request.newBuilder()
                .setStringValue("Test 01")
                .setIntValue(100)
                .setBooleanValue(false)
                .setDoubleValue(168.4567)
                .setFloatValue(45.345f)
                .setLongValue(1000000L)
                .build();
        MyServiceGrpc.MyServiceBlockingStub blockingStub = MyServiceGrpc.newBlockingStub(channel);
        siddhiAppRuntime.start();
        blockingStub.send(request);

        Thread.sleep(1000);
        siddhiAppRuntime.shutdown();
        channel.shutdown();
        channel.awaitTermination(30, TimeUnit.SECONDS);
    }

    @Test//(dependsOnMethods = {"genericTestCase01"})
    public void genericTestCase_TestWithMapping() throws Exception {
        logger.info("Test case to call process");
        logger.setLevel(Level.DEBUG);
        SiddhiManager siddhiManager = new SiddhiManager();

        String stream2 = "@source(type='grpc', receiver.url='grpc://localhost:8898/" + packageName +
                ".MyService/send', " +
                "@map(type='protobuf'," +
                " @attributes(a = 'stringValue', b = 'intValue', c = 'longValue',d = 'booleanValue', e ='floatValue'," +
                "f ='doubleValue')))" +
                "define stream FooStream (a string ,c long,b int, d bool,e float,f double);";
        String query = "@info(name = 'query') "
                + "from FooStream "
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
                            Assert.assertEquals((String) inEvents[i].getData()[0], "Test 01");
                            break;
                        default:
                            Assert.fail();
                    }
                }
            }
        });
        ManagedChannel channel = ManagedChannelBuilder.forTarget("localhost:8898")
                .usePlaintext().build();
        Request request = Request.newBuilder()
                .setStringValue("Test 01")
                .setIntValue(100)
                .setBooleanValue(false)
                .setDoubleValue(168.4567)
                .setFloatValue(45.345f)
                .setLongValue(1000000L)
                .build();
        MyServiceGrpc.MyServiceBlockingStub blockingStub = MyServiceGrpc.newBlockingStub(channel);
        siddhiAppRuntime.start();
        blockingStub.send(request);

        Thread.sleep(1000);
        siddhiAppRuntime.shutdown();

        channel.shutdown();
        channel.awaitTermination(30, TimeUnit.SECONDS);
    }

    @Test//(dependsOnMethods = {"genericTestCase_TestWithMapping"})
    public void genericTestCase_testWithMetaData() throws Exception {
        logger.info("Test case to call process");
        logger.setLevel(Level.DEBUG);
        SiddhiManager siddhiManager = new SiddhiManager();

        String stream2 = "@source(type='grpc', receiver.url='grpc://localhost:8898/" + packageName +
                ".MyService/send'," +
                "@map(type='protobuf'," +
                " @attributes(a = 'stringValue', b = 'intValue', c = 'longValue',d = 'booleanValue', e ='floatValue'," +
                "f ='doubleValue',name='trp:name', age='trp:age')))" +
                "define stream BarStream (name string , age int, a string ,c long,b int, d bool,e float,f double);";
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
                            Assert.assertEquals((String) inEvents[i].getData()[0], "John");
                            break;
                        default:
                            Assert.fail();
                    }
                }
            }
        });

        Request request = Request.newBuilder()
                .setStringValue("Test 01")
                .setIntValue(100)
                .setBooleanValue(false)
                .setDoubleValue(168.4567)
                .setFloatValue(45.345f)
                .setLongValue(1000000L)
                .build();

        ManagedChannel channel = ManagedChannelBuilder.forTarget("localhost:8898").usePlaintext().build();
        MyServiceGrpc.MyServiceBlockingStub blockingStub = MyServiceGrpc.newBlockingStub(channel);

        Metadata metadata = new Metadata();
        metadata.put(Metadata.Key.of("Name", Metadata.ASCII_STRING_MARSHALLER), "John");
        metadata.put(Metadata.Key.of("Age", Metadata.ASCII_STRING_MARSHALLER), "23");
        blockingStub = MetadataUtils.attachHeaders(blockingStub, metadata);

        siddhiAppRuntime.start();
        Empty emptyResponse = blockingStub.send(request);
        Thread.sleep(1000);
        siddhiAppRuntime.shutdown();

        channel.shutdown();
        channel.awaitTermination(30, TimeUnit.SECONDS); //otherwise channel will be garbage collected,
        // and throw
        // ~*~*~ Channel io.grpc.internal.ManagedChannelImpl-2096 for target 127.0.0.1:8771 was not
        // shutdown properly!!! ~*~*~*
        //    Make sure to call shutdown()/shutdownNow() and awaitTermination().
        //java.lang.RuntimeException: ManagedChannel allocation site....
    }


    @Test//(dependsOnMethods = {"genericTestCase_testWithMetaData"})
    public void genericTestCase_TestWithMapObject() throws Exception {
        logger.info("Test case to call process");
        logger.setLevel(Level.DEBUG);
        SiddhiManager siddhiManager = new SiddhiManager();

        String stream2 = "@source(type='grpc', receiver.url='grpc://localhost:8890/" + packageName +
                ".MyService/testMap', " +
                "@map(type='protobuf')) " +
                "define stream BarStream2 (stringValue string, intValue int,map object);";
        String query = "@info(name = 'query') "
                + "from BarStream2 "
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
                        case 0: {
                            Map<String, String> output = new HashMap<>();
                            output.put("Key 01", "Value 01");
                            output.put("Key 02", "Value 02");
                            Assert.assertEquals((String) inEvents[i].getData()[0], "Test 01");
                            Assert.assertEquals(inEvents[i].getData()[2], output);
                            break;
                        }
                        default:
                            Assert.fail();
                    }
                }
            }
        });
        ManagedChannel channel = ManagedChannelBuilder.forTarget("localhost:8890")
                .usePlaintext().build();
        Map<String, String> map = new HashMap<>();
        map.put("Key 01", "Value 01");
        map.put("Key 02", "Value 02");
        RequestWithMap request = RequestWithMap.newBuilder()
                .setIntValue(1000)
                .setStringValue("Test 01")
                .putAllMap(map).build();

        MyServiceGrpc.MyServiceBlockingStub blockingStub = MyServiceGrpc.newBlockingStub(channel);
        siddhiAppRuntime.start();
        Thread.sleep(100);
        blockingStub.testMap(request);

        Thread.sleep(1000);
        siddhiAppRuntime.shutdown();

        channel.shutdown();
        channel.awaitTermination(30, TimeUnit.SECONDS);
    }

    @Test//(dependsOnMethods = {"bindExceptionTest"})
    public void genericTestCase_SendStream() throws Exception {
        logger.info("Test case to call process");
        logger.setLevel(Level.DEBUG);
        SiddhiManager siddhiManager = new SiddhiManager();

        String stream2 = "@source(type='grpc', receiver.url='grpc://localhost:8891/" + packageName +
                ".StreamService/clientStream', " +
                "@map(type='protobuf')) " +
                "define stream BarStream (stringValue string, intValue int,longValue long,booleanValue bool," +
                "floatValue float,doubleValue double);";
        String query = "@info(name = 'query') "
                + "from BarStream "
                + "select *  "
                + "insert into outputStream;";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(stream2 + query);

        siddhiAppRuntime.addCallback("query", new QueryCallback() {
            int j = 0;

            @Override
            public void receive(long timeStamp, io.siddhi.core.event.Event[] inEvents,
                                io.siddhi.core.event.Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                for (int i = 0; i < inEvents.length; i++) {
                    eventCount.incrementAndGet();
                    switch (i) {
                        case 0:
                            Assert.assertEquals((String) inEvents[i].getData()[0], "Test " + j);
                            j++;
                            break;
                        default:
                            Assert.fail();
                    }
                }
            }
        });
        ManagedChannel channel = ManagedChannelBuilder.forTarget("localhost:8891")
                .usePlaintext().build();

        StreamServiceGrpc.StreamServiceStub stub = StreamServiceGrpc.newStub(channel);
        StreamObserver<Request> requestStreamObserver = stub.clientStream(new StreamObserver<Empty>() {
            @Override
            public void onNext(Empty value) {
                Assert.assertNotNull(value);
            }

            @Override
            public void onError(Throwable t) {

            }

            @Override
            public void onCompleted() {

            }
        });
        for (int i = 0; i < 15; i++) {
            Request request = Request.newBuilder()
                    .setStringValue("Test " + i)
                    .setIntValue(i * 100)
                    .setBooleanValue(false)
                    .setDoubleValue(168.4567)
                    .setFloatValue(45.345f)
                    .setLongValue(1000000L)
                    .build();
            requestStreamObserver.onNext(request);
        }
        requestStreamObserver.onCompleted();
        siddhiAppRuntime.start();
        Thread.sleep(1000);
        siddhiAppRuntime.shutdown();
        channel.shutdown();
        channel.awaitTermination(30, TimeUnit.SECONDS);

    }
}
