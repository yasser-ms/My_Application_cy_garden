package com.example.myapplication1;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import com.example.myapplication1.firebase.FirebaseAuthManager;
import androidx.appcompat.app.AppCompatActivity;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;
import com.example.myapplication1.models.Parcel;
import com.example.myapplication1.models.SensorReading;
import com.example.myapplication1.repository.GardenRepository;
import com.example.myapplication1.utils.NetworkStateMonitor;
import com.example.myapplication1.utils.NotificationHelper;
import com.example.myapplication1.utils.OfflineModeBanner;
import com.example.myapplication1.workers.SensorSyncWorker;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;


public class DashboardActivity extends BaseActivity {

    private TextView txtHumidity, txtTemp, txtLight, txtPH, txtParcelInfo, txtPlantingDate, txtHarvestDate;
    private TextView txtNetworkStatus;
    private Button btnMonitoring, btnAdvice, btnTakePhoto, btnViewPhotos, btnSelectParcel, btnJournal, btnPlantLibrary, btnNotifications;
    private Button btnLocation, btnGardenMap;
    private Handler dataHandler = new Handler();
    private GardenRepository repository;
    private String userEmail;
    private String currentParcelId = "B4";
    private static final int REQUEST_SELECT_PARCEL = 1001;

    // Network monitoring
    private NetworkStateMonitor networkMonitor;
    private OfflineModeBanner offlineBanner;

    private Button btnLogout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        repository = GardenRepository.getInstance(this);

        SharedPreferences prefs = getSharedPreferences("GardenApp", MODE_PRIVATE);
        userEmail = prefs.getString("user_email", "");

        NotificationHelper.createNotificationChannel(this);
        setupBackgroundSync();

