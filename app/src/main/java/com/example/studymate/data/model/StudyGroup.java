package com.example.studymate.data.model;

import androidx.annotation.NonNull;

import java.util.ArrayList;

public class StudyGroup {
    private String name = "";
    private String creator = "";
    private String location = "";
    private String department = "";

    private String id = "";
    private String course = "";

    private ArrayList<String> messages = new ArrayList<String>();

    private String description = "";
    private ArrayList<String> members = new ArrayList<String>();
    private String meetingSchedule = "";

    private Boolean isPrivate = false;

    public StudyGroup() {
    }

    public Boolean getIsPrivate() {
        return isPrivate;
    }

    public void setPrivate(Boolean aPrivate) {
        isPrivate = aPrivate;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCreator() {
        return creator;
    }

    public void setCreator(String creator) {
        this.creator = creator;
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

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getCourse() {
        return course;
    }

    public void setCourse(String course) {
        this.course = course;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public ArrayList<String> getMembers() {
        return members;
    }

    public void setMembers(ArrayList<String> members) {
        this.members = members;
    }

    public String getMeetingSchedule() {
        return meetingSchedule;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Boolean getPrivate() {
        return isPrivate;
    }

    public void setMeetingSchedule(String meetingSchedule) {
        this.meetingSchedule = meetingSchedule;
    }

    public ArrayList<String> getMessages() {
        return messages;
    }

    public void setMessages(ArrayList<String> messages) {
        this.messages = messages;
    }

    public StudyGroup(String name, String creator, String location, String department, String course, String description, ArrayList<String> members, String meetingSchedule, Boolean isPrivate, ArrayList<String> messages) {
        this.name = name;
        this.creator = creator;
        this.location = location;
        this.department = department;
        this.course = course;
        this.description = description;
        this.members = members;
        this.meetingSchedule = meetingSchedule;
        this.isPrivate = isPrivate;
        this.messages = messages;
    }

    @NonNull
    @Override
    public String toString() {
        super.toString();
        return "id: " + this.id + " with name: " + this.name + ", in department - " + this.department ;
    }


}
