package rule;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonMappingException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class RuleDefinitionTest {

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
    }

    @Test
    void noArgsConstructor_CreatesInstanceWithDefaultFieldValues() {
        // Arrange
        // no setup required

        // Act
        RuleDefinition rule = new RuleDefinition();

        // Assert
        assertNull(rule.getRuleName());
        assertNull(rule.getRuleType());
        assertFalse(rule.isEnabled());
        assertNull(rule.getComponent());
        assertNull(rule.getLogLevel());
        assertEquals(0, rule.getThreshold());
        assertEquals(0L, rule.getTimeWindowSeconds());
        assertEquals(0, rule.getMaxLastMessages());
    }

    @Test
    void allArgsConstructor_SetsAllFieldsCorrectly() {
        // Arrange
        String ruleName = "High Error Rate";
        RuleType ruleType = RuleType.LEVEL_RATE;
        boolean enabled = true;
        String component = "payment-service";
        String logLevel = "ERROR";
        int threshold = 5;
        long timeWindowSeconds = 300L;
        int maxLastMessages = 2;

        // Act
        RuleDefinition rule = new RuleDefinition(
                ruleName,
                ruleType,
                enabled,
                component,
                logLevel,
                threshold,
                timeWindowSeconds,
                maxLastMessages
        );

        // Assert
        assertEquals(ruleName, rule.getRuleName());
        assertEquals(ruleType, rule.getRuleType());
        assertTrue(rule.isEnabled());
        assertEquals(component, rule.getComponent());
        assertEquals(logLevel, rule.getLogLevel());
        assertEquals(threshold, rule.getThreshold());
        assertEquals(timeWindowSeconds, rule.getTimeWindowSeconds());
        assertEquals(maxLastMessages, rule.getMaxLastMessages());
    }

    @Test
    void setters_UpdateAllFieldsCorrectly() {
        // Arrange
        RuleDefinition rule = new RuleDefinition();

        // Act
        rule.setRuleName("System Overload");
        rule.setRuleType(RuleType.TOTAL_RATE);
        rule.setEnabled(true);
        rule.setComponent("auth-service");
        rule.setLogLevel("WARN");
        rule.setThreshold(10);
        rule.setTimeWindowSeconds(600L);
        rule.setMaxLastMessages(3);

        // Assert
        assertEquals("System Overload", rule.getRuleName());
        assertEquals(RuleType.TOTAL_RATE, rule.getRuleType());
        assertTrue(rule.isEnabled());
        assertEquals("auth-service", rule.getComponent());
        assertEquals("WARN", rule.getLogLevel());
        assertEquals(10, rule.getThreshold());
        assertEquals(600L, rule.getTimeWindowSeconds());
        assertEquals(3, rule.getMaxLastMessages());
    }

    @Test
    void equalsAndHashCode_SameFieldValues_AreEqual() {
        // Arrange
        RuleDefinition first = createSampleRule("Duplicate Rule", RuleType.ERROR, true);
        RuleDefinition second = createSampleRule("Duplicate Rule", RuleType.ERROR, true);

        // Act & Assert
        assertEquals(first, second);
        assertEquals(first.hashCode(), second.hashCode());
    }

    @Test
    void equals_DifferentRuleName_ReturnsFalse() {
        // Arrange
        RuleDefinition first = createSampleRule("Rule A", RuleType.ERROR, true);
        RuleDefinition second = createSampleRule("Rule B", RuleType.ERROR, true);

        // Act & Assert
        assertNotEquals(first, second);
    }

    @Test
    void equals_DifferentRuleType_ReturnsFalse() {
        // Arrange
        RuleDefinition first = createSampleRule("Same Name", RuleType.ERROR, true);
        RuleDefinition second = createSampleRule("Same Name", RuleType.LEVEL_RATE, true);

        // Act & Assert
        assertNotEquals(first, second);
    }

    @Test
    void equals_NullObject_ReturnsFalse() {
        // Arrange
        RuleDefinition rule = createSampleRule("Rule", RuleType.ERROR, true);

        // Act & Assert
        assertNotEquals(null, rule);
    }

    @Test
    void equals_DifferentClass_ReturnsFalse() {
        // Arrange
        RuleDefinition rule = createSampleRule("Rule", RuleType.ERROR, true);

        // Act & Assert
        assertNotEquals("Rule", rule);
    }

    @Test
    void toString_ContainsKeyFieldValues() {
        // Arrange
        RuleDefinition rule = createSampleRule("Single Error Log Alert", RuleType.ERROR, true);

        // Act
        String ruleString = rule.toString();

        // Assert
        assertTrue(ruleString.contains("Single Error Log Alert"));
        assertTrue(ruleString.contains("ERROR"));
        assertTrue(ruleString.contains("enabled=true"));
    }

    @Test
    void deserializeFromJson_ErrorRule_MapsAllFields() throws Exception {
        // Arrange
        String json = """
                {
                  "ruleName": "Single Error Log Alert",
                  "ruleType": "ERROR",
                  "enabled": true,
                  "component": null,
                  "logLevel": "ERROR",
                  "threshold": 1,
                  "timeWindowSeconds": 0,
                  "maxLastMessages": 1
                }
                """;

        // Act
        RuleDefinition rule = objectMapper.readValue(json, RuleDefinition.class);

        // Assert
        assertEquals("Single Error Log Alert", rule.getRuleName());
        assertEquals(RuleType.ERROR, rule.getRuleType());
        assertTrue(rule.isEnabled());
        assertNull(rule.getComponent());
        assertEquals("ERROR", rule.getLogLevel());
        assertEquals(1, rule.getThreshold());
        assertEquals(0L, rule.getTimeWindowSeconds());
        assertEquals(1, rule.getMaxLastMessages());
    }

    @Test
    void deserializeFromJson_LevelRateRule_MapsAllFields() throws Exception {
        // Arrange
        String json = """
                {
                  "ruleName": "High Error Rate (Test)",
                  "ruleType": "LEVEL_RATE",
                  "enabled": true,
                  "component": null,
                  "logLevel": "ERROR",
                  "threshold": 5,
                  "timeWindowSeconds": 300,
                  "maxLastMessages": 2
                }
                """;

        // Act
        RuleDefinition rule = objectMapper.readValue(json, RuleDefinition.class);

        // Assert
        assertEquals("High Error Rate (Test)", rule.getRuleName());
        assertEquals(RuleType.LEVEL_RATE, rule.getRuleType());
        assertEquals(5, rule.getThreshold());
        assertEquals(300L, rule.getTimeWindowSeconds());
        assertEquals(2, rule.getMaxLastMessages());
    }

    @Test
    void deserializeFromJson_TotalRateRule_AllowsNullLogLevel() throws Exception {
        // Arrange
        String json = """
                {
                  "ruleName": "System Overload (Test)",
                  "ruleType": "TOTAL_RATE",
                  "enabled": true,
                  "component": null,
                  "logLevel": null,
                  "threshold": 10,
                  "timeWindowSeconds": 300,
                  "maxLastMessages": 2
                }
                """;

        // Act
        RuleDefinition rule = objectMapper.readValue(json, RuleDefinition.class);

        // Assert
        assertEquals(RuleType.TOTAL_RATE, rule.getRuleType());
        assertNull(rule.getLogLevel());
        assertEquals(10, rule.getThreshold());
    }

    @Test
    void deserializeFromJson_RulesArray_MapsMultipleDefinitions() throws Exception {
        // Arrange
        String json = """
                [
                  {
                    "ruleName": "Single Error Log Alert",
                    "ruleType": "ERROR",
                    "enabled": true,
                    "component": null,
                    "logLevel": "ERROR",
                    "threshold": 1,
                    "timeWindowSeconds": 0,
                    "maxLastMessages": 1
                  },
                  {
                    "ruleName": "System Overload (Test)",
                    "ruleType": "TOTAL_RATE",
                    "enabled": true,
                    "component": null,
                    "logLevel": null,
                    "threshold": 10,
                    "timeWindowSeconds": 300,
                    "maxLastMessages": 2
                  }
                ]
                """;

        // Act
        List<RuleDefinition> rules = objectMapper.readValue(json, new TypeReference<>() {});

        // Assert
        assertEquals(2, rules.size());
        assertEquals(RuleType.ERROR, rules.get(0).getRuleType());
        assertEquals(RuleType.TOTAL_RATE, rules.get(1).getRuleType());
    }

    @Test
    void deserializeFromJson_InvalidRuleType_ThrowsException() {
        // Arrange
        String json = """
                {
                  "ruleName": "Broken Rule",
                  "ruleType": "INVALID_TYPE",
                  "enabled": true,
                  "threshold": 1,
                  "timeWindowSeconds": 0,
                  "maxLastMessages": 1
                }
                """;

        // Act & Assert
        assertThrows(JsonMappingException.class, () -> objectMapper.readValue(json, RuleDefinition.class));
    }

    @Test
    void edgeCase_NullOptionalFields_AreAccepted() {
        // Arrange
        RuleDefinition rule = new RuleDefinition();

        // Act
        rule.setComponent(null);
        rule.setLogLevel(null);
        rule.setRuleName(null);
        rule.setRuleType(null);

        // Assert
        assertNull(rule.getComponent());
        assertNull(rule.getLogLevel());
        assertNull(rule.getRuleName());
        assertNull(rule.getRuleType());
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "   "})
    void edgeCase_EmptyOrBlankComponentAndLogLevel_AreStoredAsProvided(String blankValue) {
        // Arrange
        RuleDefinition rule = new RuleDefinition();

        // Act
        rule.setComponent(blankValue);
        rule.setLogLevel(blankValue);

        // Assert
        assertEquals(blankValue, rule.getComponent());
        assertEquals(blankValue, rule.getLogLevel());
    }

    @Test
    void edgeCase_ZeroThresholdAndTimeWindow_AreStoredCorrectly() {
        // Arrange
        RuleDefinition rule = new RuleDefinition();

        // Act
        rule.setThreshold(0);
        rule.setTimeWindowSeconds(0L);
        rule.setMaxLastMessages(0);

        // Assert
        assertEquals(0, rule.getThreshold());
        assertEquals(0L, rule.getTimeWindowSeconds());
        assertEquals(0, rule.getMaxLastMessages());
    }

    @Test
    void edgeCase_MaxIntegerAndLongValues_AreStoredCorrectly() {
        // Arrange
        RuleDefinition rule = new RuleDefinition();

        // Act
        rule.setThreshold(Integer.MAX_VALUE);
        rule.setTimeWindowSeconds(Long.MAX_VALUE);
        rule.setMaxLastMessages(Integer.MAX_VALUE);

        // Assert
        assertEquals(Integer.MAX_VALUE, rule.getThreshold());
        assertEquals(Long.MAX_VALUE, rule.getTimeWindowSeconds());
        assertEquals(Integer.MAX_VALUE, rule.getMaxLastMessages());
    }

    @Test
    void edgeCase_NegativeThresholdAndTimeWindow_AreStoredCorrectly() {
        // Arrange
        RuleDefinition rule = new RuleDefinition();

        // Act
        rule.setThreshold(-1);
        rule.setTimeWindowSeconds(-100L);
        rule.setMaxLastMessages(-5);

        // Assert
        assertEquals(-1, rule.getThreshold());
        assertEquals(-100L, rule.getTimeWindowSeconds());
        assertEquals(-5, rule.getMaxLastMessages());
    }

    @Test
    void consumerAcceptance_PassedToMockConsumer_PreservesFieldValues() {
        // Arrange
        RuleDefinition rule = createSampleRule("Consumer Rule", RuleType.LEVEL_RATE, true);
        @SuppressWarnings("unchecked")
        Consumer<RuleDefinition> consumer = mock(Consumer.class);

        // Act
        consumer.accept(rule);

        // Assert
        ArgumentCaptor<RuleDefinition> captor = ArgumentCaptor.forClass(RuleDefinition.class);
        verify(consumer).accept(captor.capture());

        RuleDefinition capturedRule = captor.getValue();
        assertNotNull(capturedRule);
        assertEquals("Consumer Rule", capturedRule.getRuleName());
        assertEquals(RuleType.LEVEL_RATE, capturedRule.getRuleType());
        assertTrue(capturedRule.isEnabled());
        assertEquals("test-component", capturedRule.getComponent());
        assertEquals("ERROR", capturedRule.getLogLevel());
        assertEquals(3, capturedRule.getThreshold());
        assertEquals(120L, capturedRule.getTimeWindowSeconds());
        assertEquals(2, capturedRule.getMaxLastMessages());
    }

    private RuleDefinition createSampleRule(String ruleName, RuleType ruleType, boolean enabled) {
        return new RuleDefinition(
                ruleName,
                ruleType,
                enabled,
                "test-component",
                "ERROR",
                3,
                120L,
                2
        );
    }
}
