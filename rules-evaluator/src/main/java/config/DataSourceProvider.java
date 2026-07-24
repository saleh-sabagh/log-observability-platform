package config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Objects;
import java.util.Properties;

public class DataSourceProvider implements AutoCloseable {

    private final HikariDataSource dataSource;

    public DataSourceProvider(Properties properties) {
        Objects.requireNonNull(properties, "properties cannot be null");
        this.dataSource = initializeHikariDataSource(properties);
    }

    private HikariDataSource initializeHikariDataSource(Properties properties) {
        String host = getRequiredProperty(properties, "postgres.host");
        String port = getRequiredProperty(properties, "postgres.port");
        String database = getRequiredProperty(properties, "postgres.database");
        String username = getRequiredProperty(properties, "postgres.username");
        String password = getRequiredProperty(properties, "postgres.password");

        String jdbcUrl = String.format(
                "jdbc:postgresql://%s:%s/%s",
                host,
                port,
                database
        );

        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(jdbcUrl);
        config.setUsername(username);
        config.setPassword(password);

        int maxSize = getIntPropertyOrDefault(properties, "postgres.pool.max-size", 10);
        int minIdle = getIntPropertyOrDefault(properties, "postgres.pool.min-idle", 2);
        long timeoutMs = getLongPropertyOrDefault(properties, "postgres.pool.connection-timeout-ms", 30000L);

        config.setMaximumPoolSize(maxSize);
        config.setMinimumIdle(minIdle);
        config.setConnectionTimeout(timeoutMs);

        return new HikariDataSource(config);
    }

    public Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    private String getRequiredProperty(Properties properties, String key) {
        String value = properties.getProperty(key);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException(
                    "Missing required configuration property: " + key
            );
        }
        return value;
    }

    private int getIntPropertyOrDefault(Properties properties, String key, int defaultValue) {
        String value = properties.getProperty(key);
        if (value == null || value.isBlank()) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid integer configuration for key: " + key, e);
        }
    }

    private long getLongPropertyOrDefault(Properties properties, String key, long defaultValue) {
        String value = properties.getProperty(key);
        if (value == null || value.isBlank()) {
            return defaultValue;
        }
        try {
            return Long.parseLong(value.trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid long configuration for key: " + key, e);
        }
    }

    @Override
    public void close() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
        }
    }
}