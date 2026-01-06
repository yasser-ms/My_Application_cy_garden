package com.example.myapplication1.models;

public class Photo {
    private String id;
    private String parcelId;
    private String userEmail;
    private String filePath;
    private String notes;
    private long timestamp;

    public Photo() {}

    public Photo(String parcelId, String userEmail, String filePath, String notes) {
        this.id = System.currentTimeMillis() + "_" + parcelId;
        this.parcelId = parcelId;
        this.userEmail = userEmail;
        this.filePath = filePath;
        this.notes = notes;
        this.timestamp = System.currentTimeMillis();
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getParcelId() { return parcelId; }
    public void setParcelId(String parcelId) { this.parcelId = parcelId; }

    public String getUserEmail() { return userEmail; }
    public void setUserEmail(String userEmail) { this.userEmail = userEmail; }

    public String getFilePath() { return filePath; }
    public void setFilePath(String filePath) { this.filePath = filePath; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
}