package com.jbialy.rce.downloader;

import com.jbialy.rce.downloader.core.GenericHttpRequest;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.time.Duration;
import java.util.List;
import java.util.Map;

public class GenericRequestHelper {
    private GenericRequestHelper() {

    }

    public static HttpRequest generateRequest(String stringUri) {
        return generateRequest(URI.create(stringUri));
    }

    public static HttpRequest generateRequest(URI uri) {
        return HttpRequest.newBuilder()
                .uri(uri)
                .header("User-Agent", "Mozilla/5.0 (compatible; Googlebot/2.1; +http://www.google.com/bot.html)")
                .GET()
                .build();
    }

    public static GenericHttpRequest<URI> uriGenericGetRequest(URI uri) {
        final HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(uri)
                .header("User-Agent", "Mozilla/5.0 (compatible; Googlebot/2.1; +http://www.google.com/bot.html)")
                .GET()
                .build();

        return new GenericHttpRequest<>() {
            @Override
            public URI getUri() {
                return httpRequest.uri();
            }

            @Override
            public Map<String, List<String>> getHeaders() {
                return httpRequest.headers().map();
            }

//            @Override
//            public RequestMethod getRequestMethod() {
//                return RequestMethod.GET;
//            }
        };
    }

    public static HttpClient buildHttpClient() {
        HttpClient.Builder builder = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_2)
                .followRedirects(HttpClient.Redirect.ALWAYS)
                .connectTimeout(Duration.ofMinutes(5));

        return builder.build();
    }
}
