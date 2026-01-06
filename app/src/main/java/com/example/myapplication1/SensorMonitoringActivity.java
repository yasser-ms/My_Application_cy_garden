package com.example.myapplication1;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.Button;
import android.widget.TextView;
import android.widget.LinearLayout;
import android.view.View;
import android.util.TypedValue;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.myapplication1.api.MockApiService;
import com.example.myapplication1.models.SensorReading;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SensorMonitoringActivity extends BaseActivity {

    private Button btn7Days, btn30Days;
    private TextView tvHumidity, tvTemperature;
    private LinearLayout chartContainerHumidity, chartContainerTemperature;
    private int currentDays = 7;
    private String parcelId;
    private MockApiService apiService;
    private ExecutorService executor;
    private Handler mainHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sensor_monitoring);

        parcelId = getIntent().getStringExtra("parcel_id");
        if (parcelId == null) parcelId = "B4";

        apiService = MockApiService.getInstance();
        executor = Executors.newSingleThreadExecutor();
        mainHandler = new Handler(Looper.getMainLooper());

        initializeViews();
        setupButtonListeners();
        loadSensorData(currentDays);
    }

    private void initializeViews() {
        btn7Days = findViewById(R.id.btn7days);
        btn30Days = findViewById(R.id.btn30days);
        tvHumidity = findViewById(R.id.tvHumidity);
        tvTemperature = findViewById(R.id.tvTemperature);
        chartContainerHumidity = findViewById(R.id.chartContainerHumidity);
        chartContainerTemperature = findViewById(R.id.chartContainerTemperature);
    }

    private void setupButtonListeners() {
        btn7Days.setOnClickListener(v -> {
            currentDays = 7;
            btn7Days.setBackgroundTintList(getColorStateList(android.R.color.holo_green_dark));
            btn30Days.setBackgroundTintList(getColorStateList(android.R.color.holo_blue_light));
            loadSensorData(currentDays);
        });

        btn30Days.setOnClickListener(v -> {
            currentDays = 30;
            btn30Days.setBackgroundTintList(getColorStateList(android.R.color.holo_green_dark));
            btn7Days.setBackgroundTintList(getColorStateList(android.R.color.holo_blue_light));
            loadSensorData(currentDays);
        });
    }

    private void loadSensorData(int days) {
        // Show loading
        Toast.makeText(this, "Chargement des données...", Toast.LENGTH_SHORT).show();

        // Load data in background thread
        executor.execute(() -> {
            try {
                // Get current reading
                SensorReading currentReading = apiService.getLatestSensorData(parcelId);

                // Get historical data
                List<SensorReading> readings;
                if (days == 7) {
                    readings = apiService.getHistoricalData7Days(parcelId);
                } else {
                    readings = apiService.getHistoricalData30Days(parcelId);
                }

                // Update UI on main thread
                mainHandler.post(() -> {
                    updateCurrentValues(currentReading);
                    displayCharts(readings);
                });

            } catch (Exception e) {
                mainHandler.post(() ->
                        Toast.makeText(this, "Erreur: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
            }
        });
    }

    private void updateCurrentValues(SensorReading reading) {
        tvHumidity.setText(reading.getHumidity() + "%");
        tvTemperature.setText(String.format(Locale.FRENCH, "%.1f°C", reading.getTemperature()));

        // Change color for critical values
        if (reading.isCriticalHumidity()) {
            tvHumidity.setTextColor(Color.RED);
        } else {
            tvHumidity.setTextColor(Color.BLACK);
        }

        if (reading.isCriticalTemperature()) {
            tvTemperature.setTextColor(Color.RED);
        } else {
            tvTemperature.setTextColor(Color.BLACK);
        }
    }

    private void displayCharts(List<SensorReading> readings) {
        if (readings == null || readings.isEmpty()) {
            Toast.makeText(this, "Aucune donnée disponible", Toast.LENGTH_SHORT).show();
            return;
        }

        List<Integer> humidityData = new ArrayList<>();
        List<Integer> temperatureData = new ArrayList<>();

        for (SensorReading reading : readings) {
            humidityData.add(reading.getHumidity());
            temperatureData.add((int)reading.getTemperature());
        }

        displayHumidityChart(humidityData);
        displayTemperatureChart(temperatureData);
    }

    private void displayHumidityChart(List<Integer> humidityData) {
        chartContainerHumidity.removeAllViews();
        int maxHumidity = 100;

        for (int i = 0; i < humidityData.size(); i++) {
            int humidity = humidityData.get(i);
            int color = humidity < 30 || humidity > 70 ?
                    Color.parseColor("#F44336") : Color.parseColor("#4CAF50");
            String label = currentDays <= 7 ? "J" + (i + 1) : String.valueOf(i + 1);
            View bar = createBarView(humidity, maxHumidity, color, label);
            chartContainerHumidity.addView(bar);
        }
    }

    private void displayTemperatureChart(List<Integer> temperatureData) {
        chartContainerTemperature.removeAllViews();
        int maxTemperature = 40;

        for (int i = 0; i < temperatureData.size(); i++) {
            int temperature = temperatureData.get(i);
            int color = temperature < 10 || temperature > 30 ?
                    Color.parseColor("#F44336") : Color.parseColor("#FF9800");
            String label = currentDays <= 7 ? "J" + (i + 1) : String.valueOf(i + 1);
            View bar = createBarView(temperature, maxTemperature, color, label);
            chartContainerTemperature.addView(bar);
        }
    }

    private View createBarView(int value, int maxValue, int color, String label) {
        LinearLayout barLayout = new LinearLayout(this);
        barLayout.setOrientation(LinearLayout.VERTICAL);
        barLayout.setLayoutParams(new LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1.0f
        ));
        barLayout.setPadding(4, 0, 4, 0);

        // Value text
        TextView valueText = new TextView(this);
        valueText.setText(String.valueOf(value));
        valueText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 10);
        valueText.setGravity(View.TEXT_ALIGNMENT_CENTER);
        valueText.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));

        // Bar
        View bar = new View(this);
        int barHeight = Math.max(20, (int) (200 * ((float) value / maxValue)));
        bar.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                barHeight
        ));
        bar.setBackgroundColor(color);

        // Day label
        TextView dayText = new TextView(this);
        dayText.setText(label);
        dayText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 9);
        dayText.setGravity(View.TEXT_ALIGNMENT_CENTER);
        dayText.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));

        barLayout.addView(valueText);
        barLayout.addView(bar);
        barLayout.addView(dayText);

        return barLayout;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (executor != null) {
            executor.shutdown();
        }
    }
}