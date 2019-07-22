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
import org.testng.annotations.Test;

public class TestCaseOfGrpcSink {
    private static final Logger logger = Logger.getLogger(TestCaseOfGrpcSink.class.getName());
    private TestServer server = new TestServer();
        @Test
        public void test1() throws Exception {
            logger.info("Test case to call process");
            logger.setLevel(Level.DEBUG);
            SiddhiManager siddhiManager = new SiddhiManager();

            server.start();
            String port = String.valueOf(server.getPort());
            String inStreamDefinition = ""
                    + "@sink(type='grpc', " +
                    "url = 'dns:///localhost:" + port + "/EventService/process', " +
                    "sequence = 'mySeq', " +
                    "sink.id= '1', @map(type='json')) "
                    + "define stream FooStream (message String);";

            String stream2 = "@source(type='grpc', sequence='mySeq', sink.id= '1') " +
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
                }
            });
            InputHandler fooStream = siddhiAppRuntime.getInputHandler("FooStream");

            try {
                siddhiAppRuntime.start();

                fooStream.send(new Object[]{"niruhan"});
                fooStream.send(new Object[]{"niru"});

                Thread.sleep(5000);
                siddhiAppRuntime.shutdown();
            } finally {
                server.stop();
            }
        }

    @Test
    public void test2() throws Exception {
        logger.info("Test case to call consume");
        logger.setLevel(Level.DEBUG);
        SiddhiManager siddhiManager = new SiddhiManager();

        server.start();
        String port = String.valueOf(server.getPort());
        String inStreamDefinition = ""
                + "@sink(type='grpc', " +
                "url = 'dns:///localhost:" + port + "/EventService/consume', " +
                "sequence = 'mySeq', " +
                "sink.id= '1', @map(type='json')) "
                + "define stream FooStream (message String);";

        String stream2 = "@source(type='grpc', sequence='mySeq', sink.id= '1') " +
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
            }
        });
        InputHandler fooStream = siddhiAppRuntime.getInputHandler("FooStream");

        try {
            siddhiAppRuntime.start();

            fooStream.send(new Object[]{"niruhan"});
            fooStream.send(new Object[]{"niru"});

            Thread.sleep(5000);
            siddhiAppRuntime.shutdown();
        } finally {
            server.stop();
        }
    }

//    @Test
//    public void test2() throws Exception {
//        SiddhiManager siddhiManager = new SiddhiManager();
//
//        startServer();
//        String port = String.valueOf(server.getPort());
//        String inStreamDefinition = ""
//                + "@sink(type='grpc', " +
//                "host = 'dns:///localhost', " +
//                "port = '" + port + "', " +
//                "sequence = 'mySeq', " +
//                "response = 'true', " +
//                "sink.id= '1', @map(type='protobuf', mode='MIConnect')) "
//                + "define stream FooStream (message String);";
//
//        String stream2 = "@source(type='grpc', sequence='mySeq', response='true', sink.id= '1') " +
//                "define stream BarStream (message String);";
//        String query = "@info(name = 'query') "
//                + "from BarStream "
//                + "select *  "
//                + "insert into outputStream;";
//
//        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(inStreamDefinition + stream2 +
//                query);
//        siddhiAppRuntime.addCallback("query", new QueryCallback() {
//            @Override
//            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
//                EventPrinter.print(timeStamp, inEvents, removeEvents);
//            }
//        });
//        InputHandler fooStream = siddhiAppRuntime.getInputHandler("FooStream");
//
//        try {
//            siddhiAppRuntime.start();
//
//            fooStream.send(new Object[]{"niruhan"});
//            fooStream.send(new Object[]{"niruhan"});
//
//            Thread.sleep(5000);
//            siddhiAppRuntime.shutdown();
//        } finally {
//            stopServer();
//        }
//    }
}
