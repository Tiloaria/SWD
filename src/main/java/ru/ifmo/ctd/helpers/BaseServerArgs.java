package ru.ifmo.ctd.helpers;

import org.apache.commons.cli.*;

public class BaseServerArgs {
    protected static Options options;
    protected static CommandLine cmd;

    public static void init() {
        options = new Options();
        Option hostOpt = new Option("h", "host", true, "server hostname");
        hostOpt.setRequired(false);
        options.addOption(hostOpt);

        Option portOpt = new Option("p", "port", true, "server port to listen on");
        portOpt.setRequired(false);
        options.addOption(portOpt);

        Option dbHostOpt = new Option("dbh", "db-host", true, "database hostname");
        dbHostOpt.setRequired(false);
        options.addOption(dbHostOpt);

        Option dbPortOpt = new Option("dbp", "db-port", true, "database port number");
        dbPortOpt.setRequired(false);
        options.addOption(dbPortOpt);
    }

    public static void parseArgs(String[] args) throws ParseException {
        CommandLineParser parser = new DefaultParser();
        cmd = parser.parse(options, args);
    }

    public static String getHostname() {
        return cmd.getOptionValue("host", "localhost");
    }

    public static int getPortNumber() {
        return Integer.parseInt(cmd.getOptionValue("port", "8080"));
    }

    public static String getDbHostname() {
        return cmd.getOptionValue("db-host", "localhost");
    }

    public static int getDbPortNumber() {
        return Integer.parseInt(cmd.getOptionValue("db-port", "5432"));
    }

    public static void printUsage(String name) {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp(String.format("java -jar %s.jar", name), options);
    }
}
