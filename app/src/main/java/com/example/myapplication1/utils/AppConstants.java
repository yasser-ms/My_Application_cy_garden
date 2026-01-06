package com.example.myapplication1.utils;

/**
 * Application-wide constants
 * Centralized configuration for maintainability
 */
public class AppConstants {

    // Database Configuration
    public static final int DATABASE_VERSION = 4;
    public static final String DATABASE_NAME = "garden.db";
    public static final int DATA_RETENTION_DAYS = 30;

    // SharedPreferences Keys
    public static final String PREF_NAME = "GardenApp";
    public static final String PREF_USER_EMAIL = "user_email";
    public static final String PREF_USER_UID = "user_uid";
    public static final String PREF_USER_NAME = "user_name";
    public static final String PREF_USER_ROLE = "user_role";
    public static final String PREF_IS_LOGGED_IN = "is_logged_in";

    // Sync Configuration
    public static final int SYNC_INTERVAL_MINUTES = 15;
    public static final int MAX_SYNC_RETRIES = 3;
    public static final long SYNC_BACKOFF_MS = 1000; // 1 second

    // Garden Location (CY University Garden)
    public static final double GARDEN_CENTER_LAT = 49.0350;
    public static final double GARDEN_CENTER_LON = 2.0700;
    public static final int PARCEL_PROXIMITY_METERS = 50;

    // Request Codes
    public static final int REQUEST_CAMERA_PERMISSION = 100;
    public static final int REQUEST_LOCATION_PERMISSION = 1001;
    public static final int REQUEST_SELECT_PARCEL = 1001;

    // Network Configuration
    public static final int NETWORK_TIMEOUT_MS = 5000;
    public static final int CONNECTION_CHECK_INTERVAL_MS = 10000; // 10 seconds

    // Image Configuration
    public static final int MAX_IMAGE_WIDTH = 1920;
    public static final int MAX_IMAGE_HEIGHT = 1080;
    public static final int IMAGE_QUALITY = 85; // JPEG quality 0-100

    // Sensor Data Thresholds (for alerts)
    public static final int HUMIDITY_MIN = 30;
    public static final int HUMIDITY_MAX = 70;
    public static final double TEMPERATURE_MIN = 5.0;
    public static final double TEMPERATURE_MAX = 35.0;
    public static final int LIGHT_MIN = 300;
    public static final double PH_MIN = 6.0;
    public static final double PH_MAX = 7.5;

    // UI Configuration
    public static final int RECYCLERVIEW_CACHE_SIZE = 20;
    public static final int LOADING_DELAY_MS = 500;
    public static final int SPLASH_DURATION_MS = 2000;

    // Notification Channel
    public static final String NOTIFICATION_CHANNEL_ID = "garden_alerts";
    public static final String NOTIFICATION_CHANNEL_NAME = "Garden Alerts";
    public static final int NOTIFICATION_ID_BASE = 1000;

    // Validation
    public static final String EMAIL_PATTERN = "^[A-Za-z0-9._%+-]+@cyu\\.fr$";
    public static final int MIN_PASSWORD_LENGTH = 6;
    public static final int MAX_NOTE_LENGTH = 500;

    // Date Formats
    public static final String DATE_FORMAT_DISPLAY = "dd MMM yyyy";
    public static final String DATE_FORMAT_FULL = "dd MMMM yyyy HH:mm";
    public static final String DATE_FORMAT_SHORT = "dd/MM/yyyy";

    // Private constructor to prevent instantiation
    private AppConstants() {
        throw new AssertionError("Cannot instantiate constants class");
    }
}