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
import io.siddhi.core.util.config.ConfigReader;
import io.siddhi.core.util.transport.OptionHolder;
import io.siddhi.query.api.exception.SiddhiAppValidationException;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * class to hold grpc service configs
 */
public class ServiceConfigs {
    private String url;
    private String serviceName;
    private int port;
    private String methodName;
    private String hostPort;
    private String sequenceName;
    private boolean isDefaultService = false;
    private String fullyQualifiedServiceName;
    private String truststoreFilePath;
    private String truststorePassword;
    private String keystoreFilePath;
    private String keystorePassword;
    private String truststoreAlgorithm;
    private String keystoreAlgorithm;
    private String tlsStoreType;
    private boolean isSslEnabled;
    private static final Logger log = LoggerFactory.getLogger(ServiceConfigs.class.getName());

    public ServiceConfigs(OptionHolder optionHolder, SiddhiAppContext siddhiAppContext,
                          String streamID, ConfigReader configReader) {
        if (optionHolder.isOptionExists(GrpcConstants.RECEIVER_URL)) {
            this.url = optionHolder.validateAndGetOption(GrpcConstants.RECEIVER_URL).getValue();
            log.debug("GRPC Service for : " + streamID + " started");
        } else if (configReader.readConfig(GrpcConstants.RECEIVER_URL , GrpcConstants.DEFAULT_RECEIVER_URL) != null) {
            this.url = configReader.readConfig(GrpcConstants.RECEIVER_URL , GrpcConstants.DEFAULT_RECEIVER_URL);
            log.debug("Default GRPC Service for  : " + streamID + " started");
        } else if (optionHolder.isOptionExists(GrpcConstants.PUBLISHER_URL)) {
            this.url = optionHolder.validateAndGetOption(GrpcConstants.PUBLISHER_URL).getValue();
        } else {
            throw new SiddhiAppValidationException(siddhiAppContext.getName() + ": " + streamID + ": either " +
                    "receiver.url or publisher.url should be given. But found neither");
        }
        if (!url.startsWith(GrpcConstants.GRPC_PROTOCOL_NAME)) {
            throw new SiddhiAppValidationException(siddhiAppContext.getName() + ": " + streamID + ": The url must " +
                    "begin with \"" + GrpcConstants.GRPC_PROTOCOL_NAME + "\" for all grpc sinks");
        }
        URL aURL;
        try {
            aURL = new URL(GrpcConstants.DUMMY_PROTOCOL_NAME + url.substring(4));
        } catch (MalformedURLException e) {
            throw new SiddhiAppValidationException(siddhiAppContext.getName() + ": " + streamID +
                    ": Error in URL format. Expected format is `grpc://0.0.0.0:9763/<serviceName>/<methodName>` but " +
                    "the provided url is " + url + ". ", e);
        }
        this.port = aURL.getPort();
        this.hostPort = aURL.getAuthority();
        if (this.port == -1 || this.hostPort == null || aURL.getPath().equals("")) {
            throw new SiddhiAppValidationException(siddhiAppContext.getName() + ": " + streamID + ": URL not " +
                    "properly given. Expected format is `grpc://0.0.0.0:9763/<serviceName>/<methodName>` or " +
                    "`grpc://0.0.0.0:9763/<sequenceName>` but the provided url is " + url + ". ");
        }

        List<String> urlPathParts = new ArrayList<>(Arrays.asList(aURL.getPath().substring(1).split(GrpcConstants
                .PORT_SERVICE_SEPARATOR)));
        if (urlPathParts.contains(GrpcConstants.EMPTY_STRING)) {
            throw new SiddhiAppValidationException(siddhiAppContext.getName() + ":" + streamID + "Malformed URL. " +
                    "There should not be any empty parts in the URL between two '/'");
        }
        if (urlPathParts.size() < 2) {
            this.fullyQualifiedServiceName = GrpcConstants.DEFAULT_FULLY_QUALIFIED_SERVICE_NAME;
            this.serviceName = GrpcConstants.DEFAULT_SERVICE_NAME;
            this.sequenceName = urlPathParts.get(0);
        } else {
            this.methodName = urlPathParts.get(GrpcConstants.PATH_METHOD_NAME_POSITION);
            this.fullyQualifiedServiceName = urlPathParts.get(GrpcConstants.PATH_SERVICE_NAME_POSITION);
            String[] fullyQualifiedServiceNameParts = fullyQualifiedServiceName.split("\\.");
            this.serviceName = fullyQualifiedServiceNameParts[fullyQualifiedServiceNameParts.length - 1];
        }

        if (fullyQualifiedServiceName.equalsIgnoreCase(GrpcConstants.DEFAULT_FULLY_QUALIFIED_SERVICE_NAME)) {
            isDefaultService = true;
            if (urlPathParts.size() == 3) {
                this.sequenceName = urlPathParts.get(GrpcConstants.PATH_SEQUENCE_NAME_POSITION);
            }
        }
        //Validates and enables SSL feature
        if (optionHolder.isOptionExists(GrpcConstants.ENABLE_SSL)) {
            isSslEnabled = Boolean.parseBoolean(optionHolder.validateAndGetOption(GrpcConstants.ENABLE_SSL).getValue());
        } else {
            isSslEnabled = true;
        }
        //retrieves KeyStore File
        if (optionHolder.isOptionExists(GrpcConstants.KEYSTORE_FILE)) {
            keystoreFilePath = optionHolder.validateAndGetOption(GrpcConstants.KEYSTORE_FILE).getValue();
            keystorePassword = optionHolder.validateAndGetOption(GrpcConstants.KEYSTORE_PASSWORD).getValue();
            keystoreAlgorithm = optionHolder.validateAndGetOption(GrpcConstants.KEYSTORE_ALGORITHM).getValue();
            tlsStoreType = optionHolder.getOrCreateOption(GrpcConstants.TLS_STORE_TYPE,
                    GrpcConstants.DEFAULT_TLS_STORE_TYPE).getValue();
        } else {
            keystoreFilePath = configReader.readConfig(GrpcConstants.KEYSTORE_FILE,
                    GrpcConstants.DEFAULT_KEYSTORE_FILE);
            keystorePassword = configReader.readConfig(GrpcConstants.KEYSTORE_PASSWORD,
                    GrpcConstants.DEFAULT_KEYSTORE_PASSWORD);
            keystoreAlgorithm = configReader.readConfig(GrpcConstants.KEYSTORE_ALGORITHM,
                        GrpcConstants.DEFAULT_KEYSTORE_ALGORITHM);
            tlsStoreType = optionHolder.getOrCreateOption(GrpcConstants.TLS_STORE_TYPE,
                    GrpcConstants.DEFAULT_TLS_STORE_TYPE).getValue();
        }
        //retrieves Truststore file
        if (isSslEnabled && optionHolder.isOptionExists(GrpcConstants.SYS_TRUSTSTORE_FILE_PATH)) {
            truststoreFilePath = optionHolder.validateAndGetOption(GrpcConstants.SYS_TRUSTSTORE_FILE_PATH).getValue();
            truststorePassword = optionHolder.validateAndGetOption(GrpcConstants.SYS_TRUSTSTORE_PASSWORD).getValue();
            truststoreAlgorithm = optionHolder.validateAndGetOption(GrpcConstants.SYS_TRUSTSTORE_ALGORITHM).getValue();
        } else {
            truststoreFilePath = configReader.readConfig(GrpcConstants.SYS_TRUSTSTORE_FILE_PATH,
                    GrpcConstants.DEFAULT_TRUSTSTORE_FILE);
            truststorePassword = configReader.readConfig(GrpcConstants.SYS_TRUSTSTORE_PASSWORD,
                    GrpcConstants.DEFAULT_TRUSTSTORE_PASSWORD);
            truststoreAlgorithm = configReader.readConfig(GrpcConstants.SYS_TRUSTSTORE_ALGORITHM,
                    GrpcConstants.DEFAULT_TRUSTSTORE_ALGORITHM);
            tlsStoreType = optionHolder.getOrCreateOption(GrpcConstants.TLS_STORE_TYPE,
                    GrpcConstants.DEFAULT_TLS_STORE_TYPE).getValue();
        }
    }

