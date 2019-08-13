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
import io.siddhi.core.stream.input.InputHandler;
import io.siddhi.core.util.persistence.InMemoryPersistenceStore;
import io.siddhi.extension.io.grpc.utils.TestAppender;
import io.siddhi.extension.io.grpc.utils.TestServer;
import io.siddhi.query.api.exception.SiddhiAppValidationException;
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

public class GrpcSinkTestCase {
    private static final Logger log = Logger.getLogger(GrpcSinkTestCase.class.getName());
    private TestServer server = new TestServer(8888);
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
    public void testCaseToCallConsumeWithSimpleRequest() throws Exception {
        log.info("Test case to call consume");
        final TestAppender appender = new TestAppender();
        final Logger rootLogger = Logger.getRootLogger();
        rootLogger.setLevel(Level.DEBUG);
        rootLogger.addAppender(appender);
        SiddhiManager siddhiManager = new SiddhiManager();

        String inStreamDefinition = ""
                + "@sink(type='grpc', url = 'grpc://localhost:8888/org.wso2.grpc.EventService/consume', " +
                "@map(type='json', @payload('{{message}}'))) " +
                "define stream FooStream (message String);";

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
            logMessages.add(message);
        }
        Assert.assertTrue(logMessages.contains("Server consume hit with [Request 1]"));
    }

    @Test
    public void testCaseToCallConsumeWithTwoRequests() throws Exception {
        log.info("Test case to call consume with 2 requests");
        final TestAppender appender = new TestAppender();
        final Logger rootLogger = Logger.getRootLogger();
        rootLogger.setLevel(Level.DEBUG);
        rootLogger.addAppender(appender);
        SiddhiManager siddhiManager = new SiddhiManager();

        String inStreamDefinition = ""
                + "@sink(type='grpc', url = 'grpc://localhost:8888/org.wso2.grpc.EventService/consume', " +
                "@map(type='json', @payload('{{message}}'))) " +
                "define stream FooStream (message String);";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(inStreamDefinition);
        InputHandler fooStream = siddhiAppRuntime.getInputHandler("FooStream");

        siddhiAppRuntime.start();
        fooStream.send(new Object[]{"Request 1"});
        fooStream.send(new Object[]{"Request 2"});
        Thread.sleep(1000);
        siddhiAppRuntime.shutdown();

        final List<LoggingEvent> log = appender.getLog();
        List<String> logMessages = new ArrayList<>();
        for (LoggingEvent logEvent : log) {
            String message = String.valueOf(logEvent.getMessage());
            logMessages.add(message);
        }
        Assert.assertTrue(logMessages.contains("Server consume hit with [Request 1]"));
        Assert.assertTrue(logMessages.contains("Server consume hit with [Request 2]"));
    }

    @Test
    public void testWithHeader() throws Exception {
        log.info("Test case to call consume with headers");
        final TestAppender appender = new TestAppender();
        final Logger rootLogger = Logger.getRootLogger();
        rootLogger.setLevel(Level.DEBUG);
        rootLogger.addAppender(appender);
        SiddhiManager siddhiManager = new SiddhiManager();

        String inStreamDefinition = ""
                + "@sink(type='grpc', " +
                "url = 'grpc://localhost:8888/org.wso2.grpc.EventService/consume', " +
                "headers='{{headers}}', " +
                "@map(type='json', @payload('{{message}}'))) " +
                "define stream FooStream (message String, headers String);";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(inStreamDefinition);
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
        Assert.assertTrue(logMessages.contains("Server consume hit with [Request 1]"));
        Assert.assertTrue(logMessages.contains("Server consume hit with [Request 2]"));
        Assert.assertTrue(logMessages.contains("Header received: 'Name:John','Age:23','Content-Type:text'"));
        Assert.assertTrue(logMessages.contains("Header received: 'Name:Nash','Age:54','Content-Type:json'"));
    }

    @Test
    public void testCaseWithWrongProtocol() throws Exception {
        log.info("Test case to call consume");
        final TestAppender appender = new TestAppender();
        final Logger rootLogger = Logger.getRootLogger();
        rootLogger.setLevel(Level.DEBUG);
        rootLogger.addAppender(appender);
        SiddhiManager siddhiManager = new SiddhiManager();

        String inStreamDefinition = ""
                + "@sink(type='grpc', url = 'grc://localhost:8888/org.wso2.grpc.EventService/consume', " +
                "@map(type='json', @payload('{{message}}'))) " +
                "define stream FooStream (message String);";

        try {
            SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(inStreamDefinition);
        } catch (SiddhiAppValidationException e) {
            Assert.assertTrue(e.getMessage().contains("The url must begin with \"grpc\" for all grpc sinks"));
        }
    }

    @Test
    public void testCaseWithMalformedURL() throws Exception {
        log.info("Test case to call consume");
        final TestAppender appender = new TestAppender();
        final Logger rootLogger = Logger.getRootLogger();
        rootLogger.setLevel(Level.DEBUG);
        rootLogger.addAppender(appender);
        SiddhiManager siddhiManager = new SiddhiManager();

        String inStreamDefinition = ""
                + "@sink(type='grpc', url = 'grpc:dfasf', " +
                "@map(type='json', @payload('{{message}}'))) " +
                "define stream FooStream (message String);";

        try {
            SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(inStreamDefinition);
        } catch (SiddhiAppValidationException e) {
            Assert.assertTrue(e.getMessage().contains("Malformed URL. After port number atleast two sections " +
                    "should be available seperated by '/' as in 'grpc://<host>:<port>/<ServiceName>/<MethodName>'"));
        }
    }

    @Test
    public void testCaseWithXMLMapper() throws Exception {
        log.info("Test case to call consume");
        final TestAppender appender = new TestAppender();
        final Logger rootLogger = Logger.getRootLogger();
        rootLogger.setLevel(Level.DEBUG);
        rootLogger.addAppender(appender);
        SiddhiManager siddhiManager = new SiddhiManager();

        String inStreamDefinition = ""
                + "@sink(type='grpc', url = 'grpc://localhost:8888/org.wso2.grpc.EventService/consume', " +
                "@map(type='xml', @payload('{{message}}'))) " +
                "define stream FooStream (message String);";

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
            logMessages.add(message);
        }
        Assert.assertTrue(logMessages.contains("Server consume hit with Request 1"));
    }

    @Test
    public void testCaseWithSequenceName() throws Exception {
        log.info("Test case to call consume");
        final TestAppender appender = new TestAppender();
        final Logger rootLogger = Logger.getRootLogger();
        rootLogger.setLevel(Level.DEBUG);
        rootLogger.addAppender(appender);
        SiddhiManager siddhiManager = new SiddhiManager();

        String inStreamDefinition = ""
                + "@sink(type='grpc', url = 'grpc://localhost:8888/org.wso2.grpc.EventService/consume/mySeq', " +
                "@map(type='xml', @payload('{{message}}'))) " +
                "define stream FooStream (message String);";

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
            logMessages.add(message);
        }
        Assert.assertTrue(logMessages.contains("Server consume hit with Request 1"));
    }

    @Test
    public void testWithHeaderAndSequenceName() throws Exception {
        log.info("Test case to call consume with headers");
        final TestAppender appender = new TestAppender();
        final Logger rootLogger = Logger.getRootLogger();
        rootLogger.setLevel(Level.DEBUG);
        rootLogger.addAppender(appender);
        SiddhiManager siddhiManager = new SiddhiManager();

        String inStreamDefinition = ""
                + "@sink(type='grpc', " +
                "url = 'grpc://localhost:8888/org.wso2.grpc.EventService/consume/mySeq', " +
                "headers='{{headers}}', " +
                "@map(type='json', @payload('{{message}}'))) " +
                "define stream FooStream (message String, headers String);";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(inStreamDefinition);
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
        Assert.assertTrue(logMessages.contains("Server consume hit with [Request 1]"));
        Assert.assertTrue(logMessages.contains("Server consume hit with [Request 2]"));
        Assert.assertTrue(logMessages.contains("Header received: 'sequence:mySeq','Name:John','Age:23'," +
                "'Content-Type:text'"));
        Assert.assertTrue(logMessages.contains("Header received: 'sequence:mySeq','Name:Nash','Age:54'," +
                "'Content-Type:json'"));
    }

    @Test
    public void testWithMetaData() throws Exception {
        log.info("Test case to call consume with headers");
        final TestAppender appender = new TestAppender();
        final Logger rootLogger = Logger.getRootLogger();
        rootLogger.setLevel(Level.DEBUG);
        rootLogger.addAppender(appender);
        SiddhiManager siddhiManager = new SiddhiManager();

        String inStreamDefinition = ""
                + "@sink(type='grpc', " +
                "url = 'grpc://localhost:8888/org.wso2.grpc.EventService/consume', " +
                "metadata='{{metadata}}', " +
                "@map(type='json', @payload('{{message}}'))) " +
                "define stream FooStream (message String, metadata String);";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(inStreamDefinition);
        InputHandler fooStream = siddhiAppRuntime.getInputHandler("FooStream");

        siddhiAppRuntime.start();
        fooStream.send(new Object[]{"Request 1", "'Name:John','Age:23','Content-Type:text'"});
        fooStream.send(new Object[]{"Request 2", "'Name:Nash','Age:54','Content-Type:json'"});
        Thread.sleep(1000);
        siddhiAppRuntime.shutdown();

//        final List<LoggingEvent> log = appender.getLog();
//        List<String> logMessages = new ArrayList<>();
//        for (LoggingEvent logEvent : log) {
//            String message = String.valueOf(logEvent.getMessage());
//            logMessages.add(message);
//        }
//        Assert.assertTrue(logMessages.contains("Server consume hit with [Request 1]"));
//        Assert.assertTrue(logMessages.contains("Server consume hit with [Request 2]"));
//        Assert.assertTrue(logMessages.contains("Header received: 'Name:John','Age:23','Content-Type:text'"));
//        Assert.assertTrue(logMessages.contains("Header received: 'Name:Nash','Age:54','Content-Type:json'"));
    }

    @Test
    public void testCaseFailingWithUnavailableServer() throws Exception {
        log.info("Test case to call consume");
        final TestAppender appender = new TestAppender();
        final Logger rootLogger = Logger.getRootLogger();
        rootLogger.setLevel(Level.DEBUG);
        rootLogger.addAppender(appender);
        SiddhiManager siddhiManager = new SiddhiManager();
        server.stop();

        String inStreamDefinition = ""
                + "@sink(type='grpc', url = 'grpc://localhost:8888/org.wso2.grpc.EventService/consume', " +
                "@map(type='json', @payload('{{message}}'))) " +
                "define stream FooStream (message String);";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(inStreamDefinition);
        InputHandler fooStream = siddhiAppRuntime.getInputHandler("FooStream");

        siddhiAppRuntime.start();
        fooStream.send(new Object[]{"Request 1"});
        Thread.sleep(1000);
        siddhiAppRuntime.shutdown();
        server.start();

        final List<LoggingEvent> log = appender.getLog();
        List<String> logMessages = new ArrayList<>();
        for (LoggingEvent logEvent : log) {
            String message = String.valueOf(logEvent.getMessage());
            logMessages.add(message);
        }
        Assert.assertTrue(logMessages.contains("Server consume hit with [Request 1]"));
    }

    @Test
    public void testCaseWithSiddhiAppShutdown() throws Exception {
        log.info("Test case to call consume with 2 requests");
        final TestAppender appender = new TestAppender();
        final Logger rootLogger = Logger.getRootLogger();
        rootLogger.setLevel(Level.DEBUG);
        rootLogger.addAppender(appender);
        SiddhiManager siddhiManager = new SiddhiManager();
        siddhiManager.setPersistenceStore(new InMemoryPersistenceStore());

        String inStreamDefinition = ""
                + "@sink(type='grpc', url = 'grpc://localhost:8888/org.wso2.grpc.EventService/consume', " +
                "@map(type='json', @payload('{{message}}'))) " +
                "define stream FooStream (message String);";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(inStreamDefinition);
        InputHandler fooStream = siddhiAppRuntime.getInputHandler("FooStream");

        siddhiAppRuntime.start();
        fooStream.send(new Object[]{"Request 1"});
        Thread.sleep(1000);
        siddhiManager.persist();
        Thread.sleep(100);
        siddhiAppRuntime.shutdown();
        Thread.sleep(100);

        siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(inStreamDefinition);
        fooStream = siddhiAppRuntime.getInputHandler("FooStream");
        siddhiAppRuntime.start();
        siddhiManager.restoreLastState();

        fooStream.send(new Object[]{"Request 2"});
        Thread.sleep(1000);
        siddhiAppRuntime.shutdown();

        final List<LoggingEvent> log = appender.getLog();
        List<String> logMessages = new ArrayList<>();
        for (LoggingEvent logEvent : log) {
            String message = String.valueOf(logEvent.getMessage());
            logMessages.add(message);
        }
        Assert.assertTrue(logMessages.contains("Server consume hit with [Request 1]"));
        Assert.assertTrue(logMessages.contains("Server consume hit with [Request 2]"));
    }
}
