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
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;
import io.siddhi.annotation.Example;
import io.siddhi.annotation.Extension;
import io.siddhi.annotation.Parameter;
import io.siddhi.annotation.util.DataType;
import io.siddhi.core.util.transport.OptionHolder;
import org.wso2.grpc.Event;
import org.wso2.grpc.EventServiceGrpc;

/**
 * This handles receiving requests from grpc clients and populating the stream
 */
@Extension(
        name = "grpc",
        namespace = "source",
        description = "This extension starts a grpc server during initialization time. The server listens to " +
                "requests from grpc stubs. This source has a default mode of operation and custom user defined grpc " +
                "service mode. In the default mode this source will use EventService consume method. This method " +
                "will receive requests and injects them into stream through a mapper.",
        parameters = {
                @Parameter(name = "url",
                        description = "The url which can be used by a client to access the grpc server in this " +
                                "extension. This url should consist the host address, port, service name, method " +
                                "name in the following format. grpc://hostAddress:port/serviceName/methodName" ,
                        type = {DataType.STRING}),
        },
        examples = {
                @Example(
                        syntax = "@source(type='grpc', " +
                                "url='grpc://locanhost:8888/org.wso2.grpc.EventService/consume', @map(type='json')) " +
                                "define stream BarStream (message String);",
                        description = "Here the port is given as 8888. So a grpc server will be started on port 8888 " +
                                "and the server will expose EventService. This is the default service packed with " +
                                "the source. In EventService the consume method is"
                )
        }
)
public class GrpcSource extends AbstractGrpcSource {
    @Override
    public void initializeGrpcServer(int port) {
        if (isDefaultMode) {
            this.server = ServerBuilder.forPort(port).addService(new EventServiceGrpc.EventServiceImplBase() {
                @Override
                public void consume(Event request,
                                    StreamObserver<Empty> responseObserver) {
                    sourceEventListener.onEvent(request.getPayload(), new String[]{"1"});
                    responseObserver.onNext(Empty.getDefaultInstance());
                    responseObserver.onCompleted();
                }
            }).build();
        } else {
            //todo: generic server logic here
        }
    }

    @Override
    public void initSource(OptionHolder optionHolder) {

    }
}
