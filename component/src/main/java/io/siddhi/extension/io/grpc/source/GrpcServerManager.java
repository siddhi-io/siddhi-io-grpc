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

import io.siddhi.core.config.SiddhiAppContext;
import io.siddhi.extension.io.grpc.util.GrpcServerConfigs;
import io.siddhi.query.api.exception.SiddhiAppValidationException;
import org.apache.log4j.Logger;

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

    public void registerSource(GrpcServerConfigs serverConfigs, AbstractGrpcSource source, String methodName,
                               SiddhiAppContext siddhiAppContext, String streamID) {
        if (grpcPortServerMap.containsKey(serverConfigs.getServiceConfigs().getPort())) {
            if (!grpcPortServerMap.get(serverConfigs.getServiceConfigs().getPort()).getGrpcServerConfigs()
                    .equals(serverConfigs)) {
                throw new SiddhiAppValidationException(siddhiAppContext.getName() + ": " + streamID + ": A server " +
                        "with different configuration is already running on port " + serverConfigs.getServiceConfigs()
                        .getPort() + ". Please provide a different port or provide same configurations as server " +
                        "running on the given port.");
            }
            grpcPortServerMap.get(serverConfigs.getServiceConfigs().getPort()).subscribe(source.getStreamID(), source,
                    methodName, siddhiAppContext);
        } else {
            GrpcEventServiceServer server = new GrpcEventServiceServer(serverConfigs, siddhiAppContext, streamID);
            server.subscribe(source.getStreamID(), source, serverConfigs.getServiceConfigs().getMethodName(),
                    siddhiAppContext);
            grpcPortServerMap.put(serverConfigs.getServiceConfigs().getPort(), server);
        }
    }

    public void unregisterSource(int port, String streamID, String methodName, Logger logger,
                                 SiddhiAppContext siddhiAppContext) {
        grpcPortServerMap.get(port).unsubscribe(streamID, methodName, siddhiAppContext);
        if (grpcPortServerMap.get(port).getNumSubscribers() == 0) {
            grpcPortServerMap.get(port).disconnectServer(logger, siddhiAppContext, streamID);
            grpcPortServerMap.remove(port);
        }
    }

    public GrpcEventServiceServer getServer(int port) {
        return grpcPortServerMap.get(port);
    }
}
