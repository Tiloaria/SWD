package ru.ifmo.ctd.model;

import java.sql.Timestamp;

public class User {
    public final int id;
    public final String name;
    public long membershipExpirationTs;

    public User(int id, String name, long membershipExpirationTs) {
        this.id = id;
        this.name = name;
        this.membershipExpirationTs = membershipExpirationTs;
    }

    @Override
    public String toString() {
        return String.format("User with name: %s, has membership expiring at: %s", name, new Timestamp(membershipExpirationTs).toString());
    }
}
