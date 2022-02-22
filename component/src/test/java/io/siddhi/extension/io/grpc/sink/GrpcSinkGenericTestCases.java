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
import io.siddhi.extension.io.grpc.utils.GenericTestServer;
import io.siddhi.extension.io.grpc.utils.TestAppender;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Logger;
import org.testng.Assert;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Test cases for grpc-sink in generic way.
 */
public class GrpcSinkGenericTestCases {
    private static final Logger log = (Logger) LogManager.getLogger(GrpcSinkGenericTestCases.class);
    private int port = 6667;
    private GenericTestServer server = new GenericTestServer(port);
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
        log.info("Test case to call send");
        TestAppender appender = new TestAppender("TestAppender", null);
        final Logger logger = (Logger) LogManager.getRootLogger();
        logger.setLevel(Level.ALL);
        logger.addAppender(appender);
        appender.start();
        SiddhiManager siddhiManager = new SiddhiManager();

        String inStreamDefinition = ""
                + "@sink(type='grpc', publisher.url = 'grpc://localhost:" + port + "/" + packageName +
                ".MyService/send', @map(type='protobuf')) " +
                "define stream FooStream (stringValue string, intValue int,longValue long,booleanValue bool," +
                "floatValue float,doubleValue double);";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(inStreamDefinition);
        InputHandler fooStream = siddhiAppRuntime.getInputHandler("FooStream");

        siddhiAppRuntime.start();
        fooStream.send(new Object[]{"Test 01", 60, 10000L, true, 522.7586f, 34.5668});
        Thread.sleep(1000);
        siddhiAppRuntime.shutdown();

