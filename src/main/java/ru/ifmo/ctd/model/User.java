package ru.ifmo.ctd.model;

import org.bson.Document;

import java.util.Map;

public class User {
    private final int myId;
    private final Currency myCurrency;
    private final String myLogin;

    public User(int id, Currency currency, String login) {
        myId = id;
        myCurrency = currency;
        myLogin = login;
    }

    public User(Document doc) {
        this(doc.getInteger("id"),
                Currency.valueOf(doc.getString("currency")),
                doc.getString("login"));
    }

    public Document toDocument() {
        return new Document(Map.of("id", myId, "currency", myCurrency.name(), "login", myLogin));
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + myId +
                ", currency='" + myCurrency.name() + '\'' +
                ", login='" + myLogin + '\'' +
                '}';
    }

    public Integer getId() {
        return myId;
    }
}
