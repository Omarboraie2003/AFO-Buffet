package org.example.controller;

import com.google.gson.Gson;
import org.example.model.ItemInOrder.ItemInOrderDAO;
import org.example.model.ItemInOrder.ItemInOrderModel;
import org.example.model.MenuItem.MenuDAO;


import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import org.example.model.MenuItem.MenuItem;
import org.example.model.user.UserDAO;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Map;

import java.io.BufferedReader;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;


@WebServlet("/manage_cart")
public class ManageCartServlet extends HttpServlet {

    private final MenuDAO menuDAO = new MenuDAO();
    private final UserDAO userDAO = new UserDAO();
    private final Gson gson = new Gson();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        resp.setContentType("application/json");

        int user_id = (Integer) req.getSession().getAttribute("userId");
        int item_id = Integer.parseInt(req.getParameter("itemId"));
        int cart_id = userDAO.getUserCartId(user_id);

        String type_of_bread = req.getParameter("type_of_bread");
        String quantity = req.getParameter("quantity");
        String item_note = req.getParameter("item_note");

        boolean success = ItemInOrderDAO.createItemInOrder(cart_id, item_id, type_of_bread, quantity, item_note) > 0;

        if (success) {
            resp.setStatus(HttpServletResponse.SC_OK);
            resp.getWriter().write(gson.toJson(Response.parseResponse(true, "Item added to cart")));
        } else {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write(gson.toJson(Response.parseResponse(false, "Failed to add item to cart")));
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        Integer userId = (Integer) req.getSession().getAttribute("userId");
        if (userId == null) {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            resp.getWriter().write(gson.toJson(Response.parseResponse(false, "User not logged in")));
            return;
        }

        try {
            int cart_id = userDAO.getUserCartId(userId);
            ArrayList<ItemInOrderModel> itemsInCart = ItemInOrderDAO.getItemInOrderModels(cart_id);
            ArrayList<ItemInUserCartPage> responseItems = new ArrayList<>();

            for (ItemInOrderModel item : itemsInCart) {
                MenuItem baseItem = menuDAO.getMenuItemById(item.getItemId());
                responseItems.add(new ItemInUserCartPage(item, baseItem));
            }

            resp.setStatus(HttpServletResponse.SC_OK);
            resp.getWriter().write(gson.toJson(Response.parseResponse(true, "Cart retrieved successfully", responseItems)));

        } catch (Exception e) {
            e.printStackTrace();
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write(gson.toJson(Response.parseResponse(false, "Error retrieving cart items")));
        }
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        System.out.println("=== PUT REQUEST DEBUG ===");
        System.out.println("Content-Type: " + req.getContentType());
        System.out.println("Content-Length: " + req.getContentLength());

        try {
            // Check session first
            Integer userId = (Integer) req.getSession().getAttribute("userId");
            System.out.println("User ID from session: " + userId);

            if (userId == null || userId <= 0) {
                System.out.println("User not logged in or invalid userId");
                resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                resp.getWriter().write(gson.toJson(Response.parseResponse(false, "User not logged in")));
                return;
            }

            // For PUT requests, we need to manually read the body and parse parameters
            StringBuilder requestBody = new StringBuilder();
            try (BufferedReader reader = req.getReader()) {
                String line;
                while ((line = reader.readLine()) != null) {
                    requestBody.append(line);
                }
            }

            String bodyContent = requestBody.toString();
            System.out.println("Request body: " + bodyContent);

            // Parse URL-encoded parameters manually
            Map<String, String> parameters = new HashMap<>();
            if (bodyContent != null && !bodyContent.isEmpty()) {
                String[] pairs = bodyContent.split("&");
                for (String pair : pairs) {
                    String[] keyValue = pair.split("=", 2);
                    if (keyValue.length == 2) {
                        try {
                            String key = URLDecoder.decode(keyValue[0], StandardCharsets.UTF_8);
                            String value = URLDecoder.decode(keyValue[1], StandardCharsets.UTF_8);
                            parameters.put(key, value);
                            System.out.println("Parsed parameter: " + key + " = " + value);
                        } catch (Exception e) {
                            System.out.println("Error decoding parameter: " + pair);
                        }
                    }
                }
            }

            // Get parameters from our parsed map
            String itemInOrderIdStr = parameters.get("itemInOrderId");
            System.out.println("itemInOrderId parameter: " + itemInOrderIdStr);

            if (itemInOrderIdStr == null || itemInOrderIdStr.isEmpty()) {
                System.out.println("Missing itemInOrderId parameter");
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.getWriter().write(gson.toJson(Response.parseResponse(false, "Missing itemInOrderId parameter")));
                return;
            }

            int itemInOrderId;
            try {
                itemInOrderId = Integer.parseInt(itemInOrderIdStr);
                System.out.println("Parsed itemInOrderId: " + itemInOrderId);
            } catch (NumberFormatException e) {
                System.out.println("Invalid itemInOrderId format: " + itemInOrderIdStr);
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.getWriter().write(gson.toJson(Response.parseResponse(false, "Invalid itemInOrderId format")));
                return;
            }

            // Get the item from database
            ItemInOrderModel item_in_cart = ItemInOrderDAO.getItemInOrderById(itemInOrderId);
            System.out.println("Found item in cart: " + (item_in_cart != null));

            if (item_in_cart == null) {
                System.out.println("Item not found in database");
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                resp.getWriter().write(gson.toJson(Response.parseResponse(false, "Item not found")));
                return;
            }

            // Get update parameters from our parsed map
            String updated_type_of_bread = parameters.get("type_of_bread");
            String updated_quantity = parameters.get("quantity");
            String updated_item_note = parameters.get("item_note");

            System.out.println("Update parameters:");
            System.out.println("  type_of_bread: " + updated_type_of_bread);
            System.out.println("  quantity: " + updated_quantity);
            System.out.println("  item_note: " + updated_item_note);

            // Update the item
            item_in_cart.setTypeOfBread(updated_type_of_bread);
            item_in_cart.setQuantity(updated_quantity);
            item_in_cart.setNote(updated_item_note);

            System.out.println("Calling ItemInOrderDAO.updateItemInOrder...");
            boolean success = ItemInOrderDAO.updateItemInOrder(item_in_cart);
            System.out.println("Update result: " + success);

            if (success) {
                resp.setStatus(HttpServletResponse.SC_OK);
                resp.getWriter().write(gson.toJson(Response.parseResponse(true, "Item updated successfully", item_in_cart)));
            } else {
                resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                resp.getWriter().write(gson.toJson(Response.parseResponse(false, "Failed to update item in database")));
            }

        } catch (Exception e) {
            System.out.println("Unexpected exception: " + e.getMessage());
            e.printStackTrace();
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write(gson.toJson(Response.parseResponse(false, "Server error: " + e.getMessage())));
        }

        System.out.println("=== END PUT REQUEST DEBUG ===");
    }

