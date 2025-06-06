package com.example.my;

public class PantryItem {
    private int id;
    private String name;
    private int quantity;

    public PantryItem() {}

    public PantryItem(int id, String name, int quantity) {
        this.id = id;
        this.name = name;
        this.quantity = quantity;
    }

    public int getId() { return id; }

    public String getName() { return name; }

    public void setName(String name) {this.name = name; }

    public int getQuantity() { return quantity; }

    public void setQuantity(int quantity) { this.quantity = quantity; }

}
