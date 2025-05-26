package com.example.stepscounter;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private TextView stepCountTextView;
    private ImageView characterImageView;
    private FirebaseAuth mAuth;
    private int steps = 0;

    private static final int ACTIVITY_RECOGNITION_REQUEST_CODE = 100;
    private static final String PREFS_NAME = "step_prefs";
    private static final String STEP_COUNT_KEY = "step_count";
    private static final String LAST_RESET_DATE_KEY = "last_reset_date";
    private static final String MONTHLY_STEPS_KEY_PREFIX = "monthly_steps_";
    private static final String STEP_UPDATE_ACTION = "com.example.stepscounter.STEP_UPDATE";

    private final int[] characterImages = {
            R.drawable.character_stage_1,
            R.drawable.character_stage_2,
            R.drawable.character_stage_3,
            R.drawable.character_stage_4,
            R.drawable.character_stage_5,
            R.drawable.character_stage_6
    };

    private final BroadcastReceiver stepUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            steps = intent.getIntExtra("steps", 0);
            saveStepCount(steps);
            updateUI();
        }
    };

    @SuppressLint("NewApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser == null) {
            startActivity(new Intent(MainActivity.this, SignInActivity.class));
            finish();
            return;
        }

        stepCountTextView = findViewById(R.id.stepCountTextView);
        characterImageView = findViewById(R.id.characterImageView);
        Button btnProfile = findViewById(R.id.btnProfile);
        steps = loadStepCount();
        updateUI();
        scheduleDailyReset();

        // Request permissions
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q &&
                checkSelfPermission(android.Manifest.permission.ACTIVITY_RECOGNITION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACTIVITY_RECOGNITION},
                    ACTIVITY_RECOGNITION_REQUEST_CODE);
        } else {
            startStepCounterService();
        }

        // Register buttons
        btnProfile.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, Profile.class)));
        // Register receiver for step updates
        IntentFilter filter = new IntentFilter(STEP_UPDATE_ACTION);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(stepUpdateReceiver, filter, RECEIVER_NOT_EXPORTED);
        } else {
            registerReceiver(stepUpdateReceiver, filter);
        }
    }

    private void startStepCounterService() {
        Intent serviceIntent = new Intent(this, StepCounterService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent);
        } else {
            startService(serviceIntent);
        }
    }

    private void updateUI() {
        stepCountTextView.setText("Steps: " + steps);
        int index = Math.min(steps / 5000, characterImages.length - 1);
        characterImageView.setImageResource(characterImages[index]);
    }

    private void saveStepCount(int steps) {
        SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(STEP_COUNT_KEY, steps);
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        editor.putString(LAST_RESET_DATE_KEY, today);
        editor.apply();
    }

    private int loadStepCount() {
        SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        String lastResetDate = sharedPreferences.getString(LAST_RESET_DATE_KEY, "");

        if (!today.equals(lastResetDate)) {
            saveToMonthlyTotal(sharedPreferences.getInt(STEP_COUNT_KEY, 0));
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putInt(STEP_COUNT_KEY, 0);
            editor.putString(LAST_RESET_DATE_KEY, today);
            editor.apply();
            return 0;
        }
        return sharedPreferences.getInt(STEP_COUNT_KEY, 0);
    }

    private void saveToMonthlyTotal(int dailySteps) {
        SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String monthKey = getMonthKey();
        int currentMonthlySteps = sharedPreferences.getInt(monthKey, 0);
        currentMonthlySteps += dailySteps;
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(monthKey, currentMonthlySteps);
        editor.apply();
    }

    private String getMonthKey() {
        SimpleDateFormat monthFormat = new SimpleDateFormat("yyyy_MM", Locale.getDefault());
        return MONTHLY_STEPS_KEY_PREFIX + monthFormat.format(new Date());
    }

    public int getMonthlySteps() {
        SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        return sharedPreferences.getInt(getMonthKey(), 0);
    }

    private void scheduleDailyReset() {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, ResetStepsReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        calendar.add(Calendar.DAY_OF_MONTH, 1);

        if (alarmManager != null) {
            alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY, pendingIntent);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == ACTIVITY_RECOGNITION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startStepCounterService();
            } else {
                Toast.makeText(this, "Permission denied!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            unregisterReceiver(stepUpdateReceiver);
        } catch (IllegalArgumentException e) {
            // Receiver not registered, ignore
        }
    }
}