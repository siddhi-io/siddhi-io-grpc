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
 * Class to hold the constants used by gRPC sources and sinks.
 */
public class GrpcConstants {
    public static final String SEMI_COLON_STRING = ":";
    public static final String PUBLISHER_URL = "publisher.url";
    public static final String RECEIVER_URL = "receiver.url";
    public static final String PORT_SERVICE_SEPARATOR = "/";
    public static final String SINK_ID = "sink.id";
    public static final String STREAM_ID = "stream.id";
    public static final String DEFAULT_METHOD_NAME_WITH_RESPONSE = "process";
    public static final String DEFAULT_METHOD_NAME_WITHOUT_RESPONSE = "consume";
    public static final String EMPTY_STRING = "";
    public static final String SPACE_STRING = " ";
    public static final String COMMA_STRING = ",";
    public static final String INVERTED_COMMA_STRING = "'";
    public static final String GRPC_PROTOCOL_NAME = "grpc";
    public static final String DUMMY_PROTOCOL_NAME = "http";
    public static final String DEFAULT_SERVICE_NAME = "EventService";
    public static final String DEFAULT_FULLY_QUALIFIED_SERVICE_NAME = "org.wso2.grpc.EventService";
    public static final String SINK_TYPE_OPTION = "type";
    public static final String GRPC_CALL_SINK_NAME = "grpc-call";
    public static final String GRPC_SERVICE_RESPONSE_SINK_NAME = "grpc-service-response";
    public static final String SOURCE_ID = "source.id";
    public static final String MESSAGE_ID = "message.id";
    public static final String HEADERS = "headers";

    //SSL feature variables
    public static final String SYS_KEYSTORE_FILE = "keyStoreFile";
    public static final String SYS_KEYSTORE_PASSWORD = "keyStorePassword";
    public static final String SYS_KEYSTORE_ALGORITHM = "keyStoreAlgorithm";
    public static final String DEFAULT_KEYSTORE_FILE = "src/main/resources/security/wso2carbon.jks";
    public static final String DEFAULT_KEYSTORE_PASSWORD = "wso2carbon";
    public static final String DEFAULT_KEYSTORE_ALGORITHM = "SunX509";
    public static final String SYS_TRUSTSTORE_FILE_PATH =  "trustStoreFile";
    public static final String SYS_TRUSTSTORE_PASSWORD = "trustStorePassword";
    public static final String SYS_TRUSTSTORE_ALGORITHM = "trustStoreAlgorithm";
    public static final String DEFAULT_TRUSTSTORE_FILE = "src/main/resources/security/client-truststore.jks";
    public static final String DEFAULT_TRUSTSTORE_PASSWORD = "wso2carbon";
    public static final String DEFAULT_TRUSTSTORE_ALGORITHM = "SunX509";

    public static final String KEYSTORE_FILE = "keystore.file";
    public static final String KEYSTORE_PASSWORD = "keystore.password";
    public static final String KEYSTORE_ALGORITHM = "keystore.algorithm";
    public static final String TRUSTSTORE_FILE = "truststore.file";
    public static final String TRUSTSTORE_PASSWORD = "truststore.password";
    public static final String TRUSTSTORE_ALGORITHM = "truststore.algorithm";

    public static final String TLS_STORE_TYPE = "tls.store.type";
    public static final String DEFAULT_TLS_STORE_TYPE = "JKS";
    public static final String SEQUENCE_HEADER_KEY = "sequence";
    public static final String METADATA = "metadata";
    public static final String THREADPOOL_SIZE = "threadpool.size";
    public static final String THREADPOOL_SIZE_DEFAULT = "100";
    public static final String THREADPOOL_BUFFER_SIZE = "threadpool.buffer.size";
    public static final String THREADPOOL_BUFFER_SIZE_DEFAULT = "100";
    public static final String ENABLE_SSL = "enable.ssl";

    public static final int PATH_SERVICE_NAME_POSITION = 0;
    public static final int PATH_METHOD_NAME_POSITION = 1;
    public static final int PATH_SEQUENCE_NAME_POSITION = 2;

    //ManagedChannelBuilder Properties
    public static final String IDLE_TIMEOUT_MILLIS = "idle.timeout";
    public static final String IDLE_TIMEOUT_DEFAULT = "1800";
    public static final String MAX_INBOUND_MESSAGE_SIZE = "max.inbound.message.size";
    public static final String MAX_INBOUND_MESSAGE_SIZE_DEFAULT = "4194304";
    public static final String MAX_INBOUND_METADATA_SIZE = "max.inbound.metadata.size";
    public static final String MAX_INBOUND_METADATA_SIZE_DEFAULT = "8192";
    public static final String KEEP_ALIVE_TIME_MILLIS = "keep.alive.time";
    public static final String KEEP_ALIVE_TIME_DEFAULT = String.valueOf(Long.MAX_VALUE);
    public static final String KEEP_ALIVE_TIMEOUT_MILLIS = "keep.alive.timeout";
    public static final String KEEP_ALIVE_TIMEOUT_DEFAULT = "20";
    public static final String KEEP_ALIVE_WITHOUT_CALLS = "keep.alive.without.calls";
    public static final String KEEP_ALIVE_WITHOUT_CALLS_DEFAULT = "false";
    public static final String MAX_RETRY_ATTEMPTS = "max.retry.attempts";
    public static final String MAX_RETRY_ATTEMPTS_DEFAULT = "5";
    public static final String MAX_HEDGED_ATTEMPTS = "max.hedged.attempts";
    public static final String MAX_HEDGED_ATTEMPTS_DEFAULT = "5";
    public static final String RETRY_BUFFER_SIZE = "retry.buffer.size";
    public static final String RETRY_BUFFER_SIZE_DEFAULT = "16777216";
    public static final String PER_RPC_BUFFER_SIZE = "per.rpc.buffer.size";
    public static final String PER_RPC_BUFFER_SIZE_DEFAULT = "1048576";
    public static final String ENABLE_RETRY = "enable.retry";
    public static final String ENABLE_RETRY_DEFAULT = "false";
    public static final String CHANNEL_TERMINATION_WAITING_TIME_MILLIS = "channel.termination.waiting.time";
    public static final String CHANNEL_TERMINATION_WAITING_TIME_MILLIS_DEFAULT = "5000";
    public static final String SERVER_SHUTDOWN_WAITING_TIME = "server.shutdown.waiting.time";
    public static final String SERVER_SHUTDOWN_WAITING_TIME_DEFAULT = "5";

    public static final String SERVICE_TIMEOUT = "service.timeout";
    public static final String SERVICE_TIMEOUT_DEFAULT = "10000";
    public static final String TIMEOUT_CHECK_INTERVAL = "timeout.check.interval";

    public static final String STUB = "Stub";
    public static final String FUTURE_STUB = "FutureStub";
    public static final String FUTURE_STUB_METHOD_NAME = "newFutureStub";
    public static final String GRPC_PROTOCOL_NAME_UPPERCAMELCASE = "Grpc";
    public static final String DOLLAR_SIGN = "$";
    public static final String NEW_STUB_NAME = "newStub";
    public static final String PARSE_FROM_METHOD_NAME = "parseFrom";
    public static final String METHOD_NAME = "Method";
    public static final String GETTER = "get";
    public static final String TO_BYTE_STRING = "toByteString";

    public static final int REQUEST_CLASS_POSITION = 0;
    public static final int RESPONSE_CLASS_POSITION = 1;
    public static final int EMPTY_METHOD_ID = 1;
    public static final int NON_EMPTY_METHOD_ID = 2;
    public static final int CLIENT_STREAM_METHOD_ID = 3;
}
