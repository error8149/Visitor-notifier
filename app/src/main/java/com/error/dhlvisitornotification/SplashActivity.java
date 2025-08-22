package com.error.dhlvisitornotification;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

public class SplashActivity extends AppCompatActivity {
    private static final int SPLASH_DURATION = 3000; // 3 seconds

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // Set status bar color for better visual experience
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.dhl_red));
        }

        // Initialize views and animations
        initializeViews();
        startAnimations();

        // Navigate to main activity after delay
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            Intent intent = new Intent(SplashActivity.this, MainActivity.class);
            startActivity(intent);
            finish();

            // Use appropriate transition based on Android version
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            }
        }, SPLASH_DURATION);
    }

    private void initializeViews() {
        // Ensure all views are properly set up
        ImageView logo = findViewById(R.id.splash_logo);
        TextView appName = findViewById(R.id.splash_app_name);
        TextView tagline = findViewById(R.id.splash_tagline);

        if (logo != null) {
            logo.setVisibility(View.VISIBLE);
        }
        if (appName != null) {
            appName.setVisibility(View.VISIBLE);
        }
        if (tagline != null) {
            tagline.setVisibility(View.VISIBLE);
        }
    }

    private void startAnimations() {
        try {
            ImageView logo = findViewById(R.id.splash_logo);
            TextView appName = findViewById(R.id.splash_app_name);
            TextView tagline = findViewById(R.id.splash_tagline);

            // Load animations
            Animation fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in);
            Animation slideUp = AnimationUtils.loadAnimation(this, R.anim.slide_up);

            // Start animations with null checks
            if (logo != null && fadeIn != null) {
                logo.startAnimation(fadeIn);
            }
            if (appName != null && slideUp != null) {
                appName.startAnimation(slideUp);
            }
            if (tagline != null && slideUp != null) {
                tagline.startAnimation(slideUp);
            }
        } catch (Exception e) {
            // If animations fail, just continue without them
            e.printStackTrace();
        }
    }
}