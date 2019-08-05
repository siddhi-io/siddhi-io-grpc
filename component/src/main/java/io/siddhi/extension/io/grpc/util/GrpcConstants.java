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

/**
 * Class to hold the constants used by gRPC sources and sinks
 */
public class GrpcConstants {
    public static final String PORT_HOST_SEPARATOR = ":";
    public static final String PUBLISHER_URL = "url";
    public static final String PORT_SERVICE_SEPARATOR = "/";
    public static final String SINK_ID = "sink.id";
    public static final String DEFAULT_METHOD_NAME_WITH_RESPONSE = "process";
    public static final String DEFAULT_METHOD_NAME_WITHOUT_RESPONSE = "consume";
    public static final String EMPTY_STRING = "";
    public static final String GRPC_PROTOCOL_NAME = "grpc";
    public static final String DEFAULT_SERVICE_NAME = "EventService";
    public static final String SINK_TYPE_OPTION = "type";
    public static final String GRPC_CALL_SINK_NAME = "grpc-call";
    public static final String SOURCE_ID = "source.id";
    public static final String MESSAGE_ID = "message.id";
    public static final String HEADERS = "headers";

    public static final int URL_PROTOCOL_POSITION = 0;
    public static final int URL_HOST_AND_PORT_POSITION = 1;
    public static final int URL_SERVICE_NAME_POSITION = 2;
    public static final int URL_METHOD_NAME_POSITION = 3;
    public static final int URL_SEQUENCE_NAME_POSITION = 4;
    public static final int NUM_URL_PARTS_FOR_MI_MODE_SINK = 5;
    public static final int NUM_URL_PARTS_FOR_DEFAULT_MODE_SOURCE = 4;
}
