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

import io.grpc.stub.StreamObserver;

import java.util.Map;

import static io.siddhi.extension.io.grpc.util.GrpcUtils.extractHeaders;

/**
 * Worker thread to handover requests received in server
 */
public class GrpcWorkerThread implements Runnable {
    private AbstractGrpcSource relevantSource;
    private String payload;
    private Map<String, String> headers;
    private Map<String, String> metaData;
    private StreamObserver responseObserver;

    public GrpcWorkerThread(AbstractGrpcSource relevantSource, String payload, Map<String, String> headers,
                            Map<String, String> metaData, StreamObserver responseObserver) {
        this.relevantSource = relevantSource;
        this.payload = payload;
        this.metaData = metaData;
        this.responseObserver = responseObserver;
        this.headers = headers;
    }

    @Override
    public void run() {
        relevantSource.handleInjection(payload, extractHeaders(headers, metaData,
                relevantSource.getRequestedTransportPropertyNames()));
    }
}
