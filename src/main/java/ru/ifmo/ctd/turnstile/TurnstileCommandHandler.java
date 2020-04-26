package ru.ifmo.ctd.turnstile;

import ru.ifmo.ctd.helpers.Command;
import ru.ifmo.ctd.helpers.Handler;
import ru.ifmo.ctd.storage.Storage;
import ru.ifmo.ctd.model.PassType;
import ru.ifmo.ctd.model.User;

import java.io.IOException;
import java.sql.SQLException;

public class TurnstileCommandHandler implements Handler<Command> {
    private final Storage myStorage;
    private final StatisticsNotifier myNotifier;

    public TurnstileCommandHandler(Storage storage, StatisticsNotifier notifier) {
        myStorage = storage;
        myNotifier = notifier;
    }

    public String handle(Command command) {
        try {
            if (command instanceof EnterCommand) {
                User user = myStorage.getUserInfoById(((EnterCommand) command).userId);
                if (user.membershipExpirationTs < ((EnterCommand) command).enterTime) {
                    return "Attempt to pass with expired membership!";
                }
                myStorage.addPass(((EnterCommand) command).userId, ((EnterCommand) command).enterTime, PassType.Enter);
                myNotifier.sendNotification(((EnterCommand) command).userId, ((EnterCommand) command).enterTime, PassType.Enter);
                return "Enter event";
            } else if (command instanceof ExitCommand) {
                myStorage.addPass(((ExitCommand) command).userId, ((ExitCommand) command).exitTime, PassType.Exit);
                myNotifier.sendNotification(((ExitCommand) command).userId, ((ExitCommand) command).exitTime, PassType.Exit);
                return "Exit event";
            } else {
                return "Unknown command";
            }
        } catch (SQLException e) {
            String msg = e.getMessage();
            if (msg == null || msg.isEmpty()) {
                return "Datastore error";
            }
            return msg;
        } catch (IOException e) {
            return "Network error while sending statistics";
        }
    }
}
