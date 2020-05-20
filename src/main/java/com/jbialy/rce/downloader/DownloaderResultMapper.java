package com.jbialy.rce.downloader;

import com.jbialy.rce.downloader.core.DownloadResponse;
import com.jbialy.rce.downloader.core.DownloadResult;
import com.jbialy.rce.downloader.core.Downloader;
import org.jetbrains.annotations.NotNull;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class DownloaderResultMapper<T, R> implements Downloader<R, URI> {
    private final Downloader<T, URI> innerDownloader;
    private final Function<DownloadResponse<T, URI>, R> mapper;

    public DownloaderResultMapper(Downloader<T, URI> innerDownloader, Function<DownloadResponse<T, URI>, R> mapper) {
        this.innerDownloader = innerDownloader;
        this.mapper = mapper;
    }

    @NotNull
    private DownloadResponse<R, URI> changeBody(DownloadResponse<T, URI> httpResponse, R mappedResponseBody) {
        return new DownloadResponse<R, URI>() {
            @Override
            public int statusCode() {
                return httpResponse.statusCode();
            }

            @Override
            public Map<String, List<String>> responseHeaders() {
                return httpResponse.responseHeaders();
            }

            @Override
            public R body() {
                return mappedResponseBody;
            }

            @Override
            public URI uri() {
                return httpResponse.uri();
            }
        };
    }

    @Override
    public DownloadResult<R, URI> download(URI uri) {
        final DownloadResult<T, URI> download = this.innerDownloader.download(uri);

        if (download.getException() == null) {
            final DownloadResponse<T, URI> httpResponse = download.getResponse();
            final R mappedBody = mapper.apply(httpResponse);

            return new DownloadResult<>(null, changeBody(httpResponse, mappedBody), download.getRequest());
        } else {
            return new DownloadResult<>(download.getException(), null, download.getRequest());
        }
    }

    @Override
    public DownloadResult<R, URI> download(URI uri, SafetySwitch<DownloadResult<R, URI>> requestSafetySwitch) {
        final DownloadResult<T, URI> download = this.innerDownloader.download(uri);

        if (download.getException() == null) {
            final DownloadResponse<T, URI> httpResponse = download.getResponse();
            final R mappedBody = mapper.apply(httpResponse);

            return requestSafetySwitch.testAndReturn(new DownloadResult<>(null, changeBody(httpResponse, mappedBody), download.getRequest()));
        } else {
            return requestSafetySwitch.testAndReturn(new DownloadResult<>(download.getException(), null, download.getRequest()));
        }
    }
}
