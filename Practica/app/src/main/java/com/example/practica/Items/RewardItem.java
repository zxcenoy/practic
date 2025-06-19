package com.example.practica.Items;

import java.util.Date;

public class RewardItem {
    private String drinkName;
    private int points;
    private Date date;
    private String formattedDate;

    public RewardItem(String drinkName, int points, Date date, String formattedDate) {
        this.drinkName = drinkName;
        this.points = points;
        this.date = date;
        this.formattedDate = formattedDate;
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

    public String getFormattedDate() {
        return formattedDate;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public void setDrinkName(String drinkName) {
        this.drinkName = drinkName;
    }

    public void setFormattedDate(String formattedDate) {
        this.formattedDate = formattedDate;
    }

    public void setPoints(int points) {
        this.points = points;
    }
}