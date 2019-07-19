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

import io.siddhi.extension.io.grpc.source.GRPCSource;

import java.util.HashMap;

public class SourceStaticHolder {
    private static SourceStaticHolder instance = new SourceStaticHolder();
    private HashMap<String, GRPCSource> sourceHashMap = new HashMap<>();

    private SourceStaticHolder() {

    }

    public static SourceStaticHolder getInstance() {
        return instance;
    }

    public void putSource(String key, GRPCSource source) {
        sourceHashMap.put(key, source);
    }

    public GRPCSource getGRPCSource(String key) {
        return sourceHashMap.get(key);
    }

}
