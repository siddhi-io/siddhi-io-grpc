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
import io.grpc.stub.StreamObserver;
import org.wso2.grpc.Event;

import java.util.Map;

import static io.siddhi.extension.io.grpc.util.GrpcUtils.extractHeaders;

public class GrpcWorkerThread implements Runnable {
    private AbstractGrpcSource relevantSource;
    private Event request;
    private Map<String, String> metaData;
    private StreamObserver responseObserver;

    public GrpcWorkerThread(AbstractGrpcSource relevantSource, Event request, Map<String, String> metaData, StreamObserver responseObserver) {
        this.relevantSource = relevantSource;
        this.request = request;
        this.metaData = metaData;
        this.responseObserver = responseObserver;
    }

    @Override
    public void run() {
        relevantSource.handleInjection(request.getPayload(), extractHeaders(request
                        .getHeadersMap(), metaData,
                relevantSource.getRequestedTransportPropertyNames())); //todo: do this onEvent in a worker thread. user set threadpool parameter and buffer size
    }
}
