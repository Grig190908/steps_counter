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
    private TextView stepCountTextView;
    private int steps = 0;

    private FirebaseAuth mAuth;
    private Button btnSignOut;

    private static final int ACTIVITY_RECOGNITION_REQUEST_CODE = 100;
    private static final String TAG = "StepCounter";

    private float lastX, lastY, lastZ;
    private long lastUpdate;

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
        btnSignOut = findViewById(R.id.btnSignOut);
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        if (checkSelfPermission(android.Manifest.permission.ACTIVITY_RECOGNITION) != PackageManager.PERMISSION_GRANTED) {
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

            if (currentTime - lastUpdate > 200) { // Adjusted for better accuracy
                long diffTime = currentTime - lastUpdate;
                lastUpdate = currentTime;

                float deltaX = event.values[0] - lastX;
                float deltaY = event.values[1] - lastY;
                float deltaZ = event.values[2] - lastZ;

                lastX = event.values[0];
                lastY = event.values[1];
                lastZ = event.values[2];

                float shakeMagnitude = (float) Math.sqrt(deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ);

                Log.d(TAG, "Shake Magnitude: " + shakeMagnitude);

                if (shakeMagnitude > 12) { // Adjusted threshold for better step detection
                    steps++;
                    stepCountTextView.setText("Steps: " + steps);
                    Log.d(TAG, "Step detected. Total steps: " + steps);
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