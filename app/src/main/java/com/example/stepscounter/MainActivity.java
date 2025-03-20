package com.example.stepscounter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private SensorManager sensorManager;
    private Sensor accelerometerSensor;
    private TextView stepCountTextView, userEmailTextView;
    private int steps = 0;

    private FirebaseAuth mAuth;
    private Button btnSignOut, btnResetSteps;

    private static final int ACTIVITY_RECOGNITION_REQUEST_CODE = 100;

    private float lastX, lastY, lastZ;
    private long lastUpdate;

    // Time tracking for step limitation
    private long lastStepTime = 0;
    private int stepCountInLastSecond = 0;
    private static final int MAX_STEPS_PER_SECOND = 6;

    private static final String TAG = "ShakeDetection"; // Debugging log tag

    @SuppressLint("MissingInflatedId")
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
        userEmailTextView = findViewById(R.id.userEmailTextView);
        btnSignOut = findViewById(R.id.btnSignOut);
        btnResetSteps = findViewById(R.id.btnResetSteps);

        userEmailTextView.setText("Logged in as: " + currentUser.getEmail());

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        if (checkSelfPermission(android.Manifest.permission.ACTIVITY_RECOGNITION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACTIVITY_RECOGNITION},
                    ACTIVITY_RECOGNITION_REQUEST_CODE);
        } else {
            initAccelerometer();
        }

        btnSignOut.setOnClickListener(v -> {
            mAuth.signOut();
            Toast.makeText(MainActivity.this, "Signed out successfully", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(MainActivity.this, SignInActivity.class));
            finish();
        });

        btnResetSteps.setOnClickListener(v -> {
            steps = 0;
            stepCountTextView.setText("Steps: " + steps);
            Toast.makeText(MainActivity.this, "Step count reset!", Toast.LENGTH_SHORT).show();
        });
    }

    private void initAccelerometer() {
        if (sensorManager != null) {
            accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

            if (accelerometerSensor == null) {
                Toast.makeText(this, "Accelerometer not available!", Toast.LENGTH_LONG).show();
            } else {
                sensorManager.registerListener(this, accelerometerSensor, SensorManager.SENSOR_DELAY_UI);
            }
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            long currentTime = System.currentTimeMillis();

            // Only check for shake every 100 milliseconds
            if (currentTime - lastUpdate > 100) {
                long diffTime = currentTime - lastUpdate;
                lastUpdate = currentTime;

                float deltaX = event.values[0] - lastX;
                float deltaY = event.values[1] - lastY;
                float deltaZ = event.values[2] - lastZ;

                lastX = event.values[0];
                lastY = event.values[1];
                lastZ = event.values[2];

                // Calculate the shake magnitude
                float shakeMagnitude = (float) Math.sqrt(deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ);

                // Log the shake magnitude for debugging
                Log.d(TAG, "Shake Magnitude: " + shakeMagnitude);

                // If the shake magnitude is above a threshold, count it as a step, but limit steps per second
                if (shakeMagnitude > 10) {  // Reduced the threshold value for testing
                    if (currentTime - lastStepTime < 1000) {
                        // If steps are being counted too quickly, limit them
                        if (stepCountInLastSecond < MAX_STEPS_PER_SECOND) {
                            steps++;
                            stepCountInLastSecond++;
                            stepCountTextView.setText("Steps: " + steps);
                            Log.d(TAG, "Step detected. Total steps: " + steps);
                            Toast.makeText(this, "Shake detected as step!", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        // Reset step count for the new second
                        stepCountInLastSecond = 0;
                    }

                    // Update last step time
                    lastStepTime = currentTime;
                }
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Not used, but required by SensorEventListener
    }

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
        if (accelerometerSensor != null) {
            sensorManager.registerListener(this, accelerometerSensor, SensorManager.SENSOR_DELAY_UI);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == ACTIVITY_RECOGNITION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                initAccelerometer();
            } else {
                Toast.makeText(this, "Permission denied!", Toast.LENGTH_SHORT).show();
            }
        }
    }
}