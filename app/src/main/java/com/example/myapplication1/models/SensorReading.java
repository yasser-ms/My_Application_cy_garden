package com.example.myapplication1.models;

public class SensorReading {
    private int id;
    private String parcelId;
    private int humidity;        // Soil moisture %
    private double temperature;  // Temperature °C
    private int lightLevel;      // Light in lux
    private double ph;           // Soil pH
    private long timestamp;      // Unix timestamp

    public SensorReading() {
        this.timestamp = System.currentTimeMillis();
    }

    public SensorReading(String parcelId, int humidity, double temperature, int lightLevel, double ph) {
        this.parcelId = parcelId;
        this.humidity = humidity;
        this.temperature = temperature;
        this.lightLevel = lightLevel;
        this.ph = ph;
        this.timestamp = System.currentTimeMillis();
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getParcelId() { return parcelId; }
    public void setParcelId(String parcelId) { this.parcelId = parcelId; }

    public int getHumidity() { return humidity; }
    public void setHumidity(int humidity) { this.humidity = humidity; }

    public double getTemperature() { return temperature; }
    public void setTemperature(double temperature) { this.temperature = temperature; }

    public int getLightLevel() { return lightLevel; }
    public void setLightLevel(int lightLevel) { this.lightLevel = lightLevel; }

    public double getPh() { return ph; }
    public void setPh(double ph) { this.ph = ph; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    // Alert checking methods
    public boolean isCriticalHumidity() {
        return humidity < 30 || humidity > 70;
    }

    public boolean isCriticalTemperature() {
        return temperature < 5 || temperature > 35;
    }

    public boolean isCriticalLight() {
        return lightLevel < 300;
    }

    public boolean isCriticalPh() {
        return ph < 6.0 || ph > 7.5;
    }

    public boolean hasAnyAlert() {
        return isCriticalHumidity() || isCriticalTemperature() ||
                isCriticalLight() || isCriticalPh();
    }
}