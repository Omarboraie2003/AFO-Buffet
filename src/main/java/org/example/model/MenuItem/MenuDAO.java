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
    // Add new menu item
    public boolean addMenuItem(MenuItem item) {
        String sql = "INSERT INTO MenuItems (item_name, item_description, is_available, item_type, item_category, is_special, photo_url) VALUES (?, ?, ?, ?, ?, ?, ?)";

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
    public List<MenuItem> getAvailableMenuItems() {
        List<MenuItem> items = new ArrayList<>();
        String sql = "SELECT item_id, item_name, item_description, is_available, item_type, item_category, is_special, photo_url FROM MenuItems WHERE is_available = 1";

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
    // Get all available items (for employees)
    public List<MenuItem> getAvailableMenuItemsE() throws SQLException {
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
    public List<MenuItem> getAllMenuItems() {
        List<MenuItem> items = new ArrayList<>();
        String sql = "SELECT item_id, item_name, item_description, is_available, item_type, item_category, is_special, photo_url FROM MenuItems";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            ResultSet rs = stmt.executeQuery();
            System.out.println("🔍 DEBUG: Starting getAllMenuItems() - Total items found: ");
            int itemCount = 0;

            while (rs.next()) {
                itemCount++;
                System.out.println("=== DEBUG: Processing item #" + itemCount + " ===");
                System.out.println("Item ID: " + rs.getInt("item_id"));
                System.out.println("Item Name: " + rs.getString("item_name"));
                System.out.println("Raw is_available value: " + rs.getObject("is_available"));
                System.out.println("Raw is_available type: " + rs.getObject("is_available").getClass().getName());
                System.out.println("getBoolean result: " + rs.getBoolean("is_available"));
                System.out.println("getInt result: " + rs.getInt("is_available"));
                System.out.println("getString result: " + rs.getString("is_available"));

                MenuItem item = mapRowToMenuItem(rs);
                System.out.println("Final MenuItem isAvailable: " + item.isAvailable());
                System.out.println("================================");

                items.add(item);
            }
            System.out.println("🔍 DEBUG: getAllMenuItems() completed - Total items processed: " + itemCount);
        } catch (SQLException e) {
            System.err.println("❌ ERROR in getAllMenuItems(): " + e.getMessage());
            e.printStackTrace();
        }
        return items;
    }
    // Get all available items (for employees)
    public List<MenuItem> getAllMenuItemsE() throws SQLException {
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

    public MenuItem getTodaysSpecial() {
        String sql = "SELECT item_id, item_name, item_description, is_available, item_type, item_category, is_special, photo_url FROM MenuItems WHERE is_special = 1";
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
    public MenuItem getTodaysSpecialE() throws SQLException {
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
    public List<MenuItem> getMenuItemsByTypeAndAvailability(String type, boolean available) {
        List<MenuItem> items = new ArrayList<>();
        String sql = "SELECT item_id, item_name, item_description, is_available, item_type, item_category, is_special, photo_url FROM MenuItems WHERE item_type = ? AND is_available = ?";

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
        String sql = "SELECT item_id, item_name, item_description, is_available, item_type, item_category, is_special, photo_url FROM MenuItems WHERE item_type = ?";

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
        String sql = "SELECT item_id, item_name, item_description, is_available, item_type, item_category, is_special, photo_url FROM MenuItems WHERE item_category = ?";

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
        String sql = "SELECT item_id, item_name, item_description, is_available, item_type, item_category, is_special, photo_url FROM MenuItems WHERE item_category = ? AND is_available = ?";

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
    public boolean isItemNameExists(String name) throws SQLException {
        String sql = "SELECT COUNT(*) FROM MenuItems WHERE item_name = ?";

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
        String setSql = "UPDATE MenuItems SET is_special = 1 WHERE item_id = ?";

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
        String sql = "SELECT item_id, item_name, item_description, is_available, item_type, item_category, is_special, photo_url FROM MenuItems WHERE item_id = ?";

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
    public List<MenuItem> searchMenuItemsWithAvailability(String searchTerm, boolean available) {
        List<MenuItem> items = new ArrayList<>();
        String sql = "SELECT item_id, item_name, item_description, is_available, item_type, item_category, is_special, photo_url FROM MenuItems " +
                "WHERE (LOWER(item_name) LIKE ? OR LOWER(item_description) LIKE ? OR LOWER(item_category) LIKE ?) AND is_available = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            String searchPattern = "%" + searchTerm.toLowerCase() + "%";
            stmt.setString(1, searchPattern);
            stmt.setString(2, searchPattern);
            stmt.setString(3, searchPattern);
            stmt.setBoolean(4, available);

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                items.add(mapRowToMenuItem(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return items;
    }

    // update omar menu
    public boolean updateMenuItem(MenuItem item) {
        String sql = "UPDATE MenuItems SET item_name = ?, item_description = ?, is_available = ?, item_type = ?, item_category = ?, is_special = ?, photo_url = ? WHERE item_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            System.out.println("🔍 DEBUG updateMenuItem - Item ID: " + item.getId());
            System.out.println("🔍 DEBUG updateMenuItem - Item Name: " + item.getName());
            System.out.println("🔍 DEBUG updateMenuItem - Setting is_available to: " + item.isAvailable());

            stmt.setString(1, item.getName());
            stmt.setString(2, item.getDescription());
            stmt.setBoolean(3, item.isAvailable());
            stmt.setString(4, item.getType());
            stmt.setString(5, item.getCategory());
            stmt.setBoolean(6, item.isSpecial());
            stmt.setString(7, item.getPhotoUrl());
            stmt.setInt(8, item.getId());

            int rowsUpdated = stmt.executeUpdate();
            System.out.println("🔍 DEBUG updateMenuItem - Rows updated: " + rowsUpdated);

            return rowsUpdated > 0;
        } catch (SQLException e) {
            System.err.println("❌ ERROR in updateMenuItem(): " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    // delete
    public boolean deleteMenuItem(int id) {
        String sql = "DELETE FROM MenuItems WHERE item_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private MenuItem mapRowToMenuItem(ResultSet rs) throws SQLException {
        MenuItem item = new MenuItem();
        item.setId(rs.getInt("item_id"));
        item.setName(rs.getString("item_name"));
        item.setDescription(rs.getString("item_description"));

        Object rawAvailability = rs.getObject("is_available");
        boolean availabilityValue = rs.getBoolean("is_available");
        System.out.println("🔍 DEBUG mapRowToMenuItem - Raw DB value: " + rawAvailability + " (type: " + rawAvailability.getClass().getName() + ")");
        System.out.println("🔍 DEBUG mapRowToMenuItem - Converted to boolean: " + availabilityValue);

        item.setAvailable(availabilityValue);

        item.setType(rs.getString("item_type"));
        item.setCategory(rs.getString("item_category"));
        item.setSpecial(rs.getBoolean("is_special"));
        item.setPhotoUrl(rs.getString("photo_url"));

        System.out.println("🔍 DEBUG mapRowToMenuItem - Final item.isAvailable(): " + item.isAvailable());

        return item;
    }
}
