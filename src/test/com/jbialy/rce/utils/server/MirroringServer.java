package com.jbialy.rce.utils.server;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

public class MirroringServer {
    private final URI BASE_URI;
    private final HttpServer httpServer;

    public MirroringServer(int port) throws IOException {
        if (port < 0 || port > 65535) throw new IllegalArgumentException("Illegal port");

        BASE_URI = URI.create("http://localhost:" + port);

        httpServer = HttpServer.create(new InetSocketAddress(port), 0);
        httpServer.createContext("/", exchange -> {
            try (OutputStream os = exchange.getResponseBody()) {
                String response = requestToHTML(exchange, BASE_URI);

                exchange.sendResponseHeaders(200, response.length());
                os.write(response.getBytes());
            }
        });

        httpServer.setExecutor(Executors.newFixedThreadPool(16));
        httpServer.start();
    }

    private static String requestToHTML(HttpExchange exchange, URI BASE_URI) throws IOException {
        final String requestUriString = BASE_URI.resolve(exchange.getRequestURI()).toString();
        final String requestMethod = exchange.getRequestMethod();

        final String requestHeadersString = exchange
                .getRequestHeaders()
                .entrySet()
                .stream()
                .map(entry -> entry.getKey() + ":" + entry.getValue() + "\n")
                .collect(Collectors.joining());

        String requestBodyString = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);

        return """
                <html>
                <body>
                Request uri:<br>
                %s 
                <br>              
                Request method:<br>
                %s 
                <br>
                Request headers:<br>
                %s 
                <br>
                Request body:<br>
                %s
                </body>
                </html>                  
                """
                .formatted(
                        requestUriString,
                        requestMethod,
                        requestHeadersString.replaceAll("\n", "<br>"),
                        requestBodyString.replaceAll("\n", "<br>"));
    }

    public void stop() {
        httpServer.stop(0);
    }
}
