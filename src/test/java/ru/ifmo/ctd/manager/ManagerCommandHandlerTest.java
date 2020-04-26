package ru.ifmo.ctd.manager;

import org.junit.BeforeClass;
import org.junit.Test;
import ru.ifmo.ctd.helpers.Command;
import ru.ifmo.ctd.helpers.Handler;
import ru.ifmo.ctd.storage.InMemoryStorage;
import ru.ifmo.ctd.storage.Storage;

import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;


public class ManagerCommandHandlerTest {
    private static final Storage storage = new InMemoryStorage();
    private static final Handler<Command> handler = new ManagerCommandHandler(storage);
    private static int testUserId;
    private static final long currentTime = System.currentTimeMillis();
    private static final long validExpirationTs = currentTime + TimeUnit.DAYS.toMillis(42);

    @BeforeClass
    public static void beforeClass() throws Exception {
        testUserId = storage.createNewUser("John Doe");
    }

    @Test
    public void testRenewSubscriptionForExistingUser() {
        RenewSubscriptionCommand command = new RenewSubscriptionCommand(testUserId, validExpirationTs);
        String result = handler.handle(command);
        assertEquals("Successfully renewed", result);
    }

    @Test
    public void testRenewSubscriptionForNonExistingUser() {
        int nonExistingUserId = 2;
        RenewSubscriptionCommand command = new RenewSubscriptionCommand(nonExistingUserId, validExpirationTs);
        String result = handler.handle(command);
        assertEquals("No user found", result);
    }
}