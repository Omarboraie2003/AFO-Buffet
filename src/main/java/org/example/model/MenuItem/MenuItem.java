package org.example.model.MenuItem;


public class MenuItem {
    private int id;
    private String name;
    private String description;
    private boolean available;
    private String type;
    private String category;

    // Constructors
    public MenuItem() {}
    public MenuItem(int id, String name, String description, boolean available, String type, String category) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.available = available;
        this.type = type;
        this.category = category;
    }

    // Getters & Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public boolean isAvailable() { return available; }
    public void setAvailable(boolean available) { this.available = available; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
}
