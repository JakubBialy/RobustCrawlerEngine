package com.jbialy.rce.downloader;


import com.jbialy.rce.downloader.core.GenericHttpClient;
import com.jbialy.rce.utils.DownloaderUtils;

import java.net.ProxySelector;
import java.net.URI;
import java.net.http.HttpClient;
import java.time.Duration;

public class HttpClientHelper {
//    public static final GenericHttpClient<?, URI> DEFAULT_HTTP_CLIENT = createDefaultHttpClient();

    private HttpClientHelper() {
    }

//    public static <E> GenericHttpClient<byte[], URI, E> createDefaultHttpClient() {
//        HttpClient.Builder builder = HttpClient.newBuilder()
//                .version(HttpClient.Version.HTTP_2)
//                .followRedirects(HttpClient.Redirect.ALWAYS)
//                .proxy(ProxySelector.getDefault())
//                .connectTimeout(Duration.ofMinutes(5));
//
//        return DownloaderUtils.toGenericHttpClient(builder.build());
//    }

    public static GenericHttpClient<byte[], URI> createDefaultHttpClient() {
        HttpClient.Builder builder = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_2)
                .followRedirects(HttpClient.Redirect.ALWAYS)
                .proxy(ProxySelector.getDefault())
                .connectTimeout(Duration.ofMinutes(5));

        return DownloaderUtils.toGenericHttpClient(builder.build());
    }

//    public static GenericHttpClient createDefaultHttpClient(InetSocketAddress proxy) {
//        HttpClient.Builder builder = HttpClient.newBuilder()
//                .version(HttpClient.Version.HTTP_2)
//                .followRedirects(HttpClient.Redirect.ALWAYS)
//                .proxy(ProxySelector.of(proxy))
//                .connectTimeout(Duration.ofMinutes(5));
//
//        return DownloaderUtils.toGenericHttpClient(builder.build());
//    }

    public static HttpClient generateDefaultHttpClient() {
        return HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_2)
                .followRedirects(HttpClient.Redirect.ALWAYS)
                .proxy(ProxySelector.getDefault())
                .connectTimeout(Duration.ofMinutes(5))
                .build();
    }
}
