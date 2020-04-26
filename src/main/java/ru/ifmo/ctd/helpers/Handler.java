package ru.ifmo.ctd.helpers;

public interface Handler<T> {
    String handle(T t);
}
