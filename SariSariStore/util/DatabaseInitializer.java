package SariSariStore.util;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public final class DatabaseInitializer {
    private static final String SCHEMA_RESOURCE = "SariSariStore/schema.sql";

    private DatabaseInitializer() {}

    public static void initialize() {
        try {
            createDatabase();
            executeSchema(loadSchema());
        } catch (IOException | SQLException e) {
            throw new IllegalStateException("Could not initialize the database: " + e.getMessage(), e);
        }
    }

    private static void createDatabase() throws SQLException {
        try (Connection connection = DBConnection.getServerConnection();
             Statement statement = connection.createStatement()) {
            statement.executeUpdate("CREATE DATABASE IF NOT EXISTS sarisari_store");
        }
    }

    private static void executeSchema(String schema) throws SQLException {
        try (Connection connection = DBConnection.getConnection();
             Statement statement = connection.createStatement()) {
            for (String sql : schema.split(";")) {
                String trimmedSql = sql.trim();
                if (!trimmedSql.isEmpty()) {
                    statement.executeUpdate(trimmedSql);
                }
            }
        }
    }

    private static String loadSchema() throws IOException {
        try (InputStream input = DatabaseInitializer.class.getClassLoader()
                .getResourceAsStream(SCHEMA_RESOURCE)) {
            if (input != null) {
                return new String(input.readAllBytes(), StandardCharsets.UTF_8);
            }
        }

        Path schemaPath = Paths.get("SariSariStore", "schema.sql");
        return Files.readString(schemaPath, StandardCharsets.UTF_8);
    }
}
