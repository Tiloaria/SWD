package ru.ifmo.ctd.model;

import org.apache.commons.math3.util.Precision;
import org.bson.Document;

import java.util.Map;

public class Item {
    private final int myId;
    private final String myName;
    private final Double myPriceRub;

    public Item(int id, String name, Double priceRub) {
        myId = id;
        myName = name;
        myPriceRub = priceRub;
    }

    public Double getPriceRub() {
        return Precision.round(myPriceRub, 2);
    }

    public static Document converter(Document itemDoc, Currency currency) {
        Item item = new Item(itemDoc);
        return item.toDocument(currency);
    }

    @Override
    public String toString() {
        return "Item{" +
                "id=" + myId +
                ", name='" + myName + '\'' +
                ", price=" + getPriceRub() +
                '}';
    }

    public Document toDocument() {
        return new Document(Map.of("id", myId, "name", myName, "price", getPriceRub()));
    }

    public Item(Document doc) {
        this(doc.getInteger("id"),
                doc.getString("name"),
                doc.getDouble("price"));
    }

    public Integer getId() {
        return myId;
    }

    private Double converter(Currency currency) {
        switch (currency) {
            case RUB: {
                return Precision.round(myPriceRub, 2);
            }
            case EUR: {
                return Precision.round(myPriceRub / Currency.exchangeRateEURRUB, 2);
            }
            case USD: {
                return Precision.round(myPriceRub / Currency.exchangeRateUSDRUB, 2);
            }
            default:
                throw new IllegalArgumentException();
        }
    }

    private Document toDocument(Currency currency) {
        return new Document(Map.of("id", myId, "name", myName, "price", converter(currency)));
    }
}
