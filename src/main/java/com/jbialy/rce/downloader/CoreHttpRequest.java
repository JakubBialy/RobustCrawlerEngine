package com.jbialy.rce.downloader;

import com.jbialy.rce.downloader.core.GenericHttpRequest;

import java.net.URI;
import java.net.http.HttpRequest;
import java.util.List;
import java.util.Map;

public class CoreHttpRequest implements GenericHttpRequest<URI> {
    private final HttpRequest request;

    public CoreHttpRequest(URI uri) {
        this.request = GenericRequestHelper.generateRequest(uri);
    }

    @Override
    public String toString() {
        return "GenericHttpRequest{" +
                "request=" + request +
                '}';
    }

    @Override
    public URI getUri() {
        return this.request.uri();
    }

    @Override
    public Map<String, List<String>> getHeaders() {
        return request.headers().map();
    }


}
