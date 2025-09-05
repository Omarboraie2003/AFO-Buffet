package org.example.controller;

import com.google.gson.Gson;
import org.example.model.Order.OrderDAO;
import org.example.model.Order.OrderModel;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.example.model.user.UserDAO;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;

@WebServlet("/removeFromCart")
public class RemoveFromCartServlet extends HttpServlet {

    private final OrderDAO orderDAO = new OrderDAO();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json");
        PrintWriter out = resp.getWriter();
        Gson gson = new Gson();

        try {
            // ✅ Get user ID from session
            Integer userId = (Integer) req.getSession().getAttribute("userId");
            if (userId == null) {
                resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                out.write(gson.toJson(new Response("error", "User not logged in")));
                return;
            }

            // ✅ Get the item ID to remove
            String removeItemParam = req.getParameter("itemId");
            if (removeItemParam == null) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.write(gson.toJson(new Response("error", "Item ID is required")));
                return;
            }

            int removeItemId = Integer.parseInt(removeItemParam);

            // ✅ Get user's cart
            int cartId = new UserDAO().getUserById(userId).getCartId();
            if (cartId == -1) {
                out.write(gson.toJson(new Response("error", "Cart not found")));
                return;
            }

            OrderModel cart = orderDAO.getOrderById(cartId);
            if (cart.getItemInOrderIds() != null && cart.getItemInOrderIds().removeIf(id -> id == removeItemId)) {
                orderDAO.updateCart(cart);
                out.write(gson.toJson(new Response("success", "Item removed from cart")));
            } else {
                out.write(gson.toJson(new Response("error", "Item not found in cart")));
            }

        } catch (NumberFormatException e) {
            e.printStackTrace();
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.write(gson.toJson(new Response("error", e.getMessage())));
        }
    }

    // ✅ Helper class for JSON responses
    private static class Response {
        String status;
        String message;

        Response(String status, String message) {
            this.status = status;
            this.message = message;
        }
    }
}
