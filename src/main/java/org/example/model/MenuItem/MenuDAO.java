package org.example.model.MenuItem;
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
        String sql = "INSERT INTO MenuItems (item_name, item_description, is_available, item_type, item_category, photo_url, is_special) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, item.getName());
            ps.setString(2, item.getDescription());
            ps.setBoolean(4, item.isAvailable());
            ps.setString(5, item.getType());
            ps.setString(6, item.getCategory());
            ps.setString(7, item.getPhotoUrl());
            ps.setBoolean(8, item.isSpecial());
            ps.executeUpdate();
        }
    }

    // Get all available items (for employees)
    public List<MenuItem> getAvailableMenuItems() throws SQLException {
        List<MenuItem> items = new ArrayList<>();
        String sql = "SELECT * FROM MenuItems WHERE is_available = 1";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                items.add(new MenuItem(
                        rs.getInt("item_id"),
                        rs.getString("item_name"),
                        rs.getString("item_description"),
                        rs.getBoolean("is_available"),
                        rs.getString("item_type"),
                        rs.getString("item_category"),
                        rs.getString("photo_url"),
                        rs.getBoolean("is_special")
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
                        rs.getInt("item_id"),
                        rs.getString("item_name"),
                        rs.getString("item_description"),
                        rs.getBoolean("is_available"),
                        rs.getString("item_type"),
                        rs.getString("item_category"),
                        rs.getString("photo_url"),
                        rs.getBoolean("is_special")
                ));
            }
        }
        return items;
    }

    // Update item
    public void updateMenuItem(MenuItem item) throws SQLException {
        String sql = "UPDATE MenuItems SET item_name=?, item_description=?, is_available=?, item_type=?, item_category=?, photo_url=?, is_special=? WHERE item_id=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, item.getName());
            ps.setString(2, item.getDescription());
            ps.setBoolean(4, item.isAvailable());
            ps.setString(5, item.getType());
            ps.setString(6, item.getCategory());
            ps.setString(7, item.getPhotoUrl());
            ps.setBoolean(8, item.isSpecial());
            ps.setInt(9, item.getId());
            ps.executeUpdate();
        }
    }

    // Delete item
    public void deleteMenuItem(int item_id) throws SQLException {
        String sql = "DELETE FROM MenuItems WHERE item_id=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, item_id);
            ps.executeUpdate();
        }
    }

    // Get menu items by category
    public List<MenuItem> getMenuItemsByCategoryE(String item_category) throws SQLException {
        List<MenuItem> items = new ArrayList<>();
        String sql = "SELECT * FROM MenuItems WHERE is_available = 1 AND LOWER(item_category) = LOWER(?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, item_category);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    items.add(new MenuItem(
                            rs.getInt("item_id"),
                            rs.getString("item_name"),
                            rs.getString("item_description"),
                            rs.getBoolean("is_available"),
                            rs.getString("item_type"),
                            rs.getString("item_category"),
                            rs.getString("photo_url"),
                            rs.getBoolean("is_special")
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


    public MenuItem getTodaysSpecial() throws SQLException {
        String sql = "SELECT * FROM MenuItems WHERE is_special = 1 AND is_available = 1";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                return new MenuItem(
                        rs.getInt("item_id"),
                        rs.getString("item_name"),
                        rs.getString("item_description"),
                        rs.getBoolean("is_available"),
                        rs.getString("item_type"),
                        rs.getString("item_category"),
                        rs.getString("photo_url"),
                        rs.getBoolean("is_special")
                );
            }
        } catch (SQLException e) {
            System.err.println("Database error in getTodaysSpecial: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }

        return null; // No special dish found
    }



    // Get menu items by type and availability
    public List<MenuItem> getMenuItemsByTypeAndAvailability(String item_type, boolean is_available) {
        List<MenuItem> items = new ArrayList<>();
        String sql = "SELECT item_id, item_name, item_description, is_available, item_type, item_category, is_special, photo_url FROM MenuItems WHERE item_type = ? AND is_available = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, item_type);
            stmt.setBoolean(2, is_available);
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
    public List<MenuItem> getMenuItemsByType(String item_type) {
        List<MenuItem> items = new ArrayList<>();
        String sql = "SELECT item_id, item_name, item_description, is_available, item_type, item_category, is_special, photo_url FROM MenuItems WHERE item_type = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, item_type);
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
    public List<MenuItem> getMenuItemsByCategory(String item_category) {
        List<MenuItem> items = new ArrayList<>();
        String sql = "SELECT item_id, item_name, item_description, is_available, item_type, item_category, is_special, photo_url FROM MenuItems WHERE item_category = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, item_category);
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
    public List<MenuItem> getMenuItemsByCategoryAndAvailability(String item_category, boolean is_available) {
        List<MenuItem> items = new ArrayList<>();
        String sql = "SELECT item_id, item_name, item_description, is_available, item_type, item_category, is_special, photo_url FROM MenuItems WHERE item_category = ? AND is_available = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, item_category);
            stmt.setBoolean(2, is_available);
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
    public List<MenuItem> getUnavailableMenuItemsByCategory(String item_category) {
        return getMenuItemsByCategoryAndAvailability(item_category, false);
    }

    // Get all unavailable menu items
    public List<MenuItem> getUnavailableMenuItems() {
        List<MenuItem> items = new ArrayList<>();
        String sql = "SELECT item_id, item_name, item_description, is_available, item_type, item_category, is_special, photo_url FROM MenuItems WHERE is_available = 0";

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
    public boolean isItemNameExists(String item_name) throws SQLException {
        String sql = "SELECT COUNT(*) FROM MenuItems WHERE item_name = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, item_name);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        }
        return false;
    }

    // Set today's special
    public boolean setTodaysSpecial(int item_id) {
        String clearSql = "UPDATE MenuItems SET is_special = 0";
        String setSql = "UPDATE MenuItems SET is_special = 1 WHERE item_id = ?";

        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);

            // Clear all existing specials
            try (PreparedStatement clearStmt = conn.prepareStatement(clearSql)) {
                clearStmt.executeUpdate();
            }

            // Set new special
            try (PreparedStatement setStmt = conn.prepareStatement(setSql)) {
                setStmt.setInt(1, item_id);
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

    public MenuItem getMenuItemById(int item_id) {
        String sql = "SELECT item_id, item_name, item_description, is_available, item_type, item_category, is_special, photo_url FROM MenuItems WHERE item_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, item_id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return mapRowToMenuItem(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // Search menu items by name, description, or category
    public List<MenuItem> searchMenuItems(String searchTerm) {
        List<MenuItem> items = new ArrayList<>();
        String sql = "SELECT item_id, item_name, item_description, is_available, item_type, item_category, is_special, photo_url FROM MenuItems " +
                "WHERE LOWER(item_name) LIKE ? OR LOWER(item_description) LIKE ? OR LOWER(item_category) LIKE ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            String searchPattern = "%" + searchTerm.toLowerCase() + "%";
            stmt.setString(1, searchPattern);
            stmt.setString(2, searchPattern);
            stmt.setString(3, searchPattern);

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                items.add(mapRowToMenuItem(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return items;
    }

    // Search menu items with availability filter
    public List<MenuItem> searchMenuItemsWithAvailability(String searchTerm, boolean is_available) {
        List<MenuItem> items = new ArrayList<>();
        String sql = "SELECT item_id, item_name, item_description, is_available, item_type, item_category, is_special, photo_url FROM MenuItems " +
                "WHERE (LOWER(item_name) LIKE ? OR LOWER(item_description) LIKE ? OR LOWER(item_category) LIKE ?) AND is_available = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            String searchPattern = "%" + searchTerm.toLowerCase() + "%";
            stmt.setString(1, searchPattern);
            stmt.setString(2, searchPattern);
            stmt.setString(3, searchPattern);
            stmt.setBoolean(4, is_available);

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                items.add(mapRowToMenuItem(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return items;
    }

    private MenuItem mapRowToMenuItem(ResultSet rs) throws SQLException {
        MenuItem item = new MenuItem();
        item.setId(rs.getInt("item_id"));
        item.setName(rs.getString("item_name"));
        item.setDescription(rs.getString("item_description"));
        item.setAvailable(rs.getBoolean("is_available"));
        item.setType(rs.getString("item_type"));
        item.setCategory(rs.getString("item_category"));
        item.setSpecial(rs.getBoolean("is_special"));
        item.setPhotoUrl(rs.getString("photo_url"));
        return item;
    }

}
