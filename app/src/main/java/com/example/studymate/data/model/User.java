package com.example.studymate.data.model;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.HashMap;

public class User {
    private String email = "";
    private String displayName = "";
    private String username = "";
    private String location = "";
    private String accountRole = "";

    private HashMap<String, ArrayList<Boolean>> schedule = null;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public HashMap<String, ArrayList<Boolean>> getSchedule() {
        return schedule;
    }

    public void setSchedule(HashMap<String, ArrayList<Boolean>> schedules) {
        this.schedule = schedules;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public void setAccountRole(String accountRole) {
        this.accountRole = accountRole;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getUsername() {
        return username;
    }

    public String getLocation() {
        return location;
    }

    public String getAccountRole() {
        return accountRole;
    }

    public User() {
    }

    @NonNull
    @Override
    public String toString() {
        super.toString();
        return "Username: " + username + ", Display Name: " + displayName + " " + ", Account Role: " + accountRole + ", Schedule: "  + schedule;
    }

    public User(String email, String displayName, String username, String location, String accountRole, HashMap<String, ArrayList<Boolean>> schedules) {
        this.email = email;
        this.displayName = displayName;
        this.username = username;
        this.location = location;
        this.accountRole = accountRole;
        this.schedule = schedules;
    }
}
