package com.example.cal.calshop.model;

import com.example.cal.calshop.utils.Constants;
import com.google.firebase.database.ServerValue;

import java.util.HashMap;

public class User {
    private String email;
    private String name;
    private HashMap<String, Object> timeStampJoined;

    public User() {}

    public User(String email, String name) {
        this.email = email;
        this.name = name;
        timeStampJoined = new HashMap<>();
        timeStampJoined.put(Constants.KEY_DATE, ServerValue.TIMESTAMP);
    }

    public String getEmail() {
        return email;
    }

    public String getName() {
        return name;
    }

    public HashMap<String, Object> getTimeStampJoined() {
        return timeStampJoined;
    }
}
