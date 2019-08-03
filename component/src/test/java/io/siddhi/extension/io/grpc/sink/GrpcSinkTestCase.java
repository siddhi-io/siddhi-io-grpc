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
import io.siddhi.extension.io.grpc.TestServer;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.testng.annotations.Test;

import java.util.concurrent.atomic.AtomicInteger;

public class GrpcSinkTestCase {
    private static final Logger logger = Logger.getLogger(GrpcSinkTestCase.class.getName());
    private TestServer server = new TestServer();
    private AtomicInteger eventCount = new AtomicInteger(0);

    @Test
    public void test1() throws Exception {
        logger.info("Test case to call process");
        logger.setLevel(Level.DEBUG);
        SiddhiManager siddhiManager = new SiddhiManager();

        server.start();
        String inStreamDefinition = ""
                + "@sink(type='grpc', url = 'grpc://localhost:8888/org.wso2.grpc.EventService/consume/mySeq', " +
                "@map(type='json')) " +
                "define stream FooStream (message String);";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(inStreamDefinition);
        InputHandler fooStream = siddhiAppRuntime.getInputHandler("FooStream");
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
