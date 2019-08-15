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
import io.siddhi.extension.io.grpc.utils.GrpcTestUtil;
import io.siddhi.extension.io.grpc.utils.TestAppender;
import io.siddhi.extension.io.grpc.utils.TestServer;
import io.siddhi.extension.io.grpc.utils.TestTLSServer;
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

public class GrpcSinkAuthTestCase {
    private static final Logger log = Logger.getLogger(GrpcSinkTestCase.class.getName());
    private TestTLSServer server = new TestTLSServer(8888);
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
//        System.exit(0);
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
    public void testCaseToCallConsumeWithSimpleRequest2() throws Exception {
        GrpcTestUtil.setCarbonHome();
        Map<String, String> masterConfigs = new HashMap<>();
        masterConfigs.put("sink.http.keyStoreLocation", "${carbon.home}/resources/security/wso2carbon.jks");

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

    }
}
