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
import io.siddhi.core.stream.input.source.Source;
import io.siddhi.extension.io.grpc.source.AbstractGrpcSource;

import java.util.Set;

import static io.grpc.Metadata.ASCII_STRING_MARSHALLER;

/**
 * Server interceptor to receive headers
 */
public class SourceServerInterceptor implements ServerInterceptor {
  private AbstractGrpcSource associatedGrpcSource;
//  private static final ServerCall.Listener NOOP_LISTENER = new ServerCall.Listener() {
//  };

  public SourceServerInterceptor(AbstractGrpcSource associatedGrpcSource) {
    this.associatedGrpcSource = associatedGrpcSource;
  }

  @Override
  public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(ServerCall<ReqT, RespT> serverCall,
                                                               Metadata metadata,
                                                               ServerCallHandler<ReqT, RespT> serverCallHandler) {
    Metadata.Key<String> headerKey = Metadata.Key.of("headers", ASCII_STRING_MARSHALLER);
    String headers = metadata.get(headerKey);
    associatedGrpcSource.populateHeaderString(headers);
    System.out.println("Header received: " + headers);
    Context ctx;
    ctx = Context.ROOT;

    return Contexts.interceptCall(ctx, serverCall, metadata, serverCallHandler);
  }
}
