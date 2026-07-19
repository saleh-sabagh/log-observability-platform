package config;

import model.LogEvent;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import serializer.LogEventSerializer;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class KafkaProducerConfig {

    public KafkaProducer<String, LogEvent> createProducer() {
        Properties appProperties = loadApplicationProperties();

        Properties producerProperties = new Properties();
        producerProperties.put(
                ProducerConfig.BOOTSTRAP_SERVERS_CONFIG,
                appProperties.getProperty("kafka.bootstrap.servers")
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

    private Properties loadApplicationProperties() {
        Properties properties = new Properties();

        try (InputStream inputStream =
                     getClass().getClassLoader().getResourceAsStream("config.properties")) {

            if (inputStream == null) {
                throw new IllegalStateException("config.properties not found");
            }

            properties.load(inputStream);
            return properties;

        } catch (IOException e) {
            throw new IllegalStateException("Failed to load config.properties", e);
        }
    }
}