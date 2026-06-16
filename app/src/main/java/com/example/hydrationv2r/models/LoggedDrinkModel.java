package com.example.hydrationv2r.models;

public class LoggedDrinkModel {
    public int logId;
    public DrinkModel drink;
    public int amount;
    public long timestamp;

    public LoggedDrinkModel(int logId, DrinkModel drink, int amount, long timestamp) {
        this.logId = logId;
        this.drink = drink;
        this.amount = amount;
        this.timestamp = timestamp;
    }
}