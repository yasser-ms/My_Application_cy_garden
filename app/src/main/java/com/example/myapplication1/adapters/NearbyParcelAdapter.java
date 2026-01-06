package com.example.myapplication1.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import com.example.myapplication1.R;
import com.example.myapplication1.models.ParcelLocation;
import java.util.List;

/**
 * Adapter for displaying nearby parcels sorted by distance
 */
public class NearbyParcelAdapter extends RecyclerView.Adapter<NearbyParcelAdapter.ParcelViewHolder> {

    private Context context;
    private List<ParcelLocation> parcels;
    private OnParcelClickListener listener;

    public interface OnParcelClickListener {
        void onParcelClick(ParcelLocation parcel);
    }

    public NearbyParcelAdapter(Context context, List<ParcelLocation> parcels, OnParcelClickListener listener) {
        this.context = context;
        this.parcels = parcels;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ParcelViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_nearby_parcel, parent, false);
        return new ParcelViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ParcelViewHolder holder, int position) {
        ParcelLocation parcel = parcels.get(position);
        holder.bind(parcel, position);
    }

    @Override
    public int getItemCount() {
        return parcels.size();
    }

    public void updateParcels(List<ParcelLocation> newParcels) {
        this.parcels = newParcels;
        notifyDataSetChanged();
    }

    class ParcelViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        TextView txtRank, txtParcelId, txtDistance, txtStatus, txtDirection;

        ParcelViewHolder(View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.cardNearbyParcel);
            txtRank = itemView.findViewById(R.id.txtRank);
            txtParcelId = itemView.findViewById(R.id.txtParcelId);
            txtDistance = itemView.findViewById(R.id.txtDistance);
            txtStatus = itemView.findViewById(R.id.txtParcelStatus);
            txtDirection = itemView.findViewById(R.id.txtDirection);
        }

        void bind(ParcelLocation parcel, int position) {
            // Rank number
            txtRank.setText(String.valueOf(position + 1));

            // Parcel ID
            txtParcelId.setText("Parcelle " + parcel.getParcelId());

            // Distance
            txtDistance.setText(parcel.getFormattedDistance());

            // Status
            txtStatus.setText(parcel.getStatusDisplay());
            if (parcel.isOccupied()) {
                txtStatus.setTextColor(context.getColor(android.R.color.holo_orange_dark));
            } else {
                txtStatus.setTextColor(context.getColor(android.R.color.holo_green_dark));
            }

            // Direction indicator (would need user location to calculate)
            txtDirection.setText("🧭");

            // Card color based on distance
            if (parcel.getDistanceFromUser() < 10) {
                cardView.setCardBackgroundColor(context.getColor(android.R.color.holo_green_light));
            } else if (parcel.getDistanceFromUser() < 50) {
                cardView.setCardBackgroundColor(context.getColor(android.R.color.holo_orange_light));
            } else {
                cardView.setCardBackgroundColor(context.getColor(android.R.color.white));
            }

            // Click listener for navigation
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onParcelClick(parcel);
                }
            });
        }
    }
}
