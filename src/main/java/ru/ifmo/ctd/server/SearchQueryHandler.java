package ru.ifmo.ctd.server;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.OutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class SearchQueryHandler implements HttpHandler {

    private final String myProvider;
    private static final ObjectMapper ourMapper = new ObjectMapper();

    public SearchQueryHandler(String provider) {
        myProvider = provider;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        OutputStream outputStream = exchange.getResponseBody();
        String query = exchange.getRequestURI().getQuery();
        System.out.println("query" + query);
        if (!query.contains("search=")) {
            exchange.sendResponseHeaders(400, "Bad request".getBytes().length);
            outputStream.flush();
            outputStream.close();
            return;
        }

        if (query.contains("slow")) {
            try {
                Thread.sleep(TimeUnit.SECONDS.toMillis(10));
            } catch (InterruptedException ignored) {
            }
        }

        DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        ArrayNode results = ourMapper.createArrayNode();
        for (int i = 0; i < 10; i++) {
            results.add(String.format("result %d", i));
        }
        JsonNode responsePayload = ourMapper.createObjectNode()
                .put("searchProvider", myProvider)
                .put("timestamp", format.format(new Date()))
                .set("results", results);
        exchange.sendResponseHeaders(200, responsePayload.toString().length());
        outputStream.write(ourMapper.writeValueAsBytes(responsePayload));
        outputStream.flush();
        outputStream.close();
    }
}
