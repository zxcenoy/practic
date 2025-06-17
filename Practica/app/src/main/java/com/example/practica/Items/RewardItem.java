package com.example.practica.Items;

import java.util.Date;

public class RewardItem {
    private String drinkName;
    private int points;
    private Date date;

    public RewardItem(String drinkName, int points, Date date) {
        this.drinkName = drinkName;
        this.points = points;
        this.date = date;
    }


    public String getDrinkName() {
        return drinkName;
    }

    public int getPoints() {
        return points;
    }

    public Date getDate() {
        return date;
    }
}