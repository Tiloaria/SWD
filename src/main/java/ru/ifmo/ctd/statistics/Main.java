package ru.ifmo.ctd.statistics;

import com.sun.net.httpserver.HttpServer;
import org.apache.commons.cli.ParseException;
import org.apache.http.HttpStatus;
import ru.ifmo.ctd.storage.PostgresStorage;
import ru.ifmo.ctd.helpers.BaseServerArgs;
import ru.ifmo.ctd.helpers.MainUtils;
import ru.ifmo.ctd.model.PassType;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.sql.SQLException;
import java.util.Map;

public class Main {

    public static void main(String[] args) {
        try {
            BaseServerArgs.init();
            BaseServerArgs.parseArgs(args);

            String hostname = BaseServerArgs.getHostname();
            int port = BaseServerArgs.getPortNumber();

            String pgHostname = BaseServerArgs.getDbHostname();
            int pgPort = BaseServerArgs.getDbPortNumber();

            HttpServer server = HttpServer.create(new InetSocketAddress(hostname, port), 0);
            PostgresStorage postgresStorage = new PostgresStorage(pgHostname, pgPort);
            InMemoryStatisticsManager statsManager = new InMemoryStatisticsManager(postgresStorage);

            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                try {
                    server.stop(0);
                    postgresStorage.close();
                } catch (SQLException e) {
                    System.err.println("Fatal datastore error on shutdown!");
                    System.err.println(e.getMessage());
                }
            }));

            server.createContext("/statistics/avg_visits_freq", exchange -> {
                String result = String.valueOf(statsManager.averageFrequencyVisits());
                exchange.sendResponseHeaders(HttpStatus.SC_OK, result.getBytes().length);
                OutputStream outputStream = exchange.getResponseBody();
                outputStream.write(result.getBytes());
                outputStream.flush();
                outputStream.close();
            });
            server.createContext("/statistics/avg_visits_len", exchange -> {
                String result = String.valueOf(statsManager.averageLengthVisits());
                OutputStream outputStream = exchange.getResponseBody();
                exchange.sendResponseHeaders(HttpStatus.SC_OK, result.getBytes().length);
                outputStream.write(result.getBytes());
                outputStream.flush();
                outputStream.close();
            });
            server.createContext("/statistics/visits_stat", exchange -> {
                Map<String, Integer> visits = statsManager.numOfVisitsStats();
                StringBuilder result = new StringBuilder();
                for (Map.Entry<String, Integer> day : visits.entrySet()) {
                    result.append(String.format("In %s was %d visits\n", day.getKey(), day.getValue()));
                }
                exchange.sendResponseHeaders(HttpStatus.SC_OK, result.toString().getBytes().length);
                OutputStream outputStream = exchange.getResponseBody();
                outputStream.write(result.toString().getBytes());
                outputStream.flush();
                outputStream.close();
            });
            server.createContext("/notify", exchange -> {
                String query = exchange.getRequestURI().getQuery();
                Map<String, String> queryParams = MainUtils.queryToMap(query);
                if (!queryParams.containsKey("user_id") || !queryParams.containsKey("pass_time") || !queryParams.containsKey("pass_type")) {
                    exchange.sendResponseHeaders(HttpStatus.SC_BAD_REQUEST, "Bad request".getBytes().length);
                    OutputStream outputStream = exchange.getResponseBody();
                    outputStream.flush();
                    outputStream.close();
                    return;
                }
                Integer id = Integer.valueOf(queryParams.get("user_id"));
                Long passTime = Long.parseLong(queryParams.get("pass_time"));
                PassType type = PassType.valueOf(queryParams.get("pass_type").toUpperCase());
                statsManager.addVisit(id, passTime, type);
                exchange.sendResponseHeaders(HttpStatus.SC_OK, 0);
                OutputStream outputStream = exchange.getResponseBody();
                outputStream.flush();
                outputStream.close();
            });
            server.start();
        } catch (ParseException | NumberFormatException e) {
            System.out.println(e.getMessage());
            BaseServerArgs.printUsage("stats-server");
            System.exit(1);
        } catch (IOException e) {
            System.out.println(e.getMessage());
            System.exit(2);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            System.exit(3);
        }
    }
}
