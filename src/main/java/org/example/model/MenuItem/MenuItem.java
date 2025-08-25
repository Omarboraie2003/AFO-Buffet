package org.example.model.MenuItem;

public class MenuItem {
    private int id;
    private String name;
    private String description;
    private double price;
    private boolean available;
    private String type;       // existing field
    private String category;   // new field
    private String image;   // new field
    private boolean is_special;



    // Constructors
    public MenuItem() {}

    public MenuItem(int id, String name, String description, double price, boolean available, String type, String category, String image,boolean is_special) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.price = price;
        this.available = available;
        this.type = type;
        this.category = category;
        this.image = image;
        this.is_special=is_special;
    }

    // Getters & Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public boolean isAvailable() { return available; }
    public void setAvailable(boolean available) { this.available = available; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getImage() { return image; }
    public void setImage(String image) { this.image = image; }

    public boolean isSpecial() { return is_special; }
    public void setSpecial(boolean is_special) { this.is_special = is_special; }
}
