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
        this.dataSource = createDataSource(properties);
    }

    private HikariDataSource createDataSource(Properties properties) {

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

        /*
         * Pool Configuration
         */

        config.setMaximumPoolSize(
                getInt(properties, "postgres.pool.max-size", 10)
        );

        config.setMinimumIdle(
                getInt(properties, "postgres.pool.min-idle", 2)
        );

        config.setConnectionTimeout(
                getLong(properties, "postgres.pool.connection-timeout-ms", 30000)
        );

        config.setPoolName("rules-evaluator-pool");

        return new HikariDataSource(config);
    }

    public Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    private String getRequiredProperty(Properties properties, String key) {

        String value = properties.getProperty(key);

        if (value == null || value.isBlank()) {
            throw new IllegalStateException(
                    "Missing configuration property: " + key
            );
        }

        return value;
    }

    private int getInt(Properties properties, String key, int defaultValue) {

        String value = properties.getProperty(key);

        if (value == null || value.isBlank()) {
            return defaultValue;
        }

        return Integer.parseInt(value.trim());
    }

    private long getLong(Properties properties, String key, long defaultValue) {

        String value = properties.getProperty(key);

        if (value == null || value.isBlank()) {
            return defaultValue;
        }

        return Long.parseLong(value.trim());
    }

    @Override
    public void close() {
        dataSource.close();
    }
}