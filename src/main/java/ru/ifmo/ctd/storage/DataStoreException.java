package ru.ifmo.ctd.storage;

class DataStoreException extends Exception {
    DataStoreException(String msg) {
        super(msg);
    }

    DataStoreException() {
        super();
    }
}
