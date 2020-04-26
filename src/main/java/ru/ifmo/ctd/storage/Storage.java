package ru.ifmo.ctd.storage;

import ru.ifmo.ctd.model.PassType;
import ru.ifmo.ctd.model.User;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public interface Storage {
    Integer createNewUser(String name) throws SQLException;

    void renewSubscription(Integer id, long endTime) throws SQLException;

    void addPass(Integer id, long time, PassType type) throws SQLException;

    User getUserInfoById(Integer id) throws SQLException;

    Map<Integer, List<Long>> getVisits(PassType type) throws SQLException;

    void close() throws SQLException;
}
