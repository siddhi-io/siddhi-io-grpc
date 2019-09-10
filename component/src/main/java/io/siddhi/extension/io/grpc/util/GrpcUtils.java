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

import io.siddhi.core.exception.SiddhiAppCreationException;
import io.siddhi.core.exception.SiddhiAppRuntimeException;
import io.siddhi.query.api.exception.SiddhiAppValidationException;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Class to hold the static util methods needed
 */
public class GrpcUtils {
    public static String getServiceName(String path) {
        List<String> urlParts = new ArrayList<>(Arrays.asList(path.substring(1).split(GrpcConstants
                .PORT_SERVICE_SEPARATOR)));
        if (urlParts.contains(GrpcConstants.EMPTY_STRING)) {
            throw new SiddhiAppValidationException("Malformed URL. There should not be any empty parts in the URL " +
                    "between two '/'");
        }
        if (urlParts.size() < 2) {
            throw new SiddhiAppValidationException("Malformed URL. After port number at least two sections should " +
                    "be available separated by '/' as in 'grpc://<host>:<port>/<ServiceName>/<MethodName>'");
        }
        String[] fullyQualifiedServiceNameParts = urlParts.get(GrpcConstants.PATH_SERVICE_NAME_POSITION).split("\\.");
        return fullyQualifiedServiceNameParts[fullyQualifiedServiceNameParts.length - 1];
    }

    public static String getMethodName(String path) {
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

    public static String[] extractHeaders(Map<String, String> headersMap, Map<String, String> metaDataMap,
                                          String[] requestedTransportPropertyNames) {
        String[] headersArray = new String[requestedTransportPropertyNames.length];
        for (int i = 0; i < requestedTransportPropertyNames.length; i++) {
            if (headersMap.containsKey(requestedTransportPropertyNames[i])) {
                headersArray[i] = headersMap.get(requestedTransportPropertyNames[i]);
            }
            if (metaDataMap != null && metaDataMap.containsKey(requestedTransportPropertyNames[i])) {
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

    public static String getFullServiceName(String path) {
        List<String> urlParts = new ArrayList<>(Arrays.asList(path.substring(1).split(GrpcConstants
                .PORT_SERVICE_SEPARATOR)));
        if (urlParts.contains(GrpcConstants.EMPTY_STRING)) {
            throw new SiddhiAppValidationException("Malformed URL. There should not be any empty parts in the URL " +
                    "between two '/'");
        }
        if (urlParts.size() < 2) {
            throw new SiddhiAppValidationException("Malformed URL. After port number at least two sections should " +
                    "be available separated by '/' as in 'grpc://<host>:<port>/<ServiceName>/<MethodName>'");
        }
        return urlParts.get(GrpcConstants.PATH_SERVICE_NAME_POSITION);
    }

    public static Class getRequestClass(String serviceNameWithPackgName, String methodName)
            throws ClassNotFoundException {
        String[] serviceNameWithPackgNameArray = serviceNameWithPackgName.split("\\.");
        String stubName = serviceNameWithPackgNameArray[serviceNameWithPackgNameArray.length - 1] + "BlockingStub";
        Method[] methods = Class.forName((serviceNameWithPackgName) + "Grpc" + "$" + stubName).getMethods();
        for (Method m : methods) {
            if (m.getName().equals(methodName)) {
                return m.getParameterTypes()[0];
            }
        }
        return null;
    }

    public static Class getResponseClass(String serviceNameWithPackgName) throws ClassNotFoundException {
        String stubName = getServiceName(serviceNameWithPackgName) + "BlockingStub";
        Method[] methods = Class.forName(serviceNameWithPackgName + "Grpc" + "$" + stubName).getMethods();
        for (Method m : methods) {
            if (m.getName().equals(getMethodName(serviceNameWithPackgName))) {
                return m.getParameterTypes()[0];
            }
        }
        return null;
    }

    public static List<String> getRPCmethodList(String serviceReference, String siddhiAppName) { //require full
        // serviceName
        List<String> rpcMethodNameList = new ArrayList<>();
        String blockingStubReference = serviceReference + GrpcConstants.GRPC_PROTOCOL_NAME_UPPERCAMELCASE
                + GrpcConstants.DOLLAR_SIGN + serviceReference + GrpcConstants.BLOCKING_STUB_NAME;
        String[] serviceReferenceArray = serviceReference.split("\\.");
        String serviceName = serviceReferenceArray[serviceReference.length() - 1];
        Method[] methodsInBlockingStub; // the place where
        try {
            methodsInBlockingStub = Class.forName(blockingStubReference).getMethods();
        } catch (ClassNotFoundException e) {
            throw new SiddhiAppCreationException(siddhiAppName + ": " +
                    "Invalid service name provided in url, provided service name : '" + serviceName + "'", e);
        }
        // ClassNotFound Exception will be thrown
        for (Method method : methodsInBlockingStub) {
            if (method.getDeclaringClass().getName().equals(blockingStubReference)) { // check if the method belogs
            // to blocking stub, other methods that does not belongs to blocking stub are not rpc methods

                rpcMethodNameList.add(method.getName());
            }
        }
        return rpcMethodNameList;
    }
}
