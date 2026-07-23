package alert;

import config.DataSourceProvider;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class PostgresAlertRepository implements AlertRepository {

    private static final String INSERT_SQL = """
            INSERT INTO alerts (
                rule_name,
                component,
                description,
                created_at
            )
            VALUES (?, ?, ?, ?)
            """;

    private static final String FIND_ALL_SQL = """
            SELECT
                id,
                rule_name,
                component,
                description,
                created_at
            FROM alerts
            ORDER BY created_at DESC
            """;

    private final DataSourceProvider dataSourceProvider;

    public PostgresAlertRepository(DataSourceProvider dataSourceProvider) {
        this.dataSourceProvider =
                Objects.requireNonNull(dataSourceProvider,
                        "dataSourceProvider cannot be null");
    }

    @Override
    public void save(Alert alert) {

        Objects.requireNonNull(alert, "alert cannot be null");

        try (
                Connection connection = dataSourceProvider.getConnection();
                PreparedStatement statement =
                        connection.prepareStatement(INSERT_SQL)
        ) {

            statement.setString(1, alert.getRuleName());
            statement.setString(2, alert.getComponent());
            statement.setString(3, alert.getDescription());
            statement.setTimestamp(
                    4,
                    Timestamp.valueOf(alert.getCreatedAt())
            );

            statement.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Failed to save alert.",
                    e
            );
        }
    }

    @Override
    public List<Alert> findAllOrderedByCreatedAt() {

        List<Alert> alerts = new ArrayList<>();

        try (
                Connection connection = dataSourceProvider.getConnection();
                PreparedStatement statement =
                        connection.prepareStatement(FIND_ALL_SQL);
                ResultSet resultSet = statement.executeQuery()
        ) {

            while (resultSet.next()) {

                Alert alert = Alert.builder()
                        .id(resultSet.getLong("id"))
                        .ruleName(resultSet.getString("rule_name"))
                        .component(resultSet.getString("component"))
                        .description(resultSet.getString("description"))
                        .createdAt(
                                resultSet.getTimestamp("created_at")
                                        .toLocalDateTime()
                        )
                        .build();

                alerts.add(alert);
            }

            return alerts;

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Failed to load alerts.",
                    e
            );
        }
    }
}