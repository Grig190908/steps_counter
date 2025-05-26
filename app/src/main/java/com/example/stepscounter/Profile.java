package com.example.stepscounter;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Profile extends AppCompatActivity {

    private static final String PREFS_NAME = "step_prefs";
    private static final String MONTHLY_STEPS_KEY_PREFIX = "monthly_steps_";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        TextView emailTextView = findViewById(R.id.emailTextView);
        @SuppressLint({"MissingInflatedId", "LocalSuppress"}) TextView monthlyStepsTextView = findViewById(R.id.monthlyStepsTextView);
        Button btnSignOut = findViewById(R.id.btnSignOut);
        Button btnBack = findViewById(R.id.btnBackToMain);

        // Show email
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            emailTextView.setText("Email: " + user.getEmail());
        }

        // Load monthly steps
        int monthlySteps = getMonthlySteps();
        monthlyStepsTextView.setText("Monthly Steps: " + monthlySteps);

        // Sign out
        btnSignOut.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(Profile.this, SignInActivity.class));
            finish();
        });

        // Back to main
        btnBack.setOnClickListener(v -> {
            startActivity(new Intent(Profile.this, MainActivity.class));
            finish();
        });
    }

    private int getMonthlySteps() {
        SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String monthKey = getMonthKey();
        return sharedPreferences.getInt(monthKey, 0);
    }

    private String getMonthKey() {
        SimpleDateFormat monthFormat = new SimpleDateFormat("yyyy_MM", Locale.getDefault());
        return MONTHLY_STEPS_KEY_PREFIX + monthFormat.format(new Date());
    }
}