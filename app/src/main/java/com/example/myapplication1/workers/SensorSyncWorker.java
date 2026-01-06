package com.example.myapplication1.workers;

import android.content.Context;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;
import com.example.myapplication1.api.MockApiService;
import com.example.myapplication1.database.DatabaseHelper;
import com.example.myapplication1.models.Parcel;
import com.example.myapplication1.models.SensorReading;
import com.example.myapplication1.utils.NotificationHelper;
import java.util.List;

public class SensorSyncWorker extends Worker {

    private static final String TAG = "SensorSyncWorker";

    public SensorSyncWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull
    @Override
    public Result doWork() {
        try {
            Log.d(TAG, "Starting sensor sync...");

            DatabaseHelper dbHelper = DatabaseHelper.getInstance(getApplicationContext());
            MockApiService apiService = MockApiService.getInstance();

            List<String> parcelIds = getOccupiedParcelIds(dbHelper);

            int synced = 0;
            int alerts = 0;

            for (String parcelId : parcelIds) {
                try {
                    SensorReading reading = apiService.getLatestSensorData(parcelId);
                    dbHelper.insertSensorReading(reading);
                    synced++;

                    if (reading.hasAnyAlert()) {
                        NotificationHelper.sendAlertNotification(
                                getApplicationContext(),
                                parcelId,
                                reading
                        );
                        alerts++;
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error syncing parcel " + parcelId, e);
                }
            }

            dbHelper.deleteOldReadings(30);

            Log.d(TAG, "Sync complete: " + synced + " parcels, " + alerts + " alerts");
            return Result.success();

        } catch (Exception e) {
            Log.e(TAG, "Sync failed", e);
            return Result.retry();
        }
    }

    private List<String> getOccupiedParcelIds(DatabaseHelper dbHelper) {
        List<String> parcelIds = new java.util.ArrayList<>();

        List<Parcel> parcels = dbHelper.getAllParcels();
        for (Parcel parcel : parcels) {
            if (parcel.isOccupied()) {
                parcelIds.add(parcel.getParcelNumber());
            }
        }

        if (parcelIds.isEmpty()) {
            parcelIds.add("B4");
        }

        return parcelIds;
    }
}