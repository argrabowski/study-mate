package com.example.studymate.data.model;

import androidx.annotation.NonNull;

import java.io.Serializable;
import java.util.Date;

public class File implements Serializable {
    private String filePath = "";
    private String name = "";
    private String description = "";
    private String tanslateLanguage = "";

    private String creator = "";

    private String transscript = "";

    private Date cratedDate = new Date();

    private String downloadURL = "";

    private String translatedText = "";


    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getName() {return this.name;}

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {return this.description;}

    public void setDescription(String description) {
        this.description = description;
    }

    public String getTanslateLanguage() {return  this.tanslateLanguage;}

    private void setTanslateLanguage(String tanslateLanguage) {this.tanslateLanguage = tanslateLanguage;}

    public String getCreator() {
        return creator;
    }

    public String getTransscript() {
        return transscript;
    }


    public void setCreator(String creator) {
        this.creator = creator;
    }

    public void setTransscript(String transscript) {
        this.transscript = transscript;
    }

    public String getTranslatedText() {
        return translatedText;
    }

    public void setTranslatedText(String translatedText) {
        this.translatedText = translatedText;
    }

    public File() {
    }

    @NonNull
    @Override
    public String toString() {
        super.toString();
        return "FileName: " + name + ", Creator: " + creator;
    }

    public Date getCratedDate() {
        return cratedDate;
    }

    public void setCratedDate(Date cratedDate) {
        this.cratedDate = cratedDate;
    }

    public String getDownloadURL() {
        return downloadURL;
    }

    public void setDownloadURL(String downloadURL) {
        this.downloadURL = downloadURL;
    }

    public File(String filePath, String name, String description, String tanslateLanguage, String creator, String transscript, Date createdDate, String downloadURL, String translatedText) {
        this.filePath = filePath;
        this.name = name;
        this.description = description;
        this.tanslateLanguage = tanslateLanguage;
        this.creator = creator;
        this.transscript = transscript;
        this.cratedDate = createdDate;
        this.downloadURL = downloadURL;
        this.translatedText = translatedText;
    }
}
