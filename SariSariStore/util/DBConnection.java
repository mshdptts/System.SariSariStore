package SariSariStore.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;


public class DBConnection {
    private static final String SERVER_URL =
        "jdbc:mysql://localhost:3306/?useSSL=false&serverTimezone=Asia/Manila";
    private static final String URL =
        "jdbc:mysql://localhost:3306/sarisari_store?useSSL=false&serverTimezone=Asia/Manila";
    private static final String USER = "root";
    private static final String PASSWORD = ""; // Laragon default

    private DBConnection() {}

    public static Connection getServerConnection() throws SQLException {
        return DriverManager.getConnection(SERVER_URL, USER, PASSWORD);
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}