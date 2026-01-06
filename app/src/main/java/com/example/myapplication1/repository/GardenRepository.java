package com.example.myapplication1.repository;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import com.example.myapplication1.api.MockApiService;
import com.example.myapplication1.database.DatabaseHelper;
import com.example.myapplication1.models.SensorReading;
import com.example.myapplication1.models.Parcel;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Repository pattern - single source of truth for data
 */
public class GardenRepository {

    private static GardenRepository instance;
    private final DatabaseHelper databaseHelper;
    private final MockApiService apiService;
    private final ExecutorService executorService;
    private final Handler mainHandler;

    private GardenRepository(Context context) {
        this.databaseHelper = DatabaseHelper.getInstance(context);
        this.apiService = MockApiService.getInstance();
        this.executorService = Executors.newFixedThreadPool(3);
        this.mainHandler = new Handler(Looper.getMainLooper());
    }

    public static synchronized GardenRepository getInstance(Context context) {
        if (instance == null) {
            instance = new GardenRepository(context.getApplicationContext());
        }
        return instance;
    }

    // ==================== SENSOR DATA OPERATIONS ====================

    public void getLatestSensorReading(String parcelId, DataCallback<SensorReading> callback) {
        executorService.execute(() -> {
            try {
                SensorReading reading = apiService.getLatestSensorData(parcelId);
                databaseHelper.insertSensorReading(reading);
                mainHandler.post(() -> callback.onSuccess(reading));
            } catch (Exception e) {
                SensorReading cached = databaseHelper.getLatestReading(parcelId);
                if (cached != null) {
                    mainHandler.post(() -> callback.onSuccess(cached));
                } else {
                    mainHandler.post(() -> callback.onError(e.getMessage()));
                }
            }
        });
    }

    public void getHistoricalReadings(String parcelId, int days, DataCallback<List<SensorReading>> callback) {
        executorService.execute(() -> {
            try {
                List<SensorReading> readings = databaseHelper.getReadingsByParcel(parcelId, days);
                mainHandler.post(() -> callback.onSuccess(readings));
            } catch (Exception e) {
                mainHandler.post(() -> callback.onError(e.getMessage()));
            }
        });
    }

    public void syncSensorData(String parcelId) {
        executorService.execute(() -> {
            try {
                SensorReading reading = apiService.getLatestSensorData(parcelId);
                databaseHelper.insertSensorReading(reading);
            } catch (Exception e) {
                // Silent fail for background sync
            }
        });
    }

    public void cleanOldData() {
        executorService.execute(() -> databaseHelper.deleteOldReadings(30));
    }

    // ==================== PARCEL OPERATIONS ====================

    public void getUserParcels(String userEmail, DataCallback<List<Parcel>> callback) {
        executorService.execute(() -> {
            try {
                List<Parcel> parcels = databaseHelper.getParcelsByOwner(userEmail);
                mainHandler.post(() -> callback.onSuccess(parcels));
            } catch (Exception e) {
                mainHandler.post(() -> callback.onError(e.getMessage()));
            }
        });
    }

    public void getAllParcels(DataCallback<List<Parcel>> callback) {
        executorService.execute(() -> {
            try {
                List<Parcel> parcels = databaseHelper.getAllParcels();
                mainHandler.post(() -> callback.onSuccess(parcels));
            } catch (Exception e) {
                mainHandler.post(() -> callback.onError(e.getMessage()));
            }
        });
    }

    public void getParcelByNumber(String parcelNumber, DataCallback<Parcel> callback) {
        executorService.execute(() -> {
            try {
                Parcel parcel = databaseHelper.getParcelByNumber(parcelNumber);
                mainHandler.post(() -> {
                    if (parcel != null) {
                        callback.onSuccess(parcel);
                    } else {
                        callback.onError("Parcel not found");
                    }
                });
            } catch (Exception e) {
                mainHandler.post(() -> callback.onError(e.getMessage()));
            }
        });
    }

    public void reserveParcel(Parcel parcel, DataCallback<Boolean> callback) {
        executorService.execute(() -> {
            try {
                parcel.setOccupied(true);
                int updated = databaseHelper.updateParcel(parcel);
                boolean success = updated > 0;
                mainHandler.post(() -> {
                    if (success) {
                        callback.onSuccess(true);
                    } else {
                        callback.onError("Failed to reserve parcel");
                    }
                });
            } catch (Exception e) {
                mainHandler.post(() -> callback.onError(e.getMessage()));
            }
        });
    }

    public void releaseParcel(String parcelNumber, DataCallback<Boolean> callback) {
        executorService.execute(() -> {
            try {
                Parcel parcel = databaseHelper.getParcelByNumber(parcelNumber);
                if (parcel != null) {
                    parcel.setOccupied(false);
                    parcel.setOwnerEmail(null);
                    parcel.setPlantType(null);
                    int updated = databaseHelper.updateParcel(parcel);
                    boolean success = updated > 0;
                    mainHandler.post(() -> callback.onSuccess(success));
                } else {
                    mainHandler.post(() -> callback.onError("Parcel not found"));
                }
            } catch (Exception e) {
                mainHandler.post(() -> callback.onError(e.getMessage()));
            }
        });
    }

    // ==================== CALLBACK INTERFACE ====================

    public interface DataCallback<T> {
        void onSuccess(T data);
        void onError(String message);
    }

    public void shutdown() {
        executorService.shutdown();
    }
}