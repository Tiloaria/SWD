package ru.ifmo.ctd.manager;

import ru.ifmo.ctd.helpers.Command;

public class RenewSubscriptionCommand extends Command {
    public final Integer userId;
    public final long newExpirationTs;

    public RenewSubscriptionCommand(Integer id, long expirationTs) {
        userId = id;
        newExpirationTs = expirationTs;
    }
}
