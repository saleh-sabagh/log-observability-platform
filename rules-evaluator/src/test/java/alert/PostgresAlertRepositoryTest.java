package alert;

import config.DataSourceProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PostgresAlertRepositoryTest {

    private static final LocalDateTime FIXED_TIMESTAMP = LocalDateTime.of(2026, 7, 25, 10, 30, 0);

    @Mock
    private DataSourceProvider dataSourceProvider;

    @Mock
    private Connection connection;

    @Mock
    private PreparedStatement preparedStatement;

    @Mock
    private ResultSet resultSet;

    @InjectMocks
    private PostgresAlertRepository repository;

    private void initConnectionMocks() throws SQLException {
        when(dataSourceProvider.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
    }

    @Test
    void constructor_NullDataSourceProvider_ThrowsNullPointerException() {
        // Arrange
        // no setup required

        // Act & Assert
        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> new PostgresAlertRepository(null)
        );
        assertEquals("dataSourceProvider cannot be null", exception.getMessage());
    }

    @Test
    void save_ValidAlert_ExecutesInsertWithExpectedParameters() throws Exception {
        // Arrange
        initConnectionMocks();
        Alert alert = createSampleAlert("High Error Rate", "payment-service");
        when(preparedStatement.executeUpdate()).thenReturn(1);

        // Act
        repository.save(alert);

        // Assert
        verify(dataSourceProvider).getConnection();
        verify(connection).prepareStatement(anyString());
        verify(preparedStatement).setString(1, "High Error Rate");
        verify(preparedStatement).setString(2, "payment-service");
        verify(preparedStatement).setString(3, alert.getDescription());
        verify(preparedStatement).setTimestamp(4, Timestamp.valueOf(FIXED_TIMESTAMP));
        verify(preparedStatement).executeUpdate();
        verify(preparedStatement).close();
        verify(connection).close();
    }

    @Test
    void save_NullAlert_ThrowsNullPointerException() {
        // Arrange
        Alert alert = null;

        // Act & Assert
        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> repository.save(alert)
        );
        assertEquals("alert cannot be null", exception.getMessage());
    }

    @Test
    void save_SqlExceptionDuringInsert_ThrowsRuntimeException() throws Exception {
        // Arrange
        initConnectionMocks();
        Alert alert = createSampleAlert("Broken Rule", "auth-service");
        when(preparedStatement.executeUpdate()).thenThrow(new SQLException("insert failed"));

        // Act
        RuntimeException exception = assertThrows(RuntimeException.class, () -> repository.save(alert));

        // Assert
        assertEquals("Failed to save alert.", exception.getMessage());
        assertNotNull(exception.getCause());
        assertTrue(exception.getCause() instanceof SQLException);
    }

    @Test
    void save_ConnectionFailure_ThrowsRuntimeException() throws Exception {
        // Arrange
        Alert alert = createSampleAlert("Broken Rule", "auth-service");
        when(dataSourceProvider.getConnection()).thenThrow(new SQLException("connection failed"));

        // Act
        RuntimeException exception = assertThrows(RuntimeException.class, () -> repository.save(alert));

        // Assert
        assertEquals("Failed to save alert.", exception.getMessage());
        assertTrue(exception.getCause() instanceof SQLException);
    }

    @Test
    void save_AlertWithNullCreatedAt_ThrowsNullPointerException() throws Exception {
        // Arrange
        initConnectionMocks();
        Alert alert = Alert.builder()
                .ruleName("Missing Timestamp")
                .component("billing-service")
                .description("Alert without timestamp")
                .createdAt(null)
                .build();

        // Act & Assert
        assertThrows(NullPointerException.class, () -> repository.save(alert));
    }

    @Test
    void findAllOrderedByCreatedAt_WithRows_ReturnsAlertsOrderedFromDatabase() throws Exception {
        // Arrange
        initConnectionMocks();
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true, true, false);
        when(resultSet.getLong("id")).thenReturn(2L, 1L);
        when(resultSet.getString("rule_name")).thenReturn("Rule B", "Rule A");
        when(resultSet.getString("component")).thenReturn("component-b", "component-a");
        when(resultSet.getString("description")).thenReturn("Description B", "Description A");
        when(resultSet.getTimestamp("created_at"))
                .thenReturn(
                        Timestamp.valueOf(FIXED_TIMESTAMP.plusHours(1)),
                        Timestamp.valueOf(FIXED_TIMESTAMP)
                );

        // Act
        List<Alert> alerts = repository.findAllOrderedByCreatedAt();

        // Assert
        assertEquals(2, alerts.size());
        assertEquals(2L, alerts.get(0).getId());
        assertEquals("Rule B", alerts.get(0).getRuleName());
        assertEquals("component-b", alerts.get(0).getComponent());
        assertEquals("Description B", alerts.get(0).getDescription());
        assertEquals(FIXED_TIMESTAMP.plusHours(1), alerts.get(0).getCreatedAt());
        assertEquals(1L, alerts.get(1).getId());
        assertEquals("Rule A", alerts.get(1).getRuleName());

        verify(preparedStatement).executeQuery();
        verify(resultSet).close();
        verify(preparedStatement).close();
        verify(connection).close();
    }

    @Test
    void findAllOrderedByCreatedAt_NoRows_ReturnsEmptyList() throws Exception {
        // Arrange
        initConnectionMocks();
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(false);

        // Act
        List<Alert> alerts = repository.findAllOrderedByCreatedAt();

        // Assert
        assertNotNull(alerts);
        assertTrue(alerts.isEmpty());
    }

    @Test
    void findAllOrderedByCreatedAt_SqlExceptionDuringQuery_ThrowsRuntimeException() throws Exception {
        // Arrange
        initConnectionMocks();
        when(preparedStatement.executeQuery()).thenThrow(new SQLException("query failed"));

        // Act
        RuntimeException exception = assertThrows(
                RuntimeException.class,
                repository::findAllOrderedByCreatedAt
        );

        // Assert
        assertEquals("Failed to load alerts.", exception.getMessage());
        assertTrue(exception.getCause() instanceof SQLException);
    }

    @Test
    void save_ValidAlert_PassesExactRuleNameToPreparedStatement() throws Exception {
        // Arrange
        initConnectionMocks();
        Alert alert = createSampleAlert("Captured Rule", "inventory-service");
        when(preparedStatement.executeUpdate()).thenReturn(1);
        ArgumentCaptor<String> ruleNameCaptor = ArgumentCaptor.forClass(String.class);

        // Act
        repository.save(alert);

        // Assert
        verify(preparedStatement).setString(eq(1), ruleNameCaptor.capture());
        assertEquals("Captured Rule", ruleNameCaptor.getValue());
    }

    private Alert createSampleAlert(String ruleName, String component) {
        return Alert.builder()
                .ruleName(ruleName)
                .component(component)
                .description("Rule '" + ruleName + "' triggered.")
                .createdAt(FIXED_TIMESTAMP)
                .build();
    }
}
