package com.example.cal.calshop.model;

import com.example.cal.calshop.utils.Constants;
import com.google.firebase.database.ServerValue;

import java.util.HashMap;

public class ShoppingList {
    private static final String DATE_KEY = Constants.KEY_DATE;

    private String listName;
    private String owner;
    private boolean isShopping;
    private HashMap<String, User> usersShopping;
    private HashMap<String, Object> dateCreated;
    private HashMap<String, Object> dateLastChanged;

    public ShoppingList() {
    }

    public ShoppingList(String listName, String owner) {
        this.listName = listName;
        this.owner = owner;
        isShopping = false;
        usersShopping = new HashMap<>();
        //usersShopping.put("TestUser", new User("hi@hi.com", "testBoy"));
        //usersShopping.put("TestUser2", new User("hi@hip.com", "testGirl"));
        //usersShopping.put("TestUser3", new User("hi@hippy.com", "testMan"));

        HashMap<String, Object> dateCreatedObj = new HashMap<String, Object>();
        dateCreatedObj.put(DATE_KEY, ServerValue.TIMESTAMP);
        this.dateCreated = dateCreatedObj;

        //Date last changed will always be set to ServerValue.TIMESTAMP
        HashMap<String, Object> dateLastChangedObj = new HashMap<String, Object>();
        dateLastChangedObj.put(DATE_KEY, ServerValue.TIMESTAMP);
        this.dateLastChanged = dateLastChangedObj;
    }

    public String getListName() {
        return listName;
    }

    public String getOwner() {
        return owner;
    }

    public boolean isShopping() {
        return isShopping;
    }

    public void setShopping(boolean shopping) {
        isShopping = shopping;
    }

    public HashMap<String, User> getUsersShopping() {
        return usersShopping;
    }

    public void setUsersShopping(HashMap<String, User> usersShopping) {
        this.usersShopping = usersShopping;
    }

    public HashMap<String, Object> getDateCreated() {
        return dateCreated;
    }

    public HashMap<String, Object> getDateLastChanged() {
        return dateLastChanged;
    }

}
