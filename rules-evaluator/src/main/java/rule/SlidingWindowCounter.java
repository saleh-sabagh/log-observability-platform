package rule;

import model.LogEvent;

import java.time.ZoneId;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

public class SlidingWindowCounter {

    private final long windowSizeMillis;
    private final Deque<LogEvent> events = new ArrayDeque<>();
    private final ReentrantLock lock = new ReentrantLock();

    public SlidingWindowCounter(long timeWindowSeconds) {
        this.windowSizeMillis = timeWindowSeconds * 1000L;
    }

    public void addEvent(LogEvent event) {
        lock.lock();
        try {
            long eventTime = toEpochMilli(event);
            events.addLast(event);
            evictExpired(eventTime);
        } finally {
            lock.unlock();
        }
    }

    public int count() {
        lock.lock();
        try {
            if (events.isEmpty()) {
                return 0;
            }
            long latestTime = toEpochMilli(events.peekLast());
            evictExpired(latestTime);
            return events.size();
        } finally {
            lock.unlock();
        }
    }

    public List<LogEvent> getEvents() {
        lock.lock();
        try {
            if (events.isEmpty()) {
                return List.of();
            }
            long latestTime = toEpochMilli(events.peekLast());
            evictExpired(latestTime);
            return List.copyOf(events);
        } finally {
            lock.unlock();
        }
    }

    private void evictExpired(long referenceTimeMillis) {
        long thresholdTime = referenceTimeMillis - windowSizeMillis;
        while (!events.isEmpty() && toEpochMilli(events.peekFirst()) < thresholdTime) {
            events.pollFirst();
        }
    }

    private long toEpochMilli(LogEvent event) {
        if (event.getTimestamp() == null) {
            throw new IllegalArgumentException("LogEvent timestamp cannot be null.");
        }

        return event.getTimestamp()
                .atZone(ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli();
    }
}