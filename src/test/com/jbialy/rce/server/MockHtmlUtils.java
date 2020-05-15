package com.jbialy.tests.server;

import org.jetbrains.annotations.NotNull;

import java.net.URI;
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

    @NotNull
    public static String createHtmlMockPage(URI baseUri, long currentPageId, int hrefsToPreviousPages, long firstPageId, int hrefsToNextPages, long maxPageId) {
        String response = "";
        for (long i = Math.min(hrefsToPreviousPages, currentPageId - firstPageId - 1); i > 0L; i--) {
            final long nextId = currentPageId - i - 1;
            response += "<a href=\"" + baseUri.resolve(URI.create("/test?id=" + nextId)).toString() + "\">LINK (" + nextId + ")<a/><br>";
        }

        response += "<br><a href=\"" + baseUri.resolve(URI.create("/test?id=" + currentPageId)).toString() + "\">LINK (" + currentPageId + ")<a/><br><br>";

        final long nextHrefsCount = Math.min(hrefsToNextPages, maxPageId - currentPageId);
        for (long i = 0L; i < nextHrefsCount; i++) {
            final long nextId = currentPageId + 1L + i;
            response += "<a href=\"" + baseUri.resolve(URI.create("/test?id=" + nextId)).toString() + "\">LINK (" + nextId + ")<a/><br>";
        }
        return response;
    }
}
