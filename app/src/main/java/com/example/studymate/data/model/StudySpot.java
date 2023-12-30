package com.example.studymate.data.model;

import androidx.annotation.NonNull;

import com.google.type.LatLng;

import java.util.ArrayList;
import java.util.Date;

public class StudySpot {
    private String name = "";
    private String description = "";
    private String location = "";
    private String parentLocation = null;
    private String lat_lng = null;
    private String creator = "";

    private Integer radius = null;

    private Integer occupants = 0;
    private Integer maxOccupants = null;

    private ArrayList<String> members = new ArrayList();

    public ArrayList<String> getMembers() {
        return members;
    }

    public void setMembers(ArrayList<String> members) {
        this.members = members;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getParentLocation() {
        return parentLocation;
    }

    public void setParentLocation(String parentLocation) {
        this.parentLocation = parentLocation;
    }

    public String getLat_lng() {
        return lat_lng;
    }

    public void setLat_lng(String lat_lng) {
        this.lat_lng = lat_lng;
    }

    public String getCreator() {
        return creator;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }

    public String getName() {return this.name;}

    public void setName(String name) {
        this.name = name;
    }


    public Integer getOccupants() {
        return occupants;
    }

    public void setOccupants(Integer occupants) {
        this.occupants = occupants;
    }

    public Integer getMaxOccupants() {
        return maxOccupants;
    }

    public void setMaxOccupants(Integer maxOccupants) {
        this.maxOccupants = maxOccupants;
    }

    public StudySpot() {
    }

    public Integer getRadius() {
        return radius;
    }

    public void setRadius(Integer radius) {
        this.radius = radius;
    }

    @NonNull
    @Override
    public String toString() {
        super.toString();
        return "Study Spot: " + name + ", occupants: " + occupants;
    }

    public StudySpot(String name, String description, String location, String parentLocation, String lat_lng, String creator, Integer radius, Integer maxOccupants, Integer occupants, ArrayList<String> members) {
        this.name = name;
        this.description = description;
        this.location = location;
        this.parentLocation = parentLocation;
        this.lat_lng = lat_lng;
        this.creator = creator;
        this.radius = radius;
        this.maxOccupants = maxOccupants;
        this.occupants = occupants;
        this.members = members;
    }
}
