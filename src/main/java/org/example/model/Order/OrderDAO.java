package org.example.model.Order;

import org.example.model.ItemInOrder.ItemInOrderDAO;
import org.example.util.DBConnection;
import java.sql.*;
import java.util.ArrayList;
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
                OrderModel order = new OrderModel(order_id, user_id, ts != null ? ts.toLocalDateTime() : null, order_note, order_status);
                order.setItemInOrderIds(ItemInOrderDAO.getItemsInOrder(order_id));
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
        ItemInOrderDAO.deleteAllItemsInOrder(order_id); // delete items in order as well
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

    // Add this method to your existing OrderDAO class

    // Add this import at the top of your OrderDAO class if not already present:
// import java.util.ArrayList;

// Add this method to your existing OrderDAO class

    public static ArrayList<OrderDetailsDTO> getAllOrderDetails() {
        String sql = """
        SELECT 
            o.order_id,
            u.username,
            o.order_date,
            o.order_status,
            mi.item_name,
            iio.type_of_bread,
            iio.quantity,
            iio.item_note
        FROM Orders o
        JOIN Users u ON o.user_id = u.user_id  
        JOIN ItemsInOrder iio ON o.order_id = iio.order_id
        JOIN MenuItems mi ON iio.item_id = mi.item_id
        ORDER BY o.order_id, iio.item_in_order_id
        """;

        ArrayList<OrderDetailsDTO> orderDetailsList = new ArrayList<>();
        OrderDetailsDTO currentOrder = null;
        int lastOrderId = -1;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                int orderId = rs.getInt("order_id");

                // Check if this is a new order
                if (orderId != lastOrderId) {
                    // Save previous order if it exists
                    if (currentOrder != null) {
                        orderDetailsList.add(currentOrder);
                    }

                    // Create new order
                    currentOrder = new OrderDetailsDTO(
                            orderId,
                            rs.getString("username"),
                            rs.getTimestamp("order_date").toLocalDateTime(),
                            rs.getString("order_status")
                    );
                    lastOrderId = orderId;
                }

                // Add item to current order (item name first, as most prominent)
                OrderItemDetail item = new OrderItemDetail(
                        rs.getString("item_name"),
                        rs.getString("type_of_bread"),
                        rs.getString("quantity"),
                        rs.getString("item_note")
                );
                currentOrder.addItem(item);
            }

            // Don't forget to add the last order
            if (currentOrder != null) {
                orderDetailsList.add(currentOrder);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return orderDetailsList;
    }
    public static boolean updateOrderStatus(int orderId, String newStatus) {
        String sql = "UPDATE Orders SET order_status = ? WHERE order_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, newStatus);
            ps.setInt(2, orderId);

            int rowsAffected = ps.executeUpdate();
            System.out.println("[OrderDAO] Updated order " + orderId + " status to " + newStatus + ". Rows affected: " + rowsAffected);

            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("[OrderDAO] Error updating order status: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

}