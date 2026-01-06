package com.example.myapplication1.utils;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import androidx.core.app.NotificationCompat;
import com.example.myapplication1.DashboardActivity;
import com.example.myapplication1.R;
import com.example.myapplication1.database.DatabaseHelper;
import com.example.myapplication1.models.NotificationRecord;
import com.example.myapplication1.models.SensorReading;

public class NotificationHelper {

    private static final String CHANNEL_ID = "garden_alerts";
    private static final String CHANNEL_NAME = "Garden Alerts";
    private static final int NOTIFICATION_ID_BASE = 1000;

    public static void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Alerts for critical garden conditions");
            channel.enableVibration(true);

            NotificationManager manager = context.getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }

    public static void sendAlertNotification(Context context, String parcelId, SensorReading reading) {
        createNotificationChannel(context);

        String title = "⚠️ Alert: Parcel " + parcelId;
        String message = buildAlertMessage(reading);
        String alertType = determineAlertType(reading);
        String severity = determineSeverity(reading);

        // Store in database
        DatabaseHelper db = DatabaseHelper.getInstance(context);
        NotificationRecord record = new NotificationRecord(parcelId, alertType, message, severity);
        db.insertNotification(record);

        // Send push notification
        Intent intent = new Intent(context, DashboardActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                0,
                intent,
                PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_plant)
                .setContentTitle(title)
                .setContentText(message)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);

        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (manager != null) {
            int notificationId = NOTIFICATION_ID_BASE + parcelId.hashCode();
            manager.notify(notificationId, builder.build());
        }
    }

    private static String buildAlertMessage(SensorReading reading) {
        StringBuilder msg = new StringBuilder();

        if (reading.isCriticalHumidity()) {
            if (reading.getHumidity() < 30) {
                msg.append("💧 Sol trop sec: ").append(reading.getHumidity()).append("%\n");
            } else {
                msg.append("💧 Sol trop humide: ").append(reading.getHumidity()).append("%\n");
            }
        }

        if (reading.isCriticalTemperature()) {
            msg.append("🌡️ Température critique: ").append(String.format("%.1f", reading.getTemperature())).append("°C\n");
        }

        if (reading.isCriticalLight()) {
            msg.append("☀️ Lumière insuffisante: ").append(reading.getLightLevel()).append(" lux\n");
        }

        if (reading.isCriticalPh()) {
            msg.append("🧪 pH déséquilibré: ").append(String.format("%.1f", reading.getPh())).append("\n");
        }

        return msg.toString().trim();
    }

    private static String determineAlertType(SensorReading reading) {
        if (reading.isCriticalHumidity()) return "humidity";
        if (reading.isCriticalTemperature()) return "temperature";
        if (reading.isCriticalLight()) return "light";
        if (reading.isCriticalPh()) return "ph";
        return "general";
    }

    private static String determineSeverity(SensorReading reading) {
        int criticalCount = 0;
        if (reading.isCriticalHumidity()) criticalCount++;
        if (reading.isCriticalTemperature()) criticalCount++;
        if (reading.isCriticalLight()) criticalCount++;
        if (reading.isCriticalPh()) criticalCount++;

        if (criticalCount >= 3) return "critical";
        if (criticalCount >= 2) return "warning";
        return "info";
    }
}