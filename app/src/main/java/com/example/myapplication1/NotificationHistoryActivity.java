package com.example.myapplication1;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.myapplication1.database.DatabaseHelper;
import com.example.myapplication1.models.NotificationRecord;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class NotificationHistoryActivity extends BaseActivity {

    private RecyclerView recyclerView;
    private TextView txtEmpty;
    private Button btnMarkAllRead;
    private NotificationAdapter adapter;
    private DatabaseHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification_history);

        db = DatabaseHelper.getInstance(this);
        initializeViews();
        loadNotifications();
    }

    private void initializeViews() {
        recyclerView = findViewById(R.id.recyclerNotifications);
        txtEmpty = findViewById(R.id.txtEmpty);
        btnMarkAllRead = findViewById(R.id.btnMarkAllRead);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        btnMarkAllRead.setOnClickListener(v -> {
            db.markAllNotificationsAsRead();
            loadNotifications();
        });
    }

    private void loadNotifications() {
        List<NotificationRecord> notifications = db.getAllNotifications();

        if (notifications.isEmpty()) {
            txtEmpty.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
            btnMarkAllRead.setEnabled(false);
        } else {
            txtEmpty.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
            btnMarkAllRead.setEnabled(true);
            adapter = new NotificationAdapter(notifications);
            recyclerView.setAdapter(adapter);
        }
    }

    private class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder> {
        private List<NotificationRecord> notifications;

        NotificationAdapter(List<NotificationRecord> notifications) {
            this.notifications = notifications;
        }

        @NonNull
        @Override
        public NotificationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_notification, parent, false);
            return new NotificationViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull NotificationViewHolder holder, int position) {
            holder.bind(notifications.get(position));
        }

        @Override
        public int getItemCount() {
            return notifications.size();
        }

        class NotificationViewHolder extends RecyclerView.ViewHolder {
            CardView cardView;
            TextView txtIcon, txtMessage, txtDate, txtParcel;
            View unreadIndicator;

            NotificationViewHolder(View itemView) {
                super(itemView);
                cardView = itemView.findViewById(R.id.cardNotification);
                txtIcon = itemView.findViewById(R.id.txtNotifIcon);
                txtMessage = itemView.findViewById(R.id.txtNotifMessage);
                txtDate = itemView.findViewById(R.id.txtNotifDate);
                txtParcel = itemView.findViewById(R.id.txtNotifParcel);
                unreadIndicator = itemView.findViewById(R.id.unreadIndicator);
            }

            void bind(NotificationRecord notification) {
                txtIcon.setText(notification.getAlertIcon());
                txtMessage.setText(notification.getMessage());
                txtParcel.setText("Parcelle #" + notification.getParcelId());

                String dateStr = new SimpleDateFormat("dd MMM yyyy HH:mm", Locale.FRENCH)
                        .format(new Date(notification.getTimestamp()));
                txtDate.setText(dateStr);

                unreadIndicator.setVisibility(notification.isRead() ? View.GONE : View.VISIBLE);

                // Set card background based on severity
                int backgroundColor;
                switch (notification.getSeverity()) {
                    case "critical":
                        backgroundColor = getColor(android.R.color.holo_red_light);
                        break;
                    case "warning":
                        backgroundColor = getColor(android.R.color.holo_orange_light);
                        break;
                    default:
                        backgroundColor = getColor(android.R.color.white);
                }
                cardView.setCardBackgroundColor(backgroundColor);

                itemView.setOnClickListener(v -> {
                    if (!notification.isRead()) {
                        db.markNotificationAsRead(notification.getId());
                        notification.setRead(true);
                        notifyItemChanged(getAdapterPosition());
                    }
                });
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadNotifications();
    }
}