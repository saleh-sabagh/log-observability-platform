package config;

import model.LogEvent;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import serializer.LogEventDeserializer;

import java.util.Objects;
import java.util.Properties;

public class KafkaConsumerConfig {

    private final Properties properties;

    public KafkaConsumerConfig(Properties properties) {
        this.properties = Objects.requireNonNull(properties, "properties cannot be null");
    }

    public KafkaConsumer<String, LogEvent> createConsumer() {

        Properties consumerProperties = new Properties();

        consumerProperties.put(
                ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG,
                properties.getProperty("kafka.bootstrap.servers")
        );

        consumerProperties.put(
                ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG,
                StringDeserializer.class
        );

        consumerProperties.put(
                ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,
                LogEventDeserializer.class
        );

        consumerProperties.put(
                ConsumerConfig.GROUP_ID_CONFIG,
                properties.getProperty("kafka.consumer.group.id")
        );

        consumerProperties.put(
                ConsumerConfig.AUTO_OFFSET_RESET_CONFIG,
                "earliest"
        );

        consumerProperties.put(
                ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG,
                false
        );

        return new KafkaConsumer<>(consumerProperties);
    }
}