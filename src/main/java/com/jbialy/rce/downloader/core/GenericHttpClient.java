package com.jbialy.rce.downloader.core;

import java.io.Closeable;

public interface GenericHttpClient<T, U> extends Closeable {

    DownloadResponse<T, U> sendRequest(GenericHttpRequest<U> request) throws Exception;

    @Override
    default void close() {
    }
}
