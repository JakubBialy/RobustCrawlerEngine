package com.jbialy.rce.utils.server;

import org.jetbrains.annotations.NotNull;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MockHtmlUtils {
    private static final Pattern END_LINE_DIGITS = Pattern.compile("\\d+$");

    private MockHtmlUtils() {
    }

    public static long extractLongFromStringsEnd(String string, long defaultValue) {
        final Matcher matcher = END_LINE_DIGITS.matcher(string);

        if (matcher.find()) {
            return Long.parseLong(matcher.group());
        } else {
            return defaultValue;
        }
    }

    public static List<URI> createURIs(URI baseUri, long currentPageId, int previousUris, long firstPageId, int nextUris, long maxPageId) {
        ArrayList<URI> result = new ArrayList<>();

        for (long i = Math.min(previousUris, currentPageId - firstPageId - 1); i > 0L; i--) {
            final long nextId = currentPageId - i - 1;
            result.add(baseUri.resolve(URI.create("/test?id=" + nextId)));
        }

        result.add(baseUri.resolve(URI.create("/test?id=" + currentPageId)));

        final long nextHrefsCount = Math.min(nextUris, maxPageId - currentPageId);
        for (long i = 0L; i < nextHrefsCount; i++) {
            final long nextId = currentPageId + 1L + i;
            result.add(baseUri.resolve(URI.create("/test?id=" + nextId)));
        }

        result.trimToSize();
        return result;
    }

    public static List<URI> createURIsRange(URI baseUri, long firstPageId, long maxPageId) {
        ArrayList<URI> result = new ArrayList<>((int) (maxPageId - firstPageId));

        for (long i = firstPageId; i < maxPageId; i++) {
            result.add(baseUri.resolve(URI.create("/test?id=" + i)));
        }

        return result;
    }

    @NotNull
    public static String createHtmlMockPage(URI baseUri, long currentPageId, int hrefsToPreviousPages, long firstPageId, int hrefsToNextPages, long maxPageId) {
        final List<URI> uris = createURIs(baseUri, currentPageId, hrefsToPreviousPages, firstPageId, hrefsToNextPages, maxPageId);
        Collections.shuffle(uris);
        String result = "";

        for (int i = 0; i < uris.size(); i++) {
            result += "<a href=\"" + uris.get(i).toString() + "\">LINK (" + uris.get(i).toString() + ")<a/><br>";
        }

        return result;
    }
}
