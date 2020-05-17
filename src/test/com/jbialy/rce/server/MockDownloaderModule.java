package com.jbialy.rce.server;


import com.jbialy.rce.downloader.core.DownloadResponse;
import com.jbialy.rce.downloader.core.DownloadResult;
import com.jbialy.rce.downloader.core.Downloader;
import com.jbialy.rce.downloader.core.GenericHttpRequest;

import java.net.URI;
import java.util.List;
import java.util.Map;

import static com.jbialy.rce.server.MockHtmlUtils.createHtmlMockPage;
import static com.jbialy.rce.server.MockHtmlUtils.extractLongFromStringsEnd;

public class MockDownloaderModule implements Downloader<String, URI> {
    private static final Map<String, List<String>> EMPTY_RESPONSE_HEADERS_MAP = Map.of();

    private int hrefsToPreviousPages = 256;
    private int firstPageId = 0;
    private int hrefsToNextPages = 256;
    private int maxPageId = 250_000;

    public MockDownloaderModule() {
    }

    public MockDownloaderModule(int hrefsToPreviousPages, int firstPageId, int hrefsToNextPages, int maxPageId) {
        this.hrefsToPreviousPages = hrefsToPreviousPages;
        this.firstPageId = firstPageId;
        this.hrefsToNextPages = hrefsToNextPages;
        this.maxPageId = maxPageId;
    }

    public static MockDownloaderModule ofPagesCount(int pagesCount) {
        return new MockDownloaderModule(256, 0, 256, pagesCount - 1);
    }

    public URI getFirstPage() {
        return URI.create("http://localhost:8888/test?id=" + firstPageId);
    }

    @Override
    public DownloadResult<String, URI> download(URI uri) {
        final long currentPageId = extractLongFromStringsEnd(uri.toString(), -1L);
        final String mockPage = createHtmlMockPage(uri, currentPageId, hrefsToPreviousPages, firstPageId, hrefsToNextPages, maxPageId);
        final DownloadResponse<String, URI> response = new MockDownloadResponse(200, EMPTY_RESPONSE_HEADERS_MAP, mockPage, uri);
        final GenericHttpRequest<URI> request = new MockRequest(EMPTY_RESPONSE_HEADERS_MAP, uri);

        return new DownloadResult<>(null, response, request);
    }
}
