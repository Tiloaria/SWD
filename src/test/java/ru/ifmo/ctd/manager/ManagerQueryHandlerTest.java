package ru.ifmo.ctd.manager;

import org.junit.BeforeClass;
import org.junit.Test;
import ru.ifmo.ctd.helpers.Handler;
import ru.ifmo.ctd.helpers.Query;
import ru.ifmo.ctd.model.User;
import ru.ifmo.ctd.storage.InMemoryStorage;
import ru.ifmo.ctd.storage.Storage;

import static org.junit.Assert.*;

public class ManagerQueryHandlerTest {
    private static final Storage storage = new InMemoryStorage();
    private static final Handler<Query> handler = new ManagerQueryHandler(storage);
    private static int testUserId;

    @BeforeClass
    public static void beforeClass() throws Exception {
        testUserId = storage.createNewUser("John Doe");
    }

    @Test
    public void testGetInfoForExistingUser() {
        GetInfoQuery query = new GetInfoQuery(testUserId);
        String result = handler.handle(query);
        User testUser = new User(testUserId, "John Doe", 0L);
        assertEquals(testUser.toString(), result);
    }

    @Test
    public void testGetInfoForNonExistingUser() {
        int nonExistingUserId = 2;
        GetInfoQuery query = new GetInfoQuery(nonExistingUserId);
        String result = handler.handle(query);
        assertEquals("No user found", result);
    }
}