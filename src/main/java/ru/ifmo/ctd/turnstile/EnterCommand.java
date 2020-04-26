package ru.ifmo.ctd.turnstile;

import ru.ifmo.ctd.helpers.Command;

public class EnterCommand extends Command {
    public final Integer userId;
    public final long enterTime;

    EnterCommand(Integer id, long time) {
        userId = id;
        enterTime = time;
    }
}
