package rule;

import model.LogEvent;

import java.time.ZoneId;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Predicate;

public class SlidingWindowCounter {

    private final long windowSizeMillis;
    private final Deque<LogEvent> events = new ArrayDeque<>();
    private final ReentrantLock lock = new ReentrantLock();

    public SlidingWindowCounter(long timeWindowSeconds) {
        if (timeWindowSeconds <= 0) {
            throw new IllegalArgumentException("timeWindowSeconds must be greater than zero.");
        }

        this.windowSizeMillis = timeWindowSeconds * 1000L;
    }

    public void addEvent(LogEvent event) {

        Objects.requireNonNull(event, "event cannot be null");

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
            cleanup();

            return events.size();
        } finally {
            lock.unlock();
        }
    }

    public int countByLevel(String level) {

        Objects.requireNonNull(level);

        return count(event ->
                level.equalsIgnoreCase(event.getLevel()));
    }

    public int countByComponent(String component) {

        Objects.requireNonNull(component);

        return count(event ->
                component.equalsIgnoreCase(event.getComponent()));
    }

    public int count(Predicate<LogEvent> filter) {

        Objects.requireNonNull(filter);

        lock.lock();
        try {

            cleanup();

            return (int) events.stream()
                    .filter(filter)
                    .count();

        } finally {
            lock.unlock();
        }
    }

    public List<LogEvent> getEvents() {

        lock.lock();
        try {

            cleanup();

            return List.copyOf(events);

        } finally {
            lock.unlock();
        }
    }

    public List<LogEvent> getLastEvents(int limit) {

        if (limit <= 0) {
            return List.of();
        }

        lock.lock();
        try {

            cleanup();

            return events.stream()
                    .skip(Math.max(0, events.size() - limit))
                    .toList();

        } finally {
            lock.unlock();
        }
    }

    private void cleanup() {

        if (events.isEmpty()) {
            return;
        }

        evictExpired(toEpochMilli(events.peekLast()));
    }

    private void evictExpired(long referenceTimeMillis) {

        long threshold = referenceTimeMillis - windowSizeMillis;

        while (!events.isEmpty()
                && toEpochMilli(events.peekFirst()) < threshold) {

            events.pollFirst();
        }
    }

    private long toEpochMilli(LogEvent event) {

        Objects.requireNonNull(event, "event cannot be null");

        if (event.getTimestamp() == null) {
            throw new IllegalArgumentException(
                    "LogEvent timestamp cannot be null."
            );
        }

        return event.getTimestamp()
                .atZone(ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli();
    }
}