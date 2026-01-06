package com.example.myapplication1.models;

public class Parcel {
    private int id;
    private String parcelNumber;  // e.g., "A1", "B4"
    private String ownerEmail;
    private String plantType;
    private String plantingDate;
    private String harvestDate;
    private boolean isOccupied;

    public Parcel() {}

    public Parcel(String parcelNumber, String ownerEmail, String plantType,
                  String plantingDate, String harvestDate) {
        this.parcelNumber = parcelNumber;
        this.ownerEmail = ownerEmail;
        this.plantType = plantType;
        this.plantingDate = plantingDate;
        this.harvestDate = harvestDate;
        this.isOccupied = true;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getParcelNumber() { return parcelNumber; }
    public void setParcelNumber(String parcelNumber) { this.parcelNumber = parcelNumber; }

    public String getOwnerEmail() { return ownerEmail; }
    public void setOwnerEmail(String ownerEmail) { this.ownerEmail = ownerEmail; }

    public String getPlantType() { return plantType; }
    public void setPlantType(String plantType) { this.plantType = plantType; }

    public String getPlantingDate() { return plantingDate; }
    public void setPlantingDate(String plantingDate) { this.plantingDate = plantingDate; }

    public String getHarvestDate() { return harvestDate; }
    public void setHarvestDate(String harvestDate) { this.harvestDate = harvestDate; }

    public boolean isOccupied() { return isOccupied; }
    public void setOccupied(boolean occupied) { isOccupied = occupied; }
}