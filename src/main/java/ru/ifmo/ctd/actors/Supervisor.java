package ru.ifmo.ctd.actors;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.ReceiveTimeout;
import ru.ifmo.ctd.payload.Payload;

import java.time.Duration;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class Supervisor extends AbstractActor {
    private final Consumer<Map<String, List<String>>> myResultConsumer;
    private final Map<String, List<String>> result = new HashMap<>();
    public final static Set<String> searchEngines = Set.of("google", "yandex", "bing");
    private final String myHostName;
    private final int myPort;

    public static class SearchEngineRequest {
        private final String msg;

        public SearchEngineRequest(String msg) {
            this.msg = msg;
        }
    }

    static class SearchEngineResponse {
        private final String myEngine;
        private final List<String> myResult;

        SearchEngineResponse(String engine, List<String> result) {
            myEngine = engine;
            myResult = result;
        }
    }

    static public class SearchEngineError {
        private final String myEngine;

        SearchEngineError(String engine) {
            myEngine = engine;
        }
    }

    public Supervisor(Consumer<Map<String, List<String>>> resultConsumer, int timeout, String hostName, int port) {
        myResultConsumer = resultConsumer;
        myHostName = hostName;
        myPort = port;
        getContext().setReceiveTimeout(Duration.ofMillis(timeout));
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(SearchEngineRequest.class, this::receiveSearchEngineRequest)
                .match(SearchEngineResponse.class, this::receiveSearchEngineResponse)
                .match(ReceiveTimeout.class, s -> returnCurrentResult())
                .match(SearchEngineError.class, s -> receiveSearchEngineResponse(new SearchEngineResponse(s.myEngine, new LinkedList<>())))
                .build();
    }

    private void receiveSearchEngineRequest(SearchEngineRequest request) {
        String query = request.msg;
        for (String search : searchEngines) {
            ActorRef searchActor = getContext().actorOf(Props.create(SearchEngineActor.class, () -> new SearchEngineActor(myHostName, myPort)), search);
            searchActor.tell(new Payload(search, query), self());
        }
    }

    private void returnCurrentResult() {
        myResultConsumer.accept(result);
        getContext().cancelReceiveTimeout();
        context().stop(self());
    }

    private void receiveSearchEngineResponse(SearchEngineResponse response) {
        result.put(response.myEngine, response.myResult.stream().limit(5).collect(Collectors.toList()));
        if (result.size() == searchEngines.size()) {
            returnCurrentResult();
        }
    }
}
