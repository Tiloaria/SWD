package ru.ifmo.ctd.turnstile;

import org.junit.BeforeClass;
import org.junit.Test;
import ru.ifmo.ctd.helpers.Command;
import ru.ifmo.ctd.helpers.Handler;
import ru.ifmo.ctd.model.PassType;
import ru.ifmo.ctd.storage.InMemoryStorage;
import ru.ifmo.ctd.storage.Storage;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

public class TurnstileCommandHandlerTest {
    private static final Storage storage = new InMemoryStorage();
    private static final StatisticsNotifier mockNotifier = mock(StatisticsNotifier.class);
    private static final Handler<Command> handler = new TurnstileCommandHandler(storage, mockNotifier);
    private static int testUserId;
    private static final long currentTime = System.currentTimeMillis();
    private static final long validExpirationTs = currentTime + TimeUnit.DAYS.toMillis(42);

    @BeforeClass
    public static void beforeClass() throws Exception {
        doNothing().when(mockNotifier).sendNotification(anyInt(), anyLong(), any(PassType.class));
        testUserId = storage.createNewUser("John Doe");
        storage.renewSubscription(testUserId, validExpirationTs);
    }

    @Test
    public void testTurnstilePassesUserWithValidMembership() {
        EnterCommand command = new EnterCommand(testUserId, currentTime);
        String result = handler.handle(command);
        assertEquals("Enter event", result);
    }

    @Test
    public void testTurnstileRejectsUserWithExpiredMembership() throws IOException {
        long passTime = System.currentTimeMillis() + TimeUnit.DAYS.toMillis(43);
        EnterCommand command = new EnterCommand(testUserId, passTime);
        String result = handler.handle(command);
        assertEquals("Attempt to pass with expired membership!", result);
        verify(mockNotifier, times(0)).sendNotification(eq(testUserId), eq(passTime), any(PassType.class));
    }

    @Test
    public void testTurnstileRejectsUserWithUnknownId() throws IOException {
        long currentTime = System.currentTimeMillis();
        EnterCommand command = new EnterCommand(2, currentTime);
        String result = handler.handle(command);
        assertEquals("No user found", result);
        verify(mockNotifier, times(0)).sendNotification(eq(2), eq(currentTime), any(PassType.class));
    }
}