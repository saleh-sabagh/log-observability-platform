package rule;

import model.LogEvent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
class SlidingWindowCounterTest {

    @Test
    void constructor_PositiveTimeWindow_CreatesCounter() {
        // Arrange
        long timeWindowSeconds = 60L;

        // Act
        SlidingWindowCounter counter = new SlidingWindowCounter(timeWindowSeconds);

        // Assert
        assertEquals(0, counter.count());
    }

    @ParameterizedTest
    @ValueSource(longs = {0L, -1L, -100L})
    void constructor_NonPositiveTimeWindow_ThrowsIllegalArgumentException(long invalidWindow) {
        // Arrange
        // invalidWindow provided by parameter source

        // Act
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new SlidingWindowCounter(invalidWindow)
        );

        // Assert
        assertEquals("timeWindowSeconds must be greater than zero.", exception.getMessage());
    }

    @Test
    void addEvent_ValidEvent_IncreasesCount() {
        // Arrange
        SlidingWindowCounter counter = new SlidingWindowCounter(60);
        LogEvent event = createEvent(LocalDateTime.now(), "ERROR", "payment-service", "failure");

        // Act
        counter.addEvent(event);

        // Assert
        assertEquals(1, counter.count());
    }

    @Test
    void addEvent_NullEvent_ThrowsNullPointerException() {
        // Arrange
        SlidingWindowCounter counter = new SlidingWindowCounter(60);

        // Act & Assert
        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> counter.addEvent(null)
        );
        assertEquals("event cannot be null", exception.getMessage());
    }

    @Test
    void addEvent_NullTimestamp_ThrowsIllegalArgumentException() {
        // Arrange
        SlidingWindowCounter counter = new SlidingWindowCounter(60);
        LogEvent event = createEvent(null, "ERROR", "payment-service", "missing timestamp");

        // Act
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> counter.addEvent(event)
        );

        // Assert
        assertEquals("LogEvent timestamp cannot be null.", exception.getMessage());
    }

    @Test
    void count_ExpiredEvents_AreEvictedFromWindow() {
        // Arrange
        SlidingWindowCounter counter = new SlidingWindowCounter(60);
        LocalDateTime now = LocalDateTime.now();
        counter.addEvent(createEvent(now.minusSeconds(120), "ERROR", "auth-service", "old event"));
        counter.addEvent(createEvent(now, "ERROR", "auth-service", "recent event"));

        // Act
        int count = counter.count();

        // Assert
        assertEquals(1, count);
    }

    @Test
    void countByLevel_MatchingLevel_ReturnsFilteredCount() {
        // Arrange
        SlidingWindowCounter counter = new SlidingWindowCounter(300);
        LocalDateTime now = LocalDateTime.now();
        counter.addEvent(createEvent(now, "ERROR", "payment-service", "error message"));
        counter.addEvent(createEvent(now, "WARN", "payment-service", "warn message"));
        counter.addEvent(createEvent(now, "error", "billing-service", "lower case level"));

        // Act
        int errorCount = counter.countByLevel("ERROR");

        // Assert
        assertEquals(2, errorCount);
    }

    @Test
    void countByLevel_NullLevel_ThrowsNullPointerException() {
        // Arrange
        SlidingWindowCounter counter = new SlidingWindowCounter(60);

        // Act & Assert
        assertThrows(NullPointerException.class, () -> counter.countByLevel(null));
    }

    @Test
    void countByComponent_MatchingComponent_ReturnsFilteredCount() {
        // Arrange
        SlidingWindowCounter counter = new SlidingWindowCounter(300);
        LocalDateTime now = LocalDateTime.now();
        counter.addEvent(createEvent(now, "ERROR", "Payment-Service", "first"));
        counter.addEvent(createEvent(now, "WARN", "auth-service", "second"));
        counter.addEvent(createEvent(now, "INFO", "payment-service", "third"));

        // Act
        int componentCount = counter.countByComponent("payment-service");

        // Assert
        assertEquals(2, componentCount);
    }

    @Test
    void countByComponent_NullComponent_ThrowsNullPointerException() {
        // Arrange
        SlidingWindowCounter counter = new SlidingWindowCounter(60);

        // Act & Assert
        assertThrows(NullPointerException.class, () -> counter.countByComponent(null));
    }

    @Test
    void count_WithPredicate_ReturnsMatchingEventsOnly() {
        // Arrange
        SlidingWindowCounter counter = new SlidingWindowCounter(300);
        LocalDateTime now = LocalDateTime.now();
        counter.addEvent(createEvent(now, "ERROR", "payment-service", "boom"));
        counter.addEvent(createEvent(now, "WARN", "payment-service", "check"));
        counter.addEvent(createEvent(now, "ERROR", "auth-service", "denied"));

        // Act
        int matchingCount = counter.count(event -> event.getMessage().contains("boom"));

        // Assert
        assertEquals(1, matchingCount);
    }

    @Test
    void count_NullFilter_ThrowsNullPointerException() {
        // Arrange
        SlidingWindowCounter counter = new SlidingWindowCounter(60);

        // Act & Assert
        assertThrows(NullPointerException.class, () -> counter.count(null));
    }

    @Test
    void getEvents_AfterCleanup_ReturnsImmutableCopy() {
        // Arrange
        SlidingWindowCounter counter = new SlidingWindowCounter(300);
        LocalDateTime now = LocalDateTime.now();
        LogEvent event = createEvent(now, "ERROR", "payment-service", "immutable");
        counter.addEvent(event);

        // Act
        List<LogEvent> events = counter.getEvents();

        // Assert
        assertEquals(1, events.size());
        assertEquals(event, events.get(0));
        assertThrows(UnsupportedOperationException.class, () -> events.add(event));
    }

    @Test
    void getLastEvents_LimitGreaterThanSize_ReturnsAllEventsInOrder() {
        // Arrange
        SlidingWindowCounter counter = new SlidingWindowCounter(300);
        LocalDateTime now = LocalDateTime.now();
        LogEvent first = createEvent(now.minusSeconds(2), "ERROR", "payment-service", "first");
        LogEvent second = createEvent(now.minusSeconds(1), "ERROR", "payment-service", "second");
        LogEvent third = createEvent(now, "ERROR", "payment-service", "third");
        counter.addEvent(first);
        counter.addEvent(second);
        counter.addEvent(third);

        // Act
        List<LogEvent> lastEvents = counter.getLastEvents(5);

        // Assert
        assertEquals(3, lastEvents.size());
        assertEquals(first, lastEvents.get(0));
        assertEquals(second, lastEvents.get(1));
        assertEquals(third, lastEvents.get(2));
    }

    @Test
    void getLastEvents_LimitLessThanSize_ReturnsMostRecentEvents() {
        // Arrange
        SlidingWindowCounter counter = new SlidingWindowCounter(300);
        LocalDateTime now = LocalDateTime.now();
        counter.addEvent(createEvent(now.minusSeconds(2), "ERROR", "payment-service", "first"));
        counter.addEvent(createEvent(now.minusSeconds(1), "ERROR", "payment-service", "second"));
        counter.addEvent(createEvent(now, "ERROR", "payment-service", "third"));

        // Act
        List<LogEvent> lastEvents = counter.getLastEvents(2);

        // Assert
        assertEquals(2, lastEvents.size());
        assertEquals("second", lastEvents.get(0).getMessage());
        assertEquals("third", lastEvents.get(1).getMessage());
    }

    @ParameterizedTest
    @ValueSource(ints = {0, -1, -10})
    void getLastEvents_NonPositiveLimit_ReturnsEmptyList(int invalidLimit) {
        // Arrange
        SlidingWindowCounter counter = new SlidingWindowCounter(300);
        counter.addEvent(createEvent(LocalDateTime.now(), "ERROR", "payment-service", "event"));

        // Act
        List<LogEvent> lastEvents = counter.getLastEvents(invalidLimit);

        // Assert
        assertTrue(lastEvents.isEmpty());
    }

    @Test
    void concurrentAddEvent_MultipleThreads_MaintainsAccurateCount() throws Exception {
        // Arrange
        SlidingWindowCounter counter = new SlidingWindowCounter(300);
        int threadCount = 10;
        int eventsPerThread = 20;
        int totalEvents = threadCount * eventsPerThread;
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch completionLatch = new CountDownLatch(totalEvents);
        AtomicInteger failures = new AtomicInteger(0);
        LocalDateTime timestamp = LocalDateTime.now();

        for (int threadIndex = 0; threadIndex < threadCount; threadIndex++) {
            final int index = threadIndex;
            executorService.submit(() -> {
                try {
                    startLatch.await();
                    for (int eventIndex = 0; eventIndex < eventsPerThread; eventIndex++) {
                        counter.addEvent(createEvent(
                                timestamp,
                                "ERROR",
                                "component-" + index,
                                "message-" + eventIndex
                        ));
                        completionLatch.countDown();
                    }
                } catch (InterruptedException interruptedException) {
                    failures.incrementAndGet();
                    Thread.currentThread().interrupt();
                }
            });
        }

        // Act
        startLatch.countDown();
        boolean completed = completionLatch.await(10, TimeUnit.SECONDS);
        executorService.shutdown();
        executorService.awaitTermination(5, TimeUnit.SECONDS);

        // Assert
        assertTrue(completed, "Concurrent event additions did not finish in time");
        assertEquals(0, failures.get());
        assertEquals(totalEvents, counter.count());
        assertNotEquals(0, counter.getEvents().size());
    }

    private LogEvent createEvent(
            LocalDateTime timestamp,
            String level,
            String component,
            String message
    ) {
        return new LogEvent(
                timestamp,
                component,
                level,
                "main-thread",
                "com.example.TestClass",
                message
        );
    }
}
