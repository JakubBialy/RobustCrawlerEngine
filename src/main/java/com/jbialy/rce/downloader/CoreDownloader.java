package com.jbialy.rce.downloader;

import com.jbialy.rce.downloader.core.*;
import com.jbialy.rce.utils.DownloaderUtils;
import org.jetbrains.annotations.NotNull;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class CoreDownloader implements Downloader<byte[], URI> {
    private final GenericHttpClient<byte[], URI> httpClient;
    private final Function<URI, GenericHttpRequest<URI>> requestMaker;

    public CoreDownloader() {
        this(DownloaderUtils.toGenericHttpClient(HttpClientHelper.generateDefaultHttpClient()), CoreHttpRequest::new);
    }

    public CoreDownloader(String userAgentString) {
        this(DownloaderUtils.toGenericHttpClient(HttpClientHelper.generateDefaultHttpClient()), uri -> CoreHttpRequest.withCustomUserAgent(uri, userAgentString));
    }

    public CoreDownloader(Function<URI, GenericHttpRequest<URI>> requestMaker) {
        this(DownloaderUtils.toGenericHttpClient(HttpClientHelper.generateDefaultHttpClient()), requestMaker);
    }

    public CoreDownloader(GenericHttpClient<byte[], URI> httpClient, Function<URI, GenericHttpRequest<URI>> requestMaker) {
        this.httpClient = httpClient;
        this.requestMaker = requestMaker;
    }

    @NotNull
    private static GenericHttpRequest<URI> toGenericHttpRequest(HttpRequest request) {
        return new GenericHttpRequest<URI>() {
            @Override
            public URI getUri() {
                return request.uri();
            }

            @Override
            public Map<String, List<String>> getHeaders() {
                return request.headers().map();
            }
        };
    }

    private HttpRequest toHttpRequest(GenericHttpRequest<URI> genericHttpRequest) {
        return HttpRequest.newBuilder()
                .version(HttpClient.Version.HTTP_2)
                .GET()
                .uri(genericHttpRequest.getUri())
                .build();
    }

    @Override
    public DownloadResult<byte[], URI> download(URI uri) {
        final HttpRequest request = toHttpRequest(requestMaker.apply(uri));

        try {
            final DownloadResponse<byte[], URI> response = httpClient.sendRequest(DownloaderUtils.toGenericRequest(request));
            return new DownloadResult<>(null, response, toGenericHttpRequest(request));
        } catch (Exception e) {
            return new DownloadResult<>(e, null, toGenericHttpRequest(request));
        }
    }

    @Override
    public DownloadResult<byte[], URI> download(URI uri, SafetySwitch<DownloadResult<byte[], URI>> requestSafetySwitch) {
        final HttpRequest request = toHttpRequest(requestMaker.apply(uri));

        try {
            final DownloadResponse<byte[], URI> response = httpClient.sendRequest(DownloaderUtils.toGenericRequest(request));

            return requestSafetySwitch.testAndReturn(new DownloadResult<>(null, response, toGenericHttpRequest(request)));
        } catch (Exception e) {
            return requestSafetySwitch.testAndReturn(new DownloadResult<>(e, null, toGenericHttpRequest(request)));
        }
    }
}
