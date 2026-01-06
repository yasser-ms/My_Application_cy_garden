package com.example.myapplication1;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.myapplication1.adapters.NearbyParcelAdapter;
import com.example.myapplication1.database.DatabaseHelper;
import com.example.myapplication1.models.Parcel;
import com.example.myapplication1.models.ParcelLocation;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import android.content.ActivityNotFoundException;


/**
 * Activity for GPS-based parcel localization
 * Uses smartphone GPS to help students find their garden parcels
 */
public class LocationActivity extends BaseActivity {

    private static final int LOCATION_PERMISSION_REQUEST = 1001;

    // Garden center coordinates (CY University Garden - example coordinates)
    private static final double GARDEN_CENTER_LAT = 49.0350;
    private static final double GARDEN_CENTER_LON = 2.0700;

    // UI Components
    private TextView txtCurrentLocation, txtDistance, txtNearestParcel, txtGardenStatus;
    private Button btnRefreshLocation, btnNavigate, btnViewMap;
    private ProgressBar progressBar;
    private RecyclerView recyclerNearbyParcels;

    // Location
    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;
    private Location currentLocation;

    // Data
    private DatabaseHelper db;
    private List<ParcelLocation> parcelLocations;
    private NearbyParcelAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location);

        db = DatabaseHelper.getInstance(this);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        initializeParcelLocations();
        initializeViews();
        setupClickListeners();
        setupLocationCallback();

        if (checkLocationPermission()) {
            startLocationUpdates();
        } else {
            requestLocationPermission();
        }
    }

    private void initializeViews() {
        txtCurrentLocation = findViewById(R.id.txtCurrentLocation);
        txtDistance = findViewById(R.id.txtDistanceToGarden);
        txtNearestParcel = findViewById(R.id.txtNearestParcel);
        txtGardenStatus = findViewById(R.id.txtGardenStatus);
        btnRefreshLocation = findViewById(R.id.btnRefreshLocation);
        btnNavigate = findViewById(R.id.btnNavigate);
        btnViewMap = findViewById(R.id.btnViewMap);
        progressBar = findViewById(R.id.progressLocation);
        recyclerNearbyParcels = findViewById(R.id.recyclerNearbyParcels);

        recyclerNearbyParcels.setLayoutManager(new LinearLayoutManager(this));
        adapter = new NearbyParcelAdapter(this, new ArrayList<>(), this::onParcelSelected);
        recyclerNearbyParcels.setAdapter(adapter);
    }

    private void setupClickListeners() {
        btnRefreshLocation.setOnClickListener(v -> {
            progressBar.setVisibility(View.VISIBLE);
            requestSingleLocationUpdate();
        });

        btnNavigate.setOnClickListener(v -> openNavigationToGarden());

        btnViewMap.setOnClickListener(v -> {
            Intent intent = new Intent(this, GardenMapActivity.class);
            if (currentLocation != null) {
                intent.putExtra("user_lat", currentLocation.getLatitude());
                intent.putExtra("user_lon", currentLocation.getLongitude());
            }
            startActivity(intent);
        });
    }

    private void initializeParcelLocations() {
        // Initialize parcel locations relative to garden center
        // Each parcel has an offset from the garden center
        parcelLocations = new ArrayList<>();

        // Row A (North side)
        parcelLocations.add(new ParcelLocation("A1", GARDEN_CENTER_LAT + 0.0002, GARDEN_CENTER_LON - 0.0001));
        parcelLocations.add(new ParcelLocation("A2", GARDEN_CENTER_LAT + 0.0002, GARDEN_CENTER_LON + 0.0001));

        // Row B
        parcelLocations.add(new ParcelLocation("B1", GARDEN_CENTER_LAT + 0.0001, GARDEN_CENTER_LON - 0.00015));
        parcelLocations.add(new ParcelLocation("B2", GARDEN_CENTER_LAT + 0.0001, GARDEN_CENTER_LON - 0.00005));
        parcelLocations.add(new ParcelLocation("B3", GARDEN_CENTER_LAT + 0.0001, GARDEN_CENTER_LON + 0.00005));
        parcelLocations.add(new ParcelLocation("B4", GARDEN_CENTER_LAT + 0.0001, GARDEN_CENTER_LON + 0.00015));

        // Row C
        parcelLocations.add(new ParcelLocation("C1", GARDEN_CENTER_LAT, GARDEN_CENTER_LON - 0.0001));
        parcelLocations.add(new ParcelLocation("C2", GARDEN_CENTER_LAT, GARDEN_CENTER_LON + 0.0001));

        // Row D
        parcelLocations.add(new ParcelLocation("D1", GARDEN_CENTER_LAT - 0.0001, GARDEN_CENTER_LON - 0.00015));
        parcelLocations.add(new ParcelLocation("D2", GARDEN_CENTER_LAT - 0.0001, GARDEN_CENTER_LON));
        parcelLocations.add(new ParcelLocation("D3", GARDEN_CENTER_LAT - 0.0001, GARDEN_CENTER_LON + 0.00015));

        // Row E (South side)
        parcelLocations.add(new ParcelLocation("E1", GARDEN_CENTER_LAT - 0.0002, GARDEN_CENTER_LON - 0.0002));
        parcelLocations.add(new ParcelLocation("E2", GARDEN_CENTER_LAT - 0.0002, GARDEN_CENTER_LON - 0.0001));
        parcelLocations.add(new ParcelLocation("E3", GARDEN_CENTER_LAT - 0.0002, GARDEN_CENTER_LON));
        parcelLocations.add(new ParcelLocation("E4", GARDEN_CENTER_LAT - 0.0002, GARDEN_CENTER_LON + 0.0001));
        parcelLocations.add(new ParcelLocation("E5", GARDEN_CENTER_LAT - 0.0002, GARDEN_CENTER_LON + 0.0002));

        // Load parcel status from database
        for (ParcelLocation pl : parcelLocations) {
            Parcel parcel = db.getParcelByNumber(pl.getParcelId());
            if (parcel != null) {
                pl.setOccupied(parcel.isOccupied());
                pl.setPlantType(parcel.getPlantType());
                pl.setOwnerEmail(parcel.getOwnerEmail());
            }
        }
    }

    private void setupLocationCallback() {
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                Location location = locationResult.getLastLocation();
                if (location != null) {
                    currentLocation = location;
                    updateLocationUI(location);
                    progressBar.setVisibility(View.GONE);
                }
            }
        };
    }

    private boolean checkLocationPermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;
    }

    private void requestLocationPermission() {
        ActivityCompat.requestPermissions(this,
                new String[]{
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                },
                LOCATION_PERMISSION_REQUEST);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startLocationUpdates();
            } else {
                Toast.makeText(this, "Permission GPS requise pour la localisation",
                        Toast.LENGTH_LONG).show();
                txtCurrentLocation.setText("Permission GPS non accordée");
            }
        }
    }

    @SuppressLint("MissingPermission")
    private void startLocationUpdates() {
        if (!checkLocationPermission()) return;

        progressBar.setVisibility(View.VISIBLE);

        LocationRequest locationRequest = new LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 10000)
                .setWaitForAccurateLocation(false)
                .setMinUpdateIntervalMillis(5000)
                .setMaxUpdateDelayMillis(15000)
                .build();

        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());

        // Also get last known location for immediate display
        fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
            if (location != null) {
                currentLocation = location;
                updateLocationUI(location);
                progressBar.setVisibility(View.GONE);
            }
        });
    }

    @SuppressLint("MissingPermission")
    private void requestSingleLocationUpdate() {
        if (!checkLocationPermission()) {
            requestLocationPermission();
            return;
        }

        fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                .addOnSuccessListener(this, location -> {
                    if (location != null) {
                        currentLocation = location;
                        updateLocationUI(location);
                    }
                    progressBar.setVisibility(View.GONE);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Erreur de localisation: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.GONE);
                });
    }

    private void updateLocationUI(Location location) {
        // Display current coordinates
        String coordText = String.format("📍 %.6f, %.6f\nPrécision: %.1fm",
                location.getLatitude(),
                location.getLongitude(),
                location.getAccuracy());
        txtCurrentLocation.setText(coordText);

        // Calculate distance to garden center
        float[] results = new float[1];
        Location.distanceBetween(
                location.getLatitude(), location.getLongitude(),
                GARDEN_CENTER_LAT, GARDEN_CENTER_LON,
                results);
        float distanceToGarden = results[0];

        // Update distance display
        String distanceText;
        if (distanceToGarden < 1000) {
            distanceText = String.format("%.0f mètres du jardin", distanceToGarden);
        } else {
            distanceText = String.format("%.2f km du jardin", distanceToGarden / 1000);
        }
        txtDistance.setText(distanceText);

        // Update garden status
        if (distanceToGarden < 50) {
            txtGardenStatus.setText("✅ Vous êtes dans le jardin!");
            txtGardenStatus.setTextColor(getColor(android.R.color.holo_green_dark));
        } else if (distanceToGarden < 200) {
            txtGardenStatus.setText("🚶 Vous approchez du jardin");
            txtGardenStatus.setTextColor(getColor(android.R.color.holo_orange_dark));
        } else {
            txtGardenStatus.setText("📍 Jardin éloigné");
            txtGardenStatus.setTextColor(getColor(android.R.color.holo_red_dark));
        }

        // Calculate distances to all parcels and sort
        for (ParcelLocation pl : parcelLocations) {
            Location.distanceBetween(
                    location.getLatitude(), location.getLongitude(),
                    pl.getLatitude(), pl.getLongitude(),
                    results);
            pl.setDistanceFromUser(results[0]);
        }

        // Sort by distance
        Collections.sort(parcelLocations, Comparator.comparingDouble(ParcelLocation::getDistanceFromUser));

        // Update nearest parcel
        if (!parcelLocations.isEmpty()) {
            ParcelLocation nearest = parcelLocations.get(0);
            String nearestText = String.format("Parcelle %s (%.1fm)",
                    nearest.getParcelId(), nearest.getDistanceFromUser());
            if (nearest.getPlantType() != null && !nearest.getPlantType().isEmpty()) {
                nearestText += "\n🌱 " + nearest.getPlantType();
            }
            txtNearestParcel.setText(nearestText);
        }

        // Update recycler view with sorted parcels
        adapter.updateParcels(parcelLocations);
    }

    private void openNavigationToGarden() {
        String uri = String.format("google.navigation:q=%f,%f&mode=w",
                GARDEN_CENTER_LAT, GARDEN_CENTER_LON);
        Intent intent = new Intent(Intent.ACTION_VIEW, android.net.Uri.parse(uri));
        intent.setPackage("com.google.android.apps.maps");

        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        } else {
            // Fallback to browser
            String browserUri = String.format(
                    "https://www.google.com/maps/dir/?api=1&destination=%f,%f&travelmode=walking",
                    GARDEN_CENTER_LAT, GARDEN_CENTER_LON);
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, android.net.Uri.parse(browserUri));
            startActivity(browserIntent);
        }
    }

 /*   private void openParcelInMaps(ParcelLocation parcel) {
        if (parcel == null) return;

        // Crée l'URI Google Maps vers la parcelle
        String uriStr = String.format(
                "https://www.google.com/maps/dir/?api=1&destination=%f,%f&travelmode=walking",
                parcel.getLatitude(),
                parcel.getLongitude()
        );

        Uri uri = Uri.parse(uriStr);

        // Intent pour ouvrir Maps ou navigateur
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);

        try {
            // Supprimer setPackage() pour éviter les crash si Maps non installé
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
            // Si aucune application ne peut gérer l'Intent
            Toast.makeText(this, "Aucune application pour ouvrir Maps", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this, "Erreur lors de l'ouverture de Maps", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }*/

    private void openParcelInMaps(ParcelLocation parcel) {
        if (parcel == null) return;

        String uriStr = String.format(
                "https://www.google.com/maps/dir/?api=1&destination=%f,%f&travelmode=walking",
                parcel.getLatitude(),
                parcel.getLongitude()
        );

        Uri uri = Uri.parse(uriStr);
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);

        // Pas de setPackage → ouvre navigateur si Maps indisponible
        try {
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(this, "Impossible d'ouvrir Maps", Toast.LENGTH_SHORT).show();
        }
    }




    private void onParcelSelected(ParcelLocation parcel) {
        // Navigate to specific parcel
      //  String uri = String.format("google.navigation:q=%f,%f&mode=w",
        //        parcel.getLatitude(), parcel.getLongitude());
        //Intent intent = new Intent(Intent.ACTION_VIEW, android.net.Uri.parse(uri));
        //intent.setPackage("com.google.android.apps.maps");
        // Afficher toast facultatif
        Toast.makeText(this,
                "Navigation vers la parcelle " + parcel.getParcelId(),
                Toast.LENGTH_SHORT).show();

        // Ouvrir Maps
        openParcelInMaps(parcel);

       // if (intent.resolveActivity(getPackageManager()) != null) {
         //   startActivity(intent);
        //} else {
          //  Toast.makeText(this, "Google Maps non disponible", Toast.LENGTH_SHORT).show();
        //}
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (checkLocationPermission()) {
            startLocationUpdates();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        fusedLocationClient.removeLocationUpdates(locationCallback);
    }
}
