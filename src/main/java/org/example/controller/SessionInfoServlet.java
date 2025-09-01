package org.example.controller;

import com.google.gson.JsonObject;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;

@WebServlet("/sessionInfo")
public class SessionInfoServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json;charset=UTF-8");
        PrintWriter out = response.getWriter();

        try {
            HttpSession session = request.getSession(false); // don't create new session

            JsonObject json = new JsonObject();

            if (session != null && Boolean.TRUE.equals(session.getAttribute("isLoggedIn"))) {
                json.addProperty("isLoggedIn", true);
                json.addProperty("userId", (Integer) session.getAttribute("userId"));
                json.addProperty("username", (String) session.getAttribute("username"));
                json.addProperty("accessLevel", (String) session.getAttribute("accessLevel"));
                json.addProperty("sessionId", session.getId());
            } else {
                json.addProperty("isLoggedIn", false);
            }

            out.print(json.toString());

            System.out.println("[DEBUG][SessionInfoServlet] Sent session info: " + json);

        } catch (Exception e) {
            e.printStackTrace();
            JsonObject error = new JsonObject();
            error.addProperty("error", e.getMessage());
            out.print(error.toString());
        } finally {
            out.close();
        }
    }
}
