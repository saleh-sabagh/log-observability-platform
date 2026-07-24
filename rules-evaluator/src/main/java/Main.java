import alert.AlertRepository;
import alert.AlertService;
import alert.PostgresAlertRepository;
import api.AlertApi;
import config.DataSourceProvider;
import config.KafkaConsumerConfig;
import config.RuleConfigLoader;
import kafka.KafkaLogConsumer;
import model.LogEvent;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import rule.RuleDefinition;
import rule.RuleEngine;

import java.io.InputStream;
import java.util.List;
import java.util.Properties;

public class Main {

    public static void main(String[] args) {
        try {
            System.out.println("Starting Rules Evaluator Module...");

            Properties properties = new Properties();
            try (InputStream input = Main.class.getClassLoader().getResourceAsStream("config.properties")) {
                if (input == null) {
                    throw new IllegalStateException("config.properties not found in resources!");
                }
                properties.load(input);
            }

            DataSourceProvider dataSourceProvider = new DataSourceProvider(properties);
            KafkaConsumer<String, LogEvent> rawKafkaConsumer = new KafkaConsumerConfig(properties).createConsumer();

            List<RuleDefinition> rules = new RuleConfigLoader().loadRules();
            System.out.println("Loaded " + rules.size() + " rules successfully.");

            AlertRepository repository = new PostgresAlertRepository(dataSourceProvider);
            AlertService alertService = new AlertService(repository);
            RuleEngine ruleEngine = new RuleEngine(rules, alertService);

            AlertApi alertApi = new AlertApi(alertService);
            int apiPort = Integer.parseInt(properties.getProperty("server.port", "8080"));
            alertApi.start(apiPort);

            String topic = properties.getProperty("kafka.topic", "log-events");
            KafkaLogConsumer kafkaLogConsumer = new KafkaLogConsumer(rawKafkaConsumer, topic, ruleEngine);

            Thread mainThread = Thread.currentThread();

            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                System.out.println("\nShutdown signal received. Closing resources...");

                rawKafkaConsumer.wakeup();
                mainThread.interrupt();

                alertApi.stop();
                dataSourceProvider.close();

                System.out.println("Cleanup completed successfully.");
            }));

            kafkaLogConsumer.start();

        } catch (Exception e) {
            System.out.println("Application shut down gracefully.");
        }
    }
}