package org.example;

import org.example.util.DBConnection;

import java.sql.Connection;

public class Main {
    public static void main(String[] args) {
        testDatabaseConnection();
    }

    public static void testDatabaseConnection() {
        try (Connection conn = DBConnection.getConnection()) {
            if (conn != null && !conn.isClosed()) {
                System.out.println("✅ Connection to BuffetDB was successful!");
            } else {
                System.out.println("❌ Connection failed.");
            }
        } catch (Exception e) {
            System.out.println("❌ Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

}


