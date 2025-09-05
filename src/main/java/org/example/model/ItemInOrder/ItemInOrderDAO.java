package org.example.model.ItemInOrder;
import org.example.util.DBConnection;
import java.sql.*;

public class ItemInOrderDAO {
    public static int addItemInOrder(ItemInOrderModel item) {
        int itemId = -1;
        String sql = "INSERT INTO ItemsInOrder (item_id, type_of_bread, quantity, item_in_order_note) VALUES (?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, java.sql.Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setInt(1, item.getItemId());
            pstmt.setString(2, item.getTypeOfBread());
            pstmt.setString(3, item.getQuantity());
            pstmt.setString(4, item.getNote());
            int affectedRows = pstmt.executeUpdate();

            if (affectedRows == 0) {
                throw new RuntimeException("Creating item in order failed, no rows affected.");
            }

            try (var generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    itemId = generatedKeys.getInt(1);
                } else {
                    throw new RuntimeException("Creating item in order failed, no ID obtained.");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return itemId;
    }

    public static ItemInOrderModel getItemInOrderById(int item_in_order_id) {
        String sql = "SELECT * FROM ItemsInOrder WHERE item_in_order_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, item_in_order_id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return new ItemInOrderModel(
                        rs.getInt("item_in_order_id"),
                        rs.getInt("item_id"),
                        rs.getString("type_of_bread"),
                        rs.getString("quantity"),
                        rs.getString("item_note_id")
                );
            } else {
                return null;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }
}
