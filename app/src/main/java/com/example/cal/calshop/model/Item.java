package com.example.cal.calshop.model;

public class Item {
    String itemName;
    String owner;

    public Item() {}

    public Item(String itemName, String owner) {
        this.itemName = itemName;
        this.owner = owner;
    }

    public String getItemName() {
        return itemName;
    }

    public String getOwner() {
        return owner;
    }
}
