package com.example.myapplication1;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.myapplication1.database.DatabaseHelper;
import com.example.myapplication1.models.Plant;
import java.util.List;

public class PlantLibraryActivity extends BaseActivity {

    private EditText searchInput;
    private RecyclerView recyclerView;
    private PlantAdapter adapter;
    private DatabaseHelper db;
    private TextView txtEmpty;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_plant_library);

        db = DatabaseHelper.getInstance(this);
        initializeViews();
        loadPlants("");
    }

    private void initializeViews() {
        searchInput = findViewById(R.id.searchInput);
        recyclerView = findViewById(R.id.recyclerPlants);
        txtEmpty = findViewById(R.id.txtEmpty);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        searchInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                loadPlants(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void loadPlants(String query) {
        List<Plant> plants;
        if (query.isEmpty()) {
            plants = db.getAllPlants();
        } else {
            plants = db.searchPlants(query);
        }

        if (plants.isEmpty()) {
            txtEmpty.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            txtEmpty.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
            adapter = new PlantAdapter(plants);
            recyclerView.setAdapter(adapter);
        }
    }

    private class PlantAdapter extends RecyclerView.Adapter<PlantAdapter.PlantViewHolder> {
        private List<Plant> plants;

        PlantAdapter(List<Plant> plants) {
            this.plants = plants;
        }

        @NonNull
        @Override
        public PlantViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_plant, parent, false);
            return new PlantViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull PlantViewHolder holder, int position) {
            holder.bind(plants.get(position));
        }

        @Override
        public int getItemCount() {
            return plants.size();
        }

        class PlantViewHolder extends RecyclerView.ViewHolder {
            TextView txtName, txtScientific, txtCategory;
            CardView cardView;

            PlantViewHolder(View itemView) {
                super(itemView);
                cardView = itemView.findViewById(R.id.cardPlant);
                txtName = itemView.findViewById(R.id.txtPlantName);
                txtScientific = itemView.findViewById(R.id.txtScientificName);
                txtCategory = itemView.findViewById(R.id.txtCategory);
            }

            void bind(Plant plant) {
                txtName.setText(plant.getName());
                txtScientific.setText(plant.getScientificName());
                txtCategory.setText(plant.getCategory());

                itemView.setOnClickListener(v -> showPlantDetail(plant));
            }
        }
    }

    private void showPlantDetail(Plant plant) {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_plant_detail, null);

        TextView txtName = dialogView.findViewById(R.id.txtDetailPlantName);
        TextView txtScientific = dialogView.findViewById(R.id.txtDetailScientific);
        TextView txtCategory = dialogView.findViewById(R.id.txtDetailCategory);
        TextView txtPlanting = dialogView.findViewById(R.id.txtDetailPlanting);
        TextView txtHarvest = dialogView.findViewById(R.id.txtDetailHarvest);
        TextView txtCare = dialogView.findViewById(R.id.txtDetailCare);
        TextView txtWatering = dialogView.findViewById(R.id.txtDetailWatering);
        TextView txtSunlight = dialogView.findViewById(R.id.txtDetailSunlight);
        TextView txtSoil = dialogView.findViewById(R.id.txtDetailSoil);
        TextView txtCompatibility = dialogView.findViewById(R.id.txtDetailCompatibility);
        TextView txtGrowth = dialogView.findViewById(R.id.txtDetailGrowth);

        txtName.setText(plant.getName());
        txtScientific.setText(plant.getScientificName());
        txtCategory.setText(plant.getCategory());
        txtPlanting.setText("Plantation: " + plant.getPlantingPeriod());
        txtHarvest.setText("Récolte: " + plant.getHarvestPeriod());
        txtCare.setText(plant.getCareInstructions());
        txtWatering.setText("💧 Arrosage: " + plant.getWateringFrequency());
        txtSunlight.setText("☀️ Ensoleillement: " + plant.getSunlightRequirement());
        txtSoil.setText("🌱 Sol: " + plant.getSoilType());
        txtCompatibility.setText("🤝 Compatible avec: " + plant.getCompatibility());
        txtGrowth.setText("⏱️ Durée de croissance: " + plant.getGrowthDurationDays() + " jours");

        builder.setView(dialogView)
                .setPositiveButton("Fermer", null)
                .show();
    }
}