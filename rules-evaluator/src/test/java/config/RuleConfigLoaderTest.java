package config;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import rule.RuleDefinition;
import rule.RuleType;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;

@ExtendWith(MockitoExtension.class)
class RuleConfigLoaderTest {

    @Test
    void loadRules_DefaultRulesJson_LoadsAllConfiguredRules() {
        // Arrange
        RuleConfigLoader loader = new RuleConfigLoader();

        // Act
        List<RuleDefinition> rules = loader.loadRules();

        // Assert
        assertNotNull(rules);
        assertEquals(3, rules.size());
        assertEquals("Single Error Log Alert", rules.get(0).getRuleName());
        assertEquals(RuleType.ERROR, rules.get(0).getRuleType());
        assertEquals("High Error Rate (Test)", rules.get(1).getRuleName());
        assertEquals(RuleType.LEVEL_RATE, rules.get(1).getRuleType());
        assertEquals("System Overload (Test)", rules.get(2).getRuleName());
        assertEquals(RuleType.TOTAL_RATE, rules.get(2).getRuleType());
    }

    @Test
    void loadRules_DefaultRulesJson_MapsOptionalFieldsCorrectly() {
        // Arrange
        RuleConfigLoader loader = new RuleConfigLoader();

        // Act
        List<RuleDefinition> rules = loader.loadRules();

        // Assert
        assertTrue(rules.get(0).isEnabled());
        assertNull(rules.get(0).getComponent());
        assertEquals("ERROR", rules.get(0).getLogLevel());
        assertEquals(1, rules.get(0).getThreshold());
        assertEquals(0L, rules.get(0).getTimeWindowSeconds());
        assertEquals(1, rules.get(0).getMaxLastMessages());

        assertNull(rules.get(2).getLogLevel());
        assertEquals(10, rules.get(2).getThreshold());
        assertEquals(300L, rules.get(2).getTimeWindowSeconds());
    }

    @Test
    void loadRules_MissingConfigurationFile_ThrowsIllegalStateException() {
        // Arrange
        RuleConfigLoader loader = spy(new RuleConfigLoader());
        doReturn(null).when(loader).openConfigStream();

        // Act
        IllegalStateException exception = assertThrows(IllegalStateException.class, loader::loadRules);

        // Assert
        assertTrue(exception.getMessage().contains("Configuration file not found in resources: rules.json"));
    }

    @Test
    void loadRules_MalformedJson_ThrowsRuntimeException() {
        // Arrange
        RuleConfigLoader loader = spy(new RuleConfigLoader());
        InputStream malformedJson = new ByteArrayInputStream("{ invalid json".getBytes(StandardCharsets.UTF_8));
        doReturn(malformedJson).when(loader).openConfigStream();

        // Act
        RuntimeException exception = assertThrows(RuntimeException.class, loader::loadRules);

        // Assert
        assertTrue(exception.getMessage().contains("Failed to load or parse rule configuration file: rules.json"));
        assertNotNull(exception.getCause());
    }

    @Test
    void loadRules_InputStreamThrowsIOException_ThrowsRuntimeException() {
        // Arrange
        RuleConfigLoader loader = spy(new RuleConfigLoader());
        InputStream failingStream = new InputStream() {
            @Override
            public int read() throws IOException {
                throw new IOException("simulated IO failure");
            }
        };
        doReturn(failingStream).when(loader).openConfigStream();

        // Act
        RuntimeException exception = assertThrows(RuntimeException.class, loader::loadRules);

        // Assert
        assertTrue(exception.getMessage().contains("Failed to load or parse rule configuration file: rules.json"));
        assertTrue(exception.getCause() instanceof IOException);
    }

    @Test
    void loadRules_ValidCustomJsonStream_ParsesSingleRule() {
        // Arrange
        RuleConfigLoader loader = spy(new RuleConfigLoader());
        String json = """
                [
                  {
                    "ruleName": "Custom Rule",
                    "ruleType": "ERROR",
                    "enabled": false,
                    "component": "auth-service",
                    "logLevel": "WARN",
                    "threshold": 2,
                    "timeWindowSeconds": 120,
                    "maxLastMessages": 3
                  }
                ]
                """;
        doReturn(new ByteArrayInputStream(json.getBytes(StandardCharsets.UTF_8)))
                .when(loader)
                .openConfigStream();

        // Act
        List<RuleDefinition> rules = loader.loadRules();

        // Assert
        assertEquals(1, rules.size());
        assertEquals("Custom Rule", rules.get(0).getRuleName());
        assertEquals(RuleType.ERROR, rules.get(0).getRuleType());
        assertFalse(rules.get(0).isEnabled());
        assertEquals("auth-service", rules.get(0).getComponent());
        assertEquals("WARN", rules.get(0).getLogLevel());
        assertEquals(2, rules.get(0).getThreshold());
        assertEquals(120L, rules.get(0).getTimeWindowSeconds());
        assertEquals(3, rules.get(0).getMaxLastMessages());
    }

    @Test
    void loadRules_EmptyJsonArray_ReturnsEmptyList() {
        // Arrange
        RuleConfigLoader loader = spy(new RuleConfigLoader());
        doReturn(new ByteArrayInputStream("[]".getBytes(StandardCharsets.UTF_8)))
                .when(loader)
                .openConfigStream();

        // Act
        List<RuleDefinition> rules = loader.loadRules();

        // Assert
        assertNotNull(rules);
        assertTrue(rules.isEmpty());
    }
}
