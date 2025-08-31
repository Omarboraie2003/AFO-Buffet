package org.example;

import org.example.controller.MenuController;

import java.sql.Connection;
import java.sql.DriverManager;

public class testMenu {
    public static void main(String[] args) {
        try {
            // 1. Setup DB connection
            String url = "jdbc:sqlserver://localhost:1433;databaseName=BuffetDB;encrypt=true;trustServerCertificate=true";
            String user = "sa";
            String password = "StrongPassword123!";

            Connection conn = DriverManager.getConnection(url, user, password);

            // 2. Create Controller
            MenuController controller = new MenuController();
            // === Show menu before update/delete ===
            System.out.println("\n=== Menu Before Update/Delete ===");
            controller.showMenuForEmployee();

            // === Update Item ===
            System.out.println("\n=== Updating Item ===");
            // Example: update BBQ Burger price to 80.0 and make it inactive
            controller.deleteItem(2);
            controller.addItem("nuggets wrap","chicken nuggets,bread,lettuce,tomato",true,"special");


            // === Show menu after update/delete ===
            System.out.println("\n=== Menu After Update/Delete ===");
            controller.showMenuForEmployee();
            System.out.println("\n=== Menu After Update/Delete ===");
            controller.showMenuForChef();



            conn.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
