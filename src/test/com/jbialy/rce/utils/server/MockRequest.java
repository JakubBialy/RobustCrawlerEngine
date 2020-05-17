package com.jbialy.rce.utils.server;

import com.jbialy.rce.downloader.core.GenericHttpRequest;

import java.net.URI;
import java.util.List;
import java.util.Map;

public class MockRequest implements GenericHttpRequest<URI> {
    private final Map<String, List<String>> map;
    private final URI uri;

    public MockRequest(Map<String, List<String>> map, URI uri) {
        this.map = map;
        this.uri = uri;
    }

    @Override
    public URI getUri() {
        return uri;
    }

    @Override
    public Map<String, List<String>> getHeaders() {
        return map;
    }
}
