package publisher;

import model.LogEvent;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KafkaLogPublisher implements LogPublisher {

    private static final Logger logger =
            LoggerFactory.getLogger(KafkaLogPublisher.class);

    private final KafkaProducer<String, LogEvent> producer;
    private final String topic;

    public KafkaLogPublisher(
            KafkaProducer<String, LogEvent> producer,
            String topic
    ) {
        this.producer = producer;
        this.topic = topic;
    }

    @Override
    public void publish(LogEvent event) throws Exception {

        if (event == null) {
            throw new IllegalArgumentException("LogEvent cannot be null");
        }

        logger.debug("Publishing log event to topic {}", topic);

        var metadata = producer
                .send(new ProducerRecord<>(topic, event))
                .get();

        logger.debug(
                "Published log event to topic {} partition {} offset {}",
                metadata.topic(),
                metadata.partition(),
                metadata.offset()
        );
    }
}