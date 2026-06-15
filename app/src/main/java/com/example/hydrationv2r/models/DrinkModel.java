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

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getMl() {
        return ml;
    }

    public void setMl(int ml) {
        this.ml = ml;
    }

    public String getIconName() {
        return iconName;
    }

    public void setIconName(String iconName) {
        this.iconName = iconName;
    }

    public int getColour() {
        return colour;
    }

    public void setColour(int colour) {
        this.colour = colour;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public int getTotalMl() {
        return totalMl;
    }

    public void setTotalMl(int totalMl) {
        this.totalMl = totalMl;
    }
}
