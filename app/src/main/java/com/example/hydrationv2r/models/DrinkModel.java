package com.example.hydrationv2r.models;

public class DrinkModel {

    public int id;
    public String name;
    public int ml;
    public String iconName;
    public int colour;

    // Used when a DrinkModel contains an entire days worth of drink history for a specific drink
    // for reporting drink history
    public int count = 1;
    public int totalMl = 0;

    public DrinkModel(int id, String name, int ml, String iconName, int colour) {
        this.id = id;
        this.name = name;
        this.ml = ml;
        this.iconName = iconName;
        this.colour = colour;
    }

}
