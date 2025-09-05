package org.example.model.Order;

import java.time.LocalDateTime;
import java.util.ArrayList;

public class OrderModel {
    private int orderId;
    private int user_id;
    private int numberOfItems;
    private String order_status;
    private String order_note;
    private LocalDateTime orderDate;
    private ArrayList<Integer> item_in_order_ids; // key: index, value: item_in_order_id

    public OrderModel() {}
    public OrderModel(int user_id, LocalDateTime orderDate, String status) {
        this.user_id = user_id;
        this.orderDate = orderDate;
        this.order_status = status;
        this.order_note = null;
        this.numberOfItems = 0;
        this.item_in_order_ids = new ArrayList<>();
    }
    public OrderModel(int orderId, int user_id, LocalDateTime orderDate, String status, String order_note) {
        this.orderId = orderId;
        this.user_id = user_id;
        this.orderDate = orderDate;
        this.order_status = status;
        this.order_note = order_note;
        this.item_in_order_ids = item_in_order_ids;
        this.numberOfItems = item_in_order_ids.size();
    }

    public int getOrderId() { return orderId; }
    public void setOrderId(int orderId) { this.orderId = orderId; }

    public int getEmployeeId() { return user_id; }
    public void setEmployeeId(int user_id) { this.user_id = user_id; }

    public LocalDateTime getOrderDate() { return orderDate; }
    public void setOrderDate(LocalDateTime orderDate) { this.orderDate = orderDate; }

    public String getOrderStatus() { return order_status; }
    public void setOrderStatus(String status) { this.order_status = status; }

    public ArrayList<Integer> getItemInOrderIds() { return item_in_order_ids; }
    public void setItemInOrderIds(ArrayList<Integer> item_in_order_ids) {
        this.item_in_order_ids = item_in_order_ids;
        this.numberOfItems = this.item_in_order_ids.size();
    }

    public void addItem(int item_in_order_id) {
        this.item_in_order_ids.add(numberOfItems, item_in_order_id);
        this.numberOfItems = this.item_in_order_ids.size();
    }
    public void removeItem(int item_in_order_id) {
        this.item_in_order_ids.remove(Integer.valueOf(item_in_order_id));
        this.numberOfItems = this.item_in_order_ids.size();
    }

    public int getNumberOfItems() { return numberOfItems; }
    public void setNumberOfItems(int numberOfItems) { this.numberOfItems = numberOfItems; }


    public String getOrderNote() { return order_note; }
    public void setOrderNote(String order_note) { this.order_note = order_note; }

    public void showOrderDetails() {
        System.out.println("Order ID: " + orderId);
        System.out.println("Employee ID: " + user_id);
        System.out.println("Order Date: " + orderDate);
        System.out.println("Status: " + order_status);
        System.out.println("Number of Items: " + numberOfItems);
        System.out.println("Item in Order IDs: " + item_in_order_ids);
        System.out.println("Order Note: " + order_note);
    }
}

