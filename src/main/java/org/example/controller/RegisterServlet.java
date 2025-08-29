package org.example.controller;

import org.example.model.Order.OrderDAO;
import org.example.model.Order.OrderModel;
import org.example.model.user.UserDAO;
import org.example.model.user.UserModel;
import org.example.util.PasswordUtils;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.io.PrintWriter;

@WebServlet("/register")
public class RegisterServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private UserDAO userDAO = new UserDAO();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String username = request.getParameter("username");
        String password = request.getParameter("password"); // plain text for now
        String accessLevel = request.getParameter("accessLevel");


        response.setContentType("text/plain");
        PrintWriter out = response.getWriter();

        if (username == null || password == null || accessLevel == null) {
            out.print("All fields are required");
            return;
        }

        try {
            UserModel user = new UserModel();
            user.setUsername(username);
            user.setPasswordHash(PasswordUtils.hashPassword(password)); // store plain text for now
            user.setAccessLevel(accessLevel);

            boolean success = userDAO.addUser(user);

            if (success) {
                OrderDAO orderDAO = new OrderDAO();
                OrderModel order = new OrderModel(user.getUserId(), null, "cart");
                orderDAO.addOrder(order);
                user.setCartId(order.getOrderId());
                userDAO.updateUser(user);
                // Registration succeeded → redirect to login page
                response.sendRedirect("login.html"); // adjust path if needed
            } else {
                out.print("Registration failed: Username may already exist");
            }

        } catch (Exception e) {
            e.printStackTrace();
            out.print("Error: " + e.getMessage());
        } finally {
            out.close();
        }
    }
}
