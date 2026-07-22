package config;

import model.LogEvent;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import serializer.LogEventSerializer;

import java.util.Objects;
import java.util.Properties;

public class KafkaProducerConfig {
    private final Properties properties;

    public KafkaProducerConfig(Properties properties) {
        this.properties = Objects.requireNonNull(properties, "properties cannot be null");
    }


    public KafkaProducer<String, LogEvent> createProducer() {

        Properties producerProperties = new Properties();
        producerProperties.put(
                ProducerConfig.BOOTSTRAP_SERVERS_CONFIG,
                properties.getProperty("kafka.bootstrap.servers")
        );
        producerProperties.put(
                ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,
                StringSerializer.class
        );
        producerProperties.put(
                ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,
                LogEventSerializer.class
        );
        producerProperties.put(
                ProducerConfig.ACKS_CONFIG,
                "all"
        );
        producerProperties.put(
                ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG,
                true
        );

        return new KafkaProducer<>(producerProperties);
    }
}