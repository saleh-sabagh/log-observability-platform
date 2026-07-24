package kafka;

import model.LogEvent;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rule.RuleEngine;

import java.time.Duration;
import java.util.Objects;

public class KafkaLogConsumer {

    private static final Logger logger = LoggerFactory.getLogger(KafkaLogConsumer.class);

    private final KafkaConsumer<String, LogEvent> consumer;
    private final RuleEngine ruleEngine;
    private final String topic;

    public KafkaLogConsumer(
            KafkaConsumer<String, LogEvent> consumer,
            String topic,
            RuleEngine ruleEngine
    ) {
        this.consumer = Objects.requireNonNull(consumer, "consumer cannot be null");
        this.topic = Objects.requireNonNull(topic, "topic cannot be null");
        this.ruleEngine = Objects.requireNonNull(ruleEngine, "ruleEngine cannot be null");
    }

    /**
     * Starts consuming LogEvents from Kafka.
     * This method blocks until the thread is interrupted.
     */
    public void start() {

        consumer.subscribe(java.util.List.of(topic));
        logger.info("Kafka consumer subscribed to topic '{}'.", topic);

        try {
            while (!Thread.currentThread().isInterrupted()) {

                ConsumerRecords<String, LogEvent> records = consumer.poll(Duration.ofSeconds(1));

                for (ConsumerRecord<String, LogEvent> record : records) {
                    LogEvent event = record.value();

                    if (event == null) {
                        logger.warn(
                                "Received null LogEvent from partition {} offset {}.",
                                record.partition(),
                                record.offset()
                        );
                        continue;
                    }

                    logger.debug(
                            "Received LogEvent from partition {} offset {}.",
                            record.partition(),
                            record.offset()
                    );

                    ruleEngine.evaluate(event);
                }

                if (!records.isEmpty()) {
                    consumer.commitSync();
                    logger.debug("Successfully committed offsets for {} records.", records.count());
                }
            }

        } catch (Exception e) {
            logger.error("Unexpected error while consuming Kafka messages.", e);
            throw e;

        } finally {
            logger.info("Closing Kafka consumer.");
            consumer.close();
        }
    }
}