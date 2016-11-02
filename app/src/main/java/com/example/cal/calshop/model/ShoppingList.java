package com.example.cal.calshop.model;

import com.example.cal.calshop.utils.Constants;
import com.google.firebase.database.ServerValue;

import java.util.HashMap;

/**
 * Created by Cal on 11/1/2016.
 */

public class ShoppingList {
    public static final String DATE_KEY = Constants.KEY_DATE;

    String listName;
    String owner;
    HashMap<String, Object> dateCreated;
    HashMap<String, Object> dateLastChanged;

    public ShoppingList() {
    }

    public ShoppingList(String listName, String owner) {
        this.listName = listName;
        this.owner = owner;

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

    public HashMap<String, Object> getDateCreated() {
        return dateCreated;
    }

    public HashMap<String, Object> getDateLastChanged() {
        return dateLastChanged;
    }

}
