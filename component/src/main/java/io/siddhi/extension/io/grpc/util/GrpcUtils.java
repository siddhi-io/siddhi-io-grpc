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

import io.siddhi.core.exception.SiddhiAppRuntimeException;
import io.siddhi.query.api.exception.SiddhiAppValidationException;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Class to hold the static util methods needed.
 */
public class GrpcUtils {
    public static String[] extractHeaders(Map<String, String> headersMap, Map<String, String> metaDataMap,
                                          String[] requestedTransportPropertyNames) {
        if (requestedTransportPropertyNames == null) {
            return new String[]{};
        }
        String[] headersArray = new String[requestedTransportPropertyNames.length];
        for (int i = 0; i < requestedTransportPropertyNames.length; i++) {
            if (headersMap != null) {
                if (headersMap.containsKey(requestedTransportPropertyNames[i])) {
                    headersArray[i] = headersMap.get(requestedTransportPropertyNames[i]);
                }
            }
            if (metaDataMap.containsKey(requestedTransportPropertyNames[i])) {
                headersArray[i] = metaDataMap.get(requestedTransportPropertyNames[i]);
            }
        }
        List headersArrayList = Arrays.asList(headersArray);
        if (headersArrayList.contains(null)) {
            throw new SiddhiAppRuntimeException("Requested transport property '" +
                    requestedTransportPropertyNames[headersArrayList.indexOf(null)] + "' not present in received " +
                    "event");
        }
        return headersArray;
    }

    /**
     * @return methods that are available in the stub as list of String
     */
    public static List<String> getRpcMethodList(ServiceConfigs serviceConfigs, String siddhiAppName,
                                                String streamID) {
        List<String> rpcMethodNameList = new ArrayList<>();
        String stubReference = serviceConfigs.getFullyQualifiedServiceName() + GrpcConstants.
                GRPC_PROTOCOL_NAME_UPPERCAMELCASE + GrpcConstants.DOLLAR_SIGN + serviceConfigs.getServiceName()
                + GrpcConstants.STUB;
        Method[] methodsInStub;
        try {
            methodsInStub = Class.forName(stubReference).getMethods();
        } catch (ClassNotFoundException e) {
            throw new SiddhiAppValidationException(siddhiAppName + ":" + streamID + ": Invalid service name " +
                    "provided in the url, provided service name: '" + serviceConfigs
                    .getFullyQualifiedServiceName() + "'", e);
        }
        for (Method method : methodsInStub) {
            if (method.getDeclaringClass().getName().equals(stubReference)) { //to ignore super class method
                rpcMethodNameList.add(method.getName());
            }
        }
        return rpcMethodNameList;
    }

}
