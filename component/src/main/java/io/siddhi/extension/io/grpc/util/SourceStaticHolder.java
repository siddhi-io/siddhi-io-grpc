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

import java.util.HashMap;

/**
 * a class to maintain pointers to source with sink id as key.
 * used by sink to push responses into respective source
 */
public class SourceStaticHolder {
    private static SourceStaticHolder instance = new SourceStaticHolder();
    private HashMap<String, GrpcCallResponseSource> sourceHashMap = new HashMap<>();

    private SourceStaticHolder() {

    }

    public static SourceStaticHolder getInstance() {
        return instance;
    }

    public void putSource(String key, GrpcCallResponseSource source) {
        sourceHashMap.put(key, source);
    }

    public GrpcCallResponseSource getGRPCSource(String key) {
        return sourceHashMap.get(key);
    }

    public void removeGRPCSource(String key) {
        sourceHashMap.remove(key);
    }

}