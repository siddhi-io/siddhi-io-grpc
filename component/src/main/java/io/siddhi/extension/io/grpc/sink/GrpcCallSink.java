package io.siddhi.extension.io.grpc.sink;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.MoreExecutors;
import io.siddhi.annotation.Example;
import io.siddhi.annotation.Extension;
import io.siddhi.annotation.Parameter;
import io.siddhi.annotation.util.DataType;
import io.siddhi.core.exception.ConnectionUnavailableException;
import io.siddhi.core.exception.SiddhiAppRuntimeException;
import io.siddhi.core.util.snapshot.state.State;
import io.siddhi.core.util.transport.DynamicOptions;
import io.siddhi.core.util.transport.OptionHolder;
import org.apache.log4j.Logger;
import org.wso2.grpc.Event;

/**
 * {@code GrpcCallSink} Handle the gRPC publishing tasks.
 */
@Extension(
        name = "grpc-call", namespace = "sink",
        description = "This extension publishes event data encoded into GRPC Classes as defined in the user input " +
                "jar. This extension has a default gRPC service classes jar added. The default service is called " +
                "\"EventService\" and it has 2 rpc's. They are process and consume. Process sends a request of type " +
                "Event and receives a response of the same type. Consume sends a request of type Event and expects " +
                "no response from gRPC server. Please note that the Event type mentioned here is not " +
                "io.siddhi.core.event.Event but a type defined in the default service protobuf given in the readme.",
        parameters = {
                @Parameter(name = "url",
                        description = "The url to which the outgoing events should be published via this extension. " +
                                "This url should consist the host address, port, service name, method name in the " +
                                "following format. hostAddress:port/serviceName/methodName" ,
                        type = {DataType.STRING}),
                @Parameter(name = "sink.id",
                        description = "a unique ID that should be set for each gRPC sink. There is a 1:1 mapping " +
                                "between gRPC sinks and sources. Each sink has one particular source listening to " +
                                "the responses to requests published from that sink. So the same sink.id should be " +
                                "given when writing the source also." ,
                        type = {DataType.INT}),
                @Parameter(name = "sequence",
                        description = "This is an optional parameter to be used when connecting to Micro Integrator " +
                                "sequences from Siddhi. Micro integrator will expose a service called EventService " +
                                "which has 2 rpc's as mentioned in the extension description. Both of these rpc can " +
                                "access many different sequences in Micro Integrator. This parameter is used to " +
                                "specify the sequence which we want to use. When this parameter is given gRPC sink " +
                                "will comunicate with MI. Json map type should be used in this case to encode event " +
                                "data and send to MI" ,
                        optional = true, defaultValue = "NA. When sequence is not given the service name and method " +
                        "name should be specified in the url",
                        type = {DataType.STRING}),
        },
        examples = {
                @Example(
                        syntax = "@sink(type='grpc', " +
                                "url = '194.23.98.100:8080/EventService/process', " +
                                "sequence = 'mySeq', " +
                                "sink.id= '1', @map(type='json')) "
                                + "define stream FooStream (message String);",
                        description = "Here a stream named FooStream is defined with grpc sink. Since sequence is " +
                                "specified here sink will be in default mode. i.e communicating to MI. The " +
                                "MicroIntegrator should be running at 194.23.98.100 host and listening on port 8080. " +
                                "The sequence called mySeq will be accessed. sink.id is set to 1 here. So we can " +
                                "write a source with sink.id 1 so that it will listen to responses for requests " +
                                "published from this stream."
                        //todo: add an example for generic service access
                )
        }
)
public class GrpcCallSink extends AbstractGrpcSink {
    private static final Logger logger = Logger.getLogger(GrpcCallSink.class.getName());

    @Override
    void initSink(OptionHolder optionHolder) {

    }

    @Override
    public void publish(Object payload, DynamicOptions dynamicOptions, State state)
            throws ConnectionUnavailableException {
        if (isDefaultMode) {
            if (!(payload instanceof String)) {
                throw new SiddhiAppRuntimeException(siddhiAppContext.getName() + ": Payload should be of type String " +
                        "for communicating with Micro Integrator but found " + payload.getClass().getName());
            }
            Event.Builder requestBuilder = Event.newBuilder();
            requestBuilder.setPayload((String) payload);
            Event sequenceCallRequest = requestBuilder.build();
            ListenableFuture<Event> futureResponse =
                    futureStub.process(sequenceCallRequest);
            Futures.addCallback(futureResponse, new FutureCallback<Event>() {
                @Override
                public void onSuccess(Event result) {
                    grpcSourceRegistry.getGrpcCallResponseSourceSource(sinkID).onResponse(result);
                }

                @Override
                public void onFailure(Throwable t) {
                    if (logger.isDebugEnabled()) {
                        logger.debug(siddhiAppContext.getName() + ": " + t.getMessage());
                    }
                    throw new SiddhiAppRuntimeException(t.getMessage());
                }
            }, MoreExecutors.directExecutor());
        } else {
            //todo: handle publishing to generic service
        }
    }
}
