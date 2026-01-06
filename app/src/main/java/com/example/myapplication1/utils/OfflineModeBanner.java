package com.example.myapplication1.utils;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.core.content.ContextCompat;

/**
 * Offline Mode Banner - Visual indicator for network connectivity status
 * Can be added to any activity to show online/offline status
 */
public class OfflineModeBanner {

    private final Context context;
    private final ViewGroup parentView;
    private LinearLayout bannerView;
    private TextView txtStatus;
    private boolean isShowing = false;

    public OfflineModeBanner(Context context, ViewGroup parentView) {
        this.context = context;
        this.parentView = parentView;
        createBanner();
    }

    private void createBanner() {
        // Create banner container
        bannerView = new LinearLayout(context);
        bannerView.setOrientation(LinearLayout.HORIZONTAL);
        bannerView.setGravity(android.view.Gravity.CENTER);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                dpToPx(40)
        );
        bannerView.setLayoutParams(params);
        bannerView.setPadding(dpToPx(16), dpToPx(8), dpToPx(16), dpToPx(8));

        // Create status text
        txtStatus = new TextView(context);
        txtStatus.setTextSize(14);
        txtStatus.setTextColor(ContextCompat.getColor(context, android.R.color.white));
        txtStatus.setGravity(android.view.Gravity.CENTER);

        bannerView.addView(txtStatus);

        // Initially hidden
        bannerView.setVisibility(View.GONE);
        bannerView.setAlpha(0f);
    }

    /**
     * Show offline banner
     */
    public void showOffline() {
        if (!isShowing || bannerView.getVisibility() != View.VISIBLE) {
            bannerView.setBackgroundColor(ContextCompat.getColor(context, android.R.color.holo_red_dark));
            txtStatus.setText("❌ Mode hors ligne - Données locales uniquement");

            // Add to parent if not already added
            if (bannerView.getParent() == null) {
                parentView.addView(bannerView, 0);
            }

            bannerView.setVisibility(View.VISIBLE);
            bannerView.animate()
                    .alpha(1f)
                    .setDuration(300)
                    .setListener(null);

            isShowing = true;
        }
    }

    /**
     * Show online banner (briefly)
     */
    public void showOnline() {
        if (isShowing || bannerView.getVisibility() == View.VISIBLE) {
            bannerView.setBackgroundColor(ContextCompat.getColor(context, android.R.color.holo_green_dark));
            txtStatus.setText("✅ Connexion rétablie - Synchronisation...");

            if (bannerView.getParent() == null) {
                parentView.addView(bannerView, 0);
            }

            bannerView.setVisibility(View.VISIBLE);
            bannerView.setAlpha(1f);

            // Hide after 2 seconds
            bannerView.postDelayed(this::hide, 2000);
        }
    }

    /**
     * Show syncing status
     */
    public void showSyncing() {
        bannerView.setBackgroundColor(ContextCompat.getColor(context, android.R.color.holo_blue_dark));
        txtStatus.setText("🔄 Synchronisation en cours...");

        if (bannerView.getParent() == null) {
            parentView.addView(bannerView, 0);
        }

        bannerView.setVisibility(View.VISIBLE);
        bannerView.setAlpha(1f);
        isShowing = true;
    }

    /**
     * Show sync complete
     */
    public void showSyncComplete() {
        bannerView.setBackgroundColor(ContextCompat.getColor(context, android.R.color.holo_green_dark));
        txtStatus.setText("✅ Synchronisation terminée");

        bannerView.setVisibility(View.VISIBLE);
        bannerView.setAlpha(1f);

        // Hide after 1.5 seconds
        bannerView.postDelayed(this::hide, 1500);
    }

    /**
     * Hide the banner
     */
    public void hide() {
        if (bannerView.getVisibility() == View.VISIBLE) {
            bannerView.animate()
                    .alpha(0f)
                    .setDuration(300)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            bannerView.setVisibility(View.GONE);
                            isShowing = false;
                        }
                    });
        }
    }

    /**
     * Update based on network state
     */
    public void updateNetworkState(NetworkStateMonitor.NetworkState state) {
        switch (state) {
            case CONNECTED_WIFI:
                if (isShowing) {
                    showOnline();
                }
                break;
            case CONNECTED_MOBILE:
                if (isShowing) {
                    showOnline();
                }
                break;
            case CONNECTED_OTHER:
                if (isShowing) {
                    showOnline();
                }
                break;
            case DISCONNECTED:
                showOffline();
                break;
        }
    }

    /**
     * Check if banner is currently showing
     */
    public boolean isShowing() {
        return isShowing;
    }

    private int dpToPx(int dp) {
        float density = context.getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }
}
