package com.example.myapplication1;

import android.os.Bundle;
import android.view.MenuItem;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;


public abstract class BaseActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Enable back button in action bar for all activities except main ones
        if (getSupportActionBar() != null && shouldShowBackButton()) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle back button press
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    protected boolean shouldShowBackButton() {
        return true;
    }


    protected void onCleanup() {
        // Override in child activities, not needed for us
    }

    @Override
    protected void onDestroy() {
        onCleanup();
        super.onDestroy();
    }
}