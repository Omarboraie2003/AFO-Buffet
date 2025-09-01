package org.example.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {

    // Updated JDBC URL with SSL disabled and trust certificate enabled for local dev
    private static final String URL = "jdbc:sqlserver://localhost:1433;"
            + "databaseName=BuffetDB;"
            + "encrypt=false;"
            + "trustServerCertificate=true";

    private static final String USER = "sa";
    private static final String PASSWORD = "MySecurePass123";

    public static Connection getConnection() {
        try {
            // Explicitly load the driver to avoid Tomcat classloader issues
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
            return DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("SQL Server JDBC Driver not found. Did you put the JAR in Tomcat's lib folder?", e);
        } catch (SQLException e) {
            throw new RuntimeException("Error connecting to the database", e);
        }
    }

    public static void closeConnection(Connection conn) {
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException e) {
                System.out.println("Error closing connection: " + e.getMessage());
            }
        }
    }
}
