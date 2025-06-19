package com.example.practica.Classes;

public class LoyaltyCup {
    private boolean isEarned;


    public LoyaltyCup(boolean isEarned) {
        this.isEarned = isEarned;
    }

    public boolean isEarned() {
        return isEarned;
    }

    public void setEarned(boolean earned) {
        isEarned = earned;
    }
}