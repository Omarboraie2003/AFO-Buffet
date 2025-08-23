package org.example.model.MenuItem;

import java.sql.*;
import java.util.*;
import org.example.util.DBConnection;

public class MenuDAO {

    // Add new item
    public boolean addMenuItem(MenuItem item) {
        String sql = "INSERT INTO MenuItems (name, description, price, category, available, type) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, item.getName());
            stmt.setString(2, item.getDescription());
            stmt.setDouble(3, item.getPrice());
            stmt.setString(4, item.getCategory());
            stmt.setBoolean(5, item.isAvailable());
            stmt.setString(6, item.getType());

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Get single item by ID
    public MenuItem getMenuItemById(int id) throws SQLException {
        String sql = "SELECT * FROM MenuItems WHERE id=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRowToMenuItem(rs);
                }
            }
        }
        return null;
    }

    // Get all available items (for employees)
    public List<MenuItem> getAvailableMenuItems() throws SQLException {
        List<MenuItem> items = new ArrayList<>();
        String sql = "SELECT * FROM MenuItems WHERE available = 1";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                items.add(mapRowToMenuItem(rs));
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
                items.add(mapRowToMenuItem(rs));
            }
        }
        return items;
    }

    // Update item
    public boolean updateMenuItem(MenuItem item) throws SQLException {
        String sql = "UPDATE MenuItems SET name=?, description=?, price=?, available=?, type=?, category=? WHERE id=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, item.getName());
            ps.setString(2, item.getDescription());
            ps.setDouble(3, item.getPrice());
            ps.setBoolean(4, item.isAvailable());
            ps.setString(5, item.getType());
            ps.setString(6, item.getCategory());
            ps.setInt(7, item.getId());
            return ps.executeUpdate() > 0;
        }
    }

    // Delete item
    public boolean deleteMenuItem(int id) throws SQLException {
        String sql = "DELETE FROM MenuItems WHERE id=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    // Helper method to map ResultSet row → MenuItem object
    private MenuItem mapRowToMenuItem(ResultSet rs) throws SQLException {
        return new MenuItem(
                rs.getInt("id"),
                rs.getString("name"),
                rs.getString("description"),
                rs.getDouble("price"),
                rs.getBoolean("available"),
                rs.getString("type"),
                rs.getString("category")
        );
    }

    // Get all items by category
    public List<MenuItem> getMenuItemsByCategory(String category) throws SQLException {
        List<MenuItem> items = new ArrayList<>();
        String sql = "SELECT * FROM MenuItems WHERE category = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, category);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    items.add(mapRowToMenuItem(rs));
                }
            }
        }
        return items;
    }

    // Get items by category and availability
    public List<MenuItem> getMenuItemsByCategoryAndAvailability(String category, boolean available) throws SQLException {
        List<MenuItem> items = new ArrayList<>();
        String sql = "SELECT * FROM MenuItems WHERE category = ? AND available = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, category);
            ps.setBoolean(2, available);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    items.add(mapRowToMenuItem(rs));
                }
            }
        }
        return items;
    }

    public List<MenuItem> getUnavailableMenuItems() throws SQLException {
        List<MenuItem> items = new ArrayList<>();
        String sql = "SELECT * FROM MenuItems WHERE available = 0";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                items.add(mapRowToMenuItem(rs));
            }
        }
        return items;
    }

    public List<MenuItem> getUnavailableMenuItemsByCategory(String category) throws SQLException {
        List<MenuItem> items = new ArrayList<>();
        String sql = "SELECT * FROM MenuItems WHERE category = ? AND available = 0";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, category);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    items.add(mapRowToMenuItem(rs));
                }
            }
        }
        return items;
    }

}
