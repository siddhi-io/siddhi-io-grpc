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

import io.siddhi.query.api.exception.SiddhiAppValidationException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class GrpcUtils {
    public static String getServiceName(String path) {
        List<String> urlParts = new ArrayList<>(Arrays.asList(path.split(GrpcConstants.PORT_SERVICE_SEPARATOR)));
        urlParts.removeAll(Collections.singletonList(GrpcConstants.EMPTY_STRING));
        if (urlParts.size() < 2) {
            throw new SiddhiAppValidationException("Malformed URL. After port number atleast two sections should " +
                    "be available seperated by / as follows grpc://host:port/ServiceName/MethodName");
        }
        String[] fullyQualifiedServiceNameParts = urlParts.get(GrpcConstants.PATH_SERVICE_NAME_POSITION).split("\\.");
        return fullyQualifiedServiceNameParts[fullyQualifiedServiceNameParts.length - 1];
    }

    public static String getMethodName(String path) {
        List<String> urlParts = new ArrayList<>(Arrays.asList(path.split(GrpcConstants.PORT_SERVICE_SEPARATOR)));
        urlParts.removeAll(Collections.singletonList(GrpcConstants.EMPTY_STRING));
        if (urlParts.size() < 2) {
            throw new SiddhiAppValidationException("Malformed URL. After port number atleast two sections should " +
                    "be available seperated by / as follows grpc://host:port/ServiceName/MethodName");
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
