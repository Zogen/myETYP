package com.example.my;

public class RequiredSupplyItem {
    private int id;
    private String name;
    private int desiredQuantity;

    public RequiredSupplyItem(int id, String name, int desiredQuantity) {
        this.id = id;
        this.name = name;
        this.desiredQuantity = desiredQuantity;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getDesiredQuantity() {
        return desiredQuantity;
    }

    public void setName(String newName) { this.name = newName;}

    public void setQuantity(int newQuantity) { this.desiredQuantity = newQuantity; }
}
