package com.jbialy.rce;

import javax.net.ssl.SSLSession;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;

public class DownloaderUtils {

    private DownloaderUtils() {
    }

    public static <T> DownloadResponse<T, URI> toGenericHttpResponse(HttpResponse<T> httpResponse) {
        return new DownloadResponseImpl<>(httpResponse);
    }

    public static GenericHttpRequest<URI> toGenericRequest(HttpRequest httpRequest) {

        return new GenericHttpRequest<URI>() {
            @Override
            public URI getUri() {
                return httpRequest.uri();
            }

            @Override
            public Map<String, List<String>> getHeaders() {
                return httpRequest.headers().map();
            }

//            @Override
//            public String getRequestMethod() {
//                return httpRequest.method();
//            }
        };
    }

    public static <T, R> HttpResponse<R> mapHttpResponse(HttpResponse<T> httpResponse, Function<T, R> mapper) {
        final T body = httpResponse.body();

        if (body != null) {
            final R mapped = mapper.apply(body);
            return new HttpResponse<R>() {
                @Override
                public int statusCode() {
                    return httpResponse.statusCode();
                }

                @Override
                public HttpRequest request() {
                    return httpResponse.request();
                }

                @Override
                public Optional<HttpResponse<R>> previousResponse() {
                    return Optional.empty();
                }

                @Override
                public HttpHeaders headers() {
                    return httpResponse.headers();
                }

                @Override
                public R body() {
                    return mapped;
                }

                @Override
                public Optional<SSLSession> sslSession() {
                    return Optional.empty();
                }

                @Override
                public URI uri() {
                    return httpResponse.uri();
                }

                @Override
                public HttpClient.Version version() {
                    return httpResponse.version();
                }
            };
        } else {
            return (HttpResponse<R>) httpResponse;
        }
    }

    public static <E> GenericHttpClient<byte[], URI, E> toGenericHttpClient(HttpClient httpClient) {
        return new GenericHttpClientImpl<>(httpClient);
    }

    public static GenericHttpClient_V3_2<byte[], URI> toGenericHttpClient_V3_2(HttpClient httpClient) {
        //todo cleanup + extract methods

        return new GenericHttpClient_V3_2<byte[], URI>() {
            @Override
            public DownloadResponse<byte[], URI> sendRequest(GenericHttpRequest<URI> request) throws Exception {
                final HttpRequest httpRequest = HttpRequest.newBuilder()
                        .uri(request.getUri())
                        .GET()
                        .build();

                final HttpResponse<byte[]> httpResponse = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofByteArray());
                return DownloaderUtils.toGenericHttpResponse(httpResponse);
            }
        };
    }

    private static final class GenericHttpClientImpl<E> implements GenericHttpClient<byte[], URI, E> {
        private final HttpClient httpClient;

        private GenericHttpClientImpl(HttpClient httpClient) {
            this.httpClient = httpClient;
        }

        private final static HttpRequest uriToRequest(URI uri) {
            return HttpRequest.newBuilder()
                    .uri(uri)
                    .GET()
                    .build();
        }

        @Override
        public DownloadResultImpl<byte[], URI, E> sendRequest(GenericHttpRequest<URI> request, BiFunction<GenericHttpRequest<URI>, DownloadResponse<byte[], URI>, E> computeExtra) {
            HttpResponse<byte[]> response;
            try { //Minimum code inside try block, because it's blocking JVM optimisations
                response = this.httpClient.send(uriToRequest(request.getUri()), HttpResponse.BodyHandlers.ofByteArray());

                if (response == null) {
                    throw new NullPointerException("Response is null.");
                }
            } catch (InterruptedException | IOException e) {
                return DownloadResultImpl.ofException(e);
            }

            final DownloadResponse<byte[], URI> downloadResponse = DownloaderUtils.toGenericHttpResponse(response);
            final E extra = computeExtra.apply(request, downloadResponse);
            return DownloadResultImpl.of(downloadResponse, request, extra);
        }

        @Override
        public void close() {
        }
    }

    private static final class DownloadResponseImpl<T> implements DownloadResponse<T, URI> {
        private final HttpResponse<T> httpResponse;

        private DownloadResponseImpl(HttpResponse<T> httpResponse) {
            this.httpResponse = httpResponse;
        }

        @Override
        public int statusCode() {
            return this.httpResponse.statusCode();
        }

        @Override
        public Map<String, List<String>> responseHeaders() {
            return this.httpResponse.headers().map();
        }

        @Override
        public T body() {
            return this.httpResponse.body();
        }

        @Override
        public URI uri() {
            return this.httpResponse.uri();
        }
    }
}
