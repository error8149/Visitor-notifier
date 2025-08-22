package com.error.dhlvisitornotification;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class AgreementActivity extends AppCompatActivity {
    private static final String TAG = "AgreementActivity";
    private Visitor visitor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_agreement);

        visitor = (Visitor) getIntent().getSerializableExtra("VISITOR");
        if (visitor == null) {
            Log.e(TAG, "Visitor data not found in intent");
            Toast.makeText(this, "Error: Visitor data missing", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        CheckBox agreementCheckBox = findViewById(R.id.agreementCheckBox);
        Button nextButton = findViewById(R.id.nextButton);

        if (agreementCheckBox == null || nextButton == null) {
            Log.e(TAG, "View not found: agreementCheckBox=" + (agreementCheckBox == null) + ", nextButton=" + (nextButton == null));
            Toast.makeText(this, "Error: UI components not found", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        nextButton.setEnabled(agreementCheckBox.isChecked());
        agreementCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            nextButton.setEnabled(isChecked);
            Log.d(TAG, "Agreement checkbox checked: " + isChecked);
        });

        nextButton.setOnClickListener(v -> {
            Log.d(TAG, "Next button clicked");
            if (!agreementCheckBox.isChecked()) {
                Toast.makeText(this, "Please accept the agreement", Toast.LENGTH_SHORT).show();
                return;
            }
            Intent intent = new Intent(this, SignatureActivity.class);
            intent.putExtra("VISITOR", visitor);
            startActivity(intent);
        });
    }
}