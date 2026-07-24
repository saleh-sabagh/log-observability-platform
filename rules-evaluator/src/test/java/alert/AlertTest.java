package alert;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AlertTest {

    private static final LocalDateTime FIXED_TIMESTAMP = LocalDateTime.of(2026, 7, 25, 10, 30, 0);

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    @Test
    void noArgsConstructor_CreatesInstanceWithDefaultFieldValues() {
        // Arrange
        // no setup required

        // Act
        Alert alert = new Alert();

        // Assert
        assertNull(alert.getId());
        assertNull(alert.getRuleName());
        assertNull(alert.getComponent());
        assertNull(alert.getDescription());
        assertNull(alert.getCreatedAt());
    }

    @Test
    void allArgsConstructor_SetsAllFieldsCorrectly() {
        // Arrange
        Long id = 42L;
        String ruleName = "High Error Rate";
        String component = "payment-service";
        String description = "Rule 'High Error Rate' triggered with 3 matching event(s).";
        LocalDateTime createdAt = FIXED_TIMESTAMP;

        // Act
        Alert alert = new Alert(id, ruleName, component, description, createdAt);

        // Assert
        assertEquals(id, alert.getId());
        assertEquals(ruleName, alert.getRuleName());
        assertEquals(component, alert.getComponent());
        assertEquals(description, alert.getDescription());
        assertEquals(createdAt, alert.getCreatedAt());
    }

    @Test
    void builder_WithAllFields_BuildsCompleteAlert() {
        // Arrange
        Long id = 1L;
        String ruleName = "Single Error Log Alert";
        String component = "auth-service";
        String description = "Rule 'Single Error Log Alert' triggered with 1 matching event(s).";
        LocalDateTime createdAt = FIXED_TIMESTAMP;

        // Act
        Alert alert = Alert.builder()
                .id(id)
                .ruleName(ruleName)
                .component(component)
                .description(description)
                .createdAt(createdAt)
                .build();

        // Assert
        assertEquals(id, alert.getId());
        assertEquals(ruleName, alert.getRuleName());
        assertEquals(component, alert.getComponent());
        assertEquals(description, alert.getDescription());
        assertEquals(createdAt, alert.getCreatedAt());
    }

    @Test
    void builder_WithNoFields_BuildsAlertWithNullValues() {
        // Arrange
        // no fields provided to builder

        // Act
        Alert alert = Alert.builder().build();

        // Assert
        assertNull(alert.getId());
        assertNull(alert.getRuleName());
        assertNull(alert.getComponent());
        assertNull(alert.getDescription());
        assertNull(alert.getCreatedAt());
    }

    @Test
    void setters_UpdateAllFieldsCorrectly() {
        // Arrange
        Alert alert = new Alert();
        Long id = 99L;
        String ruleName = "System Overload";
        String component = "inventory-service";
        String description = "Rule 'System Overload' triggered with 10 matching event(s).";
        LocalDateTime createdAt = FIXED_TIMESTAMP.plusHours(2);

        // Act
        alert.setId(id);
        alert.setRuleName(ruleName);
        alert.setComponent(component);
        alert.setDescription(description);
        alert.setCreatedAt(createdAt);

        // Assert
        assertEquals(id, alert.getId());
        assertEquals(ruleName, alert.getRuleName());
        assertEquals(component, alert.getComponent());
        assertEquals(description, alert.getDescription());
        assertEquals(createdAt, alert.getCreatedAt());
    }

    @Test
    void equalsAndHashCode_SameFieldValues_AreEqual() {
        // Arrange
        Alert first = createSampleAlert(1L, "Rule A", "component-a");
        Alert second = createSampleAlert(1L, "Rule A", "component-a");

        // Act & Assert
        assertEquals(first, second);
        assertEquals(first.hashCode(), second.hashCode());
    }

    @Test
    void equals_DifferentId_ReturnsFalse() {
        // Arrange
        Alert first = createSampleAlert(1L, "Rule A", "component-a");
        Alert second = createSampleAlert(2L, "Rule A", "component-a");

        // Act & Assert
        assertNotEquals(first, second);
    }

    @Test
    void equals_DifferentRuleName_ReturnsFalse() {
        // Arrange
        Alert first = createSampleAlert(1L, "Rule A", "component-a");
        Alert second = createSampleAlert(1L, "Rule B", "component-a");

        // Act & Assert
        assertNotEquals(first, second);
    }

    @Test
    void equals_NullObject_ReturnsFalse() {
        // Arrange
        Alert alert = createSampleAlert(1L, "Rule A", "component-a");

        // Act & Assert
        assertNotEquals(null, alert);
    }

    @Test
    void equals_DifferentClass_ReturnsFalse() {
        // Arrange
        Alert alert = createSampleAlert(1L, "Rule A", "component-a");

        // Act & Assert
        assertNotEquals("not-an-alert", alert);
    }

    @Test
    void toString_ContainsKeyFieldValues() {
        // Arrange
        Alert alert = createSampleAlert(7L, "High Error Rate", "payment-service");

        // Act
        String alertString = alert.toString();

        // Assert
        assertTrue(alertString.contains("7"));
        assertTrue(alertString.contains("High Error Rate"));
        assertTrue(alertString.contains("payment-service"));
        assertTrue(alertString.contains("2026-07-25T10:30"));
    }

    @Test
    void serializeToJson_CompleteAlert_ProducesExpectedJsonStructure() throws Exception {
        // Arrange
        Alert alert = createSampleAlert(5L, "Single Error Log Alert", "auth-service");

        // Act
        String json = objectMapper.writeValueAsString(alert);

        // Assert
        assertTrue(json.contains("\"id\":5"));
        assertTrue(json.contains("\"ruleName\":\"Single Error Log Alert\""));
        assertTrue(json.contains("\"component\":\"auth-service\""));
        assertTrue(json.contains("\"description\":"));
        assertTrue(json.contains("\"createdAt\":\"2026-07-25T10:30:00\""));
    }

    @Test
    void deserializeFromJson_ValidAlertJson_MapsAllFields() throws Exception {
        // Arrange
        String json = """
                {
                  "id": 12,
                  "ruleName": "High Error Rate",
                  "component": "payment-service",
                  "description": "Rule triggered",
                  "createdAt": "2026-07-25T10:30:00"
                }
                """;

        // Act
        Alert alert = objectMapper.readValue(json, Alert.class);

        // Assert
        assertEquals(12L, alert.getId());
        assertEquals("High Error Rate", alert.getRuleName());
        assertEquals("payment-service", alert.getComponent());
        assertEquals("Rule triggered", alert.getDescription());
        assertEquals(FIXED_TIMESTAMP, alert.getCreatedAt());
    }

    @Test
    void serializeAndDeserialize_RoundTrip_PreservesAllFieldValues() throws Exception {
        // Arrange
        Alert original = createSampleAlert(3L, "System Overload", "inventory-service");

        // Act
        String json = objectMapper.writeValueAsString(original);
        Alert restored = objectMapper.readValue(json, Alert.class);

        // Assert
        assertEquals(original, restored);
    }

    @Test
    void deserializeFromJson_InvalidCreatedAt_ThrowsException() {
        // Arrange
        String json = """
                {
                  "id": 1,
                  "ruleName": "Broken Alert",
                  "component": "auth-service",
                  "description": "Invalid timestamp",
                  "createdAt": "not-a-date"
                }
                """;

        // Act & Assert
        assertThrows(JsonMappingException.class, () -> objectMapper.readValue(json, Alert.class));
    }

    @Test
    void edgeCase_NullOptionalFields_AreAccepted() {
        // Arrange
        Alert alert = new Alert();

        // Act
        alert.setId(null);
        alert.setRuleName(null);
        alert.setComponent(null);
        alert.setDescription(null);
        alert.setCreatedAt(null);

        // Assert
        assertNull(alert.getId());
        assertNull(alert.getRuleName());
        assertNull(alert.getComponent());
        assertNull(alert.getDescription());
        assertNull(alert.getCreatedAt());
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "   "})
    void edgeCase_EmptyOrBlankStringFields_AreStoredAsProvided(String blankValue) {
        // Arrange
        Alert alert = new Alert();

        // Act
        alert.setRuleName(blankValue);
        alert.setComponent(blankValue);
        alert.setDescription(blankValue);

        // Assert
        assertEquals(blankValue, alert.getRuleName());
        assertEquals(blankValue, alert.getComponent());
        assertEquals(blankValue, alert.getDescription());
    }

    @Test
    void edgeCase_MaxLongId_IsStoredCorrectly() {
        // Arrange
        Alert alert = new Alert();

        // Act
        alert.setId(Long.MAX_VALUE);

        // Assert
        assertEquals(Long.MAX_VALUE, alert.getId());
    }

    @Test
    void edgeCase_LocalDateTimeBoundaryValues_AreStoredCorrectly() {
        // Arrange
        Alert alert = new Alert();
        LocalDateTime minDateTime = LocalDateTime.MIN;
        LocalDateTime maxDateTime = LocalDateTime.MAX;

        // Act
        alert.setCreatedAt(minDateTime);
        Alert minAlert = alert;

        Alert maxAlert = Alert.builder().createdAt(maxDateTime).build();

        // Assert
        assertEquals(minDateTime, minAlert.getCreatedAt());
        assertEquals(maxDateTime, maxAlert.getCreatedAt());
    }

    @Test
    void consumerAcceptance_PassedToMockConsumer_PreservesFieldValues() {
        // Arrange
        Alert alert = createSampleAlert(10L, "Consumer Rule", "billing-service");
        @SuppressWarnings("unchecked")
        Consumer<Alert> consumer = mock(Consumer.class);

        // Act
        consumer.accept(alert);

        // Assert
        ArgumentCaptor<Alert> captor = ArgumentCaptor.forClass(Alert.class);
        verify(consumer).accept(captor.capture());

        Alert capturedAlert = captor.getValue();
        assertNotNull(capturedAlert);
        assertEquals(10L, capturedAlert.getId());
        assertEquals("Consumer Rule", capturedAlert.getRuleName());
        assertEquals("billing-service", capturedAlert.getComponent());
        assertEquals(
                "Rule 'Consumer Rule' triggered with 1 matching event(s). Recent messages: test message",
                capturedAlert.getDescription()
        );
        assertEquals(FIXED_TIMESTAMP, capturedAlert.getCreatedAt());
    }

    private Alert createSampleAlert(Long id, String ruleName, String component) {
        return Alert.builder()
                .id(id)
                .ruleName(ruleName)
                .component(component)
                .description(
                        "Rule '" + ruleName + "' triggered with 1 matching event(s). Recent messages: test message"
                )
                .createdAt(FIXED_TIMESTAMP)
                .build();
    }
}
