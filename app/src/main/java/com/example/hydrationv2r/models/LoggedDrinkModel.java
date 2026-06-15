package com.example.hydrationv2r.models;

public class LoggedDrinkModel {
    public int logId;
    public DrinkModel drink;
    public int amount;
    public String timestamp;

    public LoggedDrinkModel(int logId, DrinkModel drink, int amount, String timestamp) {
        this.logId = logId;
        this.drink = drink;
        this.amount = amount;
        this.timestamp = timestamp;
    }
}