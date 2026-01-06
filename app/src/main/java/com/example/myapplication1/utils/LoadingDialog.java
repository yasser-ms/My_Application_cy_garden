package com.example.myapplication1.utils;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

/**
 * Reusable loading dialog for async operations
 * Provides consistent loading UI across the app
 */
public class LoadingDialog {

    private Dialog dialog;
    private TextView messageTextView;

    public LoadingDialog(Context context) {
        this(context, "Chargement...");
    }

    public LoadingDialog(Context context, String message) {
        dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(false);

        // Create layout
        LinearLayout layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 50, 50, 50);
        layout.setGravity(Gravity.CENTER);
        layout.setBackgroundColor(Color.WHITE);

        // Add progress bar
        ProgressBar progressBar = new ProgressBar(context);
        layout.addView(progressBar);

        // Add message text
        messageTextView = new TextView(context);
        messageTextView.setText(message);
        messageTextView.setTextSize(16);
        messageTextView.setTextColor(Color.BLACK);
        messageTextView.setPadding(0, 20, 0, 0);
        layout.addView(messageTextView);

        dialog.setContentView(layout);

        // Make dialog background transparent
        Window window = dialog.getWindow();
        if (window != null) {
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            window.setLayout(
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.WRAP_CONTENT
            );
        }
    }

    /**
     * Show the loading dialog
     */
    public void show() {
        if (dialog != null && !dialog.isShowing()) {
            try {
                dialog.show();
            } catch (Exception e) {
                ErrorHandler.logError("Failed to show loading dialog", e);
            }
        }
    }

    /**
     * Dismiss the loading dialog
     */
    public void dismiss() {
        if (dialog != null && dialog.isShowing()) {
            try {
                dialog.dismiss();
            } catch (Exception e) {
                ErrorHandler.logError("Failed to dismiss loading dialog", e);
            }
        }
    }

    /**
     * Update loading message
     */
    public void setMessage(String message) {
        if (messageTextView != null) {
            messageTextView.setText(message);
        }
    }

    /**
     * Check if dialog is currently showing
     */
    public boolean isShowing() {
        return dialog != null && dialog.isShowing();
    }

    /**
     * Set whether dialog is cancelable
     */
    public void setCancelable(boolean cancelable) {
        if (dialog != null) {
            dialog.setCancelable(cancelable);
        }
    }
}