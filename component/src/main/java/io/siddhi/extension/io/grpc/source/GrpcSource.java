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
import org.apache.log4j.Logger;
import org.wso2.grpc.Event;
import org.wso2.grpc.EventServiceGrpc;

/**
 *
 */
@Extension(
        name = "grpc",
        namespace = "source",
        description = "sdfsdf",
        parameters = {
                @Parameter(name = "url",
                        description = "asdfa" ,
                        type = {DataType.STRING}),
        },
        examples = {
                @Example(
                        syntax = "@source(type='grpc', url='') " +
                                "define stream BarStream (message String);",
                        description = "asdfasdf"
                )
        }
)
public class GrpcSource extends AbstractGrpcSource {
    private static final Logger logger = Logger.getLogger(GrpcCallResponseSource.class.getName());

    @Override
    public void initializeGrpcServer(int port) {
        this.server = ServerBuilder.forPort(port).addService(new EventServiceGrpc.EventServiceImplBase() {
            @Override
            public void consume(Event request,
                                StreamObserver<Empty> responseObserver) {
                if (logger.isDebugEnabled()) {
                    logger.debug(siddhiAppContext.getName() + ": Server hit");
                }
                sourceEventListener.onEvent(request.getPayload(), new String[]{"1"});
                responseObserver.onNext(Empty.getDefaultInstance());
                responseObserver.onCompleted();
            }
        }).build();
    }
}
