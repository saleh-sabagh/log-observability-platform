package config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Objects;
import java.util.Properties;

public class DataSourceProvider implements AutoCloseable {

    private final Properties properties;
    private final HikariDataSource dataSource;

    public DataSourceProvider(Properties properties) {
        this.properties = Objects.requireNonNull(properties, "properties cannot be null");

        // تنظیمات استخر کانکشن دقیقاً در زمان ساخت آبجکت انجام می‌شود
        this.dataSource = initializeHikariDataSource();
    }

    private HikariDataSource initializeHikariDataSource() {
        String host = getRequiredProperty("postgres.host");
        String port = getRequiredProperty("postgres.port");
        String database = getRequiredProperty("postgres.database");
        String username = getRequiredProperty("postgres.username");
        String password = getRequiredProperty("postgres.password");

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

        config.setMaximumPoolSize(10);
        config.setMinimumIdle(2);
        config.setConnectionTimeout(30000);

        return new HikariDataSource(config);
    }

    public Connection getConnection() throws SQLException {
        // این بار به جای ساخت کانکشن جدید، یک کانکشن آماده از استخر دریافت می‌شود
        return dataSource.getConnection();
    }

    private String getRequiredProperty(String key) {
        String value = properties.getProperty(key);

        if (value == null || value.isBlank()) {
            throw new IllegalStateException(
                    "Missing required configuration property: " + key
            );
        }

        return value;
    }


    @Override
    public void close() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
        }
    }
}