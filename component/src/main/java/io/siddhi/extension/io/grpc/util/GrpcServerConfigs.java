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
    private String truststoreFilePath = null;
    private String truststorePassword = null;
    private String keystoreFilePath = null;
    private String keystorePassword = null;
    private String truststoreAlgorithm = null;
    private String keystoreAlgorithm = null;
    private String tlsStoreType = null;

    public GrpcServerConfigs(OptionHolder optionHolder, SiddhiAppContext siddhiAppContext, String streamID) {
        this.serviceConfigs = new ServiceConfigs(optionHolder, siddhiAppContext, streamID);
        if (optionHolder.isOptionExists(GrpcConstants.KEYSTORE_FILE)) {
            keystoreFilePath = optionHolder.validateAndGetOption(GrpcConstants.KEYSTORE_FILE).getValue();
            keystorePassword = optionHolder.validateAndGetOption(GrpcConstants.KEYSTORE_PASSWORD).getValue();
            keystoreAlgorithm = optionHolder.validateAndGetOption(GrpcConstants.KEYSTORE_ALGORITHM).getValue();
            tlsStoreType = optionHolder.getOrCreateOption(GrpcConstants.TLS_STORE_TYPE,
                    GrpcConstants.DEFAULT_TLS_STORE_TYPE).getValue();
        }

        if (optionHolder.isOptionExists(GrpcConstants.TRUSTSTORE_FILE)) {
            if (!optionHolder.isOptionExists(GrpcConstants.KEYSTORE_FILE)) {
                throw new SiddhiAppCreationException(siddhiAppContext.getName() + ":" + streamID + ": Truststore " +
                        "configurations given without keystore configurations. Please provide keystore");
            }
            truststoreFilePath = optionHolder.validateAndGetOption(GrpcConstants.TRUSTSTORE_FILE).getValue();
            if (optionHolder.isOptionExists(GrpcConstants.TRUSTSTORE_PASSWORD)) {
                truststorePassword = optionHolder.validateAndGetOption(GrpcConstants.TRUSTSTORE_PASSWORD).getValue();
            }
            truststoreAlgorithm = optionHolder.validateAndGetOption(GrpcConstants.TRUSTSTORE_ALGORITHM).getValue();
            tlsStoreType = optionHolder.getOrCreateOption(GrpcConstants.TLS_STORE_TYPE,
                    GrpcConstants.DEFAULT_TLS_STORE_TYPE).getValue();
        }
    }
}
