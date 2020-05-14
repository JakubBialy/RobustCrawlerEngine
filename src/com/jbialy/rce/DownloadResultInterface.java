package com.jbialy.rce;

public interface DownloadResultInterface<T, U> {

    Exception getException();

    DownloadResponse<T, U> getResponse();

    GenericHttpRequest<U> getRequest();
}
