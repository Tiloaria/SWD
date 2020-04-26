package ru.ifmo.ctd.manager;

import ru.ifmo.ctd.helpers.Query;

public class GetInfoQuery extends Query {
    public final Integer userId;

    public GetInfoQuery(Integer id) {
        userId = id;
    }
}
