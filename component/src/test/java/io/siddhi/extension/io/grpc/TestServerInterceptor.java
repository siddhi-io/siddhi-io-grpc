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

package io.siddhi.extension.io.grpc;

import io.grpc.*;

import java.util.Set;

import static io.grpc.Metadata.ASCII_STRING_MARSHALLER;

/**
 * Created by rayt on 10/6/16.
 */
public class TestServerInterceptor implements ServerInterceptor {
  private static final ServerCall.Listener NOOP_LISTENER = new ServerCall.Listener() {
  };

  public TestServerInterceptor() {
  }

  @Override
  public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(ServerCall<ReqT, RespT> serverCall,
                                                               Metadata metadata,
                                                               ServerCallHandler<ReqT, RespT> serverCallHandler) {
    String name = metadata.get(Metadata.Key.of("Name", ASCII_STRING_MARSHALLER));
    Set<String> keys = metadata.keys();
    System.out.println("Header received" + name);
    Context ctx;
    ctx = Context.ROOT;

    return Contexts.interceptCall(ctx, serverCall, metadata, serverCallHandler);
  }
}
