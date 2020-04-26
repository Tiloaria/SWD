package ru.ifmo.ctd.turnstile;

import com.sun.net.httpserver.HttpServer;
import org.apache.commons.cli.*;
import org.apache.http.HttpStatus;
import ru.ifmo.ctd.storage.PostgresStorage;
import ru.ifmo.ctd.storage.Storage;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.sql.SQLException;
import java.util.Map;

import static ru.ifmo.ctd.helpers.MainUtils.queryToMap;

public class Main {
    public static void main(String[] args) {
        try {
            TurnstileServerArgs.init();
            TurnstileServerArgs.parseArgs(args);

            String hostname = TurnstileServerArgs.getHostname();
            int port = TurnstileServerArgs.getPortNumber();

            String pgHostname = TurnstileServerArgs.getDbHostname();
            int pgPort = TurnstileServerArgs.getDbPortNumber();

            String statsHostname = TurnstileServerArgs.getStatsServerHostname();
            int statsPort = TurnstileServerArgs.getStatsServerPortNumber();
            String statsNotificationEndpoint = TurnstileServerArgs.getStatsServerNotificationEndpoint();

            HttpServer server = HttpServer.create(new InetSocketAddress(hostname, port), 0);
            Storage postgresStorage = new PostgresStorage(pgHostname, pgPort);
            StatisticsNotifier notifier = new StatisticsNotifier(statsHostname, statsPort, statsNotificationEndpoint);
            TurnstileCommandHandler handler = new TurnstileCommandHandler(postgresStorage, notifier);

            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                try {
                    server.stop(0);
                    postgresStorage.close();
                } catch (SQLException e) {
                    System.err.println("Fatal datastore error on shutdown!");
                    System.err.println(e.getMessage());
                }
            }));

            server.createContext("/enter", exchange -> {
                String query = exchange.getRequestURI().getQuery();
                Map<String, String> queryParams = queryToMap(query);
                if (!queryParams.containsKey("id")) {
                    exchange.sendResponseHeaders(HttpStatus.SC_BAD_REQUEST, "Bad request".getBytes().length);
                    OutputStream outputStream = exchange.getResponseBody();
                    outputStream.flush();
                    outputStream.close();
                    return;
                }
                Integer id = Integer.valueOf(queryParams.get("id"));
                long time = System.currentTimeMillis();
                EnterCommand command = new EnterCommand(id, time);
                String result = handler.handle(command);
                exchange.sendResponseHeaders(HttpStatus.SC_OK, result.length());
                OutputStream outputStream = exchange.getResponseBody();
                outputStream.write(result.getBytes());
                outputStream.flush();
                outputStream.close();
            });
            server.createContext("/exit", exchange -> {
                String query = exchange.getRequestURI().getQuery();
                Map<String, String> queryParams = queryToMap(query);
                Integer id = Integer.valueOf(queryParams.get("id"));
                if (!queryParams.containsKey("id")) {
                    exchange.sendResponseHeaders(HttpStatus.SC_BAD_REQUEST, "Bad request".getBytes().length);
                    OutputStream outputStream = exchange.getResponseBody();
                    outputStream.flush();
                    outputStream.close();
                    return;
                }
                long time = System.currentTimeMillis();
                ExitCommand command = new ExitCommand(id, time);
                String result = handler.handle(command);
                exchange.sendResponseHeaders(HttpStatus.SC_OK, result.length());
                OutputStream outputStream = exchange.getResponseBody();
                outputStream.write(result.getBytes());
                outputStream.flush();
                outputStream.close();
            });
            server.start();
        } catch (ParseException | NumberFormatException e) {
            System.out.println(e.getMessage());
            TurnstileServerArgs.printUsage("turnstile-server");
            System.exit(1);
        } catch (IOException e) {
            System.out.println(e.getMessage());
            System.exit(2);
        } catch (SQLException e) {
            System.err.println(e.getMessage());
            System.exit(3);
        }
    }
}
