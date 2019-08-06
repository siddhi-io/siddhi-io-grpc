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
import io.siddhi.extension.io.grpc.TestServer;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.concurrent.atomic.AtomicInteger;

public class GrpcCallSinkTestCase {
    private static final Logger logger = Logger.getLogger(GrpcSinkTestCase.class.getName());
    private TestServer server = new TestServer();
    private AtomicInteger eventCount = new AtomicInteger(0);


    @Test
    public void test1() throws Exception {
        logger.info("Test case to call process sending 2 requests");
        logger.setLevel(Level.DEBUG);
        SiddhiManager siddhiManager = new SiddhiManager();

        server.start();
        String port = String.valueOf(server.getPort());
        String inStreamDefinition = ""
                + "@sink(type='grpc-call', " +
                "url = 'grpc://localhost:8888/org.wso2.grpc.EventService/process/mySeq', " +
                "sink.id= '1', @map(type='json')) "
                + "define stream FooStream (message String);";

        String stream2 = "@source(type='grpc-call-response', sequence='mySeq', sink.id= '1', @map(type='json')) " +
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
                            Assert.assertEquals((String) inEvents[i].getData()[0], "Benjamin Watson");
                            break;
                        default:
                            Assert.fail();
                    }
                }
            }
        });
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


    @Test
    public void test2() throws Exception {
        logger.info("Test case to call process sending 2 requests");
        logger.setLevel(Level.DEBUG);
        SiddhiManager siddhiManager = new SiddhiManager();

//        server.start();

        String inStreamDefinition = ""
                + "@sink(type='grpc-call', " +
                "url = 'grpc://localhost:8888/package01.test.MyService/process', " +
                "sink.id= '1', @map(type='protobuf')) "
                + "define stream FooStream (stringValue string, intValue int,longValue long,booleanValue bool,floatValue float,doubleValue double);";

        String stream2 = "@source(type='grpc-call-response', url = 'grpc://localhost:8888/package01.test.MyService/process'," +
                "sequence='mySeq', sink.id= '1', @map(type='protobuf')) " +
                "define stream BarStream (stringValue string, intValue int,longValue long,booleanValue bool,floatValue float,doubleValue double);";
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
                        case 0: {
                            Assert.assertEquals(inEvents[i].getData()[0], "Received");
                            Assert.assertEquals(inEvents[i].getData()[1], 200 * 2);
                            Assert.assertEquals(inEvents[i].getData()[2], 10000L * 2);
                            Assert.assertEquals(inEvents[i].getData()[3], true);
                            Assert.assertEquals(inEvents[i].getData()[4], 522.7586f * 2);
                            Assert.assertEquals(inEvents[i].getData()[5], 34.5668 * 2);
                            break;
                        }
/*
                        case 1: {
                            Assert.assertEquals(inEvents[i].getData()[0], "Received");
                            Assert.assertEquals(inEvents[i].getData()[1], 524 * 2);
                            Assert.assertEquals(inEvents[i].getData()[2], 53335L * 2);
                            Assert.assertEquals(inEvents[i].getData()[3], false);
                            Assert.assertEquals(inEvents[i].getData()[4], 2000.99f * 2);
                            Assert.assertEquals(inEvents[i].getData()[5], 2365.456 * 2);
                            break;
                        }*/

                        default:
                            Assert.fail();
                    }
                }
            }
        });
        InputHandler fooStream = siddhiAppRuntime.getInputHandler("FooStream");
        try {
            siddhiAppRuntime.start();
            fooStream.send(new Object[]{"Data 01", 200, 10000L, false, 522.7586f, 34.5668});
//            fooStream.send(new Object[]{"Data 02", 524, 53335L, true, 2000.99f, 2365.456});
            Thread.sleep(1000);
            siddhiAppRuntime.shutdown();
        } finally {
//            server.stop();
        }
    }
}
