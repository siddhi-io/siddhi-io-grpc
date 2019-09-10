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
import io.siddhi.core.util.transport.OptionHolder;
import io.siddhi.query.api.exception.SiddhiAppValidationException;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ServiceConfigs {
    private String url;
    private String serviceName;
    private int port;
    private String methodName;
    private String hostPort;
    private String sequenceName;
    private boolean isDefaultService = false;
    private String fullyQualifiedServiceName;

    public ServiceConfigs(OptionHolder optionHolder, SiddhiAppContext siddhiAppContext, String streamID) {
        this.url = optionHolder.validateAndGetOption(GrpcConstants.RECEIVER_URL).getValue();
        if (!url.startsWith(GrpcConstants.GRPC_PROTOCOL_NAME)) {
            throw new SiddhiAppValidationException(siddhiAppContext.getName() + ":" + streamID + ": The url must " +
                    "begin with \"" + GrpcConstants.GRPC_PROTOCOL_NAME + "\" for all grpc sinks");
        }
        URL aURL;
        try {
            aURL = new URL(GrpcConstants.DUMMY_PROTOCOL_NAME + url.substring(4));
        } catch (MalformedURLException e) {
            throw new SiddhiAppValidationException(siddhiAppContext.getName() + ":" + streamID +
                    ": Error in URL format. Expected format is `grpc://0.0.0.0:9763/<serviceName>/<methodName>` but " +
                    "the provided url is " + url + ". ", e);
        }
        this.port = aURL.getPort();
        this.hostPort = aURL.getAuthority();

        List<String> urlPathParts = new ArrayList<>(Arrays.asList(aURL.getPath().substring(1).split(GrpcConstants
                .PORT_SERVICE_SEPARATOR)));
        if (urlPathParts.contains(GrpcConstants.EMPTY_STRING)) {
            throw new SiddhiAppValidationException("Malformed URL. There should not be any empty parts in the URL " +
                    "between two '/'");
        }
        if (urlPathParts.size() < 2) {
            //todo set to default
        } else {
            this.methodName = urlPathParts.get(GrpcConstants.PATH_METHOD_NAME_POSITION);
            this.fullyQualifiedServiceName = urlPathParts.get(GrpcConstants.PATH_SERVICE_NAME_POSITION);
            String[] fullyQualifiedServiceNameParts = fullyQualifiedServiceName.split("\\.");
            this.serviceName = fullyQualifiedServiceNameParts[fullyQualifiedServiceNameParts.length - 1];
            if (fullyQualifiedServiceName.equalsIgnoreCase(GrpcConstants.DEFAULT_FULLY_QUALIFIED_SERVICE_NAME)) {
                isDefaultService = true;
                if (urlPathParts.size() == 3) {
                    this.sequenceName = urlPathParts.get(GrpcConstants.PATH_SEQUENCE_NAME_POSITION);
                }
            }
        }
    }

    public String getServiceName() {
        return serviceName;
    }

    public int getPort() {
        return port;
    }

    public String getMethodName() {
        return methodName;
    }

    public String getHostPort() {
        return hostPort;
    }

    public String getSequenceName() {
        return sequenceName;
    }

    public boolean isDefaultService() {
        return isDefaultService;
    }

    public String getUrl() {
        return url;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    //    @Override
//    public boolean equals(Object obj) {
//
//    }
}
