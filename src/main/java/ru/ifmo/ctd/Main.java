package ru.ifmo.ctd;

import io.netty.handler.codec.http.HttpResponseStatus;
import io.reactivex.netty.protocol.http.server.HttpServer;
import org.apache.commons.cli.*;
import ru.ifmo.ctd.storage.ReactiveMongoDriver;

import java.util.List;
import java.util.Map;

public class Main {
    public static void main(String[] args) {
        Options options = new Options();
        Option serverPortOpt = new Option("sp", "server-port", true, "Receive server port");
        Option databasePortOpt = new Option("dbp", "db-port", true, "Receive database port");
        Option databaseHostOpt = new Option("dbh", "db-host", true, "Receive database host");
        Option databaseNameOpt = new Option("n", "db-name", true, "Receive database name");

        serverPortOpt.setRequired(false);
        databasePortOpt.setRequired(false);
        databaseHostOpt.setRequired(false);
        databaseNameOpt.setRequired(false);
        options.addOption(serverPortOpt);
        options.addOption(databasePortOpt);
        options.addOption(databaseHostOpt);
        options.addOption(databaseNameOpt);

        try {
            CommandLineParser parser = new DefaultParser();
            CommandLine cmd = parser.parse(options, args);

            int serverPort = Integer.parseInt(cmd.getOptionValue("server-port", "8081"));
            int dbPort = Integer.parseInt(cmd.getOptionValue("db-port", "27017"));
            String dbHost = cmd.getOptionValue("db-host", "localhost");
            String dbName = cmd.getOptionValue("db-name", "reactive");

            ReactiveMongoDriver driver = new ReactiveMongoDriver(dbPort, dbHost, dbName);
            HttpServer
                    .newServer(serverPort)
                    .start((req, resp) -> {
                        System.out.println(req.getDecodedPath());
                        try {
                            if (req.getDecodedPath().equals("/signin")) {
                                Map<String, List<String>> parameters = req.getQueryParameters();
                                List<String> currency = parameters.get("currency");
                                List<String> login = parameters.get("login");
                                if (currency.size() != 1 || login.size() != 1) {
                                    return resp.setStatus(HttpResponseStatus.BAD_REQUEST);
                                }
                                return resp.writeString(driver.addUser(currency.get(0), login.get(0)));
                            }
                            if (req.getDecodedPath().equals("/items")) {
                                List<String> id = req.getQueryParameters().get("id");
                                if (id.size() != 1) {
                                    return resp.setStatus(HttpResponseStatus.BAD_REQUEST);
                                }
                                return resp.writeString(driver.getItems(id.get(0)));
                            }
                            if (req.getDecodedPath().equals("/add")) {
                                Map<String, List<String>> parameters = req.getQueryParameters();
                                List<String> name = parameters.get("name");
                                List<String> price = parameters.get("price");
                                if (name.size() != 1 || price.size() != 1) {
                                    return resp.setStatus(HttpResponseStatus.BAD_REQUEST);
                                }
                                return resp.writeString(driver.addItem(name.get(0), Double.valueOf(price.get(0))));
                            }
                            return resp.setStatus(HttpResponseStatus.BAD_REQUEST);
                        } catch (Exception e) {
                            System.err.println(e.getMessage());
                            return resp.setStatus(HttpResponseStatus.INTERNAL_SERVER_ERROR);
                        }
                    })
                    .awaitShutdown();
        } catch (ParseException | NumberFormatException e) {
            System.out.println(e.getMessage());
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("java -jar Reactive.jar", options);
            System.exit(1);
        }
    }
}
