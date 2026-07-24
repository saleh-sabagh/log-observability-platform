package config;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.Statement;
import java.util.Objects;
import java.util.stream.Collectors;

public final class DatabaseInitializer {

    private static final String SCHEMA_FILE = "database/schema.sql";

    private DatabaseInitializer() {
    }

    public static void initialize(DataSourceProvider dataSourceProvider) {

        Objects.requireNonNull(
                dataSourceProvider,
                "dataSourceProvider cannot be null"
        );

        try (
                InputStream inputStream =
                        DatabaseInitializer.class
                                .getClassLoader()
                                .getResourceAsStream(SCHEMA_FILE)
        ) {

            if (inputStream == null) {
                throw new IllegalStateException(
                        "Schema file not found: " + SCHEMA_FILE
                );
            }

            String sql = new BufferedReader(
                    new InputStreamReader(
                            inputStream,
                            StandardCharsets.UTF_8
                    )
            )
                    .lines()
                    .collect(Collectors.joining("\n"));

            try (
                    Connection connection = dataSourceProvider.getConnection();
                    Statement statement = connection.createStatement()
            ) {

                statement.execute(sql);
            }

        } catch (Exception e) {
            throw new RuntimeException(
                    "Failed to initialize database.",
                    e
            );
        }
    }
}