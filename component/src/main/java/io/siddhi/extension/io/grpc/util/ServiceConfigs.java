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
import java.util.Collections;
import java.util.List;

public class ServiceConfigs {
    private String url;
    private String serviceName;
    private int port;
    private String methodName;
    private String hostPort;
    private String sequenceName;
    private boolean isDefaultService;

    public ServiceConfigs(String url, OptionHolder optionHolder, SiddhiAppContext siddhiAppContext, String streamID) {
        this.url = url;
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
        this.serviceName = getServiceName(aURL.getPath());
        this.port = aURL.getPort();
        this.methodName = getMethodName(aURL.getPath());
        this.hostPort = aURL.getAuthority();

        if (serviceName.equals(GrpcConstants.DEFAULT_SERVICE_NAME)) {
            this.isDefaultService = true;
            if (isSequenceNamePresent(aURL.getPath())) {
                this.sequenceName = getSequenceName(aURL.getPath());
            }
        }
    }

    public static String getServiceName(String path) {
        List<String> urlParts = new ArrayList<>(Arrays.asList(path.substring(1).split(GrpcConstants
                .PORT_SERVICE_SEPARATOR)));
        if (urlParts.contains(GrpcConstants.EMPTY_STRING)) {
            throw new SiddhiAppValidationException("Malformed URL. There should not be any empty parts in the URL " +
                    "between two '/'");
        }
        if (urlParts.size() < 2) { //todo: if user gives only sequence then infer eventswervice and method
            throw new SiddhiAppValidationException("Malformed URL. After port number at least two sections should " +
                    "be available separated by '/' as in 'grpc://<host>:<port>/<ServiceName>/<MethodName>'");
        }
        String[] fullyQualifiedServiceNameParts = urlParts.get(GrpcConstants.PATH_SERVICE_NAME_POSITION).split("\\.");
        return fullyQualifiedServiceNameParts[fullyQualifiedServiceNameParts.length - 1];
    }

    public static String getMethodName(String path) { //todo: extract service name method name and give as an object
        List<String> urlParts = new ArrayList<>(Arrays.asList(path.split(GrpcConstants.PORT_SERVICE_SEPARATOR)));
        urlParts.removeAll(Collections.singletonList(GrpcConstants.EMPTY_STRING));
        if (urlParts.size() < GrpcConstants.PATH_METHOD_NAME_POSITION) {
            return null;
        }
        return urlParts.get(GrpcConstants.PATH_METHOD_NAME_POSITION);
    }

    public static String getSequenceName(String path) {
        List<String> urlParts = new ArrayList<>(Arrays.asList(path.split(GrpcConstants.PORT_SERVICE_SEPARATOR)));
        urlParts.removeAll(Collections.singletonList(GrpcConstants.EMPTY_STRING));
        return urlParts.get(GrpcConstants.PATH_SEQUENCE_NAME_POSITION);
    }

    public static boolean isSequenceNamePresent(String path) {
        List<String> urlParts = new ArrayList<>(Arrays.asList(path.split(GrpcConstants.PORT_SERVICE_SEPARATOR)));
        urlParts.removeAll(Collections.singletonList(GrpcConstants.EMPTY_STRING));
        return urlParts.size() == 3;
    }
}
