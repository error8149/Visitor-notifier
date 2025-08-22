package com.error.dhlvisitornotification;

import android.content.Intent;
import android.os.Bundle;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class ThankYouActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_thank_you);

        boolean emailSuccess = getIntent().getBooleanExtra("EMAIL_SUCCESS", false);
        String visitorName = getIntent().getStringExtra("VISITOR_NAME");

        initializeViews(emailSuccess, visitorName);
        startAnimations();
    }

    private void initializeViews(boolean emailSuccess, String visitorName) {
        TextView titleText = findViewById(R.id.thankYouTitle);
        TextView messageText = findViewById(R.id.thankYouMessage);
        TextView detailText = findViewById(R.id.thankYouDetail);
        Button backButton = findViewById(R.id.backButton);

        if (emailSuccess && visitorName != null) {
            String firstName = visitorName.split(" ")[0];
            titleText.setText(getString(R.string.welcome_visitor_template, firstName));
            messageText.setText(R.string.registration_completed);
            detailText.setText(R.string.proceed_waiting_area);
        }

        backButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
        });
    }

    private void startAnimations() {
        ImageView successIcon = findViewById(R.id.successIcon);
        TextView titleText = findViewById(R.id.thankYouTitle);
        TextView messageText = findViewById(R.id.thankYouMessage);

        Animation bounceIn = AnimationUtils.loadAnimation(this, R.anim.bounce_in);
        Animation fadeInUp = AnimationUtils.loadAnimation(this, R.anim.fade_in_up);

        successIcon.startAnimation(bounceIn);
        titleText.startAnimation(fadeInUp);
        messageText.startAnimation(fadeInUp);
    }
}