    @Override

    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json");
        Integer userId = (Integer) req.getSession().getAttribute("userId");
        if (userId == null) {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            resp.getWriter().write(gson.toJson(Response.parseResponse(false, "User not logged in")));
            return;
        }

        try {
            String itemInOrderIdParam;

            // Try to get from query parameters first
            itemInOrderIdParam = req.getParameter("itemInOrderId");

            // If not found, try to read from request body
            if (itemInOrderIdParam == null) {
                StringBuilder buffer = new StringBuilder();
                BufferedReader reader = req.getReader();
                String line;
                while ((line = reader.readLine()) != null) {
                    buffer.append(line);
                }
                String body = buffer.toString();

                // Parse URL-encoded data
                String[] pairs = body.split("&");
                for (String pair : pairs) {
                    String[] keyValue = pair.split("=");
                    if (keyValue.length == 2 && "itemInOrderId".equals(keyValue[0])) {
                        itemInOrderIdParam = java.net.URLDecoder.decode(keyValue[1], "UTF-8");
                        break;
                    }
                }
            }

            if (itemInOrderIdParam == null) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.getWriter().write(gson.toJson(Response.parseResponse(false, "itemInOrderId parameter is required")));
                return;
            }

            int item_in_order_id = Integer.parseInt(itemInOrderIdParam);

//            int cart_id = userDAO.getUserCartId(userId);

            Boolean success = ItemInOrderDAO.deleteItemInOrder(item_in_order_id);

            if (success) {
                resp.setStatus(HttpServletResponse.SC_OK);
                resp.getWriter().write(gson.toJson(Response.parseResponse(true, "Item removed from cart")));
            } else {
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                resp.getWriter().write(gson.toJson(Response.parseResponse(false, "Item not found in cart")));
            }
        } catch (NumberFormatException e) {
            e.printStackTrace();
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write(gson.toJson(Response.parseResponse(false, "Invalid itemInOrderId format")));
        } catch (Exception e) {
            e.printStackTrace();
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write(gson.toJson(Response.parseResponse(false, "Error removing cart item")));
        }
    }


    // ------------------- DTO for frontend -------------------
    private static class ItemInUserCartPage extends ItemInOrderModel {
        private String item_name;
        private String item_description;
        private String item_type;
        private String item_category;
        private String photo_url;
        private boolean is_available;
        private boolean is_special;

        public ItemInUserCartPage(ItemInOrderModel orderItem, MenuItem baseItem) {
            super(orderItem.getItemInOrderId(),
                    orderItem.getItemId(),
                    orderItem.getTypeOfBread(),
                    orderItem.getQuantity(),
                    orderItem.getNote());

            this.item_name = baseItem.getName();
            this.item_description = baseItem.getDescription();
            this.item_type = baseItem.getType();
            this.item_category = baseItem.getCategory();
            this.photo_url = baseItem.getPhotoUrl();
            this.is_available = baseItem.isAvailable();
            this.is_special = baseItem.isSpecial();
        }
    }

    private static class Response {
        static Map<String, Object> parseResponse(boolean success, String message) {
            return Map.of(
                    "success", success,
                    "message", message
            );
        }
        static Map<String, Object> parseResponse(boolean success, String message, Object data) {
            return Map.of(
                    "success", success,
                    "message", message,
                    "data", data
            );
        }
    }
}
