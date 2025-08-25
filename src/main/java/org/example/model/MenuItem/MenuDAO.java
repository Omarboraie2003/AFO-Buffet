package org.example.model.MenuItem;

import org.example.model.MenuItem.MenuItem;
import org.example.util.DBConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MenuDAO {

    // Get today's special menu item
    public MenuItem getTodaysSpecial() {
        String sql = "SELECT id, name, description, available, type, category, is_special, photo_url FROM MenuItems WHERE is_special = 1";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return mapRowToMenuItem(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // Get all available menu items
    public List<MenuItem> getAvailableMenuItems() {
        List<MenuItem> items = new ArrayList<>();
        String sql = "SELECT id, name, description, available, type, category, is_special, photo_url FROM MenuItems WHERE available = 1";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                items.add(mapRowToMenuItem(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return items;
    }

    // Get all menu items
    public List<MenuItem> getAllMenuItems() {
        List<MenuItem> items = new ArrayList<>();
        String sql = "SELECT id, name, description, available, type, category, is_special, photo_url FROM MenuItems";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                items.add(mapRowToMenuItem(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return items;
    }

    // Add new menu item
    public boolean addMenuItem(MenuItem item) {
        String sql = "INSERT INTO MenuItems (name, description, available, type, category, is_special, photo_url) VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, item.getName());
            stmt.setString(2, item.getDescription());
            stmt.setBoolean(3, item.isAvailable());
            stmt.setString(4, item.getType());
            stmt.setString(5, item.getCategory());
            stmt.setBoolean(6, item.isSpecial());
            stmt.setString(7, item.getPhotoUrl());

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Update menu item
    public boolean updateMenuItem(MenuItem item) {
        String sql = "UPDATE MenuItems SET name = ?, description = ?, available = ?, type = ?, category = ?, is_special = ?, photo_url = ? WHERE id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, item.getName());
            stmt.setString(2, item.getDescription());
            stmt.setBoolean(3, item.isAvailable());
            stmt.setString(4, item.getType());
            stmt.setString(5, item.getCategory());
            stmt.setBoolean(6, item.isSpecial());
            stmt.setString(7, item.getPhotoUrl());
            stmt.setInt(8, item.getId());

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Delete menu item
    public boolean deleteMenuItem(int id) {
        String sql = "DELETE FROM MenuItems WHERE id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Get menu items by type and availability
    public List<MenuItem> getMenuItemsByTypeAndAvailability(String type, boolean available) {
        List<MenuItem> items = new ArrayList<>();
        String sql = "SELECT id, name, description, available, type, category, is_special, photo_url FROM MenuItems WHERE type = ? AND available = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, type);
            stmt.setBoolean(2, available);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                items.add(mapRowToMenuItem(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return items;
    }

    // Get menu items by type
    public List<MenuItem> getMenuItemsByType(String type) {
        List<MenuItem> items = new ArrayList<>();
        String sql = "SELECT id, name, description, available, type, category, is_special, photo_url FROM MenuItems WHERE type = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, type);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                items.add(mapRowToMenuItem(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return items;
    }

    // Get menu items by category
    public List<MenuItem> getMenuItemsByCategory(String category) {
        List<MenuItem> items = new ArrayList<>();
        String sql = "SELECT id, name, description, available, type, category, is_special, photo_url FROM MenuItems WHERE category = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, category);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                items.add(mapRowToMenuItem(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return items;
    }

    // Get menu items by category and availability
    public List<MenuItem> getMenuItemsByCategoryAndAvailability(String category, boolean available) {
        List<MenuItem> items = new ArrayList<>();
        String sql = "SELECT id, name, description, available, type, category, is_special, photo_url FROM MenuItems WHERE category = ? AND available = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, category);
            stmt.setBoolean(2, available);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                items.add(mapRowToMenuItem(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return items;
    }

    // Get unavailable menu items by category
    public List<MenuItem> getUnavailableMenuItemsByCategory(String category) {
        return getMenuItemsByCategoryAndAvailability(category, false);
    }

    // Get all unavailable menu items
    public List<MenuItem> getUnavailableMenuItems() {
        List<MenuItem> items = new ArrayList<>();
        String sql = "SELECT id, name, description, available, type, category, is_special, photo_url FROM MenuItems WHERE available = 0";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                items.add(mapRowToMenuItem(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return items;
    }

    // Check if item name already exists
    public boolean isItemNameExists(String name) throws SQLException {
        String sql = "SELECT COUNT(*) FROM MenuItems WHERE name = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, name);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        }
        return false;
    }

    // Set today's special
    public boolean setTodaysSpecial(int id) {
        String clearSql = "UPDATE MenuItems SET is_special = 0";
        String setSql = "UPDATE MenuItems SET is_special = 1 WHERE id = ?";

        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);

            // Clear all existing specials
            try (PreparedStatement clearStmt = conn.prepareStatement(clearSql)) {
                clearStmt.executeUpdate();
            }

            // Set new special
            try (PreparedStatement setStmt = conn.prepareStatement(setSql)) {
                setStmt.setInt(1, id);
                int updated = setStmt.executeUpdate();

                if (updated > 0) {
                    conn.commit();
                    return true;
                } else {
                    conn.rollback();
                    return false;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Clear today's special
    public boolean clearTodaysSpecial() {
        String sql = "UPDATE MenuItems SET is_special = 0";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            return stmt.executeUpdate() >= 0; // Returns true even if no rows were updated
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public MenuItem getMenuItemById(int id) {
        String sql = "SELECT id, name, description, available, type, category, is_special, photo_url FROM MenuItems WHERE id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return mapRowToMenuItem(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    private MenuItem mapRowToMenuItem(ResultSet rs) throws SQLException {
        MenuItem item = new MenuItem();
        item.setId(rs.getInt("id"));
        item.setName(rs.getString("name"));
        item.setDescription(rs.getString("description"));
        item.setAvailable(rs.getBoolean("available"));
        item.setType(rs.getString("type"));
        item.setCategory(rs.getString("category"));
        item.setSpecial(rs.getBoolean("is_special"));
        item.setPhotoUrl(rs.getString("photo_url"));
        return item;
    }
}
