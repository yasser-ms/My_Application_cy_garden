package com.example.myapplication1.utils;

import android.app.DatePickerDialog;
import android.content.Context;
import android.widget.EditText;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

/**
 * Helper class for date picker functionality
 * Provides consistent date selection across the app
 */
public class DatePickerHelper {

    private static final SimpleDateFormat DATE_FORMAT =
            new SimpleDateFormat("dd MMM yyyy", Locale.FRENCH);

    /**
     * Show date picker dialog and update EditText
     * @param context Context for dialog
     * @param editText EditText to update with selected date
     */
    public static void showDatePicker(Context context, EditText editText) {
        Calendar calendar = Calendar.getInstance();

        // Try to parse existing date if present
        String existingDate = editText.getText().toString();
        if (!existingDate.isEmpty()) {
            try {
                calendar.setTime(DATE_FORMAT.parse(existingDate));
            } catch (Exception e) {
                // Use current date if parsing fails
                calendar = Calendar.getInstance();
            }
        }

        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                context,
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    Calendar selectedDate = Calendar.getInstance();
                    selectedDate.set(selectedYear, selectedMonth, selectedDay);
                    String formattedDate = DATE_FORMAT.format(selectedDate.getTime());
                    editText.setText(formattedDate);
                },
                year, month, day
        );

        datePickerDialog.show();
    }

    /**
     * Show date picker with minimum date constraint
     */
    public static void showDatePickerWithMinDate(Context context, EditText editText, long minDate) {
        Calendar calendar = Calendar.getInstance();

        String existingDate = editText.getText().toString();
        if (!existingDate.isEmpty()) {
            try {
                calendar.setTime(DATE_FORMAT.parse(existingDate));
            } catch (Exception e) {
                calendar = Calendar.getInstance();
            }
        }

        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                context,
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    Calendar selectedDate = Calendar.getInstance();
                    selectedDate.set(selectedYear, selectedMonth, selectedDay);
                    String formattedDate = DATE_FORMAT.format(selectedDate.getTime());
                    editText.setText(formattedDate);
                },
                year, month, day
        );

        datePickerDialog.getDatePicker().setMinDate(minDate);
        datePickerDialog.show();
    }

    /**
     * Format date for display
     */
    public static String formatDate(Calendar calendar) {
        return DATE_FORMAT.format(calendar.getTime());
    }

    /**
     * Get current date formatted
     */
    public static String getCurrentDate() {
        return DATE_FORMAT.format(Calendar.getInstance().getTime());
    }
}