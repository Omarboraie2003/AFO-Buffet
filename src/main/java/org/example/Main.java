package org.example;

import org.example.model.MenuItem.MenuDAO;
import org.example.model.MenuItem.MenuItem;
import org.example.util.DBConnection;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public class Main {
    public static void main(String[] args) throws SQLException {
        testDatabaseConnection();

        MenuDAO menuDAO = new MenuDAO();
        List<MenuItem> menuItems=menuDAO.getAllMenuItems();
        System.out.println(menuItems.toString());

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




