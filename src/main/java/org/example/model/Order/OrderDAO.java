//package org.example.model.Order;
//
//import org.example.model.MenuItem.MenuItem;
//import org.example.model.user.UserDAO;
//import org.example.model.user.UserModel;
//import org.example.util.DBConnection;
//
//import java.sql.*;
//import java.time.LocalDateTime;
//import java.util.*;
//
//import java.sql.Connection;
//import java.sql.PreparedStatement;
//import java.util.Date;
//
//public class OrderDAO {
//    private static final String URL = "jdbc:sqlserver://localhost:1433;databaseName=BuffetDB;encrypt=true;trustServerCertificate=true";
//    private static final String USER = "sa";
//    private static final String PASSWORD = "MySecurePass123";
//    private Connection conn;
//
//
//    // Constructor opens the connection
//    public OrderDAO() {
//        try {
//            conn = DriverManager.getConnection(URL, USER, PASSWORD);
//            System.out.println("✅ Database connected successfully!");
//        } catch (SQLException e) {
//            e.printStackTrace();
//            System.out.println("❌ Failed to connect to database!");
//        }
//    }
//
//    public boolean addOrder(OrderModel order) throws SQLException {
//        String sql = "INSERT INTO Orders (user_id, order_date, status, item_ids) VALUES (?, ?, ?, ?)";
//        try (Connection conn = DBConnection.getConnection();
//            PreparedStatement ps = conn.prepareStatement(sql)) {
//            ps.setInt(1, order.getEmployeeId());
//            ps.setTimestamp(2, order.getOrderDate() == null ? null : Timestamp.valueOf(LocalDateTime.now()));
//            ps.setString(3, order.getStatus());
//            ps.setString(4, parseArrayListToString(order.getOrderItemIds()));
//            return ps.executeUpdate() > 0;
//        } catch (SQLException e) {
//            e.printStackTrace();
//            return false;
//        }
//    }
//
//    public OrderModel getOrderById(int order_id) throws SQLException {
//        String sql = "SELECT * FROM Orders WHERE order_id = ?";
//        try (Connection conn = DBConnection.getConnection();
//            PreparedStatement stmt = conn.prepareStatement(sql)) {
//            stmt.setInt(1, order_id);
//            ResultSet rs = stmt.executeQuery();
//            OrderModel order = new OrderModel();
//            if (rs.next()) {
//                order.setOrderId(rs.getInt("order_id"));
//                order.setEmployeeId(rs.getInt("user_id"));
//                Timestamp ts = rs.getTimestamp("order_date");
//                if (ts != null) {
//                    LocalDateTime ldt = ts.toLocalDateTime();
//                    System.out.println("DateTime: " + ldt);
//                } else {
//                    System.out.println("order_date is NULL in DB");
//                }
//                order.setStatus(rs.getString("order_status"));
//                order.setOrderItemIds(parseStringToArrayList(rs.getString("item_ids")));
//            }
//            return order;
//        } catch (SQLException e) {
//            e.printStackTrace();
//            return null;
//        }
//    }
//
//    public ArrayList<OrderModel> getAllOrders() throws SQLException {
//
//        ArrayList<OrderModel> orders = new ArrayList<>();
//        String sql = "SELECT * FROM Orders";
//        PreparedStatement ps = conn.prepareStatement(sql);
//        ResultSet rs = ps.executeQuery();
//
//        while (rs.next()) {
//            OrderModel order = new OrderModel();
//            order.setOrderId(rs.getInt("order_id"));
//            order.setEmployeeId(rs.getInt("user_id"));
//            order.setOrderDate(rs.getTimestamp("order_date").toLocalDateTime());
//            order.setStatus(rs.getString("order_status"));
//            order.setOrderItemIds(parseStringToArrayList(rs.getString("item_ids")));
//            orders.add(order);
//        }
//
//        return orders;
//    }
//
//    public ArrayList<OrderModel> getOrdersByEmployee(int employeeId) throws SQLException {
//        ArrayList<OrderModel> orders = new ArrayList<>();
//        String sql = "SELECT * FROM Orders WHERE employeeId = ?";
//        PreparedStatement ps = conn.prepareStatement(sql);
//        ps.setInt(1, employeeId);
//        ResultSet rs = ps.executeQuery();
//
//        while (rs.next()) {
//            OrderModel order = new OrderModel();
//            order.setOrderId(rs.getInt("order_id"));
//            order.setEmployeeId(rs.getInt("user_id"));
//            order.setOrderDate(rs.getTimestamp("order_date").toLocalDateTime());
//            order.setStatus(rs.getString("order_status"));
//            orders.add(order);
//        }
//
//        return orders;
//    }
//
//    public static String parseArrayListToString(ArrayList<Integer> list) {
//        StringBuilder sb = new StringBuilder();
//        for (int i = 0; i < list.size(); i++) {
//            sb.append(list.get(i));
//            if (i < list.size() - 1) {
//                sb.append("_");
//            }
//        }
//        return sb.toString();
//    }
//
//    public static ArrayList<Integer> parseStringToArrayList(String input) {
//        ArrayList<Integer> result = new ArrayList<>();
//        String[] parts = input.split("_");
//        for (String part : parts) {
//            try {
//                result.add(Integer.parseInt(part));
//            } catch (NumberFormatException e) {
//                System.out.println("Error parsing part: " + part);
//            }
//        }
//        return result;
//    }
//
//    public ArrayList<Integer> getOrderItemIds(int orderId) throws SQLException {
//        ArrayList<Integer> items = new ArrayList<>();
//        String sql = "SELECT * FROM Orders WHERE orderId = ?";
//        PreparedStatement ps = conn.prepareStatement(sql);
//        ps.setInt(1, orderId);
//        ResultSet rs = ps.executeQuery();
//
//        System.out.println("Fetching items for order ID: " + orderId);
//        System.out.println(rs);
//
//        return items;
//    }
//
//    public void updateCart(int orderId, OrderModel updatedOrder) throws SQLException {
//        String sql = "UPDATE Orders SET user_id = ?, status = ?, item_ids = ? WHERE order_id = ?";
//        PreparedStatement ps = conn.prepareStatement(sql);
//        ps.setInt(1, updatedOrder.getEmployeeId());
//        ps.setString(2, updatedOrder.getStatus());
//        ArrayList<Integer> ls = updatedOrder.getOrderItemIds();
//        ps.setString(3, ls != null ? parseArrayListToString(ls) : "");
//        ps.setInt(4, orderId);
//        ps.executeUpdate();
//    }
//
//    public void deleteOrder(int orderId) throws SQLException {
//        String sql = "DELETE FROM Orders WHERE order_id = ?";
//        PreparedStatement ps = conn.prepareStatement(sql);
//        ps.setInt(1, orderId);
//        ps.executeUpdate();
//    }
//
//    public int checkIfCartExists(int user_id) {
//        String sql = "SELECT * FROM Orders WHERE user_id = ? AND status = 'cart'";
//        try (Connection conn = DBConnection.getConnection();
//             PreparedStatement stmt = conn.prepareStatement(sql)) {
//            stmt.setInt(1, user_id);
//            ResultSet rs = stmt.executeQuery();
//            rs.next(); // If there's a result, the cart exists
//            return rs.getInt("order_id");
//        } catch (SQLException e) {
//            e.printStackTrace();
//            return -1;
//        }
//    }
//
//    public void confirmCart(int user_id) throws SQLException {
//        OrderModel order = getOrderById(checkIfCartExists(user_id));
//        order.setStatus("pending");
//        order.setOrderDate(LocalDateTime.now());
//        updateCart(order.getOrderId(), order);
//        // Create a new cart for the user
//        OrderModel newCart = new OrderModel(user_id, null, "cart");
//        addOrder(newCart);
//        // Update user's cart_id
//        UserDAO userDAO = new UserDAO();
//        UserModel user = userDAO.getUserById(user_id);
//        user.setCartId(checkIfCartExists(user.getUserId()));
//        userDAO.updateUser(user);
//    }
//
//    public static void main(String[] args) {
//        OrderDAO dao = new OrderDAO();
//        try {
//            dao.confirmCart(2);
//        } catch (SQLException e) {
//            e.printStackTrace();
//        }
////        UserDAO userDAO = new UserDAO();
////        UserModel user = userDAO.getUserById(2);
////        user.setCartId(dao.checkIfCartExists(user.getUserId()));
////        userDAO.updateUser(user);
//    }
//}
