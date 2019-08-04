package io.siddhi.extension.io.grpc.util;

import io.grpc.CallCredentials;
import io.grpc.Metadata;

import java.util.concurrent.Executor;

public class HeaderCallCredential extends CallCredentials {
    String headerString;

    public HeaderCallCredential(String headerString) {
        this.headerString = headerString;
    }

    @Override
    public void applyRequestMetadata(RequestInfo requestInfo, Executor appExecutor, MetadataApplier applier) {
        Metadata headers = new Metadata();
        Metadata.Key<String> key =
                Metadata.Key.of("headers", Metadata.ASCII_STRING_MARSHALLER);
        headers.put(key, headerString);
        applier.apply(headers);
    }

    @Override
    public void thisUsesUnstableApi() {

    }
}
