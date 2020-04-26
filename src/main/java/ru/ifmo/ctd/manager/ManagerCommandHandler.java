package ru.ifmo.ctd.manager;

import ru.ifmo.ctd.helpers.Command;
import ru.ifmo.ctd.helpers.Handler;
import ru.ifmo.ctd.storage.Storage;

import java.sql.SQLException;

public class ManagerCommandHandler implements Handler<Command> {
    private final Storage myStorage;

    public ManagerCommandHandler(Storage storage) {
        myStorage = storage;
    }

    @Override
    public String handle(Command command) {
        try {
            if (command instanceof CreateSubscriptionCommand) {
                return String.format("user id = %d",
                        myStorage.createNewUser(((CreateSubscriptionCommand) command).userName));
            } else if (command instanceof RenewSubscriptionCommand) {
                myStorage.renewSubscription(((RenewSubscriptionCommand) command).userId,
                        ((RenewSubscriptionCommand) command).newExpirationTs);
                return "Successfully renewed";
            } else {
                return "Unknown command";
            }
        } catch (SQLException e) {
            String msg = e.getMessage();
            if (msg == null || msg.isEmpty()) {
                return "Datastore error";
            }
            return msg;
        }
    }
}
