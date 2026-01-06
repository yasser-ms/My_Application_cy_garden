package com.example.myapplication1.utils;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

/**
 * Centralized error handling utility (Simplified version)
 * Provides consistent error display and logging across the app
 */
public class ErrorHandler {

    private static final String TAG = "GardenApp";

    /**
     * Show error message to user with Toast
     */
    public static void showError(Context context, String message) {
        if (context != null && message != null) {
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
            Log.e(TAG, message);
        }
    }

    /**
     * Show error with exception logging
     */
    public static void showError(Context context, String message, Exception e) {
        if (context != null && message != null) {
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
            Log.e(TAG, message, e);
        }
    }

    /**
     * Show long error message
     */
    public static void showErrorLong(Context context, String message) {
        if (context != null && message != null) {
            Toast.makeText(context, message, Toast.LENGTH_LONG).show();
            Log.e(TAG, message);
        }
    }

    /**
     * Log error without showing Toast (for background operations)
     */
    public static void logError(String message, Exception e) {
        Log.e(TAG, message, e);
    }

    /**
     * Show success message
     */
    public static void showSuccess(Context context, String message) {
        if (context != null && message != null) {
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
            Log.i(TAG, message);
        }
    }

    /**
     * Show info message
     */
    public static void showInfo(Context context, String message) {
        if (context != null && message != null) {
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
            Log.i(TAG, message);
        }
    }

    /**
     * Show debug message (only logs, no Toast)
     */
    public static void debug(String message) {
        Log.d(TAG, message);
    }

    /**
     * Show warning message
     */
    public static void showWarning(Context context, String message) {
        if (context != null && message != null) {
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
            Log.w(TAG, message);
        }
    }
}