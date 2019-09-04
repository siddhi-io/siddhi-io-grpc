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

import io.siddhi.extension.io.grpc.source.AbstractGrpcSource;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class GrpcServerManager {
    private static GrpcServerManager instance = new GrpcServerManager();
    private Map<Integer, GrpcEventServiceServer> grpcPortServerMap = Collections.synchronizedMap(
            new HashMap<>());

    public static GrpcServerManager getInstance() {
        return instance;
    }

//    public boolean registerServer(int port, GrpcEventServiceServer server) {
//        if (grpcPortServerMap.containsKey(port)) {
//            return false;
//        } else {
//            grpcPortServerMap.put(port, server);
//            return true;
//        }
//    }

    public void registerSource(GrpcServerConfigs serverConfigs, AbstractGrpcSource source, String methodName) {
        if (grpcPortServerMap.containsKey(serverConfigs.getServiceConfigs().getPort())) {
            grpcPortServerMap.get(serverConfigs.getServiceConfigs().getPort()).subscribe(source.getStreamID(), source, methodName); //todo: validate for same server configs
        } else {
            GrpcEventServiceServer server = new GrpcEventServiceServer(serverConfigs);
            server.subscribe(source.getStreamID(), source, serverConfigs.getServiceConfigs().getMethodName());
            grpcPortServerMap.put(serverConfigs.getServiceConfigs().getPort(), server);
        }
    }

    public GrpcEventServiceServer getServer(int port) {
        return grpcPortServerMap.get(port);
    }
}
