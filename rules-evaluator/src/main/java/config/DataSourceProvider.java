package config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Objects;
import java.util.Properties;

public class DataSourceProvider {

    private final Properties properties;

    public DataSourceProvider(Properties properties) {
        this.properties = Objects.requireNonNull(properties, "properties cannot be null");
    }

    public Connection getConnection() throws SQLException {

        String host = getRequiredProperty("postgres.host");
        String port = getRequiredProperty("postgres.port");
        String database = getRequiredProperty("postgres.database");
        String username = getRequiredProperty("postgres.username");
        String password = getRequiredProperty("postgres.password");

        String url = String.format(
                "jdbc:postgresql://%s:%s/%s",
                host,
                port,
                database
        );

        return DriverManager.getConnection(
                url,
                username,
                password
        );
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
}