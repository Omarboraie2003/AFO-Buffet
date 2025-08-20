package org.example.model.user;

import java.sql.*;
import java.util.*;

import org.example.util.DBConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;

public class MenuDAO {
    private static final String URL = "jdbc:sqlserver://localhost:1433;databaseName=BuffetDB;encrypt=true;trustServerCertificate=true";
    private static final String USER = "sa";
    private static final String PASSWORD = "StrongPassword123!";
    private Connection conn;


    // Constructor opens the connection
    public MenuDAO() {
        try {
            conn = DriverManager.getConnection(URL, USER, PASSWORD);
            System.out.println("✅ Database connected successfully!");
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("❌ Failed to connect to database!");
        }
    }

    // Add new item
    public void addMenuItem(MenuItem item) throws SQLException {
        String sql = "INSERT INTO MenuItems (name, description, price, available, type, category) VALUES (?, ?, ?, ?, ?, ?)";
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setString(1, item.getName());
        ps.setString(2, item.getDescription());
        ps.setDouble(3, item.getPrice());
        ps.setBoolean(4, item.isAvailable());
        ps.setString(5, item.getType());
        ps.setString(6, item.getCategory());
        ps.executeUpdate();
    }

    // Get all available items (for employees)
    public List<MenuItem> getAvailableMenuItems() throws SQLException {
        List<MenuItem> items = new ArrayList<>();
        String sql = "SELECT * FROM MenuItems WHERE available = 1";
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery(sql);
        while (rs.next()) {
            items.add(new MenuItem(
                    rs.getInt("id"),
                    rs.getString("name"),
                    rs.getString("description"),
                    rs.getDouble("price"),
                    rs.getBoolean("available"),
                    rs.getString("type"),
                    rs.getString("category")
            ));
        }
        return items;
    }

    // Get all items (for chef)
    public List<MenuItem> getAllMenuItems() throws SQLException {
        List<MenuItem> items = new ArrayList<>();
        String sql = "SELECT * FROM MenuItems";
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery(sql);
        while (rs.next()) {
            items.add(new MenuItem(
                    rs.getInt("id"),
                    rs.getString("name"),
                    rs.getString("description"),
                    rs.getDouble("price"),
                    rs.getBoolean("available"),
                    rs.getString("type"),
                    rs.getString("category")
            ));
        }
        return items;
    }

    // Update item
    public void updateMenuItem(MenuItem item) throws SQLException {
        String sql = "UPDATE MenuItems SET name=?, description=?, price=?, available=?, type=?, category=? WHERE id=?";
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setString(1, item.getName());
        ps.setString(2, item.getDescription());
        ps.setDouble(3, item.getPrice());
        ps.setBoolean(4, item.isAvailable());
        ps.setString(5, item.getType());
        ps.setString(6, item.getCategory());
        ps.setInt(7, item.getId());
        ps.executeUpdate();
    }

    // Delete item
    public void deleteMenuItem(int id) throws SQLException {
        String sql = "DELETE FROM MenuItems WHERE id=?";
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setInt(1, id);
        ps.executeUpdate();
    }
}
