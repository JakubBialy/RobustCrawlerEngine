package com.jbialy.rce.utils.server;

import com.jbialy.rce.downloader.core.DownloadResponse;

import java.net.URI;
import java.util.List;
import java.util.Map;

public class MockDownloadResponse implements DownloadResponse<String, URI> {
    private final int statusCode;
    private final Map<String, List<String>> responseHeaders;
    private final String body;
    private final URI uri;

    public MockDownloadResponse(int statusCode, Map<String, List<String>> responseHeaders, String body, URI uri) {
        this.statusCode = statusCode;
        this.responseHeaders = responseHeaders;
        this.body = body;
        this.uri = uri;
    }

    @Override
    public int statusCode() {
        return statusCode;
    }

    @Override
    public Map<String, List<String>> responseHeaders() {
        return responseHeaders;
    }

    @Override
    public String body() {
        return body;
    }

    @Override
    public URI uri() {
        return uri;
    }
}
