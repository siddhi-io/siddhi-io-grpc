package io.siddhi.extension.io.grpc.sink;

import io.siddhi.core.SiddhiAppRuntime;
import io.siddhi.core.SiddhiManager;
import io.siddhi.core.event.Event;
import io.siddhi.core.query.output.callback.QueryCallback;
import io.siddhi.core.stream.input.InputHandler;
import io.siddhi.core.util.EventPrinter;
import io.siddhi.extension.io.grpc.utils.TestServer;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.concurrent.atomic.AtomicInteger;

public class DemoTest {
    private static final Logger logger = Logger.getLogger(GrpcSinkTestCase.class.getName());
//    private TestServer server = new TestServer(8889);
    private AtomicInteger eventCount = new AtomicInteger(0);

    @Test
    public void test1() throws Exception {
        logger.info("Test case to call process sending a request");
        logger.setLevel(Level.DEBUG);
        SiddhiManager siddhiManager = new SiddhiManager();

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
                            Assert.assertEquals((String) inEvents[i].getData()[0], "Hello from Server!");
                            break;
                        default:
                            Assert.fail();
                    }
                }
            }
        });
        InputHandler fooStream = siddhiAppRuntime.getInputHandler("FooStream");

        siddhiAppRuntime.start();
        fooStream.send(new Object[]{"Request 1"});
        Thread.sleep(1000);
        siddhiAppRuntime.shutdown();
    }
}
