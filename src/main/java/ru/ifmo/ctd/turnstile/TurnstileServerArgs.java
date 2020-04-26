package ru.ifmo.ctd.turnstile;


import org.apache.commons.cli.Option;
import ru.ifmo.ctd.helpers.BaseServerArgs;

public class TurnstileServerArgs extends BaseServerArgs {
    public static void init() {
        BaseServerArgs.init();
        Option statServerHostOpt = new Option("sh", "stats-host", true, "statistics server hostname");
        statServerHostOpt.setRequired(false);
        options.addOption(statServerHostOpt);

        Option statServerPortOpt = new Option("sp", "stats-port", true, "statistics server port number");
        statServerPortOpt.setRequired(false);
        options.addOption(statServerPortOpt);

        Option statServerNotificationEndpointOpt = new Option("se", "stats-endpoint", true, "statistics server notification endpoint");
        statServerNotificationEndpointOpt.setRequired(false);
        options.addOption(statServerNotificationEndpointOpt);
    }

    public static String getStatsServerHostname() {
        return cmd.getOptionValue("stats-host", "localhost");
    }

    public static int getStatsServerPortNumber() {
        return Integer.parseInt(cmd.getOptionValue("stats-port", "8080"));
    }

    public static String getStatsServerNotificationEndpoint() {
        return cmd.getOptionValue("stats-endpoint", "/notify");
    }
}
