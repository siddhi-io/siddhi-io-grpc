/*
 * Copyright 2016 Google, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.siddhi.extension.io.grpc.util;

import io.grpc.Context;
import io.grpc.Contexts;
import io.grpc.Metadata;
import io.grpc.ServerCall;
import io.grpc.ServerCallHandler;
import io.grpc.ServerInterceptor;
import io.siddhi.core.config.SiddhiAppContext;
import io.siddhi.extension.io.grpc.source.AbstractGrpcSource;
import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Server interceptor to receive headers
 */
public class SourceServerInterceptor implements ServerInterceptor {
  private static final Logger logger = Logger.getLogger(SourceServerInterceptor.class.getName());
  private SiddhiAppContext siddhiAppContext;
  private String streamID;

  public SourceServerInterceptor(SiddhiAppContext siddhiAppContext,
                                 String streamID) {
    this.siddhiAppContext = siddhiAppContext;
    this.streamID = streamID;
  }

  @Override
  public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(ServerCall<ReqT, RespT> serverCall,
                                                               Metadata metadata,
                                                               ServerCallHandler<ReqT, RespT> serverCallHandler) {
    Set<String> metadataKeys = metadata.keys();
    Map<String, String> metaDataMap = new HashMap<>();
    for (String key: metadataKeys) {
      metaDataMap.put(key, metadata.get(Metadata.Key.of(key, io.grpc.Metadata.ASCII_STRING_MARSHALLER)));
    }
    AbstractGrpcSource.metaDataMap.set(metaDataMap);
    if (logger.isDebugEnabled()) {
      logger.debug(siddhiAppContext.getName() + ":" + streamID + ": Metadata received: " + metaDataMap.toString());
    }
    return Contexts.interceptCall(Context.ROOT, serverCall, metadata, serverCallHandler);
  }
}
