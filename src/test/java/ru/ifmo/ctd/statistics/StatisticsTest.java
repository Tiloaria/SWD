package ru.ifmo.ctd.statistics;

import org.junit.Test;
import ru.ifmo.ctd.model.PassType;
import ru.ifmo.ctd.storage.InMemoryStorage;
import ru.ifmo.ctd.storage.Storage;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;

public class StatisticsTest {
    @Test
    public void testSimpleStats() throws Exception {
        Storage storage = new InMemoryStorage();
        int testUserId = storage.createNewUser("John Doe");
        storage.renewSubscription(testUserId, System.currentTimeMillis() + TimeUnit.DAYS.toMillis(42));

        Date today = new Date();
        long enterTimeToday = today.getTime() + TimeUnit.HOURS.toMillis(12);
        long exitTimeToday = today.getTime() + TimeUnit.HOURS.toMillis(14);
        storage.addPass(testUserId, enterTimeToday, PassType.Enter);
        storage.addPass(testUserId, exitTimeToday, PassType.Exit);

        Date tomorrow = new Date(today.getTime() + TimeUnit.DAYS.toMillis(1));
        long enterTimeTomorrow = tomorrow.getTime() + TimeUnit.HOURS.toMillis(12);
        long exitTimeTomorrow = tomorrow.getTime() + TimeUnit.HOURS.toMillis(14);
        storage.addPass(testUserId, enterTimeTomorrow, PassType.Enter);
        storage.addPass(testUserId, exitTimeTomorrow, PassType.Exit);

        Date afterTomorrow = new Date(tomorrow.getTime() + TimeUnit.DAYS.toMillis(1));
        long enterTimeAfterTomorrow = afterTomorrow.getTime() + TimeUnit.HOURS.toMillis(12);
        long exitTimeAfterTomorrow = afterTomorrow.getTime() + TimeUnit.HOURS.toMillis(14);
        storage.addPass(testUserId, enterTimeAfterTomorrow, PassType.Enter);
        storage.addPass(testUserId, exitTimeAfterTomorrow, PassType.Exit);

        InMemoryStatisticsManager statisticsManager = new InMemoryStatisticsManager(storage);
        double averageFrequencyVisits = statisticsManager.averageFrequencyVisits();
        double averageLengthVisits = statisticsManager.averageLengthVisits();

        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Map<String, Integer> expectedNumOfVisits = new HashMap<>();
        expectedNumOfVisits.put(dateFormat.format(today), 1);
        expectedNumOfVisits.put(dateFormat.format(tomorrow), 1);
        expectedNumOfVisits.put(dateFormat.format(afterTomorrow), 1);
        assertEquals(TimeUnit.HOURS.toMillis(2), averageLengthVisits, TimeUnit.SECONDS.toMillis(1));
        assertEquals(3.0, averageFrequencyVisits, 0.0);
        assertEquals(expectedNumOfVisits, statisticsManager.numOfVisitsStats());
    }
}
