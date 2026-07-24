package api;

import alert.Alert;
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
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AlertResponseTest {

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
        AlertResponse response = new AlertResponse();

        // Assert
        assertNull(response.getId());
        assertNull(response.getRuleName());
        assertNull(response.getComponent());
        assertNull(response.getDescription());
        assertNull(response.getCreatedAt());
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
        AlertResponse response = new AlertResponse(
                id,
                ruleName,
                component,
                description,
                createdAt
        );

        // Assert
        assertEquals(id, response.getId());
        assertEquals(ruleName, response.getRuleName());
        assertEquals(component, response.getComponent());
        assertEquals(description, response.getDescription());
        assertEquals(createdAt, response.getCreatedAt());
    }

    @Test
    void builder_WithAllFields_BuildsCompleteResponse() {
        // Arrange
        Long id = 1L;
        String ruleName = "Single Error Log Alert";
        String component = "auth-service";
        String description = "Rule 'Single Error Log Alert' triggered with 1 matching event(s).";
        LocalDateTime createdAt = FIXED_TIMESTAMP;

        // Act
        AlertResponse response = AlertResponse.builder()
                .id(id)
                .ruleName(ruleName)
                .component(component)
                .description(description)
                .createdAt(createdAt)
                .build();

        // Assert
        assertEquals(id, response.getId());
        assertEquals(ruleName, response.getRuleName());
        assertEquals(component, response.getComponent());
        assertEquals(description, response.getDescription());
        assertEquals(createdAt, response.getCreatedAt());
    }

    @Test
    void builder_WithNoFields_BuildsResponseWithNullValues() {
        // Arrange
        // no fields provided to builder

        // Act
        AlertResponse response = AlertResponse.builder().build();

        // Assert
        assertNull(response.getId());
        assertNull(response.getRuleName());
        assertNull(response.getComponent());
        assertNull(response.getDescription());
        assertNull(response.getCreatedAt());
    }

    @Test
    void fromAlert_MapsAllFieldsCorrectly() {
        // Arrange
        Alert alert = Alert.builder()
                .id(15L)
                .ruleName("System Overload")
                .component("inventory-service")
                .description("Rule 'System Overload' triggered with 10 matching event(s).")
                .createdAt(FIXED_TIMESTAMP)
                .build();

        // Act
        AlertResponse response = toResponse(alert);

        // Assert
        assertEquals(alert.getId(), response.getId());
        assertEquals(alert.getRuleName(), response.getRuleName());
        assertEquals(alert.getComponent(), response.getComponent());
        assertEquals(alert.getDescription(), response.getDescription());
        assertEquals(alert.getCreatedAt(), response.getCreatedAt());
    }

    @Test
    void fromAlert_NullAlertFields_ProducesResponseWithNullFields() {
        // Arrange
        Alert alert = Alert.builder().build();

        // Act
        AlertResponse response = toResponse(alert);

        // Assert
        assertNull(response.getId());
        assertNull(response.getRuleName());
        assertNull(response.getComponent());
        assertNull(response.getDescription());
        assertNull(response.getCreatedAt());
    }

    @Test
    void equalsAndHashCode_SameFieldValues_AreEqual() {
        // Arrange
        AlertResponse first = createSampleResponse(1L, "Rule A", "component-a");
        AlertResponse second = createSampleResponse(1L, "Rule A", "component-a");

        // Act & Assert
        assertEquals(first, second);
        assertEquals(first.hashCode(), second.hashCode());
    }

    @Test
    void equals_DifferentId_ReturnsFalse() {
        // Arrange
        AlertResponse first = createSampleResponse(1L, "Rule A", "component-a");
        AlertResponse second = createSampleResponse(2L, "Rule A", "component-a");

        // Act & Assert
        assertNotEquals(first, second);
    }

    @Test
    void equals_DifferentDescription_ReturnsFalse() {
        // Arrange
        AlertResponse first = createSampleResponse(1L, "Rule A", "component-a");
        AlertResponse second = AlertResponse.builder()
                .id(1L)
                .ruleName("Rule A")
                .component("component-a")
                .description("Different description")
                .createdAt(FIXED_TIMESTAMP)
                .build();

        // Act & Assert
        assertNotEquals(first, second);
    }

    @Test
    void equals_NullObject_ReturnsFalse() {
        // Arrange
        AlertResponse response = createSampleResponse(1L, "Rule A", "component-a");

        // Act & Assert
        assertNotEquals(null, response);
    }

    @Test
    void equals_DifferentClass_ReturnsFalse() {
        // Arrange
        AlertResponse response = createSampleResponse(1L, "Rule A", "component-a");

        // Act & Assert
        assertNotEquals("not-a-response", response);
    }

    @Test
    void toString_ContainsKeyFieldValues() {
        // Arrange
        AlertResponse response = createSampleResponse(7L, "High Error Rate", "payment-service");

        // Act
        String responseString = response.toString();

        // Assert
        assertTrue(responseString.contains("7"));
        assertTrue(responseString.contains("High Error Rate"));
        assertTrue(responseString.contains("payment-service"));
        assertTrue(responseString.contains("2026-07-25T10:30"));
    }

    @Test
    void serializeToJson_CompleteResponse_ProducesExpectedJsonStructure() throws Exception {
        // Arrange
        AlertResponse response = createSampleResponse(5L, "Single Error Log Alert", "auth-service");

        // Act
        String json = objectMapper.writeValueAsString(response);

        // Assert
        assertTrue(json.contains("\"id\":5"));
        assertTrue(json.contains("\"ruleName\":\"Single Error Log Alert\""));
        assertTrue(json.contains("\"component\":\"auth-service\""));
        assertTrue(json.contains("\"description\":"));
        assertTrue(json.contains("\"createdAt\":\"2026-07-25T10:30:00\""));
    }

    @Test
    void serializeToJson_ResponseList_ProducesJsonArray() throws Exception {
        // Arrange
        AlertResponse first = createSampleResponse(1L, "Rule A", "component-a");
        AlertResponse second = createSampleResponse(2L, "Rule B", "component-b");
        List<AlertResponse> responses = List.of(first, second);

        // Act
        String json = objectMapper.writeValueAsString(responses);

        // Assert
        assertTrue(json.startsWith("["));
        assertTrue(json.endsWith("]"));
        assertTrue(json.contains("\"ruleName\":\"Rule A\""));
        assertTrue(json.contains("\"ruleName\":\"Rule B\""));
    }

    @Test
    void alertResponse_HasNoPublicSetters() {
        // Arrange
        // AlertResponse is an outbound API DTO without mutation methods

        // Act
        boolean hasPublicSetters = Arrays.stream(AlertResponse.class.getMethods())
                .anyMatch(method -> method.getName().startsWith("set") && method.getParameterCount() == 1);

        // Assert
        assertFalse(hasPublicSetters);
    }

    @Test
    void edgeCase_NullOptionalFields_AreAccepted() {
        // Arrange
        // builder with no values

        // Act
        AlertResponse response = AlertResponse.builder()
                .id(null)
                .ruleName(null)
                .component(null)
                .description(null)
                .createdAt(null)
                .build();

        // Assert
        assertNull(response.getId());
        assertNull(response.getRuleName());
        assertNull(response.getComponent());
        assertNull(response.getDescription());
        assertNull(response.getCreatedAt());
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "   "})
    void edgeCase_EmptyOrBlankStringFields_AreStoredAsProvided(String blankValue) {
        // Arrange
        // blankValue provided by parameter source

        // Act
        AlertResponse response = AlertResponse.builder()
                .ruleName(blankValue)
                .component(blankValue)
                .description(blankValue)
                .build();

        // Assert
        assertEquals(blankValue, response.getRuleName());
        assertEquals(blankValue, response.getComponent());
        assertEquals(blankValue, response.getDescription());
    }

    @Test
    void edgeCase_MaxLongId_IsStoredCorrectly() {
        // Arrange
        // no setup required

        // Act
        AlertResponse response = AlertResponse.builder()
                .id(Long.MAX_VALUE)
                .build();

        // Assert
        assertEquals(Long.MAX_VALUE, response.getId());
    }

    @Test
    void edgeCase_LocalDateTimeBoundaryValues_AreStoredCorrectly() {
        // Arrange
        LocalDateTime minDateTime = LocalDateTime.MIN;
        LocalDateTime maxDateTime = LocalDateTime.MAX;

        // Act
        AlertResponse minResponse = AlertResponse.builder().createdAt(minDateTime).build();
        AlertResponse maxResponse = AlertResponse.builder().createdAt(maxDateTime).build();

        // Assert
        assertEquals(minDateTime, minResponse.getCreatedAt());
        assertEquals(maxDateTime, maxResponse.getCreatedAt());
    }

    @Test
    void mapperFunction_UsingMockedAlertSource_ProducesExpectedResponse() {
        // Arrange
        Alert alert = createSampleAlert(20L, "Mocked Rule", "billing-service");
        @SuppressWarnings("unchecked")
        Function<Alert, AlertResponse> mapper = mock(Function.class);
        when(mapper.apply(alert)).thenReturn(toResponse(alert));

        // Act
        AlertResponse response = mapper.apply(alert);

        // Assert
        ArgumentCaptor<Alert> captor = ArgumentCaptor.forClass(Alert.class);
        verify(mapper).apply(captor.capture());

        Alert capturedAlert = captor.getValue();
        assertNotNull(capturedAlert);
        assertEquals(20L, capturedAlert.getId());
        assertEquals("Mocked Rule", capturedAlert.getRuleName());
        assertEquals("billing-service", capturedAlert.getComponent());
        assertEquals(20L, response.getId());
        assertEquals("Mocked Rule", response.getRuleName());
        assertEquals("billing-service", response.getComponent());
        assertEquals(FIXED_TIMESTAMP, response.getCreatedAt());
    }

    private AlertResponse toResponse(Alert alert) {
        return AlertResponse.builder()
                .id(alert.getId())
                .ruleName(alert.getRuleName())
                .component(alert.getComponent())
                .description(alert.getDescription())
                .createdAt(alert.getCreatedAt())
                .build();
    }

    private AlertResponse createSampleResponse(Long id, String ruleName, String component) {
        return AlertResponse.builder()
                .id(id)
                .ruleName(ruleName)
                .component(component)
                .description(
                        "Rule '" + ruleName + "' triggered with 1 matching event(s). Recent messages: test message"
                )
                .createdAt(FIXED_TIMESTAMP)
                .build();
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
