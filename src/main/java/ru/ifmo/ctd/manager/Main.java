package ru.ifmo.ctd.manager;

import com.sun.net.httpserver.HttpServer;
import org.apache.commons.cli.*;
import org.apache.http.HttpStatus;
import ru.ifmo.ctd.helpers.Command;
import ru.ifmo.ctd.helpers.Handler;
import ru.ifmo.ctd.storage.PostgresStorage;
import ru.ifmo.ctd.helpers.Query;
import ru.ifmo.ctd.helpers.BaseServerArgs;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.sql.SQLException;
import java.util.Map;

import static ru.ifmo.ctd.helpers.MainUtils.queryToMap;

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
            Handler<Command> managerCommandHandler = new ManagerCommandHandler(postgresStorage);
            Handler<Query> managerQueryHandler = new ManagerQueryHandler(postgresStorage);

            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                try {
                    server.stop(0);
                    postgresStorage.close();
                } catch (SQLException e) {
                    System.err.println("Fatal datastore error on shutdown!");
                    System.err.println(e.getMessage());
                }
            }));

            server.createContext("/get_info", exchange -> {
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
                GetInfoQuery getInfoQuery = new GetInfoQuery(id);
                String result = managerQueryHandler.handle(getInfoQuery);
                exchange.sendResponseHeaders(HttpStatus.SC_OK, result.getBytes().length);
                OutputStream outputStream = exchange.getResponseBody();
                outputStream.write(result.getBytes());
                outputStream.flush();
                outputStream.close();
            });
            server.createContext("/create_subscription", exchange -> {
                String query = exchange.getRequestURI().getQuery();
                Map<String, String> queryParams = queryToMap(query);
                if (!queryParams.containsKey("name")) {
                    exchange.sendResponseHeaders(HttpStatus.SC_BAD_REQUEST, "Bad request".getBytes().length);
                    OutputStream outputStream = exchange.getResponseBody();
                    outputStream.flush();
                    outputStream.close();
                    return;
                }
                String name = queryParams.get("name");
                CreateSubscriptionCommand command = new CreateSubscriptionCommand(name);
                String result = managerCommandHandler.handle(command);
                exchange.sendResponseHeaders(HttpStatus.SC_OK, result.getBytes().length);
                OutputStream outputStream = exchange.getResponseBody();
                outputStream.write(result.getBytes());
                outputStream.flush();
                outputStream.close();
            });
            server.createContext("/renew_subscription", exchange -> {
                String query = exchange.getRequestURI().getQuery();
                Map<String, String> queryParams = queryToMap(query);
                if (!queryParams.containsKey("id") || !queryParams.containsKey("expirationTs")) {
                    exchange.sendResponseHeaders(HttpStatus.SC_BAD_REQUEST, "Bad request".getBytes().length);
                    OutputStream outputStream = exchange.getResponseBody();
                    outputStream.flush();
                    outputStream.close();
                    return;
                }
                Integer id = Integer.valueOf(queryParams.get("id"));
                long expirationTs = Long.parseLong(queryParams.get("expirationTs"));
                RenewSubscriptionCommand command = new RenewSubscriptionCommand(id, expirationTs);
                String result = managerCommandHandler.handle(command);
                exchange.sendResponseHeaders(HttpStatus.SC_OK, result.getBytes().length);
                OutputStream outputStream = exchange.getResponseBody();
                outputStream.write(result.getBytes());
                outputStream.flush();
                outputStream.close();
            });
            server.start();
        } catch (ParseException | NumberFormatException e) {
            System.out.println(e.getMessage());
            BaseServerArgs.printUsage("manager-server");
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
