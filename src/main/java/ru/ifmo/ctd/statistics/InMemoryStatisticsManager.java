package ru.ifmo.ctd.statistics;

import ru.ifmo.ctd.storage.Storage;
import ru.ifmo.ctd.model.PassType;

import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class InMemoryStatisticsManager {
    private final Map<Integer, List<Long>> userEnters;
    private final Map<Integer, List<Long>> userExits;
    // 2020-01-14
    private final Map<String, Integer> numOfVisits = new HashMap<>();


    public InMemoryStatisticsManager(Storage storage) throws SQLException {
        userEnters = storage.getVisits(PassType.Enter);
        userExits = storage.getVisits(PassType.Exit);
        for (List<Long> visits : userEnters.values()) {
            for (Long visit : visits) {
                String dayDate = getDay(visit);
                numOfVisits.merge(dayDate, 1, (old, ignored) -> ++old);
            }
        }
    }

    public void addVisit(Integer userId, Long passTime, PassType type) {
        switch (type) {
            case Enter: {
                userEnters.merge(userId, new ArrayList<>(Arrays.asList(passTime)), (old, ignore) -> {
                    old.add(passTime);
                    return old;
                });
                numOfVisits.merge(getDay(passTime), 1, (old, ignored) -> ++old);
                break;
            }
            case Exit: {
                userExits.merge(userId, new ArrayList<>(Arrays.asList(passTime)), (old, ignore) -> {
                    old.add(passTime);
                    return old;
                });
                break;
            }
        }
    }

    public Map<String, Integer> numOfVisitsStats() {
        return numOfVisits;
    }

    public Double averageFrequencyVisits() {
        if (userEnters.size() == 0) {
            return null;
        }
        return numOfVisits.values().stream().mapToInt(Integer::intValue).sum() * 1.0 / userEnters.size();
    }

    /**
     * Ignore if difference in enters and exits is more than 1, ignore negative time and visits longer than 24 hours
     *
     * @return average length of visits by all users
     */
    public Double averageLengthVisits() {
        final Long limit24 = TimeUnit.HOURS.toMillis(24);
        long sumLength = 0;
        long visits = 0;
        for (Map.Entry<Integer, List<Long>> userVisits : userEnters.entrySet()) {
            Integer id = userVisits.getKey();
            List<Long> enters = userVisits.getValue();
            if (userExits.containsKey(id)) {
                List<Long> exits = userExits.get(id);
                if (enters.size() == exits.size() || enters.size() + 1 == exits.size()) {
                    for (int i = 0; i < exits.size(); i++) {
                        long length = exits.get(i) - enters.get(i);
                        if (length < limit24 && length > 0) {
                            sumLength += length;
                            visits++;
                        }
                    }
                }
            }
        }
        if (visits == 0) {
            return null;
        }
        return sumLength * 1.0 / visits;
    }

    private String getDay(Long visit) {
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Date date = new Date(visit);
        return dateFormat.format(date);
    }
}
