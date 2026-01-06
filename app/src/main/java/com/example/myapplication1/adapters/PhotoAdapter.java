package com.example.myapplication1.adapters;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.myapplication1.R;
import com.example.myapplication1.models.Photo;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class PhotoAdapter extends RecyclerView.Adapter<PhotoAdapter.PhotoViewHolder> {

    private Context context;
    private List<Photo> photoList;
    private OnPhotoClickListener listener;

    public interface OnPhotoClickListener {
        void onPhotoClick(Photo photo);
    }

    public PhotoAdapter(Context context, List<Photo> photoList, OnPhotoClickListener listener) {
        this.context = context;
        this.photoList = photoList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public PhotoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_photo, parent, false);
        return new PhotoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PhotoViewHolder holder, int position) {
        Photo photo = photoList.get(position);

        // Load image from file
        File imgFile = new File(photo.getFilePath());
        if (imgFile.exists()) {
            holder.imgPhoto.setImageBitmap(BitmapFactory.decodeFile(imgFile.getAbsolutePath()));
        } else {
            holder.imgPhoto.setImageResource(R.drawable.ic_plant);
        }

        // Format date
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy HH:mm", Locale.FRENCH);
        holder.txtPhotoDate.setText(sdf.format(new Date(photo.getTimestamp())));

        // Set notes
        if (photo.getNotes() != null && !photo.getNotes().isEmpty()) {
            holder.txtPhotoNotes.setText(photo.getNotes());
        } else {
            holder.txtPhotoNotes.setText("Aucune note");
        }

        // Click listener
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onPhotoClick(photo);
            }
        });
    }

    @Override
    public int getItemCount() {
        return photoList.size();
    }

    public void updatePhotos(List<Photo> newPhotos) {
        this.photoList = newPhotos;
        notifyDataSetChanged();
    }

    static class PhotoViewHolder extends RecyclerView.ViewHolder {
        ImageView imgPhoto;
        TextView txtPhotoDate;
        TextView txtPhotoNotes;

        public PhotoViewHolder(@NonNull View itemView) {
            super(itemView);
            imgPhoto = itemView.findViewById(R.id.imgPhoto);
            txtPhotoDate = itemView.findViewById(R.id.txtPhotoDate);
            txtPhotoNotes = itemView.findViewById(R.id.txtPhotoNotes);
        }
    }
}