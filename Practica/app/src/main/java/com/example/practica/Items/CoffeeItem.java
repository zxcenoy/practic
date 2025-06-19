package com.example.practica.Items;

public class CoffeeItem {
    private int productId;
    private String name;
    private int imageResId;
    private String category;
    private double price;
    private String imageUrl;

    public CoffeeItem(String name, int imageResId, String category, double price) {
        this.name = name;
        this.imageResId = imageResId;
        this.category = category != null ? category : "unknown";
        this.price = price;
        this.imageUrl = "";
    }

    public CoffeeItem(String name, double price, String imageUrl, int productId) {
        this.name = name;
        this.price = price;
        this.imageUrl = imageUrl;
        this.productId = productId;
        this.imageResId = 0;
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

    public void setImageResId(int imageResId) {
        this.imageResId = imageResId;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
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