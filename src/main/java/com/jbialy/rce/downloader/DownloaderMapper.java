package com.jbialy.rce.downloader;

import com.jbialy.rce.downloader.core.DownloadResponse;
import com.jbialy.rce.downloader.core.DownloadResult;
import com.jbialy.rce.downloader.core.Downloader;
import com.jbialy.rce.downloader.core.GenericHttpRequest;
import org.jetbrains.annotations.NotNull;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class DownloaderMapper<R, U> implements Downloader<R, U> {
    final Function<byte[], R> bodyMapFunction;
    final Function<U, URI> toUriFun;
    private final CoreDownloader coreDownloader = new CoreDownloader();

    public DownloaderMapper(Function<byte[], R> bodyMapFunction, Function<U, URI> toUriFun) {
        this.bodyMapFunction = bodyMapFunction;
        this.toUriFun = toUriFun;
    }

    @NotNull
    private static <R, U, R2, U2> DownloadResponse<R2, U2> mapDownloadResponseResponse(
            DownloadResponse<R, U> httpResponse,
            Function<R, R2> bodyMapFunction,
            U2 uri
    ) {
        return new DownloadResponse<R2, U2>() {
            @Override
            public int statusCode() {
                return httpResponse.statusCode();
            }

            @Override
            public Map<String, List<String>> responseHeaders() {
                return httpResponse.responseHeaders();
            }

            @Override
            public R2 body() {
                return bodyMapFunction.apply(httpResponse.body());
            }

            @Override
            public U2 uri() {
                return uri;
            }
        };
    }

    @Override
    public DownloadResult<R, U> download(U target) {
        final DownloadResult<byte[], URI> downloadResult = coreDownloader.download(toUriFun.apply(target));

        final DownloadResponse<R, U> mapResponse = mapDownloadResponseResponse(
                downloadResult.getResponse(),
                bodyMapFunction,
                target);

        final GenericHttpRequest<U> mapedRequest = new GenericHttpRequest<>() {

            @Override
            public U getUri() {
                return target;
            }

            @Override
            public Map<String, List<String>> getHeaders() {
                return downloadResult.getRequest().getHeaders();
            }
        };

        return new DownloadResult<>(downloadResult.getException(), mapResponse, mapedRequest);
    }
}
