package parser;

import model.LogEvent;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LogParser {

    private static final Pattern LOG_PATTERN = Pattern.compile(
            "^(?<timestamp>\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2},\\d{3})\\s+" +
                    "\\[(?<thread>.+?)]\\s+" +
                    "(?<level>\\w+)\\s+" +
                    "(?<class>[\\w.$]+)\\s+-\\s+" +
                    "(?<message>.*)$"
    );

    private static final DateTimeFormatter TIMESTAMP_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss,SSS");

    private String extractComponent(Path logFile) {
        String fileName = logFile.getFileName().toString();

        int index = fileName.indexOf('_');

        if (index == -1) {
            throw new IllegalArgumentException("Invalid log file name: " + fileName);
        }

        return fileName.substring(0, index);
    }

    public List<LogEvent> parse(Path logFile) throws IOException {
        List<LogEvent> events = new ArrayList<>();

        String component = extractComponent(logFile);

        try (BufferedReader reader = Files.newBufferedReader(logFile)) {
            String line;

            while ((line = reader.readLine()) != null) {

                if (line.isBlank()) {
                    continue;
                }

                LogEvent event = parseLine(line, component);

                if (event != null) {
                    events.add(event);
                }
            }
        }

        return events;
    }


    private LogEvent parseLine(String line, String component) {
        Matcher matcher = LOG_PATTERN.matcher(line);
        if (!matcher.matches()) {
            return null;
        }

        String rawTimestamp = matcher.group(1);
        String threadName = matcher.group("thread");
        String level = matcher.group("level");
        String className = matcher.group("class");
        String message = matcher.group("message");

        LocalDateTime timestamp = parseTimestamp(rawTimestamp);
        if (timestamp == null) {
            return null;
        }

        return new LogEvent(
                timestamp,
                component,
                level,
                threadName,
                className,
                message
        );
    }

    private LocalDateTime parseTimestamp(String timestamp) {
        try {
            return LocalDateTime.parse(timestamp, TIMESTAMP_FORMATTER);
        } catch (DateTimeParseException e) {
            return null;
        }
    }
}