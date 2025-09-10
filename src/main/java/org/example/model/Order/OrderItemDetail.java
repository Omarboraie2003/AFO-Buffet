package org.example.model.Order;

// DTO class for individual item details
public class OrderItemDetail {
    private String itemName;
    private String typeOfBread;
    private String quantity;
    private String itemNote;

    // Constructors
    public OrderItemDetail() {}

    public OrderItemDetail(String itemName, String typeOfBread, String quantity, String itemNote) {
        this.itemName = itemName;
        this.typeOfBread = typeOfBread;
        this.quantity = quantity;
        this.itemNote = itemNote;
    }

    // Getters and Setters
    public String getItemName() { return itemName; }
    public void setItemName(String itemName) { this.itemName = itemName; }

    public String getTypeOfBread() { return typeOfBread; }
    public void setTypeOfBread(String typeOfBread) { this.typeOfBread = typeOfBread; }

    public String getQuantity() { return quantity; }
    public void setQuantity(String quantity) { this.quantity = quantity; }

    public String getItemNote() { return itemNote; }
    public void setItemNote(String itemNote) { this.itemNote = itemNote; }

    // Helper method to display item details
    public void showItemDetails() {
        System.out.println("    Item: " + (itemName != null ? itemName : "N/A"));
        System.out.println("    Type of Bread: " + (typeOfBread != null ? typeOfBread : "N/A"));
        System.out.println("    Quantity: " + (quantity != null ? quantity : "N/A"));
        System.out.println("    Note: " + (itemNote != null ? itemNote : "No note"));
    }
}
