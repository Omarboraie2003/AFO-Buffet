package org.example.controller;

import com.google.gson.Gson;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.example.model.Order.OrderDAO;
import org.example.model.Order.OrderModel;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;

@WebServlet("/updateCartItem")
public class UpdateCartServlet extends HttpServlet {

    private final OrderDAO orderDAO = new OrderDAO();
    private final Gson gson = new Gson();

    static class UpdateCartItemRequest {
        int itemId;
        String notes;
        String bread;
        String quantity;
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json");
        PrintWriter out = resp.getWriter();

        Integer userId = (Integer) req.getSession().getAttribute("userId");
        if (userId == null) {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            out.write(gson.toJson(Map.of("status", "error", "message", "User not logged in")));
            return;
        }

        try {
            UpdateCartItemRequest requestData = gson.fromJson(req.getReader(), UpdateCartItemRequest.class);

            int cartId = orderDAO.checkIfCartExists(userId);
            if (cartId == -1) {
                out.write(gson.toJson(Map.of("status", "error", "message", "Cart not found")));
                return;
            }

            OrderModel cart = orderDAO.getOrderById(cartId);
            boolean updated = false;

            // Assuming OrderModel has a List<Map<String, Object>> items or similar
           // List<Map<String, Object>> items = cart.getItems();
//            for (Map<String, Object> item : items) {
//                int id = ((Double)item.get("item_id")).intValue(); // adjust key to your actual field
//                if (id == requestData.itemId) {
//                    item.put("notes", requestData.notes);
//                    item.put("bread", requestData.bread);
//                    item.put("quantity", requestData.quantity);
//                    updated = true;
//                    break;
//                }
//            }

            if (!updated) {
                out.write(gson.toJson(Map.of("status", "error", "message", "Item not found in cart")));
                return;
            }

            // Save updated cart
            orderDAO.updateCart(cartId, cart);

            out.write(gson.toJson(Map.of("status", "success")));

        } catch (Exception e) {
            e.printStackTrace();
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.write(gson.toJson(Map.of("status", "error", "message", e.getMessage())));
        }
    }
}
