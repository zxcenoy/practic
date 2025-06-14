package com.example.practica;

public class CoffeeItem {
    private String name;
    private int imageResId;

    public CoffeeItem(String name, int imageResId) {
        this.name = name;
        this.imageResId = imageResId;
    }

    public String getName() {
        return name;
    }

    public int getImageResId() {
        return imageResId;
    }
}