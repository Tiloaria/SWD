package ru.ifmo.ctd.storage;

import ru.ifmo.ctd.model.PassType;
import ru.ifmo.ctd.model.User;

import java.sql.*;
import java.util.*;

public class PostgresStorage implements Storage {
    private Connection connection;

    public PostgresStorage(String host, int port) throws SQLException {
        Properties props = new Properties();
        props.setProperty("user", "postgres");
        String url = String.format("jdbc:postgresql://%s:%d/", host, port);
        connection = DriverManager.getConnection(url, props);
    }


    @Override
    public Integer createNewUser(String name) throws SQLException {
        String sql = "insert into users (name) values (?)";
        String[] columns = {"user_id"};
        PreparedStatement statement = connection.prepareStatement(sql, columns);
        statement.setString(1, name);
        int affectedRows = statement.executeUpdate();
        if (affectedRows == 0) {
            throw new SQLException("Creating user failed, no rows affected");
        }

        try (ResultSet resultSet = statement.getGeneratedKeys()) {
            if (resultSet.next()) {
                return resultSet.getInt(1);
            } else {
                throw new SQLException("Creating user failed, no id obtained");
            }
        }
    }

    @Override
    public void renewSubscription(Integer id, long valid_until) throws SQLException {
        String sql = "insert into renews (user_id, valid_until) values (?, ?)";
        PreparedStatement statement = connection.prepareStatement(sql);
        statement.setInt(1, id);
        statement.setTimestamp(2, new Timestamp(valid_until));
        int affectedRows = statement.executeUpdate();
        if (affectedRows == 0) {
            throw new SQLException("Creating user failed, no rows affected");
        }
    }

    @Override
    public void addPass(Integer id, long time, PassType type) throws SQLException {
        String sql = "insert into passes (user_id, pass_time, pass_type) values (?, ?, ?::pass_type)";
        PreparedStatement statement = connection.prepareStatement(sql);
        statement.setInt(1, id);
        statement.setTimestamp(2, new Timestamp(time));
        statement.setString(3, type.name().toLowerCase());
        int affectedRows = statement.executeUpdate();
        if (affectedRows == 0) {
            throw new SQLException("Adding pass failed, no rows affected");
        }
    }

    @Override
    public User getUserInfoById(Integer id) throws SQLException {
        String sql = "select name from users where user_id=?";
        PreparedStatement statement = connection.prepareStatement(sql);
        statement.setInt(1, id);
        ResultSet result = statement.executeQuery();
        if (!result.next()) {
            throw new SQLException("Incorrect user id");
        }
        String name = result.getString("name");

        sql = "select valid_until from renews where user_id=?";
        PreparedStatement statementRenews = connection.prepareStatement(sql);
        statementRenews.setInt(1, id);
        ResultSet resultRenews = statementRenews.executeQuery();
        Timestamp last = new Timestamp(0);
        while (resultRenews.next()) {
            Timestamp validUntil = resultRenews.getTimestamp("valid_until");
            if (last.before(validUntil)) {
                last = validUntil;
            }
        }
//        if (last.equals(new Timestamp(0))) {
//            return null; //String.format("Client name: %s\nClient still didn't buy any membership", name);
//        }
//        if (last.before(new Timestamp(System.currentTimeMillis()))) {
//            return null; //String.format("Client name:%s\nClient membership is not valid", name);
//        }
        return new User(id, name, last.getTime());
    }

    @Override
    public Map<Integer, List<Long>> getVisits(PassType type) throws SQLException {
        String sql = "select user_id, pass_time from passes where pass_type=?::pass_type";
        PreparedStatement statementPasses = connection.prepareStatement(sql);
        statementPasses.setString(1, type.name().toLowerCase());
        ResultSet resultPasses = statementPasses.executeQuery();
        Map<Integer, List<Long>> visits = new HashMap<>();
        while (resultPasses.next()) {
            Integer userId = resultPasses.getInt("user_id");
            Long passTime = resultPasses.getTimestamp("pass_time").getTime();
            visits.merge(userId, new ArrayList<>(Arrays.asList(passTime)), (old, ignored) -> {
                old.add(passTime);
                return old;
            });
        }
        return visits;
    }

    public void close() throws SQLException {
        if (connection != null) {
            connection.close();
            connection = null;
        }
    }
}
