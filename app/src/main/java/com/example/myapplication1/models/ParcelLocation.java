package com.example.myapplication1.models;

/**
 * Model class representing a parcel's GPS location
 * Used for GPS-based parcel localization feature
 */
public class ParcelLocation {
    private String parcelId;
    private double latitude;
    private double longitude;
    private double distanceFromUser; // in meters
    private boolean isOccupied;
    private String plantType;
    private String ownerEmail;

    public ParcelLocation() {}

    public ParcelLocation(String parcelId, double latitude, double longitude) {
        this.parcelId = parcelId;
        this.latitude = latitude;
        this.longitude = longitude;
        this.distanceFromUser = 0;
        this.isOccupied = false;
    }

    // Getters and Setters
    public String getParcelId() { return parcelId; }
    public void setParcelId(String parcelId) { this.parcelId = parcelId; }

    public double getLatitude() { return latitude; }
    public void setLatitude(double latitude) { this.latitude = latitude; }

    public double getLongitude() { return longitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }

    public double getDistanceFromUser() { return distanceFromUser; }
    public void setDistanceFromUser(double distanceFromUser) { this.distanceFromUser = distanceFromUser; }

    public boolean isOccupied() { return isOccupied; }
    public void setOccupied(boolean occupied) { isOccupied = occupied; }

    public String getPlantType() { return plantType; }
    public void setPlantType(String plantType) { this.plantType = plantType; }

    public String getOwnerEmail() { return ownerEmail; }
    public void setOwnerEmail(String ownerEmail) { this.ownerEmail = ownerEmail; }

    /**
     * Get formatted distance string
     */
    public String getFormattedDistance() {
        if (distanceFromUser < 1000) {
            return String.format("%.0f m", distanceFromUser);
        } else {
            return String.format("%.2f km", distanceFromUser / 1000);
        }
    }

    /**
     * Get status display string
     */
    public String getStatusDisplay() {
        if (isOccupied) {
            return plantType != null ? "🌱 " + plantType : "Occupée";
        } else {
            return "✅ Disponible";
        }
    }

    /**
     * Get direction indicator based on bearing
     */
    public String getDirectionIndicator(double userLat, double userLon) {
        double bearing = calculateBearing(userLat, userLon, latitude, longitude);

        if (bearing >= 337.5 || bearing < 22.5) return "⬆️ N";
        if (bearing >= 22.5 && bearing < 67.5) return "↗️ NE";
        if (bearing >= 67.5 && bearing < 112.5) return "➡️ E";
        if (bearing >= 112.5 && bearing < 157.5) return "↘️ SE";
        if (bearing >= 157.5 && bearing < 202.5) return "⬇️ S";
        if (bearing >= 202.5 && bearing < 247.5) return "↙️ SO";
        if (bearing >= 247.5 && bearing < 292.5) return "⬅️ O";
        if (bearing >= 292.5 && bearing < 337.5) return "↖️ NO";

        return "📍";
    }

    private double calculateBearing(double lat1, double lon1, double lat2, double lon2) {
        double dLon = Math.toRadians(lon2 - lon1);
        double lat1Rad = Math.toRadians(lat1);
        double lat2Rad = Math.toRadians(lat2);

        double y = Math.sin(dLon) * Math.cos(lat2Rad);
        double x = Math.cos(lat1Rad) * Math.sin(lat2Rad) -
                Math.sin(lat1Rad) * Math.cos(lat2Rad) * Math.cos(dLon);

        double bearing = Math.toDegrees(Math.atan2(y, x));
        return (bearing + 360) % 360;
    }
}
