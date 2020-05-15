package com.jbialy.rce.downloader.core;

import com.jbialy.rce.downloader.core.DownloadResponse;
import com.jbialy.rce.downloader.core.GenericHttpRequest;

public class DownloadResult<T, U> {
    private final Exception exception;
    private final DownloadResponse<T, U> response;
    private final GenericHttpRequest<U> request;

    public DownloadResult(Exception exception, DownloadResponse<T, U> response, GenericHttpRequest<U> request) {
        this.exception = exception;
        this.response = response;
        this.request = request;
    }

    public Exception getException() {
        return this.exception;
    }

    public DownloadResponse<T, U> getResponse() {
        return this.response;
    }

    public GenericHttpRequest<U> getRequest() {
        return this.request;
    }
}