        initializeViews();
        setupNetworkMonitoring();
        setupClickListeners();
        loadUserParcel();
        startLiveDataUpdates();
    }

    @Override
    protected boolean shouldShowBackButton() {
        return false; // Dashboard is root activity
    }

    private void initializeViews() {
        txtHumidity = findViewById(R.id.txtHumidity);
        txtTemp = findViewById(R.id.txtTemp);
        txtLight = findViewById(R.id.txtLight);
        txtPH = findViewById(R.id.txtPH);
        txtParcelInfo = findViewById(R.id.txtParcelInfo);
        txtPlantingDate = findViewById(R.id.txtPlantingDate);
        txtHarvestDate = findViewById(R.id.txtHarvestDate);
        btnMonitoring = findViewById(R.id.btnMonitoring);
        btnAdvice = findViewById(R.id.btnAdvice);
        btnTakePhoto = findViewById(R.id.btnTakePhoto);
        btnViewPhotos = findViewById(R.id.btnViewPhotos);
        btnSelectParcel = findViewById(R.id.btnSelectParcel);
        btnJournal = findViewById(R.id.btnJournal);
        btnPlantLibrary = findViewById(R.id.btnPlantLibrary);
        btnNotifications = findViewById(R.id.btnNotifications);
        btnLocation = findViewById(R.id.btnLocation);
        btnGardenMap = findViewById(R.id.btnGardenMap);
        txtNetworkStatus = findViewById(R.id.txtNetworkStatus);
        btnLogout = findViewById(R.id.btnLogout);

        // Setup offline banner
        ViewGroup rootView = findViewById(R.id.rootLayout);
        if (rootView != null) {
            offlineBanner = new OfflineModeBanner(this, rootView);
        }

        TextView txtGreeting = findViewById(R.id.txtGreeting);
        if (txtGreeting != null && userEmail != null && !userEmail.isEmpty()) {
            String userName = userEmail.split("@")[0].replace(".", " ");
            txtGreeting.setText("Bonjour, " + userName + "!");
        }
    }

    private void setupNetworkMonitoring() {
        networkMonitor = NetworkStateMonitor.getInstance(this);
        updateNetworkStatusUI();

        networkMonitor.addListener((state, isConnected) -> {
            runOnUiThread(() -> {
                updateNetworkStatusUI();
                if (offlineBanner != null) {
                    offlineBanner.updateNetworkState(state);
                }
                if (isConnected) {
                    updateSensorData();
                }
            });
        });

        if (!networkMonitor.isConnected() && offlineBanner != null) {
            offlineBanner.showOffline();
        }
    }

    private void updateNetworkStatusUI() {
        if (txtNetworkStatus != null && networkMonitor != null) {
            txtNetworkStatus.setText(networkMonitor.getNetworkStatusString());
            txtNetworkStatus.setTextColor(getColor(networkMonitor.isConnected() ?
                    android.R.color.holo_green_dark : android.R.color.holo_red_dark));
        }
    }

    private void setupClickListeners() {
        btnMonitoring.setOnClickListener(v -> {
            Intent intent = new Intent(this, SensorMonitoringActivity.class);
            intent.putExtra("parcel_id", currentParcelId);
            startActivity(intent);
        });

        btnAdvice.setOnClickListener(v -> showGardeningAdvice());

        btnTakePhoto.setOnClickListener(v -> {
            if (currentParcelId != null) {
                Intent intent = new Intent(this, CameraActivity.class);
                intent.putExtra("parcel_id", currentParcelId);
                startActivity(intent);
            } else {
                Toast.makeText(this, "Veuillez d'abord sélectionner une parcelle", Toast.LENGTH_SHORT).show();
            }
        });

        btnViewPhotos.setOnClickListener(v -> {
            if (currentParcelId != null) {
                Intent intent = new Intent(this, PhotoGalleryActivity.class);
                intent.putExtra("parcel_id", currentParcelId);
                startActivity(intent);
            } else {
                Toast.makeText(this, "Veuillez d'abord sélectionner une parcelle", Toast.LENGTH_SHORT).show();
            }
        });

        btnSelectParcel.setOnClickListener(v -> {
            Intent intent = new Intent(this, ParcelSelectionActivity.class);
            startActivityForResult(intent, REQUEST_SELECT_PARCEL);
        });

        btnJournal.setOnClickListener(v -> {
            Intent intent = new Intent(this, JournalActivity.class);
            intent.putExtra("parcel_id", currentParcelId);
            startActivity(intent);
        });

        btnPlantLibrary.setOnClickListener(v -> {
            startActivity(new Intent(this, PlantLibraryActivity.class));
        });

        btnNotifications.setOnClickListener(v -> {
            startActivity(new Intent(this, NotificationHistoryActivity.class));
        });

        btnLocation.setOnClickListener(v -> {
            startActivity(new Intent(this, LocationActivity.class));
        });

        btnGardenMap.setOnClickListener(v -> {
            startActivity(new Intent(this, GardenMapActivity.class));
        });

        btnLogout.setOnClickListener(v -> showLogoutConfirmationDialog());
    }
    /**
     * Show confirmation dialog before logout
     */
    private void showLogoutConfirmationDialog() {
        new android.app.AlertDialog.Builder(this)
                .setTitle("🚪 Déconnexion")
                .setMessage("Êtes-vous sûr de vouloir vous déconnecter?")
                .setPositiveButton("Oui, déconnexion", (dialog, which) -> performLogout())
                .setNegativeButton("Annuler", null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    private void performLogout() {
        // Stop any ongoing handlers
        if (dataHandler != null) {
            dataHandler.removeCallbacksAndMessages(null);
        }

        // Sign out from Firebase
        FirebaseAuthManager authManager = FirebaseAuthManager.getInstance(this);
        authManager.signOut();

        // Clear SharedPreferences
        SharedPreferences prefs = getSharedPreferences("GardenApp", MODE_PRIVATE);
        prefs.edit().clear().apply();

        // Show success message
        Toast.makeText(this, "Déconnexion réussie", Toast.LENGTH_SHORT).show();

        // Navigate to login
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void loadUserParcel() {
        repository.getUserParcels(userEmail, new GardenRepository.DataCallback<List<Parcel>>() {
            @Override
            public void onSuccess(List<Parcel> parcels) {
                if (!parcels.isEmpty()) {
                    Parcel parcel = parcels.get(0);
                    currentParcelId = parcel.getParcelNumber();
                    updateParcelInfo(parcel);
                } else {
                    repository.getParcelByNumber("B4", new GardenRepository.DataCallback<Parcel>() {
                        @Override
                        public void onSuccess(Parcel parcel) {
                            if (parcel != null) {
                                currentParcelId = parcel.getParcelNumber();
                                updateParcelInfo(parcel);
                            }
                        }

                        @Override
                        public void onError(String message) {
                            txtParcelInfo.setText("Parcelle #B4 - Tomates Cerises");
                        }
                    });
                }
            }

            @Override
            public void onError(String message) {
                Toast.makeText(DashboardActivity.this, "Erreur: " + message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateParcelInfo(Parcel parcel) {
        String plantType = parcel.getPlantType() != null ? parcel.getPlantType() : "Tomates Cerises";
        txtParcelInfo.setText("Parcelle #" + parcel.getParcelNumber() + " - " + plantType);

        String plantingDate = parcel.getPlantingDate() != null ? parcel.getPlantingDate() : "15 Mars 2024";
        String harvestDate = parcel.getHarvestDate() != null ? parcel.getHarvestDate() : "15 Juin 2024";

        txtPlantingDate.setText(plantingDate);
        txtHarvestDate.setText(harvestDate);
    }

    private void startLiveDataUpdates() {
        dataHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                updateSensorData();
                dataHandler.postDelayed(this, 10000);
            }
        }, 1000);
    }

    private void updateSensorData() {
        // Only fetch if connected, otherwise use cached data
        if (networkMonitor != null && !networkMonitor.isConnected()) {
            return; // Skip API call if offline
        }

        repository.getLatestSensorReading(currentParcelId, new GardenRepository.DataCallback<SensorReading>() {
            @Override
            public void onSuccess(SensorReading reading) {
                displaySensorData(reading);
            }

            @Override
            public void onError(String message) {
                // Don't show toast for every sync error when offline
                if (networkMonitor != null && networkMonitor.isConnected()) {
                    Toast.makeText(DashboardActivity.this, "Erreur de synchronisation", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void displaySensorData(SensorReading reading) {
        txtHumidity.setText(reading.getHumidity() + "%");
        txtTemp.setText(String.format(Locale.FRENCH, "%.1f°C", reading.getTemperature()));
        txtLight.setText(reading.getLightLevel() + " lux");
        txtPH.setText(String.format(Locale.FRENCH, "%.1f", reading.getPh()));
        setAlertColors(reading);
    }

    private void setAlertColors(SensorReading reading) {
        txtHumidity.setTextColor(getColor(reading.isCriticalHumidity() ?
                android.R.color.holo_red_dark : android.R.color.white));

        txtTemp.setTextColor(getColor(reading.isCriticalTemperature() ?
                android.R.color.holo_red_dark : android.R.color.white));

        txtLight.setTextColor(getColor(reading.isCriticalLight() ?
                android.R.color.holo_red_dark : android.R.color.white));

        txtPH.setTextColor(getColor(reading.isCriticalPh() ?
                android.R.color.holo_red_dark : android.R.color.white));
    }

    private void showGardeningAdvice() {
        repository.getLatestSensorReading(currentParcelId, new GardenRepository.DataCallback<SensorReading>() {
            @Override
            public void onSuccess(SensorReading reading) {
                String advice = generateSmartAdvice(reading);
                new android.app.AlertDialog.Builder(DashboardActivity.this)
                        .setTitle("💡 Conseil de Jardinage")
                        .setMessage(advice)
                        .setPositiveButton("OK", null)
                        .show();
            }

            @Override
            public void onError(String message) {
                showGenericAdvice();
            }
        });
    }

    private String generateSmartAdvice(SensorReading reading) {
        StringBuilder advice = new StringBuilder();

        if (reading.isCriticalHumidity()) {
            if (reading.getHumidity() < 30) {
                advice.append("💧 Sol trop sec! Arrosez abondamment.\n\n");
            } else {
                advice.append("💧 Sol trop humide. Réduisez l'arrosage.\n\n");
            }
        } else if (reading.getHumidity() < 45) {
            advice.append("💧 Sol légèrement sec. Arrosage recommandé.\n\n");
        } else {
            advice.append("💧 Humidité optimale. Continuez ainsi!\n\n");
        }

        if (reading.isCriticalTemperature()) {
            if (reading.getTemperature() < 10) {
                advice.append("🌡️ Température trop basse. Protégez vos plantes.\n\n");
            } else {
                advice.append("🌡️ Température élevée. Ombrage recommandé.\n\n");
            }
        }

        if (reading.isCriticalLight()) {
            advice.append("☀️ Lumière insuffisante. Repositionnez vers zone ensoleillée.\n\n");
        }

        if (reading.isCriticalPh()) {
            advice.append("🧪 pH déséquilibré. Ajustez avec amendements.\n\n");
        }

        if (advice.length() == 0) {
            advice.append("✅ Conditions optimales!\n\nVos plantes se portent bien.");
        }

        return advice.toString().trim();
    }

    private void showGenericAdvice() {
        String[] adviceList = {
                "💧 Arrosez régulièrement, de préférence le matin.",
                "🌱 Fertilisez avec un engrais naturel mensuel.",
                "☀️ Assurez 6-8h d'ensoleillement quotidien.",
                "🐛 Surveillez les parasites sur les feuilles.",
                "✂️ Taillez pour stimuler la croissance.",
                "🌧️ Protégez des fortes pluies."
        };
        int index = (int) (System.currentTimeMillis() % adviceList.length);

        new android.app.AlertDialog.Builder(this)
                .setTitle("💡 Conseil de Jardinage")
                .setMessage(adviceList[index])
                .setPositiveButton("OK", null)
                .show();
    }

    private void setupBackgroundSync() {
        PeriodicWorkRequest syncWork = new PeriodicWorkRequest.Builder(
                SensorSyncWorker.class,
                15, TimeUnit.MINUTES
        ).build();
        WorkManager.getInstance(this).enqueue(syncWork);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_SELECT_PARCEL && resultCode == RESULT_OK && data != null) {
            String selectedParcelId = data.getStringExtra("PARCEL_ID");
            if (selectedParcelId != null) {
                currentParcelId = selectedParcelId;
                loadUserParcel();
                updateSensorData();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateNetworkStatusUI();
        updateSensorData();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        dataHandler.removeCallbacksAndMessages(null);
    }
}