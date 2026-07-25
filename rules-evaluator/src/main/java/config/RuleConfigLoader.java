package config;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import rule.RuleDefinition;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class RuleConfigLoader {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final String DEFAULT_CONFIG_FILE = "rules.json";

    /**
     * Loads rule definitions from the rules.json configuration file.
     *
     * @return a list of RuleDefinition objects
     */
    public List<RuleDefinition> loadRules() {
        try (InputStream inputStream = openConfigStream()) {
            if (inputStream == null) {
                throw new IllegalStateException("Configuration file not found in resources: " + DEFAULT_CONFIG_FILE);
            }

            return OBJECT_MAPPER.readValue(inputStream, new TypeReference<List<RuleDefinition>>() {});

        } catch (IOException e) {
            throw new RuntimeException("Failed to load or parse rule configuration file: " + DEFAULT_CONFIG_FILE, e);
        }
    }

    InputStream openConfigStream() {
        return getClass().getClassLoader().getResourceAsStream(DEFAULT_CONFIG_FILE);
    }
}