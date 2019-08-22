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
import io.siddhi.extension.io.grpc.utils.TestAppender;
import io.siddhi.extension.io.grpc.utils.TestServer;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.spi.LoggingEvent;
import org.testng.Assert;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class GrpcCallSinkTestCase {
    private static final Logger logger = Logger.getLogger(GrpcSinkTestCase.class.getName());
    private TestServer server = new TestServer(8889);
    private AtomicInteger eventCount = new AtomicInteger(0);

    @BeforeTest
    public void init() throws IOException {
        server.start();
    }

    @AfterTest
    public void stop() throws InterruptedException {
        server.stop();
    }

    @Test
    public void test1() throws Exception {
        logger.info("Test case to call process sending a request");
        final TestAppender appender = new TestAppender();
        final Logger rootLogger = Logger.getRootLogger();
        rootLogger.setLevel(Level.DEBUG);
        rootLogger.addAppender(appender);
        SiddhiManager siddhiManager = new SiddhiManager();

        String inStreamDefinition = ""
                + "@sink(type='grpc-call', " +
                "url = 'grpc://localhost:8889/org.wso2.grpc.EventService/process/mySeq', " +
                "sink.id= '1', @map(type='json')) "
                + "define stream FooStream (message String);";

        String stream2 = "@source(type='grpc-call-response', sink.id= '1', @map(type='json')) " +
                "define stream BarStream (message String);";
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
                            Assert.assertEquals((String) inEvents[i].getData()[0], "Hello from Server!");
                            break;
                        default:
                            Assert.fail();
                    }
                }
            }
        });
        InputHandler fooStream = siddhiAppRuntime.getInputHandler("FooStream");

        siddhiAppRuntime.start();
        fooStream.send(new Object[]{"Request 1"});
        Thread.sleep(1000);
        siddhiAppRuntime.shutdown();

        final List<LoggingEvent> log = appender.getLog();
        List<String> logMessages = new ArrayList<>();
        for (LoggingEvent logEvent : log) {
            String message = String.valueOf(logEvent.getMessage());
            logMessages.add(message);
        }
        Assert.assertTrue(logMessages.contains("Server process hit with payload = [{\"event\":{\"message\":\"" +
                "Request 1\"}}] and Headers = {{sequence=mySeq}}"));
    }

    @Test//(dependsOnMethods = "test1")
    public void testWithHeaders() throws Exception {
        logger.info("Test case to call process sending 2 requests");
        final TestAppender appender = new TestAppender();
        final Logger rootLogger = Logger.getRootLogger();
        rootLogger.setLevel(Level.DEBUG);
        rootLogger.addAppender(appender);
        SiddhiManager siddhiManager = new SiddhiManager();

        String inStreamDefinition = ""
                + "@sink(type='grpc-call', " +
                "url = 'grpc://localhost:8889/org.wso2.grpc.EventService/process/mySeq', " +
                "sink.id= '2', " +
                "headers='{{headers}}', " +
                "@map(type='json', @payload('{{message}}'))) "
                + "define stream FooStream (message String, headers String);";

        String stream2 = "@source(type='grpc-call-response', sink.id= '2', @map(type='json')) " +
                "define stream BarStream (message String);";
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
                            Assert.assertEquals((String) inEvents[i].getData()[0], "Hello from Server!");
                            break;
                        default:
                            Assert.fail();
                    }
                }
            }
        });
        InputHandler fooStream = siddhiAppRuntime.getInputHandler("FooStream");
        siddhiAppRuntime.start();
        fooStream.send(new Object[]{"Request 1", "'Name:John','Age:23','Content-Type:text'"});
        fooStream.send(new Object[]{"Request 2", "'Name:Nash','Age:54','Content-Type:json'"});
        Thread.sleep(1000);
        siddhiAppRuntime.shutdown();

        final List<LoggingEvent> log = appender.getLog();
        List<String> logMessages = new ArrayList<>();
        for (LoggingEvent logEvent : log) {
            String message = String.valueOf(logEvent.getMessage());
            logMessages.add(message);
        }
        Assert.assertTrue(logMessages.contains("Server process hit with payload = [Request 1] and Headers = " +
                "{{Name=John, Age=23, Content-Type=text, sequence=mySeq}}"));
        Assert.assertTrue(logMessages.contains("Server process hit with payload = [Request 2] and Headers = " +
                "{{Name=Nash, Age=54, Content-Type=json, sequence=mySeq}}"));
    }

    @Test
    public void testWithoutRelevantSource() throws Exception {
        logger.info("Test case to call process sending 2 requests");
        final TestAppender appender = new TestAppender();
        final Logger rootLogger = Logger.getRootLogger();
        rootLogger.setLevel(Level.DEBUG);
        rootLogger.addAppender(appender);
        SiddhiManager siddhiManager = new SiddhiManager();

        String inStreamDefinition = ""
                + "@sink(type='grpc-call', " +
                "url = 'grpc://localhost:8889/org.wso2.grpc.EventService/process/mySeq', " +
                "sink.id= '1', @map(type='json')) "
                + "define stream FooStream (message String);";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(inStreamDefinition);

        InputHandler fooStream = siddhiAppRuntime.getInputHandler("FooStream");

        siddhiAppRuntime.start();
        fooStream.send(new Object[]{"Request 1"});
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

    @Test
    public void testWithMetaData() throws Exception {
        logger.info("Test case to call consume with headers");
        final TestAppender appender = new TestAppender();
        final Logger rootLogger = Logger.getRootLogger();
        rootLogger.setLevel(Level.DEBUG);
        rootLogger.addAppender(appender);
        SiddhiManager siddhiManager = new SiddhiManager();
//        server.start();

        String inStreamDefinition = ""
                + "@sink(type='grpc-call', " +
                "url = 'grpc://localhost:8889/org.wso2.grpc.EventService/process', " +
                "sink.id = '1', " +
                "metadata='{{metadata}}', " +
                "@map(type='json', @payload('{{message}}'))) " +
                "define stream FooStream (message String, metadata String);";

        String stream2 = "@source(type='grpc-call-response', sequence='mySeq', sink.id= '1', @map(type='json')) " +
                "define stream BarStream (message String);";
        String query = "@info(name = 'query') "
                + "from BarStream "
                + "select *  "
                + "insert into outputStream;";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(inStreamDefinition + stream2 + query);
        InputHandler fooStream = siddhiAppRuntime.getInputHandler("FooStream");

        siddhiAppRuntime.start();
        fooStream.send(new Object[]{"Request 1", "'Name:John','Age:23','Content-Type:text'"});
        fooStream.send(new Object[]{"Request 2", "'Name:Nash','Age:54','Content-Type:json'"});
        Thread.sleep(1000);
        siddhiAppRuntime.shutdown();

        final List<LoggingEvent> log = appender.getLog();
        List<String> logMessages = new ArrayList<>();
        for (LoggingEvent logEvent : log) {
            String message = String.valueOf(logEvent.getMessage());
            logMessages.add(message);
        }
        Assert.assertTrue(logMessages.contains("Server process hit with payload = [Request 1] and Headers = {{}}"));
        Assert.assertTrue(logMessages.contains("Server process hit with payload = [Request 2] and Headers = {{}}"));
        Assert.assertTrue(logMessages.contains("Metadata received: name: John"));
        Assert.assertTrue(logMessages.contains("Metadata received: name: Nash"));
    }

    @Test
    public void testOfEnrichingData() throws Exception {
        logger.info("Test case to call process sending 2 requests");
        logger.setLevel(Level.DEBUG);
        SiddhiManager siddhiManager = new SiddhiManager();

        String inStreamDefinition = ""
                + "@sink(type='grpc-call', " +
                "url = 'grpc://localhost:8889/org.wso2.grpc.EventService/process/mySeq', " +
                "sink.id= '1', @map(type='json')) "
                + "define stream FooStream (requestMessage String);";

        String stream2 = "@source(type='grpc-call-response', sequence='mySeq', sink.id= '1', " +
                "@map(type='json', @attributes(requestMessage='trp:requestMessage', message='message'))) " +
                "define stream BarStream (message String, requestMessage String);";
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
                            Assert.assertEquals((String) inEvents[i].getData()[0], "Hello from Server!");
                            Assert.assertEquals((String) inEvents[i].getData()[1], "Request 1");
                            break;
                        default:
                            Assert.fail();
                    }
                }
            }
        });
        InputHandler fooStream = siddhiAppRuntime.getInputHandler("FooStream");

        siddhiAppRuntime.start();
        fooStream.send(new Object[]{"Request 1"});
        Thread.sleep(1000);
        siddhiAppRuntime.shutdown();
    }
}
