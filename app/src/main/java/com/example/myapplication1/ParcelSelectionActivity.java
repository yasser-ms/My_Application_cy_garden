package com.example.myapplication1;

import com.example.myapplication1.utils.DatePickerHelper;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.myapplication1.database.DatabaseHelper;
import com.example.myapplication1.models.Parcel;
import java.util.List;

public class ParcelSelectionActivity extends BaseActivity {
    private RecyclerView recyclerView;
    private ParcelAdapter adapter;
    private String userEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_parcel_selection);

        userEmail = getSharedPreferences("GardenApp", MODE_PRIVATE)
                .getString("user_email", "");

        initializeViews();
        loadParcels();
    }

    private void initializeViews() {
        recyclerView = findViewById(R.id.recyclerParcels);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 3));
    }

    private void loadParcels() {
        DatabaseHelper db = DatabaseHelper.getInstance(this);
        List<Parcel> parcels = db.getAllParcels();

        adapter = new ParcelAdapter(parcels);
        recyclerView.setAdapter(adapter);
    }

    private class ParcelAdapter extends RecyclerView.Adapter<ParcelAdapter.ParcelViewHolder> {
        private List<Parcel> parcels;

        ParcelAdapter(List<Parcel> parcels) {
            this.parcels = parcels;
        }

        @NonNull
        @Override
        public ParcelViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_parcel, parent, false);
            return new ParcelViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ParcelViewHolder holder, int position) {
            holder.bind(parcels.get(position));
        }

        @Override
        public int getItemCount() {
            return parcels.size();
        }

        class ParcelViewHolder extends RecyclerView.ViewHolder {
            CardView cardParcel;
            TextView txtParcelNumber, txtStatus;

            ParcelViewHolder(View itemView) {
                super(itemView);
                cardParcel = itemView.findViewById(R.id.cardParcel);
                txtParcelNumber = itemView.findViewById(R.id.txtParcelNumber);
                txtStatus = itemView.findViewById(R.id.txtParcelStatus);
            }

            void bind(Parcel parcel) {
                txtParcelNumber.setText(parcel.getParcelNumber());

                if (parcel.isOccupied()) {
                    txtStatus.setText("Occupée");
                    txtStatus.setTextColor(getColor(android.R.color.holo_red_dark));
                    cardParcel.setCardBackgroundColor(getColor(android.R.color.darker_gray));
                    itemView.setEnabled(false);
                } else {
                    txtStatus.setText("Disponible");
                    txtStatus.setTextColor(getColor(android.R.color.holo_green_dark));
                    cardParcel.setCardBackgroundColor(getColor(android.R.color.white));

                    itemView.setOnClickListener(v -> showReservationDialog(parcel));
                }
            }
        }
    }

    private void showReservationDialog(Parcel parcel) {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_reserve_parcel, null);

        EditText inputPlantType = dialogView.findViewById(R.id.editPlantType);
        EditText inputPlantingDate = dialogView.findViewById(R.id.editPlantingDate);
        EditText inputHarvestDate = dialogView.findViewById(R.id.editHarvestDate);

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

                    DatabaseHelper db = DatabaseHelper.getInstance(this);
                    int updated = db.updateParcel(parcel);

                    if (updated > 0) {
                        Toast.makeText(this, "Parcelle réservée!", Toast.LENGTH_SHORT).show();
                        loadParcels();
                        setResult(RESULT_OK);
                    } else {
                        Toast.makeText(this, "Erreur de réservation", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Annuler", null)
                .show();
    }
}