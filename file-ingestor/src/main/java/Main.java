import config.KafkaProducerConfig;
import model.LogEvent;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import parser.LogParser;
import publisher.KafkaLogPublisher;
import publisher.LogPublisher;
import watcher.FileWatcher;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

public class Main {

    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {

        try {
            Properties properties = loadApplicationProperties();

            Path logDirectory = Paths.get(
                    properties.getProperty("log.directory")
            );

            String topic = properties.getProperty("kafka.topic");

            KafkaProducer<String, LogEvent> producer =
                    new KafkaProducerConfig(properties).createProducer();

            LogParser parser = new LogParser();

            LogPublisher publisher =
                    new KafkaLogPublisher(producer, topic);

            FileWatcher watcher =
                    new FileWatcher(logDirectory, parser, publisher);

            logger.info("Starting File Ingestor...");

            watcher.start();

        } catch (Exception e) {
            logger.error("Application failed to start.", e);
            System.exit(1);
        }
    }

    private static Properties loadApplicationProperties() throws IOException {

        Properties properties = new Properties();

        try (InputStream inputStream =
                     Main.class.getClassLoader()
                             .getResourceAsStream("config.properties")) {

            if (inputStream == null) {
                throw new IllegalStateException(
                        "config.properties not found."
                );
            }

            properties.load(inputStream);
        }

        return properties;
    }
}