package com.example.myapplication1;

import com.example.myapplication1.utils.DatePickerHelper;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.example.myapplication1.database.DatabaseHelper;
import com.example.myapplication1.models.Parcel;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Interactive Garden Map Activity
 * Displays a visual map of all garden parcels with their status
 */
public class GardenMapActivity extends BaseActivity {

    private FrameLayout mapContainer;
    private GardenMapView gardenMapView;
    private TextView txtLegend, txtSelectedParcel;
    private Button btnRefresh, btnSelectParcel;

    private DatabaseHelper db;
    private List<Parcel> parcels;
    private Parcel selectedParcel;
    private String userEmail;

    // User location (if provided)
    private double userLat = 0;
    private double userLon = 0;
    private boolean hasUserLocation = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_garden_map);

        db = DatabaseHelper.getInstance(this);
        userEmail = getSharedPreferences("GardenApp", MODE_PRIVATE).getString("user_email", "");

        // Get user location if provided
        if (getIntent().hasExtra("user_lat")) {
            userLat = getIntent().getDoubleExtra("user_lat", 0);
            userLon = getIntent().getDoubleExtra("user_lon", 0);
            hasUserLocation = true;
        }

        initializeViews();
        loadParcels();
        setupClickListeners();
    }

    private void initializeViews() {
        mapContainer = findViewById(R.id.mapContainer);
        txtLegend = findViewById(R.id.txtLegend);
        txtSelectedParcel = findViewById(R.id.txtSelectedParcel);
        btnRefresh = findViewById(R.id.btnRefresh);
        btnSelectParcel = findViewById(R.id.btnSelectParcel);

        // Set legend text
        txtLegend.setText("🟢 Disponible  🟠 Occupée  🔵 Votre parcelle");

        btnSelectParcel.setEnabled(false);
    }

    private void loadParcels() {
        parcels = db.getAllParcels();

        // Create custom map view
        gardenMapView = new GardenMapView(this);
        mapContainer.removeAllViews();
        mapContainer.addView(gardenMapView);
    }

    private void setupClickListeners() {
        btnRefresh.setOnClickListener(v -> {
            loadParcels();
            Toast.makeText(this, "Carte actualisée", Toast.LENGTH_SHORT).show();
        });

        btnSelectParcel.setOnClickListener(v -> {
            if (selectedParcel != null) {
                showParcelDetails(selectedParcel);
            }
        });
    }

    private void onParcelSelected(Parcel parcel) {
        selectedParcel = parcel;

        String info = "Parcelle #" + parcel.getParcelNumber();
        if (parcel.isOccupied()) {
            info += "\n🌱 " + (parcel.getPlantType() != null ? parcel.getPlantType() : "Occupée");
            if (parcel.getOwnerEmail() != null && parcel.getOwnerEmail().equals(userEmail)) {
                info += "\n👤 Votre parcelle";
            }
        } else {
            info += "\n✅ Disponible";
        }

        txtSelectedParcel.setText(info);
        btnSelectParcel.setEnabled(true);

        // Redraw map with selection
        gardenMapView.invalidate();
    }

    private void showParcelDetails(Parcel parcel) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Parcelle #" + parcel.getParcelNumber());

        StringBuilder details = new StringBuilder();

        if (parcel.isOccupied()) {
            details.append("Status: Occupée\n\n");

            if (parcel.getPlantType() != null) {
                details.append("🌱 Culture: ").append(parcel.getPlantType()).append("\n");
            }
            if (parcel.getPlantingDate() != null) {
                details.append("📅 Planté le: ").append(parcel.getPlantingDate()).append("\n");
            }
            if (parcel.getHarvestDate() != null) {
                details.append("🌾 Récolte prévue: ").append(parcel.getHarvestDate()).append("\n");
            }

            if (parcel.getOwnerEmail() != null && parcel.getOwnerEmail().equals(userEmail)) {
                details.append("\n👤 C'est votre parcelle!");

                builder.setNeutralButton("Voir capteurs", (d, w) -> {
                    Intent intent = new Intent(this, SensorMonitoringActivity.class);
                    intent.putExtra("parcel_id", parcel.getParcelNumber());
                    startActivity(intent);
                });

                builder.setNegativeButton("Journal", (d, w) -> {
                    Intent intent = new Intent(this, JournalActivity.class);
                    intent.putExtra("parcel_id", parcel.getParcelNumber());
                    startActivity(intent);
                });
            }
        } else {
            details.append("Status: ✅ Disponible\n\n");
            details.append("Cette parcelle est libre et peut être réservée.");

            builder.setNegativeButton("Réserver", (d, w) -> {
                showReservationDialog(parcel);
            });
        }

        builder.setMessage(details.toString());
        builder.setPositiveButton("Fermer", null);
        builder.show();
    }

    private void showReservationDialog(Parcel parcel) {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_reserve_parcel, null);

        android.widget.EditText inputPlantType = dialogView.findViewById(R.id.editPlantType);
        android.widget.EditText inputPlantingDate = dialogView.findViewById(R.id.editPlantingDate);
        android.widget.EditText inputHarvestDate = dialogView.findViewById(R.id.editHarvestDate);

        inputPlantingDate.setOnClickListener(v ->
                DatePickerHelper.showDatePicker(this, inputPlantingDate));

        inputHarvestDate.setOnClickListener(v ->
                DatePickerHelper.showDatePicker(this, inputHarvestDate));

        builder.setView(dialogView)
                .setTitle("Réserver Parcelle #" + parcel.getParcelNumber())
                .setPositiveButton("Réserver", (dialog, which) -> {
                    String plantType = inputPlantType.getText().toString().trim();
                    String plantingDate = inputPlantingDate.getText().toString().trim();
                    String harvestDate = inputHarvestDate.getText().toString().trim();

                    if (plantType.isEmpty()) {
                        Toast.makeText(this, "Type de plante requis", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    parcel.setOwnerEmail(userEmail);
                    parcel.setPlantType(plantType);
                    parcel.setPlantingDate(plantingDate.isEmpty() ?
                            new java.text.SimpleDateFormat("dd MMM yyyy", java.util.Locale.FRENCH)
                                    .format(new java.util.Date()) : plantingDate);
                    parcel.setHarvestDate(harvestDate);
                    parcel.setOccupied(true);

                    int updated = db.updateParcel(parcel);

                    if (updated > 0) {
                        Toast.makeText(this, "Parcelle réservée!", Toast.LENGTH_SHORT).show();
                        loadParcels();
                    } else {
                        Toast.makeText(this, "Erreur de réservation", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Annuler", null)
                .show();
    }

    /**
     * Custom View for drawing the garden map
     */
    private class GardenMapView extends View {

        private Paint paintAvailable, paintOccupied, paintUserParcel, paintSelected;
        private Paint paintText, paintBorder, paintPath, paintLabel;
        private Map<String, RectF> parcelRects;

        // Layout configuration
        private final String[][] layout = {
                {"", "A1", "A2", ""},
                {"B1", "B2", "B3", "B4"},
                {"", "C1", "C2", ""},
                {"D1", "D2", "D3", ""},
                {"E1", "E2", "E3", "E4", "E5"}
        };

        public GardenMapView(android.content.Context context) {
            super(context);
            init();
        }

        private void init() {
            parcelRects = new HashMap<>();

            // Available parcel (green)
            paintAvailable = new Paint();
            paintAvailable.setColor(Color.parseColor("#4CAF50"));
            paintAvailable.setStyle(Paint.Style.FILL);

            // Occupied parcel (orange)
            paintOccupied = new Paint();
            paintOccupied.setColor(Color.parseColor("#FF9800"));
            paintOccupied.setStyle(Paint.Style.FILL);

            // User's parcel (blue)
            paintUserParcel = new Paint();
            paintUserParcel.setColor(Color.parseColor("#2196F3"));
            paintUserParcel.setStyle(Paint.Style.FILL);

            // Selected parcel border
            paintSelected = new Paint();
            paintSelected.setColor(Color.parseColor("#E91E63"));
            paintSelected.setStyle(Paint.Style.STROKE);
            paintSelected.setStrokeWidth(8);

            // Text
            paintText = new Paint();
            paintText.setColor(Color.WHITE);
            paintText.setTextSize(36);
            paintText.setTextAlign(Paint.Align.CENTER);
            paintText.setAntiAlias(true);

            // Border
            paintBorder = new Paint();
            paintBorder.setColor(Color.parseColor("#333333"));
            paintBorder.setStyle(Paint.Style.STROKE);
            paintBorder.setStrokeWidth(3);

            // Path (walkways)
            paintPath = new Paint();
            paintPath.setColor(Color.parseColor("#D7CCC8"));
            paintPath.setStyle(Paint.Style.FILL);

            // Row labels
            paintLabel = new Paint();
            paintLabel.setColor(Color.parseColor("#666666"));
            paintLabel.setTextSize(32);
            paintLabel.setTextAlign(Paint.Align.CENTER);
            paintLabel.setAntiAlias(true);
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);

            int width = getWidth();
            int height = getHeight();

            // Background
            canvas.drawColor(Color.parseColor("#E8F5E9"));

            // Calculate cell size
            int maxCols = 5;
            int maxRows = layout.length;
            int padding = 40;
            int cellWidth = (width - padding * 2) / maxCols;
            int cellHeight = (height - padding * 2 - 100) / maxRows;
            int cellSize = Math.min(cellWidth, cellHeight);

            // Centeredd
            int startX = (width - maxCols * cellSize) / 2;
            int startY = padding + 80;

            // Draw title
            Paint titlePaint = new Paint();
            titlePaint.setColor(Color.parseColor("#2E7D32"));
            titlePaint.setTextSize(48);
            titlePaint.setTextAlign(Paint.Align.CENTER);
            titlePaint.setAntiAlias(true);
            titlePaint.setFakeBoldText(true);
            canvas.drawText("🌿 Jardin CYU - CATI", width / 2f, 60, titlePaint);

            // Draw row labels
            String[] rowLabels = {"A", "B", "C", "D", "E"};
            for (int row = 0; row < layout.length; row++) {
                float y = startY + row * cellSize + cellSize / 2f + 10;
                canvas.drawText(rowLabels[row], startX - 30, y, paintLabel);
            }

            // Draw parcels
            parcelRects.clear();
            for (int row = 0; row < layout.length; row++) {
                for (int col = 0; col < layout[row].length; col++) {
                    String parcelId = layout[row][col];
                    if (parcelId.isEmpty()) continue;

                    float left = startX + col * cellSize + 5;
                    float top = startY + row * cellSize + 5;
                    float right = left + cellSize - 10;
                    float bottom = top + cellSize - 10;

                    RectF rect = new RectF(left, top, right, bottom);
                    parcelRects.put(parcelId, rect);

                    // Find parcel data
                    Parcel parcel = findParcel(parcelId);
                    Paint fillPaint;

                    if (parcel != null) {
                        if (parcel.getOwnerEmail() != null && parcel.getOwnerEmail().equals(userEmail)) {
                            fillPaint = paintUserParcel;
                        } else if (parcel.isOccupied()) {
                            fillPaint = paintOccupied;
                        } else {
                            fillPaint = paintAvailable;
                        }
                    } else {
                        fillPaint = paintAvailable;
                    }

                    // Draw parcel rectangle with rounded corners
                    canvas.drawRoundRect(rect, 12, 12, fillPaint);
                    canvas.drawRoundRect(rect, 12, 12, paintBorder);

                    // Draw selection highlight
                    if (selectedParcel != null && selectedParcel.getParcelNumber().equals(parcelId)) {
                        canvas.drawRoundRect(rect, 12, 12, paintSelected);
                    }

                    // Draw parcel ID
                    canvas.drawText(parcelId, rect.centerX(), rect.centerY() - 10, paintText);

                    // Draw plant icon or status
                    Paint smallText = new Paint(paintText);
                    smallText.setTextSize(24);
                    if (parcel != null && parcel.isOccupied() && parcel.getPlantType() != null) {
                        canvas.drawText("🌱", rect.centerX(), rect.centerY() + 25, smallText);
                    } else if (parcel != null && !parcel.isOccupied()) {
                        canvas.drawText("✓", rect.centerX(), rect.centerY() + 25, smallText);
                    }
                }
            }

            // Draw compass
            drawCompass(canvas, width - 80, startY + 40);

            // Draw scale
            Paint scalePaint = new Paint();
            scalePaint.setColor(Color.parseColor("#666666"));
            scalePaint.setTextSize(24);
            scalePaint.setTextAlign(Paint.Align.CENTER);
            canvas.drawText("~3m par parcelle", width / 2f, height - 20, scalePaint);
        }

        private void drawCompass(Canvas canvas, float cx, float cy) {
            Paint compassPaint = new Paint();
            compassPaint.setColor(Color.parseColor("#666666"));
            compassPaint.setTextSize(28);
            compassPaint.setTextAlign(Paint.Align.CENTER);
            compassPaint.setAntiAlias(true);

            canvas.drawText("N", cx, cy - 25, compassPaint);
            canvas.drawText("↑", cx, cy, compassPaint);
        }

        private Parcel findParcel(String parcelId) {
            if (parcels == null) return null;
            for (Parcel p : parcels) {
                if (p.getParcelNumber().equals(parcelId)) {
                    return p;
                }
            }
            return null;
        }

        @Override
        public boolean onTouchEvent(MotionEvent event) {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                float x = event.getX();
                float y = event.getY();

                // Check which parcel was touched
                for (Map.Entry<String, RectF> entry : parcelRects.entrySet()) {
                    if (entry.getValue().contains(x, y)) {
                        Parcel parcel = findParcel(entry.getKey());
                        if (parcel != null) {
                            onParcelSelected(parcel);
                            return true;
                        }
                    }
                }
            }
            return super.onTouchEvent(event);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadParcels();
    }
}
