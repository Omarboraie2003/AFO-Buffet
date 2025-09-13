package org.example.controller;

import com.google.gson.Gson;
import org.example.model.Order.OrderDAO;
import org.example.model.MenuItem.MenuItem;
import org.example.model.MenuItem.MenuDAO;


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
import java.time.LocalDateTime;
import java.util.ArrayList;


import java.util.*;


@WebServlet("/addToCart")
public class AddToCartServlet extends HttpServlet {

    private final OrderDAO orderDAO = new OrderDAO();
    private final MenuDAO menuDAO = new MenuDAO();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json");
        PrintWriter out = resp.getWriter();
        Gson gson = new Gson();

        try {
            // ✅ Get user ID from session instead of request parameter
            Integer userId = (Integer) req.getSession().getAttribute("userId");
            if (userId == null) {
                resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("order_status", "error");
                errorResponse.put("message", "User not logged in");
                out.write(gson.toJson(errorResponse));
                return;
            }

            //todo: change itemId to item in order id
            int itemId = Integer.parseInt(req.getParameter("itemId"));

            // Check if a cart already exists
            int cartId = new UserDAO().getUserById(userId).getCartId();

            if (cartId != -1) {
                OrderModel existingCart = orderDAO.getOrderById(cartId);
//                if (existingCart.getItemInOrderIds() == null) {
//                    existingCart.setItemInOrderIds(new ArrayList<>());
//                }
                existingCart.addItem(itemId);
                orderDAO.updateCart(existingCart);

                out.write(gson.toJson(new Response("success", "Item added to existing cart", cartId)));
            }
//            else {
//                OrderModel newCart = new OrderModel();
//                newCart.setEmployeeId(userId);
//                newCart.setOrderDate(LocalDateTime.now());
//                newCart.setOrderStatus("cart");
//                newCart.setItemInOrderIds(new ArrayList<>());
//                newCart.addItem(itemId);
//
//                boolean inserted = orderDAO.addOrder(newCart);
//
//                if (inserted) {
//                    int newCartId = orderDAO.checkIfCartExists(userId);
//                    out.write(gson.toJson(new Response("success", "New cart created and item added", newCartId)));
//                } else {
//                    resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
//                    out.write(gson.toJson(new Response("error", "Failed to create cart", -1)));
//                }
//            }

        } catch (NumberFormatException e) {
            e.printStackTrace();
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("order_status", "error");
            errorResponse.put("message", e.getMessage());
            out.write(gson.toJson(errorResponse));
        }
    }

    // ------------------------- GET CART (GET) -------------------------
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json");
        PrintWriter out = resp.getWriter();
        Gson gson = new Gson();

        Integer userId = (Integer) req.getSession().getAttribute("userId");
        if (userId == null) {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            out.write(gson.toJson(Map.of(
                    "success", false,
                    "message", "User not logged in"
            )));
            return;
        }

        int cartId = new UserDAO().getUserById(userId).getCartId();
        if (cartId == -1) {
            out.write(gson.toJson(Map.of("success", true, "items", new ArrayList<>())));
            return;
        }

        OrderModel cart = orderDAO.getOrderById(cartId);
        List<Map<String, Object>> items = new ArrayList<>();

        if (cart.getItemInOrderIds() != null) {
            for (Integer itemId : cart.getItemInOrderIds()) {
                MenuItem item = menuDAO.getMenuItemById(itemId); // 🔹 get full item from DB
                if (item != null) {
                    Map<String, Object> itemData = new HashMap<>();
                    itemData.put("item_id", item.getId());
                    itemData.put("item_name", item.getName());
                    itemData.put("photoUrl", item.getPhotoUrl());
                    itemData.put("item_description", item.getDescription()); // optional
                    itemData.put("notes", ""); // optional
                    items.add(itemData);
                }
            }
        }

        out.write(gson.toJson(Map.of(
                "success", true,
                "cartId", cartId,
                "items", items
        )));

    }

    // ✅ Helper class for JSON responses
    private static class Response {
        String order_status;
        String message;
        int cartId;
        Response(String order_status, String message, int cartId) {
            this.order_status = order_status;
            this.message = message;
            this.cartId = cartId;
        }
    }
}