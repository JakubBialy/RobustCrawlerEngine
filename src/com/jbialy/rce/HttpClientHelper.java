package com.jbialy.rce;


import java.net.InetSocketAddress;
import java.net.ProxySelector;
import java.net.URI;
import java.net.http.HttpClient;
import java.time.Duration;

public class HttpClientHelper {
//    public static final GenericHttpClient<?, URI> DEFAULT_HTTP_CLIENT = createDefaultHttpClient();

    private HttpClientHelper() {
    }

    public static <E> GenericHttpClient<byte[], URI, E> createDefaultHttpClient() {
        HttpClient.Builder builder = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_2)
                .followRedirects(HttpClient.Redirect.ALWAYS)
                .proxy(ProxySelector.getDefault())
                .connectTimeout(Duration.ofMinutes(5));

        return DownloaderUtils.toGenericHttpClient(builder.build());
    }

    public static GenericHttpClient_V3_2<byte[], URI> createDefaultHttpClient_V3_2() {
        HttpClient.Builder builder = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_2)
                .followRedirects(HttpClient.Redirect.ALWAYS)
                .proxy(ProxySelector.getDefault())
                .connectTimeout(Duration.ofMinutes(5));

        return DownloaderUtils.toGenericHttpClient_V3_2(builder.build());
    }

    public static GenericHttpClient createDefaultHttpClient(InetSocketAddress proxy) {
        HttpClient.Builder builder = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_2)
                .followRedirects(HttpClient.Redirect.ALWAYS)
                .proxy(ProxySelector.of(proxy))
                .connectTimeout(Duration.ofMinutes(5));

        return DownloaderUtils.toGenericHttpClient(builder.build());
    }

    public static HttpClient generateDefaultHttpClient() {
        return HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_2)
                .followRedirects(HttpClient.Redirect.ALWAYS)
                .proxy(ProxySelector.getDefault())
                .connectTimeout(Duration.ofMinutes(5))
                .build();
    }
}
