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
    private ArrayList<MenuItem> menuItems; // Assuming each order has a single menu item
    
    public OrderModel() {}

    public OrderModel(int orderId, int employeeId, Date orderDate, double totalAmount, String status) {
        this.orderId = orderId;
        this.employeeId = employeeId;
        this.orderDate = orderDate;
        this.totalAmount = totalAmount;
        this.status = status;
    }
}

