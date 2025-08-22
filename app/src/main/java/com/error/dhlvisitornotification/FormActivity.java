package com.error.dhlvisitornotification;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.textfield.TextInputEditText;

public class FormActivity extends AppCompatActivity {
    private static final String TAG = "FormActivity";
    private TextInputEditText firstNameEditText, lastNameEditText, companyEditText, phoneEditText, emailEditText, reasonEditText;
    private Button nextButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_form);

        firstNameEditText = findViewById(R.id.firstNameEditText);
        lastNameEditText = findViewById(R.id.lastNameEditText);
        companyEditText = findViewById(R.id.companyEditText);
        phoneEditText = findViewById(R.id.phoneEditText);
        emailEditText = findViewById(R.id.emailEditText);
        reasonEditText = findViewById(R.id.reasonEditText);
        nextButton = findViewById(R.id.nextButton);

        if (firstNameEditText == null || lastNameEditText == null || companyEditText == null ||
                phoneEditText == null || emailEditText == null || reasonEditText == null || nextButton == null) {
            Toast.makeText(this, "Error: UI components not found", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        nextButton.setOnClickListener(v -> validateAndProceed());
    }

    private void validateAndProceed() {
        String firstName = firstNameEditText.getText().toString().trim();
        String lastName = lastNameEditText.getText().toString().trim();
        String company = companyEditText.getText().toString().trim();
        String phone = phoneEditText.getText().toString().trim();
        String email = emailEditText.getText().toString().trim();
        String reason = reasonEditText.getText().toString().trim();

        if (firstName.isEmpty()) {
            firstNameEditText.setError("First name is required");
            firstNameEditText.requestFocus();
            return;
        }
        if (lastName.isEmpty()) {
            lastNameEditText.setError("Last name is required");
            lastNameEditText.requestFocus();
            return;
        }
        if (phone.isEmpty()) {
            phoneEditText.setError("Phone number is required");
            phoneEditText.requestFocus();
            return;
        }
        if (!phone.matches("^[0-9]{10,15}$")) {
            phoneEditText.setError("Enter a valid phone number (10-15 digits)");
            phoneEditText.requestFocus();
            return;
        }
        if (email.isEmpty()) {
            emailEditText.setError("Email is required");
            emailEditText.requestFocus();
            return;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailEditText.setError("Enter a valid email address");
            emailEditText.requestFocus();
            return;
        }
        if (reason.isEmpty()) {
            reasonEditText.setError("Reason for visit is required");
            reasonEditText.requestFocus();
            return;
        }

        Visitor visitor = new Visitor(firstName, lastName, company.isEmpty() ? null : company, phone, email, reason);
        Intent intent = new Intent(this, AgreementActivity.class);
        intent.putExtra("VISITOR", visitor);
        startActivity(intent);
    }
}