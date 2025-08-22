package com.error.dhlvisitornotification;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.journeyapps.barcodescanner.BarcodeCallback;
import com.journeyapps.barcodescanner.BarcodeResult;
import com.journeyapps.barcodescanner.DecoratedBarcodeView;
import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity implements BarcodeCallback {
    private static final String TAG = "MainActivity";
    private static final int CAMERA_PERMISSION_REQUEST = 100;
    private DecoratedBarcodeView barcodeView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        barcodeView = findViewById(R.id.barcode_scanner);
        Button signInButton = findViewById(R.id.signInButton);

        if (barcodeView == null || signInButton == null) {
            Log.e(TAG, "Views not found: barcodeView=" + (barcodeView == null) + ", signInButton=" + (signInButton == null));
            Toast.makeText(this, "Error: UI components not found", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        signInButton.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, FormActivity.class)));

        if (checkCameraPermission()) {
            startQrScan();
        } else {
            requestCameraPermission();
        }
    }

    private boolean checkCameraPermission() {
        return ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestCameraPermission() {
        ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.CAMERA}, CAMERA_PERMISSION_REQUEST);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startQrScan();
            } else {
                Toast.makeText(this, "Camera permission required for QR scanning", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void startQrScan() {
        if (barcodeView != null) {
            barcodeView.setStatusText(getString(R.string.scan_prompt));
            barcodeView.decodeContinuous(this);
            Log.d(TAG, "QR scanner initialized");
        } else {
            Log.e(TAG, "BarcodeView is null, cannot start QR scan");
            Toast.makeText(this, "Error: Scanner not initialized", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void barcodeResult(BarcodeResult result) {
        if (result != null && result.getText() != null) {
            barcodeView.pause();
            processQrCode(result.getText());
        } else {
            Log.w(TAG, "Invalid barcode result received");
            barcodeView.resume();
        }
    }

    @Override
    public void possibleResultPoints(java.util.List<com.google.zxing.ResultPoint> resultPoints) {
        // Not used
    }

    private void processQrCode(String qrContent) {
        try {
            JSONObject json = new JSONObject(qrContent);
            Visitor visitor = new Visitor(
                    json.getString("firstName"),
                    json.getString("lastName"),
                    json.optString("company", null),
                    json.getString("phone"),
                    json.getString("email"),
                    json.getString("reason")
            );
            Log.d(TAG, "QR code parsed: " + visitor.getFullName());
            Intent intent = new Intent(this, AgreementActivity.class);
            intent.putExtra("VISITOR", visitor);
            startActivity(intent);
        } catch (JSONException e) {
            Log.e(TAG, "Invalid QR code format: " + e.getMessage(), e);
            Toast.makeText(this, "Invalid QR code data, please try again", Toast.LENGTH_LONG).show();
            if (barcodeView != null) barcodeView.resume();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (checkCameraPermission() && barcodeView != null) {
            barcodeView.resume();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (barcodeView != null) {
            barcodeView.pause();
        }
    }
}