package com.jbialy.rce.downloader.core;

import java.util.List;
import java.util.Map;

public interface GenericHttpRequest<U> {

    U getUri();

    Map<String, List<String>> getHeaders();
}
