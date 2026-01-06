package com.example.myapplication1;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import com.example.myapplication1.api.MockApiService;
import com.example.myapplication1.models.SensorReading;
import com.example.myapplication1.utils.NetworkStateMonitor;
import com.example.myapplication1.utils.OfflineModeBanner;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.LimitLine;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Sensor Monitoring Activity with MPAndroidChart integration
 * Displays real-time sensor data with interactive charts
 */
public class SensorMonitoringActivityMPChart extends BaseActivity {

    // UI Components
    private Button btn7Days, btn30Days;
    private TextView tvHumidity, tvTemperature, tvLight, tvPH;
    private TextView tvHumidityStatus, tvTempStatus, tvLightStatus, tvPHStatus;
    private CardView cardHumidity, cardTemperature, cardLight, cardPH;
    private LineChart chartHumidity, chartTemperature, chartLight, chartPH;
    private ProgressBar progressBar;
    private TextView tvLastUpdate, tvParcelInfo;

    // Data
    private int currentDays = 7;
    private String parcelId;
    private MockApiService apiService;
    private ExecutorService executor;
    private Handler mainHandler;

    // Network monitoring
    private NetworkStateMonitor networkMonitor;
    private OfflineModeBanner offlineBanner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sensor_monitoring_mpchart);

        parcelId = getIntent().getStringExtra("parcel_id");
        if (parcelId == null) parcelId = "B4";

        apiService = MockApiService.getInstance();
        executor = Executors.newSingleThreadExecutor();
        mainHandler = new Handler(Looper.getMainLooper());

        // Initialize network monitoring
        networkMonitor = NetworkStateMonitor.getInstance(this);

        initializeViews();
        setupCharts();
        setupButtonListeners();
        setupNetworkMonitoring();
        loadSensorData(currentDays);
    }

    private void initializeViews() {
        // Buttons
        btn7Days = findViewById(R.id.btn7days);
        btn30Days = findViewById(R.id.btn30days);

        // Current values
        tvHumidity = findViewById(R.id.tvHumidity);
        tvTemperature = findViewById(R.id.tvTemperature);
        tvLight = findViewById(R.id.tvLight);
        tvPH = findViewById(R.id.tvPH);

        // Status texts
        tvHumidityStatus = findViewById(R.id.tvHumidityStatus);
        tvTempStatus = findViewById(R.id.tvTempStatus);
        tvLightStatus = findViewById(R.id.tvLightStatus);
        tvPHStatus = findViewById(R.id.tvPHStatus);

        // Cards
        cardHumidity = findViewById(R.id.cardHumidity);
        cardTemperature = findViewById(R.id.cardTemperature);
        cardLight = findViewById(R.id.cardLight);
        cardPH = findViewById(R.id.cardPH);

        // Charts (MPAndroidChart)
        chartHumidity = findViewById(R.id.chartHumidity);
        chartTemperature = findViewById(R.id.chartTemperature);
        chartLight = findViewById(R.id.chartLight);
        chartPH = findViewById(R.id.chartPH);

        // Other
        progressBar = findViewById(R.id.progressBar);
        tvLastUpdate = findViewById(R.id.tvLastUpdate);
        tvParcelInfo = findViewById(R.id.tvParcelInfo);

        tvParcelInfo.setText("Parcelle #" + parcelId);

        // Setup offline banner
        View rootView = findViewById(android.R.id.content);
        if (rootView instanceof android.view.ViewGroup) {
            offlineBanner = new OfflineModeBanner(this, (android.view.ViewGroup) rootView);
        }
    }

    private void setupCharts() {
        // Configure all charts with common settings
        configureChart(chartHumidity, "Humidité (%)", 0, 100);
        configureChart(chartTemperature, "Température (°C)", -10, 50);
        configureChart(chartLight, "Lumière (lux)", 0, 2000);
        configureChart(chartPH, "pH", 0, 14);

        // Add limit lines for optimal ranges
        addHumidityLimitLines();
        addTemperatureLimitLines();
        addLightLimitLines();
        addPHLimitLines();
    }

    private void configureChart(LineChart chart, String label, float min, float max) {
        chart.setTouchEnabled(true);
        chart.setDragEnabled(true);
        chart.setScaleEnabled(true);
        chart.setPinchZoom(true);
        chart.setDrawGridBackground(false);
        chart.setBackgroundColor(Color.TRANSPARENT);

        // Description
        Description desc = new Description();
        desc.setText("");
        chart.setDescription(desc);

        // Legend
        Legend legend = chart.getLegend();
        legend.setEnabled(true);
        legend.setTextColor(Color.DKGRAY);
        legend.setTextSize(12f);

        // X-Axis
        XAxis xAxis = chart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setGranularity(1f);
        xAxis.setTextColor(Color.DKGRAY);

        // Y-Axis (Left)
        YAxis leftAxis = chart.getAxisLeft();
        leftAxis.setAxisMinimum(min);
        leftAxis.setAxisMaximum(max);
        leftAxis.setDrawGridLines(true);
        leftAxis.setGridColor(Color.LTGRAY);
        leftAxis.setTextColor(Color.DKGRAY);

        // Y-Axis (Right) - disabled
        chart.getAxisRight().setEnabled(false);

        // Value selection listener
        chart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(Entry e, Highlight h) {
                Toast.makeText(SensorMonitoringActivityMPChart.this,
                        String.format(Locale.FRENCH, "Jour %d: %.1f", (int)e.getX() + 1, e.getY()),
                        Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onNothingSelected() {}
        });

        // Animation
        chart.animateX(1000);
    }

    private void addHumidityLimitLines() {
        YAxis leftAxis = chartHumidity.getAxisLeft();

        // Low humidity limit (30%)
        LimitLine lowLimit = new LimitLine(30f, "Sec");
        lowLimit.setLineColor(Color.parseColor("#FF9800"));
        lowLimit.setLineWidth(2f);
        lowLimit.setTextColor(Color.parseColor("#FF9800"));
        lowLimit.setTextSize(10f);
        leftAxis.addLimitLine(lowLimit);

        // High humidity limit (70%)
        LimitLine highLimit = new LimitLine(70f, "Humide");
        highLimit.setLineColor(Color.parseColor("#2196F3"));
        highLimit.setLineWidth(2f);
        highLimit.setTextColor(Color.parseColor("#2196F3"));
        highLimit.setTextSize(10f);
        leftAxis.addLimitLine(highLimit);
    }

    private void addTemperatureLimitLines() {
        YAxis leftAxis = chartTemperature.getAxisLeft();

        // Low temp limit (5°C)
        LimitLine lowLimit = new LimitLine(5f, "Froid");
        lowLimit.setLineColor(Color.parseColor("#2196F3"));
        lowLimit.setLineWidth(2f);
        lowLimit.setTextColor(Color.parseColor("#2196F3"));
        lowLimit.setTextSize(10f);
        leftAxis.addLimitLine(lowLimit);

        // High temp limit (35°C)
        LimitLine highLimit = new LimitLine(35f, "Chaud");
        highLimit.setLineColor(Color.parseColor("#F44336"));
        highLimit.setLineWidth(2f);
        highLimit.setTextColor(Color.parseColor("#F44336"));
        highLimit.setTextSize(10f);
        leftAxis.addLimitLine(highLimit);
    }

    private void addLightLimitLines() {
        YAxis leftAxis = chartLight.getAxisLeft();

        // Low light limit (300 lux)
        LimitLine lowLimit = new LimitLine(300f, "Faible");
        lowLimit.setLineColor(Color.parseColor("#FF9800"));
        lowLimit.setLineWidth(2f);
        lowLimit.setTextColor(Color.parseColor("#FF9800"));
        lowLimit.setTextSize(10f);
        leftAxis.addLimitLine(lowLimit);
    }

    private void addPHLimitLines() {
        YAxis leftAxis = chartPH.getAxisLeft();

        // pH 6.0 - lower optimal
        LimitLine lowLimit = new LimitLine(6.0f, "Acide");
        lowLimit.setLineColor(Color.parseColor("#FF9800"));
        lowLimit.setLineWidth(2f);
        lowLimit.setTextColor(Color.parseColor("#FF9800"));
        lowLimit.setTextSize(10f);
        leftAxis.addLimitLine(lowLimit);

        // pH 7.5 - upper optimal
        LimitLine highLimit = new LimitLine(7.5f, "Alcalin");
        highLimit.setLineColor(Color.parseColor("#9C27B0"));
        highLimit.setLineWidth(2f);
        highLimit.setTextColor(Color.parseColor("#9C27B0"));
        highLimit.setTextSize(10f);
        leftAxis.addLimitLine(highLimit);
    }

    private void setupButtonListeners() {
        btn7Days.setOnClickListener(v -> {
            currentDays = 7;
            updateButtonStates();
            loadSensorData(currentDays);
        });

        btn30Days.setOnClickListener(v -> {
            currentDays = 30;
            updateButtonStates();
            loadSensorData(currentDays);
        });
    }

    private void updateButtonStates() {
        if (currentDays == 7) {
            btn7Days.setBackgroundTintList(getColorStateList(android.R.color.holo_green_dark));
            btn30Days.setBackgroundTintList(getColorStateList(android.R.color.holo_blue_light));
        } else {
            btn30Days.setBackgroundTintList(getColorStateList(android.R.color.holo_green_dark));
            btn7Days.setBackgroundTintList(getColorStateList(android.R.color.holo_blue_light));
        }
    }

    private void setupNetworkMonitoring() {
        networkMonitor.addListener((state, isConnected) -> {
            mainHandler.post(() -> {
                if (offlineBanner != null) {
                    offlineBanner.updateNetworkState(state);
                }

                // Reload data when connection is restored
                if (isConnected && offlineBanner != null && offlineBanner.isShowing()) {
                    loadSensorData(currentDays);
                }
            });
        });

        // Check initial state
        if (!networkMonitor.isConnected() && offlineBanner != null) {
            offlineBanner.showOffline();
        }
    }

    private void loadSensorData(int days) {
        progressBar.setVisibility(View.VISIBLE);

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

                mainHandler.post(() -> {
                    updateCurrentValues(currentReading);
                    updateCharts(readings);
                    progressBar.setVisibility(View.GONE);

                    // Update last sync time
                    SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss", Locale.FRENCH);
                    tvLastUpdate.setText("Dernière mise à jour: " + sdf.format(new Date()));
                });

            } catch (Exception e) {
                mainHandler.post(() -> {
                    Toast.makeText(this, "Erreur: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.GONE);
                });
            }
        });
    }

    private void updateCurrentValues(SensorReading reading) {
        // Humidity
        tvHumidity.setText(reading.getHumidity() + "%");
        updateStatusCard(cardHumidity, tvHumidityStatus, reading.isCriticalHumidity(),
                reading.getHumidity() < 30 ? "Sol trop sec!" :
                        reading.getHumidity() > 70 ? "Sol trop humide!" : "Optimal");

        // Temperature
        tvTemperature.setText(String.format(Locale.FRENCH, "%.1f°C", reading.getTemperature()));
        updateStatusCard(cardTemperature, tvTempStatus, reading.isCriticalTemperature(),
                reading.getTemperature() < 5 ? "Trop froid!" :
                        reading.getTemperature() > 35 ? "Trop chaud!" : "Optimal");

        // Light
        tvLight.setText(reading.getLightLevel() + " lux");
        updateStatusCard(cardLight, tvLightStatus, reading.isCriticalLight(),
                reading.getLightLevel() < 300 ? "Lumière insuffisante!" : "Optimal");

        // pH
        tvPH.setText(String.format(Locale.FRENCH, "%.1f", reading.getPh()));
        updateStatusCard(cardPH, tvPHStatus, reading.isCriticalPh(),
                reading.getPh() < 6.0 ? "Trop acide!" :
                        reading.getPh() > 7.5 ? "Trop alcalin!" : "Optimal");
    }

    private void updateStatusCard(CardView card, TextView statusText, boolean isCritical, String status) {
        statusText.setText(status);
        if (isCritical) {
            card.setCardBackgroundColor(Color.parseColor("#FFEBEE")); // Light red
            statusText.setTextColor(Color.parseColor("#D32F2F"));
        } else {
            card.setCardBackgroundColor(Color.parseColor("#E8F5E9")); // Light green
            statusText.setTextColor(Color.parseColor("#388E3C"));
        }
    }

    private void updateCharts(List<SensorReading> readings) {
        if (readings == null || readings.isEmpty()) return;

        // Prepare data arrays
        List<Entry> humidityEntries = new ArrayList<>();
        List<Entry> temperatureEntries = new ArrayList<>();
        List<Entry> lightEntries = new ArrayList<>();
        List<Entry> phEntries = new ArrayList<>();
        List<String> labels = new ArrayList<>();

        for (int i = 0; i < readings.size(); i++) {
            SensorReading r = readings.get(i);
            humidityEntries.add(new Entry(i, r.getHumidity()));
            temperatureEntries.add(new Entry(i, (float) r.getTemperature()));
            lightEntries.add(new Entry(i, r.getLightLevel()));
            phEntries.add(new Entry(i, (float) r.getPh()));

            if (currentDays <= 7) {
                labels.add("J" + (i + 1));
            } else {
                labels.add(String.valueOf(i + 1));
            }
        }

        // Update each chart
        updateChart(chartHumidity, humidityEntries, labels, "Humidité", Color.parseColor("#2196F3"));
        updateChart(chartTemperature, temperatureEntries, labels, "Température", Color.parseColor("#FF9800"));
        updateChart(chartLight, lightEntries, labels, "Lumière", Color.parseColor("#FFC107"));
        updateChart(chartPH, phEntries, labels, "pH", Color.parseColor("#9C27B0"));
    }

    private void updateChart(LineChart chart, List<Entry> entries, List<String> labels,
                             String label, int color) {
        LineDataSet dataSet = new LineDataSet(entries, label);
        dataSet.setColor(color);
        dataSet.setCircleColor(color);
        dataSet.setLineWidth(2f);
        dataSet.setCircleRadius(4f);
        dataSet.setDrawCircleHole(true);
        dataSet.setCircleHoleRadius(2f);
        dataSet.setValueTextSize(10f);
        dataSet.setValueTextColor(Color.DKGRAY);
        dataSet.setDrawFilled(true);
        dataSet.setFillColor(color);
        dataSet.setFillAlpha(50);
        dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);

        // Highlight
        dataSet.setHighLightColor(Color.parseColor("#E91E63"));
        dataSet.setHighlightLineWidth(1.5f);

        LineData lineData = new LineData(dataSet);
        chart.setData(lineData);

        // Update X-axis labels
        chart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(labels));

        chart.invalidate();
        chart.animateX(500);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (executor != null) {
            executor.shutdown();
        }
    }
}