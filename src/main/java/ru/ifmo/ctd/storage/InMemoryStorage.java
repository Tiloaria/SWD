package ru.ifmo.ctd.storage;

import ru.ifmo.ctd.model.PassType;
import ru.ifmo.ctd.model.User;

import java.sql.SQLException;
import java.util.*;

public class InMemoryStorage implements Storage {
    private final Map<Integer, User> users = new HashMap<>();
    private final Map<Integer, List<Long>> enters = new HashMap<>();
    private final Map<Integer, List<Long>> exits = new HashMap<>();
    private static int USERS_COUNT = 0;

    @Override
    public Integer createNewUser(String name) {
        int id = USERS_COUNT++;
        User user = new User(id, name, 0L);
        users.put(id, user);
        return id;
    }

    @Override
    public void renewSubscription(Integer id, long endTime) throws SQLException {
        if (users.containsKey(id)) {
            users.get(id).membershipExpirationTs = endTime;
            return;
        }
        throw new SQLException("No user found");
    }

    @Override
    public void addPass(Integer id, long time, PassType type) {
        switch (type) {
            case Enter:
                enters.merge(id, new ArrayList<>(Arrays.asList(time)), (old, ignored) -> {
                    old.add(time);
                    return old;
                });
                break;
            case Exit:
                exits.merge(id, new ArrayList<>(Arrays.asList(time)), (old, ignored) -> {
                    old.add(time);
                    return old;
                });
                break;
        }
    }

    @Override
    public User getUserInfoById(Integer id) throws SQLException {
        if (users.containsKey(id)) {
            return users.get(id);
        }
        throw new SQLException("No user found");
    }

    @Override
    public Map<Integer, List<Long>> getVisits(PassType type) {
        switch (type) {
            case Enter:
                return enters;
            case Exit:
                return exits;
        }
        return null;
    }

    @Override
    public void close() {

    }
}
