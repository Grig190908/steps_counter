package com.example.stepscounter;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class Profile extends AppCompatActivity {

    private static final String PREFS_NAME = "step_prefs";
    private static final String STEP_COUNT_KEY = "step_count";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        TextView emailTextView = findViewById(R.id.emailTextView);
        TextView monthlyStepsTextView = findViewById(R.id.monthlyStepsTextView);
        Button btnSignOut = findViewById(R.id.btnSignOut);
        Button btnBack = findViewById(R.id.btnBackToMain);

        // Показ email
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            emailTextView.setText("Почта: " + user.getEmail());
        }

        // Получение количества шагов
        SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        int steps = sharedPreferences.getInt(STEP_COUNT_KEY, 0);
        monthlyStepsTextView.setText("Шаги за месяц: " + steps);

        // Кнопка выхода
        btnSignOut.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(Profile.this, SignInActivity.class));
            finish();
        });

        // Кнопка назад
        btnBack.setOnClickListener(v -> {
            startActivity(new Intent(Profile.this, MainActivity.class));
            finish();
        });
    }
}
