package com.example.stepscounter;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Calendar;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private SensorManager sensorManager;
    private Sensor stepDetectorSensor;
    private TextView stepCountTextView;
    private ImageView characterImageView;
    private int steps = 0;
    private FirebaseAuth mAuth;

    private static final int ACTIVITY_RECOGNITION_REQUEST_CODE = 100;
    private static final String PREFS_NAME = "step_prefs";
    private static final String STEP_COUNT_KEY = "step_count";

    private int[] characterImages = {
            R.drawable.character_stage_1,
            R.drawable.character_stage_2,
            R.drawable.character_stage_3,
            R.drawable.character_stage_4,
            R.drawable.character_stage_5,
            R.drawable.character_stage_6
    };

    @RequiresApi(api = Build.VERSION_CODES.Q)
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
        Button btnProfile = findViewById(R.id.btnProfile); // Profile Button
        Button btnOnline = findViewById(R.id.btnSettings); // Online Button (renamed as "Online")

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        steps = loadStepCount();
        updateUI();
        scheduleDailyReset();

        if (checkSelfPermission(android.Manifest.permission.ACTIVITY_RECOGNITION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACTIVITY_RECOGNITION},
                    ACTIVITY_RECOGNITION_REQUEST_CODE);
        } else {
            initStepDetector();
        }
        btnProfile.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, Profile.class));
        });

        btnOnline.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, Online.class));
        });
    }

    private void initStepDetector() {
        if (sensorManager != null) {
            stepDetectorSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR);
            if (stepDetectorSensor == null) {
                Toast.makeText(this, "Step Detector not available!", Toast.LENGTH_LONG).show();
            } else {
                sensorManager.registerListener(this, stepDetectorSensor, SensorManager.SENSOR_DELAY_UI);
            }
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_STEP_DETECTOR) {
            steps++;
            saveStepCount(steps);
            updateUI();
            Log.d("StepCounter", "Step detected. Total steps: " + steps);

            if (steps % 5000 == 0) {
                sendCongratulatoryNotification("Great job!", "You've walked " + steps + " steps! Keep going!");
            }
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
        editor.apply();
    }

    private int loadStepCount() {
        SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        return sharedPreferences.getInt(STEP_COUNT_KEY, 0);
    }

    private void sendCongratulatoryNotification(String title, String message) {
        NotificationHelper notificationHelper = new NotificationHelper(this);
        notificationHelper.sendNotification(title, message);
    }

    private void scheduleDailyReset() {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, ResetStepsReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);

        if (alarmManager != null) {
            alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY, pendingIntent);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {}

    @Override
    protected void onPause() {
        super.onPause();
        if (sensorManager != null) {
            sensorManager.unregisterListener(this);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (stepDetectorSensor != null) {
            sensorManager.registerListener(this, stepDetectorSensor, SensorManager.SENSOR_DELAY_UI);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == ACTIVITY_RECOGNITION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                initStepDetector();
            } else {
                Toast.makeText(this, "Permission denied!", Toast.LENGTH_SHORT).show();
            }
        }
    }
}