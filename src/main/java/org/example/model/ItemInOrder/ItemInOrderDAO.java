package org.example.model.ItemInOrder;

import org.example.util.DBConnection;

import java.sql.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.time.LocalDateTime;
import java.util.ArrayList;

public class ItemInOrderDAO {
    //todo: delete
    //done: getById, add, getByOrderId, update,

    public static int createItemInOrder(int order_id, int item_id) {
        String sql = "INSERT INTO ItemsInOrder (order_id, item_id) VALUES (?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, order_id);
            ps.setInt(2, item_id);
            ps.executeUpdate();
            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) {
                return rs.getInt(1); // Return the generated item_in_order ID
            }
        } catch (SQLException e) {e.printStackTrace();}
        return -1; // Indicate failure
    }
    public static int createItemInOrder(int order_id, int item_id, String type_of_bread, String quantity, String note) {
        String sql = "INSERT INTO ItemsInOrder (order_id, item_id, type_of_bread, quantity, item_note) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, order_id);
            ps.setInt(2, item_id);
            ps.setString(3, type_of_bread);
            ps.setString(4, quantity);
            ps.setString(5, note);
            ps.executeUpdate();
            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) {
                return rs.getInt(1); // Return the generated item_in_order ID
            }
        } catch (SQLException e) {e.printStackTrace();}
        return -1; // Indicate failure
    }

    public static ItemInOrderModel getItemInOrderById(int item_in_order_id) {
        String sql = "SELECT * FROM ItemsInOrder WHERE item_in_order_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, item_in_order_id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) { return new ItemInOrderModel(
                    rs.getInt("item_in_order_id"),
                    rs.getInt("item_id"),
                    rs.getString("type_of_bread"),
                    rs.getString("quantity"),
                    rs.getString("item_note"));
            }
        } catch (SQLException e) {e.printStackTrace();}
        return null;
    }

    public static ArrayList<Integer> getItemsInOrder(int order_id) {
        String sql = "SELECT item_in_order_id FROM ItemsInOrder WHERE order_id = ?";
        ArrayList<Integer> item_ids = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, order_id);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                    rs.getInt("1");
            }
        } catch (SQLException e) {e.printStackTrace();}
        return item_ids;
    }
    public static ArrayList<ItemInOrderModel> getItemInOrderModels(int order_id) {
        String sql = "SELECT * FROM ItemsInOrder WHERE order_id = ?";
        ArrayList<ItemInOrderModel> items = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, order_id);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                items.add(new ItemInOrderModel(
                        rs.getInt("item_in_order_id"),
                        rs.getInt("item_id"),
                        rs.getString("type_of_bread"),
                        rs.getString("quantity"),
                        rs.getString("item_note")));
            }
        } catch (SQLException e) {e.printStackTrace();}
        return items;
    }

    private static boolean updateString(ItemInOrderModel item_to_update, String parameter, String new_value) {
        String sql = "UPDATE ItemsInOrder SET " + parameter + " = ? WHERE item_in_order_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, new_value);
            ps.setInt(2, item_to_update.getItemInOrderId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {e.printStackTrace();}
        return false;
    }
    public static boolean updateTypeOfBread(ItemInOrderModel item_to_update, String new_type_of_bread) {
        return updateString(item_to_update, "type_of_bread", new_type_of_bread);
    }
    public static boolean updateQuantity(ItemInOrderModel item_to_update, String new_quantity) {
        return updateString(item_to_update, "quantity", new_quantity);
    }
    public static boolean updateNote(ItemInOrderModel item_to_update, String new_note) {
        return updateString(item_to_update, "item_note", new_note);
    }
    public static boolean updateItemInOrder(ItemInOrderModel item_to_update) {
        boolean success = true;
        success &= updateTypeOfBread(item_to_update, item_to_update.getTypeOfBread());
        success &= updateQuantity(item_to_update, item_to_update.getQuantity());
        success &= updateNote(item_to_update, item_to_update.getNote());
        return success;
    }

    public static boolean deleteItemInOrder(int item_in_order_id) {
        String sql = "DELETE FROM ItemsInOrder WHERE item_in_order_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, item_in_order_id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {e.printStackTrace();}
        return false;
    }

    public static boolean deleteAllItemsInOrder(int order_id) {
        String sql = "DELETE FROM ItemsInOrder WHERE order_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, order_id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {e.printStackTrace();}
        return false;
    }
}
