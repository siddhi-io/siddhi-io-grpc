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
import io.siddhi.extension.io.grpc.utils.TestAppender;
import io.siddhi.extension.io.grpc.utils.TestTLSServer;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.spi.LoggingEvent;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyStoreException;
import java.util.ArrayList;
import java.util.List;

public class GrpcSinkAuthTestCase {
    private static final Logger log = Logger.getLogger(GrpcSinkTestCase.class.getName());
    public static final String CARBON_HOME = "carbon.home";

    public GrpcSinkAuthTestCase() throws KeyStoreException {
    }

    private void setCarbonHome() {
        Path carbonHome = Paths.get("");
        carbonHome = Paths.get(carbonHome.toString(), "src", "test");
        System.setProperty(CARBON_HOME, carbonHome.toString());

    }

    @Test
    public void testForServerAuthentication() throws Exception {
        TestTLSServer server = new TestTLSServer(8888, false);
        setCarbonHome();
        SiddhiManager siddhiManager = new SiddhiManager();

        String inStreamDefinition = ""
                + "@sink(type='grpc', url = 'grpc://localhost:8888/org.wso2.grpc.EventService/consume'," +
                "truststore.file = 'src/test/resources/security/wso2carbon.jks'," +
                "truststore.password = 'wso2carbon', " +
                "truststore.algorithm = 'SunX509', " +
                "@map(type='json', @payload('{{message}}'))) " +
                "define stream FooStream (message String);";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(inStreamDefinition);
        InputHandler fooStream = siddhiAppRuntime.getInputHandler("FooStream");

        server.start();
        try {
            siddhiAppRuntime.start();
            fooStream.send(new Object[]{"Request 1"});
            Thread.sleep(1000);
            siddhiAppRuntime.shutdown();
        } finally {
            server.stop();
        }
    }

    @Test
    public void testForMutualAuthentication() throws Exception {
        TestTLSServer server = new TestTLSServer(8888, true);
        setCarbonHome();
        SiddhiManager siddhiManager = new SiddhiManager();

        String inStreamDefinition = ""
                + "@sink(type='grpc', url = 'grpc://localhost:8888/org.wso2.grpc.EventService/consume'," +
                "truststore.file = 'src/test/resources/security/wso2carbon.jks'," +
                "truststore.password = 'wso2carbon', " +
                "truststore.algorithm = 'SunX509', " +
                "keystore.file = 'src/test/resources/security/wso2carbon.jks', " +
                "keystore.password = 'wso2carbon', " +
                "keystore.algorithm = 'SunX509', " +
                "@map(type='json', @payload('{{message}}'))) " +
                "define stream FooStream (message String);";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(inStreamDefinition);
        InputHandler fooStream = siddhiAppRuntime.getInputHandler("FooStream");

        server.start();
        try {
            siddhiAppRuntime.start();
            fooStream.send(new Object[]{"Request 1"});
            Thread.sleep(1000);
            siddhiAppRuntime.shutdown();
        } finally {
            server.stop();
        }
    }

    @Test
    public void testCallSinkForServerAuthentication() throws Exception {
        TestTLSServer server = new TestTLSServer(8888, false);
        setCarbonHome();
        SiddhiManager siddhiManager = new SiddhiManager();

        String inStreamDefinition = ""
                + "@sink(type='grpc-call', url = 'grpc://localhost:8888/org.wso2.grpc.EventService/process'," +
                "truststore.file = 'src/test/resources/security/wso2carbon.jks'," +
                "truststore.password = 'wso2carbon', " +
                "truststore.algorithm = 'SunX509', " +
                "sink.id = '1', " +
                "@map(type='json', @payload('{{message}}'))) " +
                "define stream FooStream (message String);";

        String stream2 = "@source(type='grpc-call-response', sink.id= '1', @map(type='json')) " +
                "define stream BarStream (message String);";
        String query = "@info(name = 'query') "
                + "from BarStream "
                + "select *  "
                + "insert into outputStream;";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(inStreamDefinition + stream2 + query);
        InputHandler fooStream = siddhiAppRuntime.getInputHandler("FooStream");

        server.start();
        try {
            siddhiAppRuntime.start();
            fooStream.send(new Object[]{"Request 1"});
            Thread.sleep(1000);
            siddhiAppRuntime.shutdown();
        } finally {
            server.stop();
        }
    }

    @Test
    public void testCallSinkForServerAuthenticationFailure() throws Exception {
        TestTLSServer server = new TestTLSServer(8888, false);
        final TestAppender appender = new TestAppender();
        final Logger rootLogger = Logger.getRootLogger();
        rootLogger.setLevel(Level.DEBUG);
        rootLogger.addAppender(appender);
        setCarbonHome();
        SiddhiManager siddhiManager = new SiddhiManager();

        String inStreamDefinition = ""
                + "@sink(type='grpc-call', url = 'grpc://localhost:8888/org.wso2.grpc.EventService/process'," +
                "sink.id = '1', " +
                "@map(type='json', @payload('{{message}}'))) " +
                "define stream FooStream (message String);";

        String stream2 = "@source(type='grpc-call-response', sink.id= '1', @map(type='json')) " +
                "define stream BarStream (message String);";
        String query = "@info(name = 'query') "
                + "from BarStream "
                + "select *  "
                + "insert into outputStream;";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(inStreamDefinition + stream2 + query);
        InputHandler fooStream = siddhiAppRuntime.getInputHandler("FooStream");

        server.start();
        try {
            siddhiAppRuntime.start();
            fooStream.send(new Object[]{"Request 1"});
            Thread.sleep(1000);
            siddhiAppRuntime.shutdown();
        } finally {
            server.stop();
        }
        final List<LoggingEvent> log = appender.getLog();
        List<String> logMessages = new ArrayList<>();
        for (LoggingEvent logEvent : log) {
            String message = String.valueOf(logEvent.getMessage());
            if (message.contains("FooStream: ")) {
                message = message.split("FooStream: ")[1];
            }
            logMessages.add(message);
        }
        Assert.assertTrue(logMessages.contains("UNAVAILABLE: Network closed for unknown reason"));
    }

    @Test
    public void testCallSinkForMutualAuthentication() throws Exception {
        TestTLSServer server = new TestTLSServer(8888, true);
        setCarbonHome();
        SiddhiManager siddhiManager = new SiddhiManager();

        String inStreamDefinition = ""
                + "@sink(type='grpc-call', url = 'grpc://localhost:8888/org.wso2.grpc.EventService/process'," +
                "truststore.file = 'src/test/resources/security/wso2carbon.jks'," +
                "truststore.password = 'wso2carbon', " +
                "truststore.algorithm = 'SunX509', " +
                "keystore.file = 'src/test/resources/security/wso2carbon.jks', " +
                "keystore.password = 'wso2carbon', " +
                "keystore.algorithm = 'SunX509', " +
                "sink.id = '1', " +
                "@map(type='json', @payload('{{message}}'))) " +
                "define stream FooStream (message String);";
        String stream2 = "@source(type='grpc-call-response', sink.id= '1', @map(type='json')) " +
                "define stream BarStream (message String);";
        String query = "@info(name = 'query') "
                + "from BarStream "
                + "select *  "
                + "insert into outputStream;";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(inStreamDefinition + stream2 + query);
        InputHandler fooStream = siddhiAppRuntime.getInputHandler("FooStream");

        server.start();
        try {
            siddhiAppRuntime.start();
            fooStream.send(new Object[]{"Request 1"});
            Thread.sleep(1000);
            siddhiAppRuntime.shutdown();
        } finally {
            server.stop();
        }
    }
}
