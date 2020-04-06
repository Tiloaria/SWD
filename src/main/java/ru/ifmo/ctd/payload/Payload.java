package ru.ifmo.ctd.payload;

public class Payload {
    private final String searchEngine;
    private final String query;

    public Payload(String searchEngine, String query) {
        this.searchEngine = searchEngine;
        this.query = query;
    }

    public String getSearchEngine() {
        return searchEngine;
    }

    public String getQuery() {
        return query;
    }
}
