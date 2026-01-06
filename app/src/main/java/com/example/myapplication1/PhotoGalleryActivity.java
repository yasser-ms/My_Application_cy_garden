package com.example.myapplication1;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.myapplication1.database.DatabaseHelper;
import com.example.myapplication1.models.Photo;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class PhotoGalleryActivity extends BaseActivity {

    private RecyclerView recyclerView;
    private Button btnTakePhoto;
    private TextView txtEmpty;
    private String parcelId;
    private PhotoAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_gallery);

        parcelId = getIntent().getStringExtra("parcel_id");
        if (parcelId == null) parcelId = "B4";

        initializeViews();
        loadPhotos();
    }

    private void initializeViews() {
        recyclerView = findViewById(R.id.recyclerPhotos);
        btnTakePhoto = findViewById(R.id.btnTakePhoto);
        txtEmpty = findViewById(R.id.txtEmpty);

        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));

        btnTakePhoto.setOnClickListener(v -> {
            Intent intent = new Intent(this, CameraActivity.class);
            intent.putExtra("parcel_id", parcelId);
            startActivity(intent);
        });
    }

    private void loadPhotos() {
        DatabaseHelper db = DatabaseHelper.getInstance(this);
        List<Photo> photos = db.getPhotosByParcel(parcelId);

        if (photos.isEmpty()) {
            txtEmpty.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            txtEmpty.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
            adapter = new PhotoAdapter(photos);
            recyclerView.setAdapter(adapter);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadPhotos();
    }

    private class PhotoAdapter extends RecyclerView.Adapter<PhotoAdapter.PhotoViewHolder> {
        private List<Photo> photos;

        PhotoAdapter(List<Photo> photos) {
            this.photos = photos;
        }

        @NonNull
        @Override
        public PhotoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_photo, parent, false);
            return new PhotoViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull PhotoViewHolder holder, int position) {
            Photo photo = photos.get(position);
            holder.bind(photo);
        }

        @Override
        public int getItemCount() {
            return photos.size();
        }

        class PhotoViewHolder extends RecyclerView.ViewHolder {
            ImageView imgPhoto;
            TextView txtDate, txtNotes;

            PhotoViewHolder(View itemView) {
                super(itemView);
                imgPhoto = itemView.findViewById(R.id.imgPhoto);
                txtDate = itemView.findViewById(R.id.txtPhotoDate);
                txtNotes = itemView.findViewById(R.id.txtPhotoNotes);
            }

            void bind(Photo photo) {
                File file = new File(photo.getFilePath());
                if (file.exists()) {
                    imgPhoto.setImageURI(android.net.Uri.fromFile(file));
                }

                String dateStr = new SimpleDateFormat("dd MMM yyyy HH:mm", Locale.FRENCH)
                        .format(new Date(photo.getTimestamp()));
                txtDate.setText(dateStr);

                txtNotes.setText(photo.getNotes() != null && !photo.getNotes().isEmpty() ?
                        photo.getNotes() : "Aucune note");

                itemView.setOnClickListener(v -> showPhotoDetail(photo));
            }
        }
    }

    private void showPhotoDetail(Photo photo) {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_photo_detail, null);

        ImageView imgDetail = dialogView.findViewById(R.id.imgFullPhoto);
        TextView txtDetailDate = dialogView.findViewById(R.id.txtFullPhotoDate);
        TextView txtDetailNotes = dialogView.findViewById(R.id.txtFullPhotoNotes);

        File file = new File(photo.getFilePath());
        if (file.exists()) {
            imgDetail.setImageURI(android.net.Uri.fromFile(file));
        }

        String dateStr = new SimpleDateFormat("dd MMMM yyyy HH:mm", Locale.FRENCH)
                .format(new Date(photo.getTimestamp()));
        txtDetailDate.setText(dateStr);
        txtDetailNotes.setText(photo.getNotes());

        builder.setView(dialogView)
                .setPositiveButton("Fermer", null)
                .setNegativeButton("Supprimer", (dialog, which) -> {
                    DatabaseHelper.getInstance(this).deletePhoto(photo.getId());
                    file.delete();
                    loadPhotos();
                })
                .show();
    }
}