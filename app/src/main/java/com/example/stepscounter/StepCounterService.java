package com.example.stepscounter;

import android.app.Service;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.IBinder;
import android.util.Log;

public class StepCounterService extends Service implements SensorEventListener {

    private static final String TAG = "StepCounterService";
    private SensorManager sensorManager;
    private Sensor accelerometerSensor;
    private float lastX, lastY, lastZ;
    private long lastUpdate;
    private int steps = 0;

    @Override
    public void onCreate() {
        super.onCreate();
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        if (sensorManager != null) {
            accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            if (accelerometerSensor != null) {
                sensorManager.registerListener(this, accelerometerSensor, SensorManager.SENSOR_DELAY_UI);
            } else {
                Log.e(TAG, "Accelerometer not available!");
            }
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // Return START_STICKY so that the service is restarted if it's killed
        return START_STICKY;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            long currentTime = System.currentTimeMillis();

            if (currentTime - lastUpdate > 100) {
                long diffTime = currentTime - lastUpdate;
                lastUpdate = currentTime;

                float deltaX = event.values[0] - lastX;
                float deltaY = event.values[1] - lastY;
                float deltaZ = event.values[2] - lastZ;

                lastX = event.values[0];
                lastY = event.values[1];
                lastZ = event.values[2];

                float shakeMagnitude = (float) Math.sqrt(deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ);

                if (shakeMagnitude > 10) {
                    steps++;
                    Log.d(TAG, "Step detected. Total steps: " + steps);
                    // You can use a notification to show the step count if desired.
                }
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Not used, but required by SensorEventListener
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (sensorManager != null) {
            sensorManager.unregisterListener(this);
        }
    }
}