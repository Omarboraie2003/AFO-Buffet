package org.example.model.user;

import java.sql.*;
import java.util.*;

import org.example.util.DBConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;

public class MenuDAO {

    // Constructor - no longer manages connection directly
    public MenuDAO() {
        try (Connection testConn = DBConnection.getConnection()) {
            System.out.println("✅ MenuDAO initialized - Database connection test successful!");
        } catch (SQLException e) {
            System.err.println("❌ MenuDAO initialization failed - Database connection test failed!");
            e.printStackTrace();
        }
    }

    // Add new item
    public void addMenuItem(MenuItem item) throws SQLException {
        String sql = "INSERT INTO MenuItems (name, description, price, available, type, category, image) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, item.getName());
            ps.setString(2, item.getDescription());
            ps.setDouble(3, item.getPrice());
            ps.setBoolean(4, item.isAvailable());
            ps.setString(5, item.getType());
            ps.setString(6, item.getCategory());
            ps.setString(7, item.getImage());
            ps.executeUpdate();
        }
    }

    // Get all available items (for employees)
    public List<MenuItem> getAvailableMenuItems() throws SQLException {
        List<MenuItem> items = new ArrayList<>();
        String sql = "SELECT * FROM MenuItems WHERE available = 1";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                items.add(new MenuItem(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("description"),
                        rs.getDouble("price"),
                        rs.getBoolean("available"),
                        rs.getString("type"),
                        rs.getString("category"),
                        rs.getString("image")
                ));
            }
        }
        return items;
    }

    // Get all items (for chef)
    public List<MenuItem> getAllMenuItems() throws SQLException {
        List<MenuItem> items = new ArrayList<>();
        String sql = "SELECT * FROM MenuItems";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                items.add(new MenuItem(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("description"),
                        rs.getDouble("price"),
                        rs.getBoolean("available"),
                        rs.getString("type"),
                        rs.getString("category"),
                        rs.getString("image")
                ));
            }
        }
        return items;
    }

    // Update item
    public void updateMenuItem(MenuItem item) throws SQLException {
        String sql = "UPDATE MenuItems SET name=?, description=?, price=?, available=?, type=?, category=?, image=? WHERE id=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, item.getName());
            ps.setString(2, item.getDescription());
            ps.setDouble(3, item.getPrice());
            ps.setBoolean(4, item.isAvailable());
            ps.setString(5, item.getType());
            ps.setString(6, item.getCategory());
            ps.setString(7, item.getImage());
            ps.setInt(8, item.getId());
            ps.executeUpdate();
        }
    }

    // Delete item
    public void deleteMenuItem(int id) throws SQLException {
        String sql = "DELETE FROM MenuItems WHERE id=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    // Get menu items by category
    public List<MenuItem> getMenuItemsByCategory(String category) throws SQLException {
        List<MenuItem> items = new ArrayList<>();
        String sql = "SELECT * FROM MenuItems WHERE available = 1 AND LOWER(category) = LOWER(?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, category);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    items.add(new MenuItem(
                            rs.getInt("id"),
                            rs.getString("name"),
                            rs.getString("description"),
                            rs.getDouble("price"),
                            rs.getBoolean("available"),
                            rs.getString("type"),
                            rs.getString("category"),
                            rs.getString("image")
                    ));
                }
            }
        } catch (SQLException e) {
            System.err.println("Database error in getMenuItemsByCategory: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }

        return items;
    }
}
