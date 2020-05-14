package com.jbialy.rce;

import java.io.Closeable;
import java.util.function.BiFunction;

public interface GenericHttpClient<T, U, E> extends Closeable {

    DownloadResultImpl<T, U, E> sendRequest(
            GenericHttpRequest<U> request,
            BiFunction<GenericHttpRequest<U>, DownloadResponse<T, U>, E> computeExtra
    );

    @Override
    default void close() {
    }
}
