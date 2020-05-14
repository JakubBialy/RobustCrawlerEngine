package com.jbialy.rce;

import java.io.Closeable;

public interface GenericHttpClient_V3_2<T, U> extends Closeable {

    DownloadResponse<T, U> sendRequest(GenericHttpRequest<U> request) throws Exception;

    @Override
    default void close() {
    }
}
