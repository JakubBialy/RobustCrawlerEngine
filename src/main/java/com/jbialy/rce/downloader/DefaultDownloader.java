package com.jbialy.rce.downloader;

import com.jbialy.rce.ReTryPredicate;
import com.jbialy.rce.downloader.core.DownloadResponse;
import com.jbialy.rce.downloader.core.DownloadResult;
import com.jbialy.rce.downloader.core.Downloader;
import com.jbialy.rce.downloader.core.GenericHttpRequest;
import org.jetbrains.annotations.NotNull;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class DefaultDownloader implements Downloader<String, URI> {
    final ReTryPredicate<String, URI> reTryPredicate = null;
    private final HttpClient httpClient;
    private final Function<URI, GenericHttpRequest<URI>> requestMaker;
    private final Charset charset;

    public DefaultDownloader(Function<URI, GenericHttpRequest<URI>> requestMaker) {
        this(requestMaker, StandardCharsets.UTF_8);
    }

    public DefaultDownloader(Function<URI, GenericHttpRequest<URI>> requestMaker, Charset charset) {
        this.httpClient = HttpClientHelper.generateDefaultHttpClient();
        this.requestMaker = requestMaker;
        this.charset = charset;
    }

    private HttpRequest toHttpRequest(GenericHttpRequest<URI> genericHttpRequest) {
        return HttpRequest.newBuilder()
                .version(HttpClient.Version.HTTP_2)
                .GET()
                .uri(genericHttpRequest.getUri())
                .build();
    }

    @NotNull
    private DownloadResponse<String, URI> toGenericHttpResponse(HttpResponse<byte[]> httpResponse, String mappedResponseBody) {
        return new DownloadResponse<>() {
            @Override
            public int statusCode() {
                return httpResponse.statusCode();
            }

            @Override
            public Map<String, List<String>> responseHeaders() {
                return httpResponse.headers().map();
            }

            @Override
            public String body() {
                return mappedResponseBody;
            }

            @Override
            public URI uri() {
                return httpResponse.uri();
            }
        };
    }

    @NotNull
    private GenericHttpRequest<URI> toGenericHttpRequest(HttpRequest request) {
        return new GenericHttpRequest<URI>() {
            @Override
            public URI getUri() {
                return request.uri();
            }

            @Override
            public Map<String, List<String>> getHeaders() {
                return request.headers().map();
            }

//            @Override
//            public RequestMethod getRequestMethod() {
//                return RequestMethod.GET;
//            }
        };
    }

    @Override
    public DownloadResult<String, URI> download(URI uri) {
        final HttpRequest request = toHttpRequest(requestMaker.apply(uri));

        try {
            final HttpResponse<byte[]> httpResponse = httpClient.send(request, HttpResponse.BodyHandlers.ofByteArray());

            final String stringResponse = new String(httpResponse.body(), this.charset);

            return new DownloadResult<>(null, toGenericHttpResponse(httpResponse, stringResponse), toGenericHttpRequest(request));
        } catch (Exception e) {
            return new DownloadResult<>(e, null, toGenericHttpRequest(request));
        }
    }

    @Override
    public DownloadResult<String, URI> download(URI uri, SafetySwitch<DownloadResult<String, URI>> requestSafetySwitch) {
        final HttpRequest request = toHttpRequest(requestMaker.apply(uri));

        try {
            final HttpResponse<byte[]> httpResponse = httpClient.send(request, HttpResponse.BodyHandlers.ofByteArray());

            final String stringResponse = new String(httpResponse.body(), this.charset);

            return requestSafetySwitch.testAndReturn(new DownloadResult<>(null, toGenericHttpResponse(httpResponse, stringResponse), toGenericHttpRequest(request)));
        } catch (Exception e) {
            return requestSafetySwitch.testAndReturn(new DownloadResult<>(e, null, toGenericHttpRequest(request)));
        }
    }
}
