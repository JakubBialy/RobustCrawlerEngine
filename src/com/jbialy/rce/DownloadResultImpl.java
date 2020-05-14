package com.jbialy.rce;

import org.jetbrains.annotations.NotNull;

public class DownloadResultImpl<T, U, E> implements DownloadResultInterface<T, U> { //Type, Uri, Extra
    private final GenericHttpRequest<U> request;
    private final DownloadResponse<T, U> response;
    private final E computedExtra;
    private final Exception exception;

    private DownloadResultImpl(GenericHttpRequest<U> request, E computedExtra, DownloadResponse<T, U> response) {
        this.response = response;
        this.request = request;
        this.computedExtra = computedExtra;
        this.exception = null;
    }

    private DownloadResultImpl(Exception exception) {
        this.response = null;
        this.request = null;
        this.computedExtra = null;
        this.exception = exception;
    }

    public static <T, U, E> DownloadResultImpl<T, U, E> of(@NotNull DownloadResponse<T, U> response, GenericHttpRequest<U> request, E computedExtra) {
        return new DownloadResultImpl<>(request, computedExtra, response);
    }

    public static <T, U, E> DownloadResultImpl<T, U, E> ofException(Exception e) {
        return new DownloadResultImpl<>(e);
    }

    public Exception getException() {
        return exception;
    }

    public DownloadResponse<T, U> getResponse() {
        return response;
    }

    public E getComputedExtra() {
        return computedExtra;
    }

    @Override
    public String toString() {
        return "DownloadResult{" +
                "request=" + request +
                ", response=" + response +
                ", computedExtra=" + computedExtra +
                ", exception=" + exception +
                '}';
    }

    public GenericHttpRequest<U> getRequest() {
        return this.request;
    }

    public enum DownloadStatusEnum {
        OK, ERROR
    }
}
