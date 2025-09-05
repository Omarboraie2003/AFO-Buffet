package org.example.model.Order;

import org.example.util.DBConnection;

import java.sql.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.time.LocalDateTime;

public class OrderDAO {
    public static OrderModel getOrderById(int order_id) {
        String sql = "SELECT * FROM Orders WHERE order_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, order_id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                int user_id = rs.getInt("user_id");
                Timestamp ts = rs.getTimestamp("order_date");
                String order_note = rs.getString("order_note");
                String order_status = rs.getString("order_status");
                return new OrderModel(order_id, user_id, ts != null ? ts.toLocalDateTime() : null, order_note, order_status);
            }
        } catch (SQLException e) {e.printStackTrace();}
        return null;
    }

    public static int createCart(int user_id) {
        String sql = "INSERT INTO Orders (user_id) VALUES (?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, user_id);
            ps.executeUpdate();
            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) {
                return rs.getInt(1); // Return the generated cart ID
            }
        } catch (SQLException e) {e.printStackTrace();}
        return -1; // Indicate failure
    }

    public static boolean updateCart(OrderModel order_to_update) {
        //todo: update items in order as well
        for (int item_in_order_id : order_to_update.getItemInOrderIds()) {
            System.out.println("Item in order ID: " + item_in_order_id);
        }

        String sql = "UPDATE Orders order_note = ?, order_status = ? WHERE order_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, order_to_update.getOrderNote());
            ps.setString(2, order_to_update.getOrderStatus());
            ps.setInt(3, order_to_update.getOrderId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {e.printStackTrace();}
        return false;
    }

    public static void confirmCart(int user_id) {
        String sql = "UPDATE Orders SET order_date = ?, order_status = ? WHERE user_id = ? AND order_status = 'cart'";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now()));
            ps.setString(2, "pending");
            ps.setInt(3, user_id);
        } catch (SQLException e) {e.printStackTrace();}
    }

    public static boolean deleteOrder(int order_id) {
        //todo: delete items in order as well
        String sql = "DELETE FROM Orders WHERE order_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, order_id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {e.printStackTrace();}
            return false;
    }

    public static boolean deleteAllOrdersForUser(int user_id) {
        //todo: delete items in orders as well
        String sql = "DELETE FROM Orders WHERE user_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, user_id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {e.printStackTrace();}
            return false;
    }

    public static void main(String[] args) {

    }
}
