package com.example.practica;

public class CoffeeItem {
    private String name;
    private int imageResId;
    private String category;
    private double price;

    public CoffeeItem(String name, int imageResId, String category, double price) {
        this.name = name;
        this.imageResId = imageResId;
        this.category = category;
        this.price = price;
    }

    public String getName() {
        return name;
    }

    public int getImageResId() {
        return imageResId;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }
}