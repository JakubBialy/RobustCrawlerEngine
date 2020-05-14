package com.jbialy.rce;

import java.util.List;
import java.util.Map;

public interface DownloadResponse<T, U> {
    int statusCode();

    Map<String, List<String>> responseHeaders();

    T body();

    U uri();
}
