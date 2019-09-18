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
package io.siddhi.extension.io.grpc.sink;

import io.siddhi.core.SiddhiAppRuntime;
import io.siddhi.core.SiddhiManager;
import io.siddhi.core.event.Event;
import io.siddhi.core.query.output.callback.QueryCallback;
import io.siddhi.core.stream.input.InputHandler;
import io.siddhi.core.util.EventPrinter;
import io.siddhi.extension.io.grpc.utils.GenericTestServer;
import io.siddhi.extension.io.grpc.utils.TestAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.spi.LoggingEvent;
import org.testng.Assert;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class GrpcCallSinkGenericTestCase {
    private static final Logger logger = Logger.getLogger(GrpcSinkTestCase.class.getName());
    private GenericTestServer server = new GenericTestServer();
    private AtomicInteger eventCount = new AtomicInteger(0);
    private String packageName = "io.siddhi.extension.io.grpc.proto";

    @BeforeTest
    public void init() throws IOException {
        server.start();
    }

    @AfterTest
    public void stop() throws InterruptedException {
        server.stop();

    }

    @Test
    public void testCase01() throws Exception {
        logger.info("Test case to call process sending 1 requests");
        logger.setLevel(Level.DEBUG);
        SiddhiManager siddhiManager = new SiddhiManager();
        final TestAppender appender = new TestAppender();
        final Logger rootLogger = Logger.getRootLogger();
        rootLogger.setLevel(Level.DEBUG);
        rootLogger.addAppender(appender);

        String inStreamDefinition = ""
                + "@sink(type='grpc-call', " +
                "publisher.url = 'grpc://localhost:8888/" + packageName + ".MyService/process', " +
                "sink.id= '1', @map(type='protobuf')) "
                + "define stream FooStream (stringValue string, intValue int,longValue long,booleanValue bool," +
                "floatValue float,doubleValue double);";

        String stream2 = "@source(type='grpc-call-response', receiver.url = 'grpc://localhost:8888/" + packageName +
                ".MyService/process', " +
                "sink.id= '1', @map(type='protobuf')) " +
                "define stream BarStream (stringValue string, intValue int,longValue long,booleanValue bool," +
                "floatValue float,doubleValue double);";
        String query = "@info(name = 'query') "
                + "from BarStream "
                + "select *  "
                + "insert into outputStream;";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(inStreamDefinition + stream2 +
                query);
        siddhiAppRuntime.addCallback("query", new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
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
        InputHandler fooStream = siddhiAppRuntime.getInputHandler("FooStream");

        siddhiAppRuntime.start();
        fooStream.send(new Object[]{"Test 01", 60, 10000L, true, 522.7586f, 34.5668});
        Thread.sleep(1000);
        siddhiAppRuntime.shutdown();

        final List<LoggingEvent> log = appender.getLog();
        List<String> logMessages = new ArrayList<>();
        for (LoggingEvent logEvent : log) {
            String message = String.valueOf(logEvent.getMessage());
            logMessages.add(message);
        }
        Assert.assertTrue(logMessages.contains("Server hits with request :\n" +
                "stringValue: \"Test 01\"\n" +
                "intValue: 60\n" +
                "longValue: 10000\n" +
                "booleanValue: true\n" +
                "floatValue: 522.7586\n" +
                "doubleValue: 34.5668\n"));

    }

    @Test
    public void testWithMappingAttributes() throws Exception {
        logger.info("Test case to call process sending 1 requests with mapping");
        logger.setLevel(Level.DEBUG);
        SiddhiManager siddhiManager = new SiddhiManager();
        final TestAppender appender = new TestAppender();
        final Logger rootLogger = Logger.getRootLogger();
        rootLogger.setLevel(Level.DEBUG);
        rootLogger.addAppender(appender);

        String inStreamDefinition = ""
                + "@sink(type='grpc-call', " +
                "publisher.url = 'grpc://localhost:8888/" + packageName + ".MyService/process', " +
                "sink.id= '1', @map(type='protobuf'," +
                "@payload(stringValue='a',longValue='c',intValue='b',booleanValue='d',floatValue = 'e', " +
                "doubleValue = 'f'))) "
                + "define stream FooStream (a string, b int,c long,d bool,e float,f double);";

        String stream2 = "@source(type='grpc-call-response', receiver.url = 'grpc://localhost:8888/" + packageName +
                ".MyService/process', " +
                "sink.id= '1', @map(type='protobuf'," +
                "@attributes(a = 'stringValue', b = 'intValue', c = 'longValue',d = 'booleanValue', e ='floatValue', " +
                "f ='doubleValue'))) " +
                "define stream BarStream (a string, b int,c long,d bool,e float,f double);";
        String query = "@info(name = 'query') "
                + "from BarStream "
                + "select *  "
                + "insert into outputStream;";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(inStreamDefinition + stream2 +
                query);
        siddhiAppRuntime.addCallback("query", new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
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
        InputHandler fooStream = siddhiAppRuntime.getInputHandler("FooStream");

        siddhiAppRuntime.start();
        fooStream.send(new Object[]{"Test 01", 60, 10000L, true, 522.7586f, 34.5668});
        Thread.sleep(1000);
        siddhiAppRuntime.shutdown();

        final List<LoggingEvent> log = appender.getLog();
        List<String> logMessages = new ArrayList<>();
        for (LoggingEvent logEvent : log) {
            String message = String.valueOf(logEvent.getMessage());
            logMessages.add(message);
        }
        Assert.assertTrue(logMessages.contains("Server hits with request :\n" +
                "stringValue: \"Test 01\"\n" +
                "intValue: 60\n" +
                "longValue: 10000\n" +
                "booleanValue: true\n" +
                "floatValue: 522.7586\n" +
                "doubleValue: 34.5668\n"));
    }

    @Test
    public void testCaseForSendingMapObject() throws Exception {
        logger.info("Test case to call process sending 1 requests with map object");
        logger.setLevel(Level.DEBUG);
        SiddhiManager siddhiManager = new SiddhiManager();
        final TestAppender appender = new TestAppender();
        final Logger rootLogger = Logger.getRootLogger();
        rootLogger.setLevel(Level.DEBUG);
        rootLogger.addAppender(appender);

        String inStreamDefinition = ""
                + "@sink(type='grpc-call', " +
                "publisher.url = 'grpc://localhost:8888/" + packageName + ".MyService/testMap', " +
                "sink.id= '1', @map(type='protobuf'))" +
                "define stream FooStream (stringValue string,intValue int,map object);";

        String stream2 = "@source(type='grpc-call-response', receiver.url = 'grpc://localhost:8888/" + packageName +
                ".MyService/testMap', " +
                "sink.id= '1', @map(type='protobuf')) " +
                "define stream BarStream (stringValue string,intValue int,map object);";
        String query = "@info(name = 'query') "
                + "from BarStream "
                + "select *  "
                + "insert into outputStream;";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(inStreamDefinition + stream2 +
                query);
        siddhiAppRuntime.addCallback("query", new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
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
        InputHandler fooStream = siddhiAppRuntime.getInputHandler("FooStream");

        siddhiAppRuntime.start();
        Map<String, String> mapObject = new HashMap<>();
        mapObject.put("Key 01", "Value 01");
        mapObject.put("Key 02", "Value 02");
        fooStream.send(new Object[]{"Test 01", 60, mapObject});
        Thread.sleep(1000);
        siddhiAppRuntime.shutdown();

        final List<LoggingEvent> log = appender.getLog();
        List<String> logMessages = new ArrayList<>();
        for (LoggingEvent logEvent : log) {
            String message = String.valueOf(logEvent.getMessage());
            logMessages.add(message);
        }
        Assert.assertTrue(logMessages.contains("Server hits with request :\n" +
                "stringValue: \"Test 01\"\n" +
                "intValue: 60\n" +
                "map {\n" +
                "  key: \"Key 01\"\n" +
                "  value: \"Value 01\"\n" +
                "}\n" +
                "map {\n" +
                "  key: \"Key 02\"\n" +
                "  value: \"Value 02\"\n" +
                "}\n"));

    }

    @Test
    public void testWithMetaDataWithMapping() throws Exception {
        logger.info("Test case to test metadata");
        final TestAppender appender = new TestAppender();
        final Logger rootLogger = Logger.getRootLogger();
        rootLogger.setLevel(Level.DEBUG);
        rootLogger.addAppender(appender);
        SiddhiManager siddhiManager = new SiddhiManager();

        String inStreamDefinition = ""
                + "@sink(type='grpc-call', " +
                "publisher.url = 'grpc://localhost:8888/" + packageName + ".MyService/process', " +
                "sink.id = '1', " +
                "metadata='{{metadata}}', " +
                "@map(type='protobuf'," +
                "@payload(stringValue='stringValue',intValue='intValue',longValue='longValue'," +
                "booleanValue='booleanValue',floatValue = 'floatValue', doubleValue = 'doubleValue'))) " +
                "define stream FooStream (stringValue string, intValue int,longValue long,booleanValue bool," +
                "floatValue float,doubleValue double, metadata string);";

        String stream2 = "@source(type='grpc-call-response', receiver.url = 'grpc://localhost:8888/" + packageName +
                ".MyService/process', sink.id= '1', " +
                "@map(type='protobuf')) " +
                "define stream BarStream (stringValue string, intValue int,longValue long,booleanValue bool," +
                "floatValue float,doubleValue double);";
        String query = "@info(name = 'query') "
                + "from BarStream "
                + "select *  "
                + "insert into outputStream;";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(inStreamDefinition + stream2 + query);
        InputHandler fooStream = siddhiAppRuntime.getInputHandler("FooStream");

        siddhiAppRuntime.start();
        fooStream.send(new Object[]{"Test 01", 60, 10000L, true, 522.7586f, 34.5668, "'Name:John','Age:23'," +
                "'Content-Type:text'"});
        fooStream.send(new Object[]{"Test 02", 1200, 852340L, true, 487.5478f, 34.5668, "'Name:Nash','Age:54'," +
                "'Content-Type:json'"});
        Thread.sleep(1000);
        siddhiAppRuntime.shutdown();

        final List<LoggingEvent> log = appender.getLog();
        List<String> logMessages = new ArrayList<>();
        for (LoggingEvent logEvent : log) {
            String message = String.valueOf(logEvent.getMessage());
            logMessages.add(message);
        }
        Assert.assertTrue(logMessages.contains("Metadata received: name: John"));
        Assert.assertTrue(logMessages.contains("Metadata received: name: Nash"));
    }

    @Test
    public void testWithMetaDataWithoutMapping() throws Exception {
        logger.info("Test case to test metadata");
        final TestAppender appender = new TestAppender();
        final Logger rootLogger = Logger.getRootLogger();
        rootLogger.setLevel(Level.DEBUG);
        rootLogger.addAppender(appender);
        SiddhiManager siddhiManager = new SiddhiManager();

        String inStreamDefinition = ""
                + "@sink(type='grpc-call', " +
                "publisher.url = 'grpc://localhost:8888/" + packageName + ".MyService/process', " +
                "sink.id = '1', " +
                "metadata=\"'Name:John','Age:54','Content-Type:json'\", " +
                "@map(type='protobuf'," +
                "@payload(stringValue='stringValue',intValue='intValue',longValue='longValue'," +
                "booleanValue='booleanValue',floatValue = 'floatValue', doubleValue = 'doubleValue'))) " +
                "define stream FooStream (stringValue string, intValue int,longValue long,booleanValue bool," +
                "floatValue float,doubleValue double);";

        String stream2 = "@source(type='grpc-call-response', receiver.url = 'grpc://localhost:8888/" + packageName +
                ".MyService/process', sink.id= '1', " +
                "@map(type='protobuf')) " +
                "define stream BarStream (stringValue string, intValue int,longValue long,booleanValue bool," +
                "floatValue float,doubleValue double);";
        String query = "@info(name = 'query') "
                + "from BarStream "
                + "select *  "
                + "insert into outputStream;";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(inStreamDefinition + stream2 + query);
        InputHandler fooStream = siddhiAppRuntime.getInputHandler("FooStream");

        siddhiAppRuntime.start();
        fooStream.send(new Object[]{"Test 01", 60, 10000L, true, 522.7586f, 34.5668});
        Thread.sleep(1000);
        siddhiAppRuntime.shutdown();

        final List<LoggingEvent> log = appender.getLog();
        List<String> logMessages = new ArrayList<>();
        for (LoggingEvent logEvent : log) {
            String message = String.valueOf(logEvent.getMessage());
            logMessages.add(message);
        }
        Assert.assertTrue(logMessages.contains("Metadata received: name: John"));
    }

    @Test
    public void testWithoutRelevantSource() throws InterruptedException {
        logger.info("Test case to call process sending 2 requests");
        final TestAppender appender = new TestAppender();
        final Logger rootLogger = Logger.getRootLogger();
        rootLogger.setLevel(Level.DEBUG);
        rootLogger.addAppender(appender);
        SiddhiManager siddhiManager = new SiddhiManager();

        String inStreamDefinition = ""
                + "@sink(type='grpc-call', " +
                "publisher.url = 'grpc://localhost:8888/" + packageName + ".MyService/process', " +
                "sink.id= '1', @map(type='protobuf')) "
                + "define stream FooStream (stringValue string, intValue int,longValue long,booleanValue bool," +
                "floatValue float,doubleValue double);";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(inStreamDefinition);
        InputHandler fooStream = siddhiAppRuntime.getInputHandler("FooStream");
        siddhiAppRuntime.start();
        fooStream.send(new Object[]{"Test 01", 60, 10000L, true, 522.7586f, 34.5668});
        Thread.sleep(1000);
        siddhiAppRuntime.shutdown();

        final List<LoggingEvent> log = appender.getLog();
        List<String> logMessages = new ArrayList<>();
        for (LoggingEvent logEvent : log) {
            String message = String.valueOf(logEvent.getMessage());
            if (message.contains("FooStream: ")) {
                message = message.split("FooStream: ")[1];
            }
            logMessages.add(message);
        }
        Assert.assertTrue(logMessages.contains("For grpc-call sink to work a grpc-call-response source should be " +
                "available with the same sink.id. In this case sink.id is 1. Please provide a grpc-call-response " +
                "source with the sink.id 1"));
    }


}
