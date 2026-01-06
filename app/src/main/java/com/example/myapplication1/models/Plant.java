package com.example.myapplication1.models;

public class Plant {
    private int id;
    private String name;
    private String scientificName;
    private String category;
    private String plantingPeriod;
    private String harvestPeriod;
    private String careInstructions;
    private String wateringFrequency;
    private String sunlightRequirement;
    private String soilType;
    private String compatibility; // Comma-separated compatible plants
    private int growthDurationDays;

    public Plant() {}

    public Plant(String name, String scientificName, String category,
                 String plantingPeriod, String harvestPeriod, String careInstructions,
                 String wateringFrequency, String sunlightRequirement, String soilType,
                 String compatibility, int growthDurationDays) {
        this.name = name;
        this.scientificName = scientificName;
        this.category = category;
        this.plantingPeriod = plantingPeriod;
        this.harvestPeriod = harvestPeriod;
        this.careInstructions = careInstructions;
        this.wateringFrequency = wateringFrequency;
        this.sunlightRequirement = sunlightRequirement;
        this.soilType = soilType;
        this.compatibility = compatibility;
        this.growthDurationDays = growthDurationDays;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getScientificName() { return scientificName; }
    public void setScientificName(String scientificName) { this.scientificName = scientificName; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getPlantingPeriod() { return plantingPeriod; }
    public void setPlantingPeriod(String plantingPeriod) { this.plantingPeriod = plantingPeriod; }

    public String getHarvestPeriod() { return harvestPeriod; }
    public void setHarvestPeriod(String harvestPeriod) { this.harvestPeriod = harvestPeriod; }

    public String getCareInstructions() { return careInstructions; }
    public void setCareInstructions(String careInstructions) { this.careInstructions = careInstructions; }

    public String getWateringFrequency() { return wateringFrequency; }
    public void setWateringFrequency(String wateringFrequency) { this.wateringFrequency = wateringFrequency; }

    public String getSunlightRequirement() { return sunlightRequirement; }
    public void setSunlightRequirement(String sunlightRequirement) { this.sunlightRequirement = sunlightRequirement; }

    public String getSoilType() { return soilType; }
    public void setSoilType(String soilType) { this.soilType = soilType; }

    public String getCompatibility() { return compatibility; }
    public void setCompatibility(String compatibility) { this.compatibility = compatibility; }

    public int getGrowthDurationDays() { return growthDurationDays; }
    public void setGrowthDurationDays(int growthDurationDays) { this.growthDurationDays = growthDurationDays; }
}