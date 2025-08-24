package org.example.model.Order;

import org.example.model.MenuItem.MenuItem;
import org.example.model.user.UserModel;

import java.util.ArrayList;
import java.util.Date;

public class OrderModel {
    private int orderId;
    private int employeeId;
    private Date orderDate;
    private double totalAmount;
    private String status;
    private ArrayList<Integer> menuItemIds;
    
    public OrderModel() {}

    public OrderModel(int orderId, int employeeId, Date orderDate, double totalAmount, String status) {
        this.orderId = orderId;
        this.employeeId = employeeId;
        this.orderDate = orderDate;
        this.totalAmount = totalAmount;
        this.status = status;
    }

    public int getOrderId() { return orderId; }
    public void setOrderId(int orderId) { this.orderId = orderId; }

    public int getEmployeeId() { return employeeId; }
    public void setEmployeeId(int employeeId) { this.employeeId = employeeId; }

    public Date getOrderDate() { return orderDate; }
    public void setOrderDate(Date orderDate) { this.orderDate = orderDate; }

    public double getTotalAmount() { return totalAmount; }
    public void setTotalAmount(double totalAmount) { this.totalAmount = totalAmount; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public ArrayList<Integer> getMenuItemIds() { return menuItemIds; }
    public void setMenuItemIds(ArrayList<Integer> menuItemIds) { this.menuItemIds = menuItemIds; }
}

