package com.example.myapplication1.models;

public class NotificationRecord {
    private int id;
    private String parcelId;
    private String alertType; // "humidity", "temperature", "light", "ph", "harvest"
    private String message;
    private String severity; // "critical", "warning", "info"
    private boolean isRead;
    private long timestamp;

    public NotificationRecord() {}

    public NotificationRecord(String parcelId, String alertType, String message, String severity) {
        this.parcelId = parcelId;
        this.alertType = alertType;
        this.message = message;
        this.severity = severity;
        this.isRead = false;
        this.timestamp = System.currentTimeMillis();
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getParcelId() { return parcelId; }
    public void setParcelId(String parcelId) { this.parcelId = parcelId; }

    public String getAlertType() { return alertType; }
    public void setAlertType(String alertType) { this.alertType = alertType; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getSeverity() { return severity; }
    public void setSeverity(String severity) { this.severity = severity; }

    public boolean isRead() { return isRead; }
    public void setRead(boolean read) { isRead = read; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    public String getAlertIcon() {
        switch (alertType) {
            case "humidity": return "💧";
            case "temperature": return "🌡️";
            case "light": return "☀️";
            case "ph": return "🧪";
            case "harvest": return "🌾";
            default: return "⚠️";
        }
    }
}