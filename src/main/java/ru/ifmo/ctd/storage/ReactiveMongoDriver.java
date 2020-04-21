package ru.ifmo.ctd.storage;

import com.mongodb.client.model.Filters;
import com.mongodb.rx.client.MongoClient;
import com.mongodb.rx.client.MongoClients;
import ru.ifmo.ctd.model.Currency;
import ru.ifmo.ctd.model.Item;
import ru.ifmo.ctd.model.User;
import rx.Observable;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Random;

import static com.mongodb.client.model.Projections.fields;
import static com.mongodb.client.model.Projections.include;

public class ReactiveMongoDriver {
    private final MongoClient client;
    private final String dbName;

    private int MAX_USER_ID = 10;
    private int MAX_ITEM_ID = 10;
    private Map<Integer, User> userById = new HashMap<>();
    private Set<Integer> userIds = new HashSet<>();
    private Set<Integer> itemIds = new HashSet<>();

    public ReactiveMongoDriver(int port, String host, String name) {
        dbName = name;
        client = createMongoClient(port, host);   
    }


    public Observable<String> addUser(String currencyStr, String login) throws DataStoreException {
        try {
            Currency currency = Currency.valueOf(currencyStr);
            int userId = getNewId(MAX_USER_ID, userIds);
            User user = new User(userId, currency, login);
            userById.put(userId, user);
            return client.getDatabase(dbName).getCollection("user").insertOne(
                    user.toDocument()
            ).map(success -> String.format("Signin successful id = %d", userId));
        }
        catch (IllegalArgumentException exception) {
            throw new DataStoreException("Fail to add new user");
        }
    }

    public Observable<String> addItem(String name, Double price) throws DataStoreException {
        try {
            Item item = new Item(getNewId(MAX_ITEM_ID, itemIds), name, price);
            return client.getDatabase(dbName).getCollection("item").insertOne(
                    item.toDocument()).map(success -> "Added successfuly");
        }
        catch (IllegalArgumentException exception) {
            throw new DataStoreException("Fail to add new user");
        }
    }

    public Observable<String> getItems(String idStr) throws DataStoreException {
        Integer id = Integer.parseInt(idStr);
        if (!userById.containsKey(id)) {
            throw new DataStoreException();
        }

        try {
            return client.getDatabase(dbName)
                    .getCollection("user")
                    .find(Filters.eq("id", id)).projection(fields(include("currency")))
                    .toObservable()
                    .flatMap( currencyDoc -> {
                        Currency currency = Currency.valueOf(currencyDoc.getString("currency"));
                        return client.getDatabase(dbName)
                                .getCollection("item")
                                .find()
                                .toObservable()
                                .map(itemDoc -> Item.converter(itemDoc, currency).toString());
                    });
        }

        catch (IllegalArgumentException exception) {
            throw new DataStoreException("Failed to list items");
        }
    }

    private MongoClient createMongoClient(int port, String host) {
        MongoClient client = MongoClients.create(String.format("mongodb://%s:%d", host, port));
        client.getDatabase(dbName).getCollection("user").find().toObservable().subscribe(document -> {

            User user = new User(document);
            Integer id = user.getId();
            userIds.add(id);
            userById.put(id, user);
            MAX_USER_ID = Math.max(id, MAX_USER_ID);
        });
        client.getDatabase(dbName).getCollection("items").find().toObservable().subscribe(document -> {
            Item item = new Item(document);
            Integer id = item.getId();
            itemIds.add(id);
            MAX_ITEM_ID = Math.max(id, MAX_ITEM_ID);
        });
        return client;
    }

    private Integer getNewId(Integer MAX_SIZE, Set<Integer> idsSet) {
        if (idsSet.size() * 3 > MAX_SIZE) {
            MAX_SIZE = idsSet.size() * 3;
        }
        Random rand = new Random();
        //math expectation for creating id < 2
        int id = Math.abs(rand.nextInt()) % MAX_SIZE;
        while (idsSet.contains(id)) {
            id = Math.abs(rand.nextInt()) % MAX_SIZE;
        }
        idsSet.add(id);
        return id;
    }
}
