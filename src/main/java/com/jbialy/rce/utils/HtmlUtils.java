package com.jbialy.rce.utils;

import com.jbialy.rce.downloader.core.DownloadResult;
import org.jetbrains.annotations.NotNull;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class   HtmlUtils {
    //        public static final Pattern HREF_PATTERN = Pattern.compile(" href=\"(.*?)\"");
    public static final Pattern HREF_PATTERN = Pattern.compile(" href=\"(.+?)\"");

    private HtmlUtils() {

    }

    public static List<URI> extractHrefs(String html, URI requestUri, Pattern removePattern, boolean removeDuplicates) {
        Matcher m = HREF_PATTERN.matcher(html);
        List<URI> result = new ArrayList<>();

        while (m.find()) {
            try {
                final String href = removePattern.matcher(m.group(1)).replaceAll("");
                if (!href.isEmpty()) {
                    result.add(requestUri.resolve(href));
                }
            } catch (Throwable ignored) {
            }
        }

        if (removeDuplicates) {
            result = new ArrayList<>(new HashSet<>(result));
        }

        return result;
    }

    public static List<URI> extractHrefs(String html, URI requestUri, boolean removeDuplicates) {
        Matcher m = HREF_PATTERN.matcher(html);
        List<String> stringHrefs = new ArrayList<>();

        while (m.find()) {
            final String href = m.group(1);
            stringHrefs.add(href);
//            try {
//                if (!href.isEmpty()) {
//                    final URI uri = URI.create(href);
//                    stringHrefs.add(requestUri.resolve(uri));
//                }
//            } catch (Throwable ignored) {
//                ignored.printStackTrace();
//            }
        }

        if (removeDuplicates) {
            stringHrefs = new ArrayList<>(new HashSet<>(stringHrefs));
        }

        List<URI> uriHrefs = new ArrayList<>(stringHrefs.size());

        for (int i = 0; i < stringHrefs.size(); i++) {
            uriHrefs.add(requestUri.resolve(stringHrefs.get(i)));
        }

        if (removeDuplicates) {
            uriHrefs = new ArrayList<>(new HashSet<>(uriHrefs));
        }

        return uriHrefs;
    }

    @NotNull
    public static Function<DownloadResult<String, URI>, Collection<URI>> getDefaultUriExtractor() {
        return downloadResult -> {
            final String html = downloadResult.getResponse().body();
            final URI uri = downloadResult.getResponse().uri();
            return HtmlUtils.extractHrefs(html, uri, true);
        };
    }

    @NotNull
    public static Collection<URI> simpleExtractUris(DownloadResult<String, URI> downloadResult) {
        final String html = downloadResult.getResponse().body();
        final URI uri = downloadResult.getResponse().uri();
        return HtmlUtils.extractHrefs(html, uri, true);
    }
}
