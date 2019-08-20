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
import io.siddhi.extension.io.grpc.utils.TestTLSServer;
import org.apache.log4j.Logger;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyStoreException;
import java.util.concurrent.atomic.AtomicInteger;

public class GrpcSinkAuthTestCase {
    private static final Logger log = Logger.getLogger(GrpcSinkTestCase.class.getName());
    private TestTLSServer server = new TestTLSServer(8888);
    public static final String CARBON_HOME = "carbon.home";

    public GrpcSinkAuthTestCase() throws KeyStoreException {
    }

    @BeforeTest
    public void init() throws IOException {
        setCarbonHome();
        server.start();
    }

    @AfterTest
    public void stop() throws InterruptedException {
        server.stop();
    }

    private void setCarbonHome() {
        Path carbonHome = Paths.get("");
        carbonHome = Paths.get(carbonHome.toString(), "src", "test");
        System.setProperty(CARBON_HOME, carbonHome.toString());
//        logger.info("Carbon Home Absolute path set to: " + carbonHome.toAbsolutePath());

    }

    @Test
    public void testCaseToCallConsumeWithSimpleRequest() throws Exception {
//        System.exit(0);
//        log.info("Test case to call consume");
//        final TestAppender appender = new TestAppender();
//        final Logger rootLogger = Logger.getRootLogger();
//        rootLogger.setLevel(Level.DEBUG);
//        rootLogger.addAppender(appender);
        SiddhiManager siddhiManager = new SiddhiManager();

        String inStreamDefinition = ""
                + "@sink(type='grpc', url = 'grpc://localhost:8888/org.wso2.grpc.EventService/consume', tls='true', " +
                "@map(type='json', @payload('{{message}}'))) " +
                "define stream FooStream (message String);";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(inStreamDefinition);
        InputHandler fooStream = siddhiAppRuntime.getInputHandler("FooStream");

        siddhiAppRuntime.start();
        fooStream.send(new Object[]{"Request 1"});
        Thread.sleep(1000);
        siddhiAppRuntime.shutdown();

//        final List<LoggingEvent> log = appender.getLog();
//        List<String> logMessages = new ArrayList<>();
//        for (LoggingEvent logEvent : log) {
//            String message = String.valueOf(logEvent.getMessage());
//            logMessages.add(message);
//        }
//        Assert.assertTrue(logMessages.contains("Server consume hit with [Request 1]"));
    }
}
