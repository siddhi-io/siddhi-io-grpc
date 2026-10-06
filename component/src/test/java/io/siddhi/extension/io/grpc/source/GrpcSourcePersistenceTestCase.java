/*
 * Copyright (c) 2026, WSO2 LLC. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
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
import io.grpc.stub.StreamObserver;
import io.siddhi.core.SiddhiAppRuntime;
import io.siddhi.core.SiddhiManager;
import io.siddhi.core.stream.output.StreamCallback;
import io.siddhi.core.util.persistence.InMemoryPersistenceStore;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.wso2.grpc.Event;
import org.wso2.grpc.EventServiceGrpc;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Test cases for grpc-source with state persistence enabled.
 */
public class GrpcSourcePersistenceTestCase {
    private static final String PORT = "8185";

    private static String app(String name, String stream) {
        return "@App:name('" + name + "') " +
                "@source(type='grpc', receiver.url = 'grpc://localhost:" + PORT +
                "/org.wso2.grpc.EventService/consume', @map(type='json')) " +
                "define stream " + stream + " (message String); " +
                "from " + stream + " select * insert into OutputStream;";
    }

    private static void send(String stream, String message) throws InterruptedException {
        ManagedChannel channel = ManagedChannelBuilder.forTarget("localhost:" + PORT).usePlaintext().build();
        StreamObserver<Event> requestObserver = EventServiceGrpc.newStub(channel).consume(
                new StreamObserver<Empty>() {
                    @Override
                    public void onNext(Empty value) {
                    }

                    @Override
                    public void onError(Throwable t) {
                    }

                    @Override
                    public void onCompleted() {
                    }
                });
        requestObserver.onNext(Event.newBuilder().setPayload("{ \"message\": \"" + message + "\"}")
                .putHeaders("stream.id", stream).build());
        requestObserver.onCompleted();
        Thread.sleep(1000);
        channel.shutdown();
        channel.awaitTermination(1, TimeUnit.SECONDS);
    }

    @Test
    public void testRestoreBeforeStart() throws Exception {
        SiddhiManager siddhiManager = new SiddhiManager();
        siddhiManager.setPersistenceStore(new InMemoryPersistenceStore());
        SiddhiAppRuntime runtime = siddhiManager.createSiddhiAppRuntime(app("RestoreBeforeStart", "FooStream"));
        AtomicInteger count = new AtomicInteger();
        runtime.addCallback("OutputStream", new StreamCallback() {
            @Override
            public void receive(io.siddhi.core.event.Event[] events) {
                count.addAndGet(events.length);
            }
        });
        try {
            runtime.restoreLastRevision();
            runtime.start();
            runtime.persist().getFullStateFuture().get(5, TimeUnit.SECONDS);
            send("FooStream", "after restore");
            Assert.assertEquals(count.get(), 1);
        } finally {
            siddhiManager.shutdown();
        }
    }

    @Test
    public void testPersistWithSharedPort() throws Exception {
        SiddhiManager siddhiManager = new SiddhiManager();
        siddhiManager.setPersistenceStore(new InMemoryPersistenceStore());
        SiddhiAppRuntime first = siddhiManager.createSiddhiAppRuntime(app("SharedPortFirst", "FirstStream"));
        SiddhiAppRuntime second = siddhiManager.createSiddhiAppRuntime(app("SharedPortSecond", "SecondStream"));
        AtomicInteger count = new AtomicInteger();
        second.addCallback("OutputStream", new StreamCallback() {
            @Override
            public void receive(io.siddhi.core.event.Event[] events) {
                count.addAndGet(events.length);
            }
        });
        try {
            first.start();
            second.start();
            second.persist().getFullStateFuture().get(5, TimeUnit.SECONDS);
            send("SecondStream", "after persist");
            Assert.assertEquals(count.get(), 1);
        } finally {
            siddhiManager.shutdown();
        }
    }

    @Test
    public void testServiceSourceRestoreBeforeStart() throws Exception {
        SiddhiManager siddhiManager = new SiddhiManager();
        siddhiManager.setPersistenceStore(new InMemoryPersistenceStore());
        SiddhiAppRuntime runtime = siddhiManager.createSiddhiAppRuntime("@App:name('ServiceRestoreBeforeStart') " +
                "@source(type='grpc-service', receiver.url = 'grpc://localhost:" + PORT +
                "/org.wso2.grpc.EventService/process', source.id='persistence', " +
                "@map(type='json', @attributes(messageId='trp:message.id', message='message'))) " +
                "define stream RequestStream (messageId String, message String); " +
                "@sink(type='grpc-service-response', source.id='persistence', message.id='{{messageId}}', " +
                "@map(type='json')) " +
                "define stream ResponseStream (messageId String, message String); " +
                "from RequestStream select * insert into ResponseStream;");
        ManagedChannel channel = ManagedChannelBuilder.forTarget("localhost:" + PORT).usePlaintext().build();
        try {
            runtime.restoreLastRevision();
            runtime.start();
            runtime.persist().getFullStateFuture().get(5, TimeUnit.SECONDS);
            Event response = EventServiceGrpc.newBlockingStub(channel).withDeadlineAfter(10, TimeUnit.SECONDS)
                    .process(Event.newBuilder().setPayload("{ \"message\": \"after restore\"}")
                            .putHeaders("stream.id", "RequestStream").build());
            Assert.assertTrue(response.getPayload().contains("after restore"));
        } finally {
            channel.shutdown();
            channel.awaitTermination(1, TimeUnit.SECONDS);
            siddhiManager.shutdown();
        }
    }
}
