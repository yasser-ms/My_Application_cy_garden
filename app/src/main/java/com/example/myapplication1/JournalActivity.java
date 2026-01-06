package com.example.myapplication1;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.myapplication1.database.DatabaseHelper;
import com.example.myapplication1.models.JournalEntry;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class JournalActivity extends BaseActivity {

    private RecyclerView recyclerView;
    private Button btnAddEntry;
    private TextView txtEmpty, txtParcelTitle;
    private JournalAdapter adapter;
    private DatabaseHelper db;
    private String parcelId;
    private String userEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_journal);

        parcelId = getIntent().getStringExtra("parcel_id");
        if (parcelId == null) parcelId = "B4";

        userEmail = getSharedPreferences("GardenApp", MODE_PRIVATE).getString("user_email", "");
        db = DatabaseHelper.getInstance(this);

        initializeViews();
        loadEntries();
    }

    private void initializeViews() {
        recyclerView = findViewById(R.id.recyclerJournal);
        btnAddEntry = findViewById(R.id.btnAddEntry);
        txtEmpty = findViewById(R.id.txtEmpty);
        txtParcelTitle = findViewById(R.id.txtParcelTitle);

        txtParcelTitle.setText("Journal - Parcelle #" + parcelId);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        btnAddEntry.setOnClickListener(v -> showAddEntryDialog());
    }

    private void loadEntries() {
        List<JournalEntry> entries = db.getJournalEntriesByParcel(parcelId);

        if (entries.isEmpty()) {
            txtEmpty.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            txtEmpty.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
            adapter = new JournalAdapter(entries);
            recyclerView.setAdapter(adapter);
        }
    }

    private void showAddEntryDialog() {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_journal_entry, null);

        Spinner spinnerType = dialogView.findViewById(R.id.spinnerEntryType);
        EditText inputNotes = dialogView.findViewById(R.id.inputNotes);
        EditText inputWaterAmount = dialogView.findViewById(R.id.inputWaterAmount);
        LinearLayout waterAmountLayout = dialogView.findViewById(R.id.waterAmountLayout);

        String[] types = {"Observation", "Arrosage", "Fertilisation", "Taille", "Récolte"};
        ArrayAdapter<String> typeAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, types);
        typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerType.setAdapter(typeAdapter);

        spinnerType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                waterAmountLayout.setVisibility(position == 1 ? View.VISIBLE : View.GONE);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        builder.setView(dialogView)
                .setTitle("➕ Nouvelle Entrée")
                .setPositiveButton("Ajouter", (dialog, which) -> {
                    String notes = inputNotes.getText().toString().trim();
                    if (notes.isEmpty()) {
                        Toast.makeText(this, "Veuillez ajouter une note", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    int typePosition = spinnerType.getSelectedItemPosition();
                    String[] typeIds = {"observation", "watering", "fertilizing", "pruning", "harvest"};
                    String entryType = typeIds[typePosition];

                    JournalEntry entry;
                    if (entryType.equals("watering") && !inputWaterAmount.getText().toString().isEmpty()) {
                        double waterAmount = Double.parseDouble(inputWaterAmount.getText().toString());
                        entry = new JournalEntry(parcelId, userEmail, entryType, notes, waterAmount);
                    } else {
                        entry = new JournalEntry(parcelId, userEmail, entryType, notes);
                    }

                    long id = db.insertJournalEntry(entry);
                    if (id > 0) {
                        Toast.makeText(this, "Entrée ajoutée!", Toast.LENGTH_SHORT).show();
                        loadEntries();
                    } else {
                        Toast.makeText(this, "Erreur lors de l'ajout", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Annuler", null)
                .show();
    }

    private class JournalAdapter extends RecyclerView.Adapter<JournalAdapter.JournalViewHolder> {
        private List<JournalEntry> entries;

        JournalAdapter(List<JournalEntry> entries) {
            this.entries = entries;
        }

        @NonNull
        @Override
        public JournalViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_journal_entry, parent, false);
            return new JournalViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull JournalViewHolder holder, int position) {
            holder.bind(entries.get(position));
        }

        @Override
        public int getItemCount() {
            return entries.size();
        }

        class JournalViewHolder extends RecyclerView.ViewHolder {
            TextView txtType, txtDate, txtNotes, txtWaterAmount;

            JournalViewHolder(View itemView) {
                super(itemView);
                txtType = itemView.findViewById(R.id.txtEntryType);
                txtDate = itemView.findViewById(R.id.txtEntryDate);
                txtNotes = itemView.findViewById(R.id.txtEntryNotes);
                txtWaterAmount = itemView.findViewById(R.id.txtWaterAmount);
            }

            void bind(JournalEntry entry) {
                txtType.setText(entry.getEntryTypeDisplay());

                String dateStr = new SimpleDateFormat("dd MMM yyyy HH:mm", Locale.FRENCH)
                        .format(new Date(entry.getTimestamp()));
                txtDate.setText(dateStr);

                txtNotes.setText(entry.getNotes());

                if (entry.getEntryType().equals("watering") && entry.getWaterAmount() > 0) {
                    txtWaterAmount.setVisibility(View.VISIBLE);
                    txtWaterAmount.setText("Quantité: " + entry.getWaterAmount() + "L");
                } else {
                    txtWaterAmount.setVisibility(View.GONE);
                }

                itemView.setOnLongClickListener(v -> {
                    showDeleteConfirmation(entry);
                    return true;
                });
            }
        }
    }

    private void showDeleteConfirmation(JournalEntry entry) {
        new android.app.AlertDialog.Builder(this)
                .setTitle("Supprimer l'entrée")
                .setMessage("Voulez-vous supprimer cette entrée du journal?")
                .setPositiveButton("Supprimer", (dialog, which) -> {
                    db.deleteJournalEntry(entry.getId());
                    Toast.makeText(this, "Entrée supprimée", Toast.LENGTH_SHORT).show();
                    loadEntries();
                })
                .setNegativeButton("Annuler", null)
                .show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadEntries();
    }
}