    public String getServiceName() {
        return serviceName;
    }

    public String getFullyQualifiedServiceName() {
        return fullyQualifiedServiceName;
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

    public String getTruststoreFilePath() {
        return truststoreFilePath;
    }

    public String getTruststorePassword() {
        return truststorePassword;
    }

    public String getKeystoreFilePath() {
        return keystoreFilePath;
    }

    public String getKeystorePassword() {
        return keystorePassword;
    }

    public String getTruststoreAlgorithm() {
        return truststoreAlgorithm;
    }

    public String getKeystoreAlgorithm() {
        return keystoreAlgorithm;
    }

    public String getTlsStoreType() {
        return tlsStoreType;
    }


    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }

        if (!ServiceConfigs.class.isAssignableFrom(obj.getClass())) {
            return false;
        }

        final ServiceConfigs other = (ServiceConfigs) obj;
        if (!Objects.equals(this.url, other.url)) {
            return false;
        }
        if (!Objects.equals(this.serviceName, other.serviceName)) {
            return false;
        }
        if (this.port != other.port) {
            return false;
        }
        if (!Objects.equals(this.methodName, other.methodName)) {
            return false;
        }
        if (!Objects.equals(this.hostPort, other.hostPort)) {
            return false;
        }
        if (!Objects.equals(this.sequenceName, other.sequenceName)) {
            return false;
        }
        if (this.isDefaultService != other.isDefaultService) {
            return false;
        }
        if (!Objects.equals(this.fullyQualifiedServiceName, other.fullyQualifiedServiceName)) {
            return false;
        }
        if (!Objects.equals(this.truststoreFilePath, other.truststoreFilePath)) {
            return false;
        }
        if (!Objects.equals(this.truststorePassword, other.truststorePassword)) {
            return false;
        }
        if (!Objects.equals(this.keystoreFilePath, other.keystoreFilePath)) {
            return false;
        }
        if (!Objects.equals(this.keystorePassword, other.keystorePassword)) {
            return false;
        }
        if (!Objects.equals(this.truststoreAlgorithm, other.truststoreAlgorithm)) {
            return false;
        }
        if (!Objects.equals(this.keystoreAlgorithm, other.keystoreAlgorithm)) {
            return false;
        }
        if (!Objects.equals(this.tlsStoreType, other.tlsStoreType)) {
            return false;
        }
        if (this.isSslEnabled != other.isSslEnabled) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 31).append(url).append(serviceName).append(port).append(methodName)
                .append(hostPort).append(sequenceName).append(isDefaultService).append(fullyQualifiedServiceName)
                .append(truststoreFilePath).append(truststorePassword).append(keystoreFilePath).append(keystorePassword)
                .append(truststoreAlgorithm).append(keystoreAlgorithm).append(tlsStoreType).append(isSslEnabled)
                .toHashCode();
    }
}
