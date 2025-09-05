package org.example.model.Order;

import org.example.model.user.UserDAO;
import org.example.model.user.UserModel;
import org.example.util.DBConnection;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;

import java.sql.Connection;
import java.sql.PreparedStatement;

public class OrderDAO1 {

    public static boolean addOrder(OrderModel order) throws SQLException {
        String sql = "INSERT INTO Orders (user_id, order_date, status, order_note) VALUES (?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, order.getEmployeeId());
            ps.setTimestamp(2, order.getOrderDate() == null ? null : Timestamp.valueOf(LocalDateTime.now()));
            ps.setString(3, order.getOrderStatus());
            ps.setString(4, order.getOrderNote());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static int createCartForUser(int user_id) {
        String sql = "INSERT INTO Orders (user_id) VALUES (?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, user_id);
            ps.executeUpdate();
            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) {
                return rs.getInt(1); // Return the generated cart ID
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1; // Indicate failure
    }

    public OrderModel getOrderById(int order_id) throws SQLException {
        String sql = "SELECT * FROM Orders WHERE order_id = ?";
        try (Connection conn = DBConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, order_id);
            ResultSet rs = stmt.executeQuery();
            OrderModel order = new OrderModel();
            if (rs.next()) {
                order.setOrderId(rs.getInt("order_id"));
                order.setEmployeeId(rs.getInt("user_id"));
                Timestamp ts = rs.getTimestamp("order_date");
                if (ts != null) {
                    LocalDateTime ldt = ts.toLocalDateTime();
                    System.out.println("DateTime: " + ldt);
                } else {
                    System.out.println("order_date is NULL in DB");
                }
                order.setOrderStatus(rs.getString("order_status"));
            }
            return order;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public ArrayList<OrderModel> getAllOrders() throws SQLException {

        ArrayList<OrderModel> orders = new ArrayList<>();
        String sql = "SELECT * FROM Orders";
        Connection conn = DBConnection.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql);
        ResultSet rs = ps.executeQuery();

        while (rs.next()) {
            OrderModel order = new OrderModel();
            order.setOrderId(rs.getInt("order_id"));
            order.setEmployeeId(rs.getInt("user_id"));
            order.setOrderDate(rs.getTimestamp("order_date").toLocalDateTime());
            order.setOrderStatus(rs.getString("order_status"));
            orders.add(order);
        }

        return orders;
    }

    public ArrayList<OrderModel> getOrdersByEmployee(int employeeId) throws SQLException {
        ArrayList<OrderModel> orders = new ArrayList<>();
        String sql = "SELECT * FROM Orders WHERE employeeId = ?";
        Connection conn = DBConnection.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setInt(1, employeeId);
        ResultSet rs = ps.executeQuery();

        while (rs.next()) {
            OrderModel order = new OrderModel();
            order.setOrderId(rs.getInt("order_id"));
            order.setEmployeeId(rs.getInt("user_id"));
            order.setOrderDate(rs.getTimestamp("order_date").toLocalDateTime());
            order.setOrderStatus(rs.getString("order_status"));
            orders.add(order);
        }

        return orders;
    }

    public static String parseArrayListToString(ArrayList<Integer> list) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < list.size(); i++) {
            sb.append(list.get(i));
            if (i < list.size() - 1) {
                sb.append("_");
            }
        }
        return sb.toString();
    }

    public static ArrayList<Integer> parseStringToArrayList(String input) {
        ArrayList<Integer> result = new ArrayList<>();
        String[] parts = input.split("_");
        for (String part : parts) {
            try {
                result.add(Integer.parseInt(part));
            } catch (NumberFormatException e) {
                System.out.println("Error parsing part: " + part);
            }
        }
        return result;
    }

    public ArrayList<Integer> getOrderItemIds(int orderId) throws SQLException {
        ArrayList<Integer> items = new ArrayList<>();
        String sql = "SELECT * FROM Orders WHERE orderId = ?";
        Connection conn = DBConnection.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setInt(1, orderId);
        ResultSet rs = ps.executeQuery();

        System.out.println("Fetching items for order ID: " + orderId);
        System.out.println(rs);

        return items;
    }

    public void updateCart(int orderId, OrderModel updatedOrder) throws SQLException {
        String sql = "UPDATE Orders SET user_id = ?, status = ? WHERE order_id = ?";
        Connection conn = DBConnection.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setInt(1, updatedOrder.getEmployeeId());
        ps.setString(2, updatedOrder.getOrderStatus());
        ps.setInt(3, orderId);
        ps.executeUpdate();
    }

    public void deleteOrder(int orderId) throws SQLException {
        String sql = "DELETE FROM Orders WHERE order_id = ?";
        Connection conn = DBConnection.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setInt(1, orderId);
        ps.executeUpdate();
    }

    public int checkIfCartExists(int user_id) {
        String sql = "SELECT * FROM Orders WHERE user_id = ? AND status = 'cart'";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, user_id);
            ResultSet rs = stmt.executeQuery();
            rs.next(); // If there's a result, the cart exists
            return rs.getInt("order_id");
        } catch (SQLException e) {
            e.printStackTrace();
            return -1;
        }
    }

    public void confirmCart(int user_id) throws SQLException {
        OrderModel order = getOrderById(checkIfCartExists(user_id));
        order.setOrderStatus("pending");
        order.setOrderDate(LocalDateTime.now());
        updateCart(order.getOrderId(), order);
        // Create a new cart for the user
        OrderModel newCart = new OrderModel(user_id, null, "cart");
        addOrder(newCart);
        // Update user's cart_id
        UserDAO userDAO = new UserDAO();
        UserModel user = userDAO.getUserById(user_id);
        user.setCartId(checkIfCartExists(user.getUserId()));
//        userDAO.updateUser(user);
        userDAO.updateUserCartId(user_id, user.getCartId());
    }

    public static void main(String[] args) {
        OrderDAO dao = new OrderDAO();
        dao.confirmCart(2);
        //        UserDAO userDAO = new UserDAO();
//        UserModel user = userDAO.getUserById(2);
//        user.setCartId(dao.checkIfCartExists(user.getUserId()));
//        userDAO.updateUser(user);
    }

    public void deleteAllOrdersForUser(int userId) {
        String sql = "DELETE FROM Orders WHERE user_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
