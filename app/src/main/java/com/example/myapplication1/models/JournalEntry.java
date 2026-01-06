package com.example.myapplication1.models;

public class JournalEntry {
    private String id;
    private String parcelId;
    private String userEmail;
    private String entryType; // "observation", "watering", "fertilizing", "pruning", "harvest"
    private String notes;
    private double waterAmount; // in liters, for watering entries
    private long timestamp;

    public JournalEntry() {}

    public JournalEntry(String parcelId, String userEmail, String entryType, String notes) {
        this.id = System.currentTimeMillis() + "_" + parcelId;
        this.parcelId = parcelId;
        this.userEmail = userEmail;
        this.entryType = entryType;
        this.notes = notes;
        this.timestamp = System.currentTimeMillis();
    }

    public JournalEntry(String parcelId, String userEmail, String entryType, String notes, double waterAmount) {
        this(parcelId, userEmail, entryType, notes);
        this.waterAmount = waterAmount;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getParcelId() { return parcelId; }
    public void setParcelId(String parcelId) { this.parcelId = parcelId; }

    public String getUserEmail() { return userEmail; }
    public void setUserEmail(String userEmail) { this.userEmail = userEmail; }

    public String getEntryType() { return entryType; }
    public void setEntryType(String entryType) { this.entryType = entryType; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public double getWaterAmount() { return waterAmount; }
    public void setWaterAmount(double waterAmount) { this.waterAmount = waterAmount; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    public String getEntryTypeDisplay() {
        switch (entryType) {
            case "observation": return "📝 Observation";
            case "watering": return "💧 Arrosage";
            case "fertilizing": return "🌱 Fertilisation";
            case "pruning": return "✂️ Taille";
            case "harvest": return "🌾 Récolte";
            default: return "📋 Autre";
        }
    }
}