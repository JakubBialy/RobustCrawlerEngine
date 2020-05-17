package com.jbialy.rce.downloader;

import com.jbialy.rce.downloader.core.DownloadResult;
import com.jbialy.rce.downloader.core.Downloader;
import org.jetbrains.annotations.NotNull;

import java.net.URI;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class HtmlDownloader implements Downloader<String, URI> {
    private static final Pattern CHARSET_HEADER_PATTERN = Pattern.compile("charset=(.+?)( |$)");
    private static final Charset SPARE_CHARSET = StandardCharsets.UTF_8;
    private final Downloader<String, URI> internalDownloader = new MapDownloader<>(new CoreDownloader(), download -> {
        final List<String> contentTypeValues = download.responseHeaders().get("content-type");

        if (contentTypeValues != null) {
            final String contentTypeValuesJoined = contentTypeValues.stream().map(x -> x + " ").collect(Collectors.joining());
            final Charset detectedCharset = getCharset(contentTypeValuesJoined);
            return new String(download.body(), detectedCharset);
        } else {
            return new String(download.body(), SPARE_CHARSET);
        }
    });

    @NotNull
    private static Charset getCharset(String contentTypeValue) {
        final Matcher matcher = CHARSET_HEADER_PATTERN.matcher(contentTypeValue);
        if (matcher.find()) {
            final String charsetString = matcher.group(1);
            if (Charset.isSupported(charsetString)) {
                return Charset.forName(charsetString);
            }
        }

        return SPARE_CHARSET;
    }

    @Override
    public DownloadResult<String, URI> download(URI uri) {
        return internalDownloader.download(uri);
    }
}
