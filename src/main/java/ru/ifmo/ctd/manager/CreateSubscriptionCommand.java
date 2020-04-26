package ru.ifmo.ctd.manager;

import ru.ifmo.ctd.helpers.Command;

public class CreateSubscriptionCommand extends Command {
    public final String userName;


    public CreateSubscriptionCommand(String name) {
        userName = name;
    }
}
