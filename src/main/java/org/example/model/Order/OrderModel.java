package org.example.model.Order;

import org.example.model.MenuItem.MenuItem;
import org.example.model.user.UserModel;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;

public class OrderModel {
    private int orderId;
    private int employeeId;
    private LocalDateTime orderDate;
    private String status;
    private ArrayList<Integer> menuItemIds;
    
    public OrderModel() {}

    public OrderModel(int orderId, int employeeId, LocalDateTime orderDate, String status) {
        this.orderId = orderId;
        this.employeeId = employeeId;
        this.orderDate = orderDate;
        this.status = status;
        this.menuItemIds = new ArrayList<>();
    }

    public OrderModel(int employeeId, LocalDateTime orderDate, String status) {
        this.employeeId = employeeId;
        this.orderDate = orderDate;
        this.status = status;
        this.menuItemIds = new ArrayList<>();
    }

    public int getOrderId() { return orderId; }
    public void setOrderId(int orderId) { this.orderId = orderId; }

    public int getEmployeeId() { return employeeId; }
    public void setEmployeeId(int employeeId) { this.employeeId = employeeId; }

    public LocalDateTime getOrderDate() { return orderDate; }
    public void setOrderDate(LocalDateTime orderDate) { this.orderDate = orderDate; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public ArrayList<Integer> getOrderItemIds() { return menuItemIds; }
    public void setOrderItemIds(ArrayList<Integer> menuItemIds) { this.menuItemIds = menuItemIds; }
    public void addOrderItemId(int menuItemId) { this.menuItemIds.add(menuItemId); }
    public void removeOrderItemId(int menuItemId) { this.menuItemIds.remove(Integer.valueOf(menuItemId)); }

    public void showOrderDetails() {
        System.out.println("Order ID: " + orderId);
        System.out.println("Employee ID: " + employeeId);
        System.out.println("Order Date: " + orderDate);
        System.out.println("Status: " + status);
        System.out.println("Menu Item IDs: " + menuItemIds);
    }
}

