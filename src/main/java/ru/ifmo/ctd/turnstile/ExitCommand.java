package ru.ifmo.ctd.turnstile;

import ru.ifmo.ctd.helpers.Command;

public class ExitCommand extends Command {
    public final Integer userId;
    public final long exitTime;

    public ExitCommand(Integer id, long time) {
        userId = id;
        exitTime = time;
    }
}