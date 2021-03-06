package com.jbialy.rce.utils.server;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SimpleHttpServer {
    private static final Pattern END_LINE_DIGITS = Pattern.compile("\\d+$");
    private final URI BASE_URI;
    private final HttpServer httpServer;

    public SimpleHttpServer(long count, int port) throws IOException {
        if (port < 0 || port > 65535) throw new IllegalArgumentException("Illegal port");

        BASE_URI = URI.create("http://localhost:" + port);

        long firstValue = 0;
        int nextIdHrefsPerPage = 1024;
        int previousIdHrefsPerPage = 24;

        httpServer = HttpServer.create(new InetSocketAddress(8888), 0);
        httpServer.createContext("/test", new HttpHandler() {
            @Override
            public void handle(HttpExchange exchange) throws IOException {
                final URI uri = exchange.getRequestURI();
                final String query = uri.getQuery();
                final Matcher matcher = END_LINE_DIGITS.matcher(query);

                String response;

                if (matcher.find()) {
                    long id = MockHtmlUtils.extractLongFromStringsEnd(query, -1L);

                    if (id >= firstValue && id < count) {
                        response = MockHtmlUtils.createHtmlMockPage(BASE_URI, id, previousIdHrefsPerPage, firstValue, nextIdHrefsPerPage, firstValue + count - 1);

                        exchange.sendResponseHeaders(200, response.length());

                        try (OutputStream os = exchange.getResponseBody()) {
                            os.write(response.getBytes());
                        }
                    } else {
                        response = "404";

                        exchange.sendResponseHeaders(404, response.length());

                        try (OutputStream os = exchange.getResponseBody()) {
                            os.write(response.getBytes());
                        }
                    }
                }
            }
        });

        httpServer.setExecutor(Executors.newFixedThreadPool(16));
        httpServer.start();
    }


    public void stop() {
        httpServer.stop(0);
    }
}
