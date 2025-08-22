package com.error.dhlvisitornotification;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SignatureActivity extends AppCompatActivity {
    private static final String TAG = "SignatureActivity";
    private SignatureView signatureView;
    private Button nextButton, clearButton;
    private CircularProgressIndicator progressIndicator;
    private Visitor visitor;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign);

        visitor = (Visitor) getIntent().getSerializableExtra("VISITOR");
        if (visitor == null) {
            Log.e(TAG, "Visitor data not found in intent");
            Toast.makeText(this, "Error: Visitor data missing", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        initializeViews();
        setupClickListeners();
    }

    private void initializeViews() {
        signatureView = findViewById(R.id.signatureArea);
        nextButton = findViewById(R.id.nextButton);
        clearButton = findViewById(R.id.clearButton);
        progressIndicator = findViewById(R.id.progressIndicator);

        if (signatureView == null || nextButton == null || clearButton == null) {
            Log.e(TAG, "Views not found");
            Toast.makeText(this, "Error: UI components not found", Toast.LENGTH_LONG).show();
            finish();
        }
    }

    private void setupClickListeners() {
        clearButton.setOnClickListener(v -> {
            signatureView.clear();
            showToast("Signature cleared");
        });

        nextButton.setOnClickListener(v -> {
            Log.d(TAG, "Next button clicked");
            if (signatureView.isEmpty()) {
                showSignatureRequiredDialog();
                return;
            }
            showEmailPreviewDialog();
        });
    }

    private void showSignatureRequiredDialog() {
        new MaterialAlertDialogBuilder(this)
                .setTitle("📝 Signature Required")
                .setMessage("Please provide your signature before submitting the visitor information.")
                .setPositiveButton("OK", null)
                .setIcon(R.drawable.ic_signature)
                .show();
    }

    private void showEmailPreviewDialog() {
        String emailPreview = String.format(
                "Preview - Visitor Notification\n\n" +
                        "👤 Name: %s\n" +
                        "🏢 Company: %s\n" +
                        "📞 Phone: %s\n" +
                        "📧 Email: %s\n" +
                        "📝 Purpose: %s\n\n" +
                        "✍️ Digital signature will be attached",
                visitor.getFullName(),
                visitor.getCompany() != null ? visitor.getCompany() : "Not specified",
                visitor.getPhone(),
                visitor.getEmail(),
                visitor.getReason()
        );

        new MaterialAlertDialogBuilder(this)
                .setTitle("📧 Email Preview")
                .setMessage(emailPreview)
                .setPositiveButton("Send Notification", (dialog, which) -> sendEmail())
                .setNegativeButton("Edit Information", (dialog, which) -> {
                    // Go back to form to edit
                    finish();
                })
                .setNeutralButton("Cancel", null)
                .setCancelable(false)
                .show();
    }

    private void sendEmail() {
        // Show loading state
        nextButton.setEnabled(false);
        clearButton.setEnabled(false);
        progressIndicator.setVisibility(android.view.View.VISIBLE);

        executor.execute(() -> {
            Bitmap signatureBitmap = signatureView.getSignatureBitmap();
            boolean success = EmailSender.sendVisitorEmail(SignatureActivity.this, visitor, signatureBitmap);

            runOnUiThread(() -> {
                // Hide loading state
                progressIndicator.setVisibility(android.view.View.GONE);
                nextButton.setEnabled(true);
                clearButton.setEnabled(true);

                if (success) {
                    showSuccessDialog();
                } else {
                    showErrorDialog();
                }
            });
        });
    }

    private void showSuccessDialog() {
        new MaterialAlertDialogBuilder(this)
                .setTitle("✅ Success!")
                .setMessage("Visitor notification sent successfully!\n\nThe notification has been delivered to the reception team.")
                .setPositiveButton("Continue", (dialog, which) -> {
                    Intent intent = new Intent(SignatureActivity.this, ThankYouActivity.class);
                    intent.putExtra("EMAIL_SUCCESS", true);
                    intent.putExtra("VISITOR_NAME", visitor.getFullName());
                    startActivity(intent);
                    finish();
                })
                .setCancelable(false)
                .setIcon(R.drawable.ic_success)
                .show();
    }

    private void showErrorDialog() {
        new MaterialAlertDialogBuilder(this)
                .setTitle("❌ Delivery Failed")
                .setMessage("Failed to send visitor notification. This could be due to:\n\n• Network connection issues\n• Server temporarily unavailable\n\nPlease try again or contact IT support.")
                .setPositiveButton("Retry", (dialog, which) -> sendEmail())
                .setNegativeButton("Cancel", null)
                .setNeutralButton("Contact Support", (dialog, which) -> {
                    // You can add contact support functionality here
                    showToast("Please contact IT support for assistance");
                })
                .show();
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.shutdown();
    }
}