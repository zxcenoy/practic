package com.example.practica.Managers;

import com.example.practica.Classes.CoffeeOrder;

import java.util.ArrayList;
import java.util.List;

public class CartManager {
    private static CartManager instance;
    private List<CoffeeOrder> items = new ArrayList<>();


    public static CartManager getInstance() {
        if (instance == null) {
            instance = new CartManager();
        }
        return instance;
    }

    public void addToCart(CoffeeOrder order) {
        for (CoffeeOrder item : items) {
            if (item.equals(order)) {
                item.setQuantity(item.getQuantity() + order.getQuantity());
                return;
            }
        }
        items.add(order);
    }

    public List<CoffeeOrder> getItems() {
        return new ArrayList<>(items);
    }

    public void removeItem(int position) {
        if (position >= 0 && position < items.size()) {
            items.remove(position);
        }
    }

    public double calculateTotal() {
        double total = 0;
        for (CoffeeOrder item : items) {
            total += item.getPrice() * item.getQuantity();
        }
        return total;
    }
    public void clearCart() {
        items.clear();
    }

}