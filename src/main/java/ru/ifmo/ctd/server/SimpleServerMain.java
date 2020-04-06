package ru.ifmo.ctd.server;

import com.sun.net.httpserver.HttpServer;
import org.apache.commons.cli.*;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Arrays;

public class SimpleServerMain {
        public static void main(String[] args) {
        Options options = new Options();

        Option hostOpt = new Option("h", "host", true, "server hostname");
        hostOpt.setRequired(true);
        options.addOption(hostOpt);

        Option portOpt = new Option("p", "port", true, "server port to listen on");
        portOpt.setRequired(true);
        options.addOption(portOpt);

        Option enginesOpt = new Option("e", "engines", true, "Comma-separated list of search engines");
        enginesOpt.setArgs(5);
        enginesOpt.setValueSeparator(',');
        options.addOption(enginesOpt);

        try {
            CommandLineParser parser = new DefaultParser();
            CommandLine cmd = parser.parse(options, args);

            String hostname = cmd.getOptionValue("host");
            int port = Integer.parseInt(cmd.getOptionValue("port"));
            String[] engines = cmd.getOptionValues("engines");

            HttpServer server = HttpServer.create(new InetSocketAddress(hostname, port), 0);

            Arrays.stream(engines)
                    .forEach(engine -> server.createContext("/" + engine, new SearchQueryHandler(engine)));

            server.start();
        } catch (ParseException | NumberFormatException e) {
            System.out.println(e.getMessage());
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("java -jar server.jar", options);
            System.exit(1);
        } catch (IOException e) {
            System.out.println(e.getMessage());
            System.exit(2);
        }
    }
}
