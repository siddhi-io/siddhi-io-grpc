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
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.io.IOException;

public class GrpcSinkGenericTestCases {
    private static final Logger log = Logger.getLogger(GrpcSinkTestCase.class.getName());
    private GenericTestServer server = new GenericTestServer();

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
        log.info("Test case to call send");
        log.setLevel(Level.DEBUG);
        SiddhiManager siddhiManager = new SiddhiManager();

        String inStreamDefinition = ""
                + "@sink(type='grpc', publisher.url = 'grpc://localhost:8888/org.wso2.grpc.test.MyService/send', " +
                "@map(type='protobuf')) " +
                "define stream FooStream (stringValue string, intValue int,longValue long,booleanValue bool," +
                "floatValue float,doubleValue double);";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(inStreamDefinition);
        InputHandler fooStream = siddhiAppRuntime.getInputHandler("FooStream");

        siddhiAppRuntime.start();
        fooStream.send(new Object[]{"Test 01", 60, 10000L, true, 522.7586f, 34.5668});
        Thread.sleep(1000);
        siddhiAppRuntime.shutdown();

    }


    @Test
    public void testWithHeader2() throws Exception {
        log.info("Test case to call send");
        log.setLevel(Level.DEBUG);
        SiddhiManager siddhiManager = new SiddhiManager();

        String inStreamDefinition = ""
                + "@sink(type='grpc', " +
                "publisher.url = 'grpc://localhost:8888/org.wso2.grpc.test.MyService/send', " +
                "metadata='{{metadata}}', " +
                "@map(type='protobuf', " +
                "@payload(stringValue='a',longValue='b',intValue='c',booleanValue='d',floatValue = 'e', doubleValue =" +
                " 'f'))) " +
                "define stream FooStream (a string, b long, c int,d bool,e float,f double, metadata string);";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(inStreamDefinition);
        InputHandler fooStream = siddhiAppRuntime.getInputHandler("FooStream");
        siddhiAppRuntime.start();
        fooStream.send(new Object[]{"Test 01", 10000L, 60, true, 522.7586f, 34.5668, "'Name:Sahan','Age:21'," +
                "'Content-Type:json'"});
        Thread.sleep(1000);
        fooStream.send(new Object[]{"Test 02", 10000L, 60, false, 768.987f, 34.5668, "'Name:Sahan','Age:21'," +
                "'Content-Type:json'"});
        Thread.sleep(1000);

        siddhiAppRuntime.shutdown();


    }

}
