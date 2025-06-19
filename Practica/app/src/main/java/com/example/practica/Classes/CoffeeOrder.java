package com.example.practica.Classes;

public class CoffeeOrder {
    private boolean id;
    private String name;
    private double price;
    private String description;
    private int quantity;
    private int productId;

    public CoffeeOrder(String name, double price, String description, int quantity) {
        this.name = name;
        this.price = price;
        this.description = description;
        this.quantity = quantity;
    }
    public CoffeeOrder(String name, double price, String description, int quantity, int productId) {
        this.name = name;
        this.price = price;
        this.description = description;
        this.quantity = quantity;
        this.productId = productId;
    }

    public String getName() {
        return name;
    }

    public double getPrice() {
        return price;
    }

    public String getDescription() {
        return description;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public double getTotalPrice() {
        return price * quantity;
    }

    public boolean getId() {
        return id;
    }

    public void setId(boolean id) {
        this.id = id;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isId() {
        return id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public int getProductId() {
        return productId;
    }

    public void setProductId(int productId) {
        this.productId = productId;
    }
}