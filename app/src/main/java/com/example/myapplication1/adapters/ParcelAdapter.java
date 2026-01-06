package com.example.myapplication1.adapters;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import com.example.myapplication1.R;
import com.example.myapplication1.models.Parcel;

import java.util.List;

public class ParcelAdapter extends RecyclerView.Adapter<ParcelAdapter.ParcelViewHolder> {

    private Context context;
    private List<Parcel> parcelList;
    private OnParcelClickListener listener;

    public interface OnParcelClickListener {
        void onParcelClick(Parcel parcel);
    }

    public ParcelAdapter(Context context, List<Parcel> parcelList, OnParcelClickListener listener) {
        this.context = context;
        this.parcelList = parcelList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ParcelViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_parcel, parent, false);
        return new ParcelViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ParcelViewHolder holder, int position) {
        Parcel parcel = parcelList.get(position);

        holder.txtParcelNumber.setText(parcel.getParcelNumber());

        if (parcel.isOccupied()) {
            holder.txtParcelStatus.setText("Occupée");
            holder.cardParcel.setCardBackgroundColor(Color.parseColor("#9E9E9E"));
            holder.txtParcelNumber.setTextColor(Color.WHITE);
            holder.txtParcelStatus.setTextColor(Color.parseColor("#E0E0E0"));
        } else {
            holder.txtParcelStatus.setText("Disponible");
            holder.cardParcel.setCardBackgroundColor(Color.parseColor("#4CAF50"));
            holder.txtParcelNumber.setTextColor(Color.WHITE);
            holder.txtParcelStatus.setTextColor(Color.parseColor("#E8F5E8"));
        }

        // Only allow clicking available parcels
        holder.itemView.setOnClickListener(v -> {
            if (!parcel.isOccupied() && listener != null) {
                listener.onParcelClick(parcel);
            }
        });

        // Disable click effect for occupied parcels
        holder.itemView.setClickable(!parcel.isOccupied());
        holder.itemView.setFocusable(!parcel.isOccupied());
    }

    @Override
    public int getItemCount() {
        return parcelList.size();
    }

    public void updateParcels(List<Parcel> newParcels) {
        this.parcelList = newParcels;
        notifyDataSetChanged();
    }

    static class ParcelViewHolder extends RecyclerView.ViewHolder {
        CardView cardParcel;
        TextView txtParcelNumber;
        TextView txtParcelStatus;

        public ParcelViewHolder(@NonNull View itemView) {
            super(itemView);
            cardParcel = itemView.findViewById(R.id.cardParcel);
            txtParcelNumber = itemView.findViewById(R.id.txtParcelNumber);
            txtParcelStatus = itemView.findViewById(R.id.txtParcelStatus);
        }
    }
}