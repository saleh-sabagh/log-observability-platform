package watcher;

import model.LogEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import parser.LogParser;
import publisher.LogPublisher;

import java.io.IOException;
import java.nio.file.*;
import java.util.List;
import java.util.stream.Stream;

import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.OVERFLOW;

public class FileWatcher {

    private static final Logger logger =
            LoggerFactory.getLogger(FileWatcher.class);

    private final Path directory;
    private final LogParser logParser;
    private final LogPublisher logPublisher;

    public FileWatcher(
            Path directory,
            LogParser logParser,
            LogPublisher logPublisher
    ) {
        this.directory = directory;
        this.logParser = logParser;
        this.logPublisher = logPublisher;
    }

    public void start() throws IOException {

        if (!Files.isDirectory(directory)) {
            throw new IllegalArgumentException(
                    "Invalid log directory: " + directory
            );
        }

        processExistingFiles();
        watchDirectory();
    }

    private void processExistingFiles() throws IOException {

        try (Stream<Path> files = Files.list(directory)) {

            files.filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith(".log"))
                    .forEach(this::processFile);

        }
    }

    private void watchDirectory() throws IOException {

        try (WatchService watchService =
                     FileSystems.getDefault().newWatchService()) {

            directory.register(watchService, ENTRY_CREATE);

            while (true) {

                WatchKey key;

                try {
                    key = watchService.take();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    logger.error("File watching interrupted.", e);
                    return;
                }

                for (WatchEvent<?> event : key.pollEvents()) {

                    if (event.kind() == OVERFLOW) {
                        continue;
                    }

                    Path file = directory.resolve((Path) event.context());

                    if (Files.isRegularFile(file)
                            && file.toString().endsWith(".log")) {

                        processFile(file);
                    }

                }

                if (!key.reset()) {
                    logger.warn("Watch key is no longer valid.");
                    break;
                }

            }

        }

    }

    private void processFile(Path file) {

        try {

            List<LogEvent> events = logParser.parse(file);

            for (LogEvent event : events) {
                logPublisher.publish(event);
            }

            Files.deleteIfExists(file);

            logger.info("Processed file {}", file.getFileName());

        } catch (Exception e) {

            logger.error("Failed to process file {}", file, e);

        }

    }

}