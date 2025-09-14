package org.example.controller;

import com.google.gson.Gson;
import org.example.model.ItemInOrder.ItemInOrderDAO;
import org.example.model.ItemInOrder.ItemInOrderModel;
import org.example.model.MenuItem.MenuDAO;


import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import org.example.model.MenuItem.MenuItem;
import org.example.model.Order.OrderDAO;
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


@WebServlet("/confirm_cart")
public class ConfirmCartServlet extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json");
        PrintWriter out = resp.getWriter();
        Gson gson = new Gson();

        UserDAO userDAO = new UserDAO();

        // Extract userId from query parameters
        int userId = (Integer) req.getSession().getAttribute("userId");

        boolean success = userDAO.confirmUserCart(userId);

        if (success) {
            resp.setStatus(HttpServletResponse.SC_OK);
            out.print(gson.toJson(Response.parseResponse(true, "Cart confirmed successfully")));
        } else {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print(gson.toJson(Response.parseResponse(false, "Failed to confirm cart")));
        }

        // Validate user existence
        if (userDAO.getUserById(userId) == null) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.print(gson.toJson(Response.parseResponse(false, "User does not exist")));
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