package com.example.myapplication1.api;

import com.example.myapplication1.models.SensorReading;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Mock API Service simulating REST endpoints
 * In production, replace with Retrofit/OkHttp
 */
public class MockApiService {

    private static MockApiService instance;
    private final Random random;

    private MockApiService() {
        this.random = new Random();
    }

    public static synchronized MockApiService getInstance() {
        if (instance == null) {
            instance = new MockApiService();
        }
        return instance;
    }

    /**
     * Simulates GET /api/sensors/{parcelId}/latest
     * Generates realistic sensor data with natural variations
     */
    public SensorReading getLatestSensorData(String parcelId) {
        // Simulate network delay
        simulateNetworkDelay();

        // Base values for realistic data
        int baseHumidity = 50;
        double baseTemp = 20;
        int baseLight = 800;
        double basePh = 6.8;

        // Add realistic variations based on time of day
        long currentHour = (System.currentTimeMillis() / (1000 * 60 * 60)) % 24;

        // Temperature varies by time of day
        double tempVariation = Math.sin((currentHour - 6) * Math.PI / 12) * 8;

        // Light varies significantly by time (high at noon, low at night)
        double lightMultiplier = Math.max(0.1, Math.sin((currentHour - 6) * Math.PI / 12));

        // Humidity tends to be higher at night
        double humidityVariation = -Math.sin((currentHour - 6) * Math.PI / 12) * 10;

        int humidity = (int) Math.max(20, Math.min(80,
                baseHumidity + humidityVariation + random.nextInt(15) - 7));

        double temperature = Math.max(5, Math.min(35,
                baseTemp + tempVariation + (random.nextDouble() * 4 - 2)));

        int lightLevel = (int) Math.max(50, Math.min(1500,
                baseLight * lightMultiplier + random.nextInt(200) - 100));

        double ph = Math.max(5.5, Math.min(8.0,
                basePh + (random.nextDouble() * 0.6 - 0.3)));

        return new SensorReading(parcelId, humidity, temperature, lightLevel, ph);
    }

    /**
     * Simulates GET /api/sensors/{parcelId}/history?days=7
     * Returns 7 days of historical sensor data
     */
    public List<SensorReading> getHistoricalData7Days(String parcelId) {
        return getHistoricalData(parcelId, 7);
    }

    /**
     * Simulates GET /api/sensors/{parcelId}/history?days=30
     * Returns 30 days of historical sensor data
     */
    public List<SensorReading> getHistoricalData30Days(String parcelId) {
        return getHistoricalData(parcelId, 30);
    }

    /**
     * Generates realistic historical sensor data for specified number of days
     */
    private List<SensorReading> getHistoricalData(String parcelId, int days) {
        simulateNetworkDelay();

        List<SensorReading> readings = new ArrayList<>();
        long now = System.currentTimeMillis();
        long dayInMillis = 24 * 60 * 60 * 1000;

        for (int i = days - 1; i >= 0; i--) {
            long timestamp = now - (i * dayInMillis);

            // Base values
            int baseHumidity = 55;
            double baseTemp = 22;
            int baseLight = 850;
            double basePh = 6.7;

            // Add realistic variations based on day cycle
            double dayFactor = i * 0.2;
            int humidity = (int) Math.max(25, Math.min(85,
                    baseHumidity + Math.sin(dayFactor) * 20 + (random.nextInt(10) - 5)));

            double temperature = Math.max(8, Math.min(32,
                    baseTemp + Math.sin(dayFactor + 0.5) * 8 + (random.nextDouble() * 4 - 2)));

            int lightLevel = (int) Math.max(300, Math.min(1400,
                    baseLight + Math.sin(dayFactor + 1) * 400 + (random.nextInt(100) - 50)));

            double ph = Math.max(5.8, Math.min(7.8,
                    basePh + Math.sin(dayFactor * 0.3) * 0.6 + (random.nextDouble() * 0.2 - 0.1)));

            SensorReading reading = new SensorReading(parcelId, humidity, temperature, lightLevel, ph);
            reading.setTimestamp(timestamp);
            readings.add(reading);
        }

        return readings;
    }

    /**
     * Simulates POST /api/sensors
     * In real API, this would save data to server
     */
    public boolean uploadSensorReading(SensorReading reading) {
        simulateNetworkDelay();
        // Simulate 95% success rate
        return random.nextInt(100) < 95;
    }

    /**
     * Check API connectivity
     */
    public boolean checkConnection() {
        try {
            simulateNetworkDelay();
            // Simulate 90% uptime
            return random.nextInt(100) < 90;
        } catch (Exception e) {
            return false;
        }
    }

    private void simulateNetworkDelay() {
        try {
            // Simulate 100-300ms network latency
            Thread.sleep(100 + random.nextInt(200));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}