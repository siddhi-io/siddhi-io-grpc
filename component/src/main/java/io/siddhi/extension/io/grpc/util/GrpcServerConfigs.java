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

import io.siddhi.core.config.SiddhiAppContext;
import io.siddhi.core.exception.SiddhiAppCreationException;
import io.siddhi.core.util.transport.OptionHolder;

public class GrpcServerConfigs {
    private ServiceConfigs serviceConfigs;
    private int maxInboundMessageSize = -1;
    private int maxInboundMetadataSize = -1;
    private long serverShutdownWaitingTimeInMillis = -1L;
    private int threadPoolSize;

    public GrpcServerConfigs(OptionHolder optionHolder, SiddhiAppContext siddhiAppContext, String streamID) {
        this.serviceConfigs = new ServiceConfigs(optionHolder, siddhiAppContext, streamID);

        if (optionHolder.isOptionExists(GrpcConstants.MAX_INBOUND_MESSAGE_SIZE)) {
            maxInboundMessageSize = Integer.parseInt(optionHolder.validateAndGetOption(
                    GrpcConstants.MAX_INBOUND_MESSAGE_SIZE).getValue());
        }
        if (optionHolder.isOptionExists(GrpcConstants.MAX_INBOUND_METADATA_SIZE)) {
            maxInboundMetadataSize = Integer.parseInt(optionHolder.validateAndGetOption(
                    GrpcConstants.MAX_INBOUND_METADATA_SIZE).getValue());
        }
        if (optionHolder.isOptionExists(GrpcConstants.SERVER_SHUTDOWN_WAITING_TIME)) {
            this.serverShutdownWaitingTimeInMillis = Long.parseLong(optionHolder.validateAndGetOption(
                    GrpcConstants.SERVER_SHUTDOWN_WAITING_TIME).getValue());
        }
        this.threadPoolSize = Integer.parseInt(optionHolder.getOrCreateOption(GrpcConstants.THREADPOOL_SIZE,
                GrpcConstants.THREADPOOL_SIZE_default).getValue());
    }

    public ServiceConfigs getServiceConfigs() {
        return serviceConfigs;
    }

    public int getThreadPoolSize() {
        return threadPoolSize;
    }

    public int getMaxInboundMessageSize() {
        return maxInboundMessageSize;
    }

    public int getMaxInboundMetadataSize() {
        return maxInboundMetadataSize;
    }

    public long getServerShutdownWaitingTimeInMillis() {
        return serverShutdownWaitingTimeInMillis;
    }
}
