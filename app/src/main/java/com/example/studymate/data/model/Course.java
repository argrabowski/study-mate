package com.example.studymate.data.model;

import androidx.annotation.NonNull;

import java.util.ArrayList;

public class Course {
    private String name = "";
    private String creator = "";
    private String location = "";
    private String department = "";

    private String instructor = "";
    private ArrayList<String> studyGroups = new ArrayList<String>();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLocation() {
        return location;
    }
    public void setLocation(String location) {
        this.location = location;
    }

    public String getDepartment() {
        return department;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }

    public String getCreator() {
        return creator;
    }

    public void setDepartment(String department) {
        this.department = department;
    }


    public String getInstructor() {
        return instructor;
    }

    public void setInstructor(String instructor) {
        this.instructor = instructor;
    }

    public ArrayList<String> getStudyGroups() { return studyGroups; }

    public void setStudyGroups(ArrayList<String> studyGroups) {
        this.studyGroups = studyGroups;
    }
    public Course() {
    }

    @NonNull
    @Override
    public String toString() {
        super.toString();
        return this.name + " in department - " + this.department;
    }

    public Course(String name, String location, String department, ArrayList<String> studyGroups, String creator, String instructor) {
        this.name = name;
        this.department = department;
        this.location = location;
        this.studyGroups = studyGroups;
        this.creator = creator;
        this.instructor = instructor;
    }
}
