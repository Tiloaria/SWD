package ru.ifmo.ctd.actors;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClients;
import ru.ifmo.ctd.payload.Payload;

import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class SearchEngineActor extends AbstractActor {
    private static final ObjectMapper ourMapper = new ObjectMapper();
    private final String myHostName;
    private final int myPort;
    private final CloseableHttpAsyncClient client;

    static {
        ourMapper.getFactory().configure(JsonGenerator.Feature.ESCAPE_NON_ASCII, true);
    }

    @Override
    public void postStop() throws Exception {
        super.postStop();
        client.close();
    }

    public SearchEngineActor(String hostName, int port) {
        myHostName = hostName;
        myPort = port;
        client = HttpAsyncClients.createDefault();
    }

    private void onReceive(Payload payload) {
        String encodedQuery = URLEncoder.encode(payload.getQuery(), StandardCharsets.UTF_8);
        String path = String.format("/%s?search=%s",
                payload.getSearchEngine(),
                encodedQuery);
        HttpGet request = new HttpGet(path);
        ActorRef sender = sender();
        client.start();
        client.execute(new HttpHost(myHostName, myPort), request, new FutureCallback<>() {
            @Override
            public void completed(HttpResponse httpResponse) {
                try {
                    final InputStream responseContentStream = httpResponse.getEntity().getContent();
                    JsonNode responseContentNode = ourMapper.readTree(responseContentStream);
                    List<String> results = new ArrayList<>();
                    responseContentNode.get("results").forEach(jsonNode -> results.add(jsonNode.asText()));
                    sender.tell(new Supervisor.SearchEngineResponse(payload.getSearchEngine(), results), self());
                } catch (IOException e) {
                    sender.tell(new Supervisor.SearchEngineError(payload.getSearchEngine()), ActorRef.noSender());
                } finally {
                    getContext().stop(self());
                }
            }

            @Override
            public void failed(Exception e) {
                sender.tell(new Supervisor.SearchEngineError(payload.getSearchEngine()), ActorRef.noSender());
                getContext().stop(self());
            }

            @Override
            public void cancelled() {
                sender.tell(new Supervisor.SearchEngineError(payload.getSearchEngine()), ActorRef.noSender());
                getContext().stop(self());
            }
        });
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(Payload.class, this::onReceive)
                .build();
    }
}
