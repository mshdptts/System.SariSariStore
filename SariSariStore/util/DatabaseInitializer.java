package SariSariStore.util;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public final class DatabaseInitializer {
    private static final String SCHEMA_RESOURCE = "SariSariStore/schema.sql";

    private DatabaseInitializer() {}

    public static void initialize() {
        try {
            createDatabase();
            executeSchema(loadSchema());
            ensureSoldAtColumn();
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

    private static void ensureSoldAtColumn() throws SQLException {
        try (Connection connection = DBConnection.getConnection();
             ResultSet columns = connection.getMetaData().getColumns(null, null, "sales", "sold_at")) {
            if (columns.next()) {
                return;
            }
        }

        try (Connection connection = DBConnection.getConnection();
             Statement statement = connection.createStatement()) {
            try (ResultSet legacyColumn = connection.getMetaData()
                    .getColumns(null, null, "sales", "sale_date")) {
                if (legacyColumn.next()) {
                    statement.executeUpdate("ALTER TABLE sales CHANGE COLUMN sale_date sold_at DATETIME "
                            + "NOT NULL DEFAULT CURRENT_TIMESTAMP");
                    return;
                }
            }
            statement.executeUpdate("ALTER TABLE sales ADD COLUMN sold_at DATETIME "
                    + "NOT NULL DEFAULT CURRENT_TIMESTAMP");
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
