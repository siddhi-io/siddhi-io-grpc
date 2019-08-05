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
package io.siddhi.extension.io.grpc.source;

import com.google.protobuf.Empty;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Metadata;
import io.grpc.stub.MetadataUtils;
import io.siddhi.core.SiddhiAppRuntime;
import io.siddhi.core.SiddhiManager;
import io.siddhi.core.query.output.callback.QueryCallback;
import io.siddhi.core.util.EventPrinter;
import io.siddhi.extension.io.grpc.util.GrpcConstants;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.wso2.grpc.Event;
import org.wso2.grpc.EventServiceGrpc;

import java.util.concurrent.atomic.AtomicInteger;

public class GrpcSourceTestCase {
    private static final Logger logger = Logger.getLogger(GrpcSourceTestCase.class.getName());
    private AtomicInteger eventCount = new AtomicInteger(0);

    @Test
    public void test1() throws Exception {
        logger.info("Test case to call process");
        logger.setLevel(Level.DEBUG);
        SiddhiManager siddhiManager = new SiddhiManager();

        String stream2 = "@source(type='grpc', url='grpc://localhost:8888/org.wso2.grpc.EventService/consume', " +
                "@map(type='json')) " +
                "define stream BarStream (message String);";
        String query = "@info(name = 'query') "
                + "from BarStream "
                + "select *  "
                + "insert into outputStream;";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(stream2 + query);
        siddhiAppRuntime.addCallback("query", new QueryCallback() {
            @Override
            public void receive(long timeStamp, io.siddhi.core.event.Event[] inEvents,
                                io.siddhi.core.event.Event[] removeEvents) {
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

        Event.Builder requestBuilder = Event.newBuilder();

        String json = "{ \"message\": \"Benjamin Watson\"}";

        requestBuilder.setPayload(json);
        Event sequenceCallRequest = requestBuilder.build();
        ManagedChannel channel = ManagedChannelBuilder.forTarget("localhost:8888")
                .usePlaintext(true)
                .build();
        EventServiceGrpc.EventServiceBlockingStub blockingStub = EventServiceGrpc.newBlockingStub(channel);

        siddhiAppRuntime.start();
        Empty emptyResponse = blockingStub.consume(sequenceCallRequest);
        Thread.sleep(1000);
        siddhiAppRuntime.shutdown();
    }

    @Test
    public void testWithHeaders() throws Exception {
        logger.info("Test case to call process");
        logger.setLevel(Level.DEBUG);
        SiddhiManager siddhiManager = new SiddhiManager();

        String stream2 = "@source(type='grpc', url='grpc://localhost:8888/org.wso2.grpc.EventService/consume', " +
                "@map(type='json', @attributes(name='trp:name', age='trp:age', message='message'))) " +
                "define stream BarStream (message String, name String, age int);";
        String query = "@info(name = 'query') "
                + "from BarStream "
                + "select *  "
                + "insert into outputStream;";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(stream2 + query);
        siddhiAppRuntime.addCallback("query", new QueryCallback() {
            @Override
            public void receive(long timeStamp, io.siddhi.core.event.Event[] inEvents,
                                io.siddhi.core.event.Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                for (int i = 0; i < inEvents.length; i++) {
                    eventCount.incrementAndGet();
                    switch (i) {
                        case 0:
                            Assert.assertEquals((String) inEvents[i].getData()[0], "Hello !");
                            break;
                        default:
                            Assert.fail();
                    }
                }
            }
        });

        Event.Builder requestBuilder = Event.newBuilder();

        String json = "{ \"message\": \"Hello !\"}";

        requestBuilder.setPayload(json);
        Event sequenceCallRequest = requestBuilder.build();
        ManagedChannel channel = ManagedChannelBuilder.forTarget("localhost:8888")
                .usePlaintext(true)
                .build();
        EventServiceGrpc.EventServiceBlockingStub blockingStub = EventServiceGrpc.newBlockingStub(channel);

        Metadata header = new Metadata();
        String headers = "'Name:John', 'Age:23'";
        Metadata.Key<String> key =
                Metadata.Key.of(GrpcConstants.HEADERS, Metadata.ASCII_STRING_MARSHALLER);
        header.put(key, headers);
        blockingStub = MetadataUtils.attachHeaders(blockingStub, header);

        siddhiAppRuntime.start();
        Empty emptyResponse = blockingStub.consume(sequenceCallRequest);
        Thread.sleep(1000);
        siddhiAppRuntime.shutdown();
    }

    @Test
    public void testWithIncompleteHeaders() throws Exception {
        logger.info("Test case to call process");
        logger.setLevel(Level.DEBUG);
        SiddhiManager siddhiManager = new SiddhiManager();

        String stream2 = "@source(type='grpc', url='grpc://localhost:8888/org.wso2.grpc.EventService/consume', " +
                "@map(type='json', @attributes(name='trp:name', age='trp:age', message='message'))) " +
                "define stream BarStream (message String, name String, age int);";
        String query = "@info(name = 'query') "
                + "from BarStream "
                + "select *  "
                + "insert into outputStream;";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(stream2 + query);
        siddhiAppRuntime.addCallback("query", new QueryCallback() {
            @Override
            public void receive(long timeStamp, io.siddhi.core.event.Event[] inEvents,
                                io.siddhi.core.event.Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                for (int i = 0; i < inEvents.length; i++) {
                    eventCount.incrementAndGet();
                    switch (i) {
                        case 0:
                            Assert.assertEquals((String) inEvents[i].getData()[0], "Hello !");
                            break;
                        default:
                            Assert.fail();
                    }
                }
            }
        });

        Event.Builder requestBuilder = Event.newBuilder();

        String json = "{ \"message\": \"Hello !\"}";

        requestBuilder.setPayload(json);
        Event sequenceCallRequest = requestBuilder.build();
        ManagedChannel channel = ManagedChannelBuilder.forTarget("localhost:8888")
                .usePlaintext(true)
                .build();
        EventServiceGrpc.EventServiceBlockingStub blockingStub = EventServiceGrpc.newBlockingStub(channel);

        Metadata header = new Metadata();
        String headers = "'Name:John'";
        Metadata.Key<String> key =
                Metadata.Key.of(GrpcConstants.HEADERS, Metadata.ASCII_STRING_MARSHALLER);
        header.put(key, headers);
        blockingStub = MetadataUtils.attachHeaders(blockingStub, header);

        siddhiAppRuntime.start();
        Empty emptyResponse = blockingStub.consume(sequenceCallRequest);
        Thread.sleep(1000);
        siddhiAppRuntime.shutdown();
    }

    @Test
    public void test () {
        long a = 1L << 24;
        System.out.println(a);
    }
}
