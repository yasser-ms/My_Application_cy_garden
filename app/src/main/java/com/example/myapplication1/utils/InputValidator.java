package com.example.myapplication1.utils;

import android.text.TextUtils;
import android.widget.EditText;
import java.util.regex.Pattern;

/**
 * Input validation utility
 * Provides consistent validation across the app
 */
public class InputValidator {

    // Email pattern for CYU email addresses
    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9._%+-]+@cyu\\.fr$");

    // Phone pattern (French format)
    private static final Pattern PHONE_PATTERN =
            Pattern.compile("^0[1-9][0-9]{8}$");

    /**
     * Validate CYU email address
     */
    public static boolean isValidEmail(String email) {
        return !TextUtils.isEmpty(email) && EMAIL_PATTERN.matcher(email).matches();
    }

    /**
     * Validate password (minimum 6 characters)
     */
    public static boolean isValidPassword(String password) {
        return !TextUtils.isEmpty(password) && password.length() >= AppConstants.MIN_PASSWORD_LENGTH;
    }

    /**
     * Validate phone number
     */
    public static boolean isValidPhone(String phone) {
        return !TextUtils.isEmpty(phone) && PHONE_PATTERN.matcher(phone).matches();
    }

    /**
     * Validate required field
     */
    public static boolean isNotEmpty(String text) {
        return !TextUtils.isEmpty(text) && !text.trim().isEmpty();
    }

    /**
     * Validate EditText and show error if invalid
     */
    public static boolean validateRequired(EditText editText, String errorMessage) {
        String text = editText.getText().toString().trim();
        if (TextUtils.isEmpty(text)) {
            editText.setError(errorMessage);
            editText.requestFocus();
            return false;
        }
        editText.setError(null);
        return true;
    }

    /**
     * Validate email EditText
     */
    public static boolean validateEmail(EditText editText) {
        String email = editText.getText().toString().trim();
        if (!isValidEmail(email)) {
            editText.setError("Email @cyu.fr requis");
            editText.requestFocus();
            return false;
        }
        editText.setError(null);
        return true;
    }

    /**
     * Validate password EditText
     */
    public static boolean validatePassword(EditText editText) {
        String password = editText.getText().toString();
        if (!isValidPassword(password)) {
            editText.setError("Minimum " + AppConstants.MIN_PASSWORD_LENGTH + " caractères");
            editText.requestFocus();
            return false;
        }
        editText.setError(null);
        return true;
    }

    /**
     * Validate numeric input
     */
    public static boolean isValidNumber(String text) {
        if (TextUtils.isEmpty(text)) return false;
        try {
            Double.parseDouble(text);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * Validate positive number
     */
    public static boolean isPositiveNumber(String text) {
        if (!isValidNumber(text)) return false;
        try {
            return Double.parseDouble(text) > 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * Validate length constraints
     */
    public static boolean isWithinLengthLimit(String text, int maxLength) {
        return !TextUtils.isEmpty(text) && text.length() <= maxLength;
    }

    /**
     * Sanitize user input (remove special characters)
     */
    public static String sanitizeInput(String input) {
        if (TextUtils.isEmpty(input)) return "";
        // Remove potentially dangerous characters
        return input.replaceAll("[<>\"'%;()&+]", "");
    }

    /**
     * Validate parcel ID format (e.g., "A1", "B4")
     */
    public static boolean isValidParcelId(String parcelId) {
        if (TextUtils.isEmpty(parcelId)) return false;
        return parcelId.matches("^[A-E][1-5]$");
    }

    /**
     * Validate sensor reading ranges
     */
    public static boolean isValidHumidity(int humidity) {
        return humidity >= 0 && humidity <= 100;
    }

    public static boolean isValidTemperature(double temperature) {
        return temperature >= -20 && temperature <= 50;
    }

    public static boolean isValidPH(double ph) {
        return ph >= 0 && ph <= 14;
    }

    public static boolean isValidLightLevel(int lightLevel) {
        return lightLevel >= 0 && lightLevel <= 2000;
    }
}