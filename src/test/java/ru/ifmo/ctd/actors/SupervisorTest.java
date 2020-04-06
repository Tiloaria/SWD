package ru.ifmo.ctd.actors;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import org.apache.http.HttpHost;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.entity.StringEntity;
import org.apache.http.localserver.LocalServerTestBase;
import org.junit.*;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.Assert.*;

public class SupervisorTest extends LocalServerTestBase {
    private static final String defaultQuery = "my little cat";
    private static final ObjectMapper ourMapper = new ObjectMapper();

    @Test
    public void testAllActorsSucceeded() throws Exception {
        ActorSystem system = ActorSystem.create("testSystem");
        for (String search : Supervisor.searchEngines) {
            serverBootstrap.registerHandler("/" + search, (request, response, context) -> {
                String encodedQuery = URLEncoder.encode(defaultQuery, StandardCharsets.UTF_8);
                assertTrue(request.getRequestLine().getUri().contains(encodedQuery));
                ArrayNode result = ourMapper.createArrayNode();
                for (int i = 0; i < 5 + (new Random()).nextInt(5); i++) {
                    result.add(String.format(search + "%d", i));
                }
                JsonNode responsePayload = ourMapper.createObjectNode()
                        .put("searchProvider", search)
                        .set("results", result);
                response.setEntity(new StringEntity(responsePayload.toString()));
                response.setStatusCode(200);
            });
        }
        HttpHost server = start();
        AtomicBoolean finalFlag = new AtomicBoolean(false);
        ActorRef master = system.actorOf(
                Props.create(Supervisor.class, () -> new Supervisor(result -> {
                    assertEquals(Supervisor.searchEngines, result.keySet());
                    for (String search : Supervisor.searchEngines) {
                        List<String> searchRes = result.get(search);
                        assertEquals(5, searchRes.size());
                    }
                    finalFlag.set(true);
                }, 5000, server.getHostName(), server.getPort())),
                "testSupervisor0");
        master.tell(new Supervisor.SearchEngineRequest(defaultQuery), ActorRef.noSender());
        Thread.sleep(5000);
        assertTrue(finalFlag.get());
        shutDown();
        system.terminate();
    }

    @Test
    public void testOneActorsTimeouts() throws Exception {
        ActorSystem system = ActorSystem.create("testSystem");
        final String longRequestSearch = Supervisor.searchEngines.toArray(new String[0])[0];
        for (String search : Supervisor.searchEngines) {
            serverBootstrap.registerHandler("/" + search, (request, response, context) -> {
                String encodedQuery = URLEncoder.encode(defaultQuery, StandardCharsets.UTF_8);
                assertTrue(request.getRequestLine().getUri().contains(encodedQuery));
                ArrayNode result = ourMapper.createArrayNode();
                for (int i = 0; i < 5 + (new Random()).nextInt(5); i++) {
                    result.add(String.format(search + "%d", i));
                }
                if (search.equals(longRequestSearch)) {
                    try {
                        Thread.sleep(11000);
                    } catch (InterruptedException ignored) {
                    }
                }
                JsonNode responsePayload = ourMapper.createObjectNode()
                        .put("searchProvider", search)
                        .set("results", result);
                response.setEntity(new StringEntity(responsePayload.toString()));
                response.setStatusCode(200);
            });
        }
        HttpHost server = start();
        AtomicBoolean finalFlag = new AtomicBoolean(false);
        ActorRef master = system.actorOf(
                Props.create(Supervisor.class, () -> new Supervisor(result -> {
                    Set<String> expected = new HashSet<>(Supervisor.searchEngines);
                    expected.remove(longRequestSearch);
                    assertEquals(expected, result.keySet());
                    for (String search : result.keySet()) {
                        List<String> searchRes = result.get(search);
                        assertEquals(5, searchRes.size());
                    }
                    finalFlag.set(true);
                }, 5000, server.getHostName(), server.getPort())),
                "testSupervisor1");
        master.tell(new Supervisor.SearchEngineRequest(defaultQuery), ActorRef.noSender());
        Thread.sleep(10000);
        assertTrue(finalFlag.get());
        shutDown();
        system.terminate();
    }

    @Test
    public void testRequestWithError() throws Exception {
        ActorSystem system = ActorSystem.create("testSystem");
        final String errorRequestSearch = Supervisor.searchEngines.toArray(new String[0])[0];
        for (String search : Supervisor.searchEngines) {
            serverBootstrap.registerHandler("/" + search, (request, response, context) -> {
                String encodedQuery = URLEncoder.encode(defaultQuery, StandardCharsets.UTF_8);
                assertTrue(request.getRequestLine().getUri().contains(encodedQuery));
                ArrayNode result = ourMapper.createArrayNode();
                for (int i = 0; i < 5 + (new Random()).nextInt(5); i++) {
                    result.add(String.format(search + "%d", i));
                }
                if (search.equals(errorRequestSearch)) {
                    ((HttpGet)request).abort();
                }
                JsonNode responsePayload = ourMapper.createObjectNode()
                        .put("searchProvider", search)
                        .set("results", result);
                response.setEntity(new StringEntity(responsePayload.toString()));
                response.setStatusCode(200);
            });
        }
        HttpHost server = start();
        AtomicBoolean finalFlag = new AtomicBoolean(false);
        ActorRef master = system.actorOf(
                Props.create(Supervisor.class, () -> new Supervisor(result -> {
                    assertEquals(Supervisor.searchEngines, result.keySet());
                    for (String search : Supervisor.searchEngines) {
                        List<String> searchRes = result.get(search);
                        assertEquals(search.equals(errorRequestSearch) ? 0 : 5, searchRes.size());
                    }
                    finalFlag.set(true);
                }, 5000, server.getHostName(), server.getPort())),
                "testSupervisor2");
        master.tell(new Supervisor.SearchEngineRequest(defaultQuery), ActorRef.noSender());
        Thread.sleep(6000);
        assertTrue(finalFlag.get());
        shutDown();
        system.terminate();
    }
}