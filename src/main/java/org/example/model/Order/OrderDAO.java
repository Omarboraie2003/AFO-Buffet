package org.example.model.Order;

import org.example.model.MenuItem.MenuItem;

import java.sql.*;
import java.sql.Date;
import java.util.*;

import java.sql.Connection;
import java.sql.PreparedStatement;

public class OrderDAO {
    private static final String URL = "jdbc:sqlserver://localhost:1433;databaseName=BuffetDB;encrypt=true;trustServerCertificate=true";
    private static final String USER = "sa";
    private static final String PASSWORD = "MySecurePass123";
    private Connection conn;


    // Constructor opens the connection
    public OrderDAO() {
        try {
            conn = DriverManager.getConnection(URL, USER, PASSWORD);
            System.out.println("✅ Database connected successfully!");
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("❌ Failed to connect to database!");
        }
    }

    public void addOrder(OrderModel order) throws SQLException {
        String sql = "INSERT INTO Orders (employeeId, orderDate, totalAmount, status) VALUES (?, ?, ?, ?)";
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setInt(1, order.getEmployeeId());
        ps.setTimestamp(2, new Timestamp(order.getOrderDate().getTime()));
        ps.setDouble(3, order.getTotalAmount());
        ps.setString(4, order.getStatus());
        ps.executeUpdate();
    }

    public List<OrderModel> getAllOrders() throws SQLException {
        List<OrderModel> orders = new ArrayList<>();
        String sql = "SELECT * FROM Orders";
        PreparedStatement ps = conn.prepareStatement(sql);
        ResultSet rs = ps.executeQuery();

        while (rs.next()) {
            OrderModel order = new OrderModel();
            order.setOrderId(rs.getInt("orderId"));
            order.setEmployeeId(rs.getInt("employeeId"));
            order.setOrderDate(rs.getTimestamp("orderDate"));
            order.setTotalAmount(rs.getDouble("totalAmount"));
            order.setStatus(rs.getString("status"));
            orders.add(order);
        }

        return orders;
    }

    public List<OrderModel> getOrdersByEmployee(int employeeId) throws SQLException {
        List<OrderModel> orders = new ArrayList<>();
        String sql = "SELECT * FROM Orders WHERE employeeId = ?";
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setInt(1, employeeId);
        ResultSet rs = ps.executeQuery();

        while (rs.next()) {
            OrderModel order = new OrderModel();
            order.setOrderId(rs.getInt("orderId"));
            order.setEmployeeId(rs.getInt("employeeId"));
            order.setOrderDate(rs.getTimestamp("orderDate"));
            order.setTotalAmount(rs.getDouble("totalAmount"));
            order.setStatus(rs.getString("status"));
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
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setInt(1, orderId);
        ResultSet rs = ps.executeQuery();

        System.out.println("Fetching items for order ID: " + orderId);
        System.out.println(rs);

        return items;
    }

    public static void main(String[] args) {
        OrderModel order = new OrderModel(
                1, // orderId
                1, // employeeId
                new Date(125, 8, 20), // orderDate
                100.50, // totalAmount
                "Pending" // status
        );
        OrderDAO orderDAO = new OrderDAO();
        try {
            orderDAO.addOrder(order);
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("❌ Failed to add order!");
        }
        try {
            List<OrderModel> orders = orderDAO.getAllOrders();
            for (OrderModel o : orders) {
                System.out.println("Order ID: " + o.getOrderId() + ", Employee ID: " + o.getEmployeeId() +
                        ", Date: " + o.getOrderDate() + ", Total: " + o.getTotalAmount() + ", Status: " + o.getStatus());
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("❌ Failed to fetch orders!");
        }
    }
}
