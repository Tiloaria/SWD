package ru.ifmo.ctd;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import org.apache.commons.cli.*;
import ru.ifmo.ctd.actors.Supervisor;

import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        ActorSystem system = ActorSystem.create("system");
        Options options = new Options();
        Option timeoutOpt = new Option("t", "timeout", true, "Receive timeout for master-actor");
        Option serverOpt = new Option("s", "server", true, "Receive server address(host and port) for server");
        timeoutOpt.setRequired(true);
        serverOpt.setRequired(false);
        options.addOption(timeoutOpt);
        options.addOption(serverOpt);

        try {
            CommandLineParser parser = new DefaultParser();
            CommandLine cmd = parser.parse(options, args);

            int timeout = Integer.parseInt(cmd.getOptionValue("timeout"));
            String[] serverValues = cmd.getOptionValues("server");
            if (serverValues != null && serverValues.length != 2 && serverValues.length != 0) {
                HelpFormatter formatter = new HelpFormatter();
                formatter.printHelp("option", options);
                System.exit(1);
            }
            final String hostName;
            final int port;
            if (serverValues != null && serverValues.length == 2) {
                hostName = serverValues[0];
                port = Integer.parseInt(serverValues[1]);//setconst
            }
            else {
                hostName = "localhost";
                port = 8081;
            }
            Scanner scanner = new Scanner(System.in);
            while (scanner.hasNext()) {
                String query = scanner.nextLine();
                ActorRef master = system.actorOf(
                        Props.create(Supervisor.class, () -> new Supervisor(result -> {
                            for (Map.Entry<String, List<String>> entry : result.entrySet()) {
                                String engine = entry.getKey();
                                System.out.println("Search engine: " + engine);
                                System.out.println("results: ");
                                entry.getValue().forEach(System.out::println);
                            }
                        }, timeout, hostName, port)),
                        "supervisor");
                master.tell(new Supervisor.SearchEngineRequest(query), ActorRef.noSender());
            }
        } catch (ParseException | NumberFormatException e) {
            System.out.println(e.getMessage());
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("java -jar actors-client.jar", options);
            System.exit(1);
        } finally {
            system.terminate();
        }
    }
}