        Assert.assertTrue(((TestAppender) logger.getAppenders().
                get("TestAppender")).getMessages().contains("Server hits with request :\n" +
                "stringValue: \"Test 01\"\n" +
                "intValue: 60\n" +
                "longValue: 10000\n" +
                "booleanValue: true\n" +
                "floatValue: 522.7586\n" +
                "doubleValue: 34.5668\n"));
        logger.removeAppender(appender);
    }

    @Test
    public void testWithMappingAttributes() throws Exception {
        log.info("Test case to call send");
        SiddhiManager siddhiManager = new SiddhiManager();
        TestAppender appender = new TestAppender("TestAppender", null);
        final Logger logger = (Logger) LogManager.getRootLogger();
        logger.setLevel(Level.ALL);
        logger.addAppender(appender);
        appender.start();

        String inStreamDefinition = ""
                + "@sink(type='grpc', " +
                "publisher.url = 'grpc://localhost:" + port + "/" + packageName + ".MyService/send', " +
                "@map(type='protobuf', " +
                "@payload(stringValue='a',longValue='b',intValue='c',booleanValue='d',floatValue = 'e', doubleValue =" +
                " 'f'))) " +
                "define stream FooStream (a string, b long, c int,d bool,e float,f double);";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(inStreamDefinition);
        InputHandler fooStream = siddhiAppRuntime.getInputHandler("FooStream");
        siddhiAppRuntime.start();
        fooStream.send(new Object[]{"Test 01", 10000L, 60, true, 522.7586f, 34.5668});
        Thread.sleep(1000);
        fooStream.send(new Object[]{"Test 02", 10000L, 60, false, 768.987f, 34.5668});
        Thread.sleep(1000);
        siddhiAppRuntime.shutdown();

        Assert.assertTrue(((TestAppender) logger.getAppenders().
                get("TestAppender")).getMessages().contains("Server hits with request :\n" +
                "stringValue: \"Test 02\"\n" +
                "intValue: 60\n" +
                "longValue: 10000\n" +
                "floatValue: 768.987\n" +
                "doubleValue: 34.5668\n"));
        logger.removeAppender(appender);
    }

    @Test
    public void testWithMapObject() throws Exception {
        log.info("Test case to call send");
        SiddhiManager siddhiManager = new SiddhiManager();
        TestAppender appender = new TestAppender("TestAppender", null);
        final Logger logger = (Logger) LogManager.getRootLogger();
        logger.setLevel(Level.ALL);
        logger.addAppender(appender);
        appender.start();

        String inStreamDefinition = ""
                + "@sink(type='grpc', " +
                "publisher.url = 'grpc://localhost:" + port + "/" + packageName + ".MyService/testMap', " +
                "@map(type='protobuf')) " +
                "define stream FooStream (stringValue string, intValue int,map object);";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(inStreamDefinition);
        InputHandler fooStream = siddhiAppRuntime.getInputHandler("FooStream");
        siddhiAppRuntime.start();
        Map<String, String> mapObject = new HashMap<>();
        mapObject.put("Key 01", "Value 01");
        mapObject.put("Key 02", "Value 02");
        fooStream.send(new Object[]{"Test 01", 60, mapObject});

        Thread.sleep(1000);
        siddhiAppRuntime.shutdown();

        Assert.assertTrue(((TestAppender) logger.getAppenders().
                get("TestAppender")).getMessages().contains("Server hits with request :\n" +
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
        logger.removeAppender(appender);

    }

    @Test
    public void testWithMetadata() throws Exception {
        log.info("Test case to call send");
        SiddhiManager siddhiManager = new SiddhiManager();
        TestAppender appender = new TestAppender("TestAppender", null);
        final Logger logger = (Logger) LogManager.getRootLogger();
        logger.setLevel(Level.ALL);
        logger.addAppender(appender);
        appender.start();

        String inStreamDefinition = ""
                + "@sink(type='grpc', " +
                "publisher.url = 'grpc://localhost:" + port + "/" + packageName + ".MyService/send', " +
                "metadata = \"'Name:John','Age:23','Content-Type:text'\", " +
                "@map(type='protobuf', " +
                "@payload(stringValue='a',longValue='b',intValue='c',booleanValue='d',floatValue = 'e', doubleValue =" +
                " 'f'))) " +
                "define stream FooStream (a string, b long, c int,d bool,e float,f double, metadata string);";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(inStreamDefinition);
        InputHandler fooStream = siddhiAppRuntime.getInputHandler("FooStream");
        siddhiAppRuntime.start();
        fooStream.send(new Object[]{"Test 01", 10000L, 60, true, 522.7586f, 34.5668});
        Thread.sleep(1000);
        fooStream.send(new Object[]{"Test 02", 10000L, 60, false, 768.987f, 34.5668});
        Thread.sleep(1000);
        siddhiAppRuntime.shutdown();

        Assert.assertTrue(((TestAppender) logger.getAppenders().
                get("TestAppender")).getMessages().contains("Metadata received: name: John"));
        Assert.assertTrue(((TestAppender) logger.getAppenders().
                get("TestAppender")).getMessages().contains("Metadata received: age: 23"));
        logger.removeAppender(appender);

    }


    @Test
    public void testCase_SendAsStream() throws Exception {
        log.info("Test case to call send");
        SiddhiManager siddhiManager = new SiddhiManager();
        TestAppender appender = new TestAppender("TestAppender", null);
        final Logger logger = (Logger) LogManager.getRootLogger();
        logger.setLevel(Level.ALL);
        logger.addAppender(appender);
        appender.start();

        String inStreamDefinition = ""
                + "@sink(type='grpc', publisher.url = 'grpc://localhost:" + port + "/" + packageName +
                ".StreamService/clientStream', " +
                "@map(type='protobuf')) " +
                "define stream FooStream (stringValue string, intValue int,longValue long,booleanValue bool," +
                "floatValue float,doubleValue double);";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(inStreamDefinition);
        InputHandler fooStream = siddhiAppRuntime.getInputHandler("FooStream");

        siddhiAppRuntime.start();
        for (int i = 1; i <= 20; i++) {
            fooStream.send(new Object[]{"Test " + i, i, i * 1000L, true, 10.456f * i, 34.5668 * i});
        }
        Thread.sleep(1000); //wait till data sends to the server
        siddhiAppRuntime.shutdown();
        Thread.sleep(1000);

        Assert.assertTrue(((TestAppender) logger.getAppenders().
                get("TestAppender")).getMessages().contains("Done Streaming"));
        logger.removeAppender(appender);
    }

    @Test
    public void testCase0_SendAsStream_SendMap() throws Exception {
        log.info("Test case to call send");
        SiddhiManager siddhiManager = new SiddhiManager();
        TestAppender appender = new TestAppender("TestAppender", null);
        final Logger logger = (Logger) LogManager.getRootLogger();
        logger.setLevel(Level.ALL);
        logger.addAppender(appender);
        appender.start();

        String inStreamDefinition = ""
                + "@sink(type='grpc', publisher.url = 'grpc://localhost:" + port + "/" + packageName +
                ".StreamService/clientStreamWithMap', " +
                "@map(type='protobuf')) " +
                "define stream FooStream (stringValue string, intValue int, map object);";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(inStreamDefinition);
        InputHandler fooStream = siddhiAppRuntime.getInputHandler("FooStream");

        siddhiAppRuntime.start();
        Map<String, String> map = new HashMap<>();

        for (int i = 1; i <= 20; i++) {
            map.put("Key " + i, "Value " + i);
            fooStream.send(new Object[]{"Test " + i, i, map});
            map.clear();
        }
        Thread.sleep(1000);
        siddhiAppRuntime.shutdown();
        Thread.sleep(1000);
        Assert.assertTrue(((TestAppender) logger.getAppenders().
                get("TestAppender")).getMessages().contains("Done Streaming"));
        logger.removeAppender(appender);

    }


}
