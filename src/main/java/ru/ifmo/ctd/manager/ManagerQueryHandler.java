package ru.ifmo.ctd.manager;

import ru.ifmo.ctd.helpers.Handler;
import ru.ifmo.ctd.helpers.Query;
import ru.ifmo.ctd.storage.Storage;

import java.sql.SQLException;


public class ManagerQueryHandler implements Handler<Query> {
    private final Storage myStorage;

    public ManagerQueryHandler(Storage storage) {
        myStorage = storage;
    }

    @Override
    public String handle(Query query) {
        try {
            if (query instanceof GetInfoQuery) {
                return myStorage.getUserInfoById(((GetInfoQuery) query).userId).toString();
            } else {
                return "Unknown query";
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
