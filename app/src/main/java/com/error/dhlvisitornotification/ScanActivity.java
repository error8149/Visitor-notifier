package com.error.dhlvisitornotification;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

public class ScanActivity extends AppCompatActivity {
    private static final String TAG = "ScanActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan);

        findViewById(R.id.signInButton).setOnClickListener(v -> {
            startActivity(new Intent(ScanActivity.this, FormActivity.class));
        });
    }
}