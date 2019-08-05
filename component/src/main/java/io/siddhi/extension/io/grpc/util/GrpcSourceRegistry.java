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
package io.siddhi.extension.io.grpc.util;

import io.siddhi.extension.io.grpc.source.GrpcCallResponseSource;
import io.siddhi.extension.io.grpc.source.GrpcServiceSource;

import java.util.HashMap;

/**
 * a class to register GrpcSource with respective sink.id or source.id. Used by GrpcCallSink and GrpcServiceResponseSink
 * to push responses
 */
public class GrpcSourceRegistry {
    private static GrpcSourceRegistry instance = new GrpcSourceRegistry();
    private HashMap<String, GrpcCallResponseSource> grpcCallResponseSourceHashMap = new HashMap<>();
    private HashMap<String, GrpcServiceSource> grpcServiceSourceHashMap = new HashMap<>();

    private GrpcSourceRegistry() {

    }

    public static GrpcSourceRegistry getInstance() {
        return instance;
    }

    public void putGrpcCallResponseSource(String key, GrpcCallResponseSource source) {
        grpcCallResponseSourceHashMap.put(key, source);
    }

    public void putGrpcServiceSource(String key, GrpcServiceSource source) {
        grpcServiceSourceHashMap.put(key, source);
    }

    public GrpcCallResponseSource getGrpcCallResponseSourceSource(String key) {
        return grpcCallResponseSourceHashMap.get(key);
    }

    public GrpcServiceSource getGrpcServiceSource(String key) {
        return grpcServiceSourceHashMap.get(key);
    }

    public void removeGrpcCallResponseSource(String key) {
        grpcCallResponseSourceHashMap.remove(key);
    }

    public void removeGrpcServiceSource(String key) {
        grpcServiceSourceHashMap.remove(key);
    }

}
