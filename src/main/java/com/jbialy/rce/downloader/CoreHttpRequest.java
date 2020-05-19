package com.jbialy.rce.downloader;

import com.jbialy.rce.downloader.core.GenericHttpRequest;

import java.net.URI;
import java.net.http.HttpRequest;
import java.util.List;
import java.util.Map;

public class CoreHttpRequest implements GenericHttpRequest<URI> {
    private final HttpRequest request;

    private CoreHttpRequest(HttpRequest request) {
        this.request = request;
    }

    public CoreHttpRequest(URI uri) {
        this.request = GenericRequestHelper.generateRequest(uri);
    }

    public static CoreHttpRequest withCustomUserAgent(URI uri, String userAgentString) {
        return new CoreHttpRequest(GenericRequestHelper.generateRequest(uri, userAgentString));
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
