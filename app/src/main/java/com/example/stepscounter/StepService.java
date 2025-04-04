package com.example.stepscounter;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

public class StepService extends Service implements SensorEventListener {

    private SensorManager sensorManager;
    private int steps = 0;
    private int initialStepCount = 0;
    private static final String PREFS_NAME = "step_prefs";
    private static final String STEP_COUNT_KEY = "step_count";
    private static final String CHANNEL_ID = "step_channel";
    private static final int NOTIFICATION_ID = 1;
    private boolean isFirstReading = true;

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
        startForeground(NOTIFICATION_ID, createNotification("Counting your steps..."));

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        Sensor stepCounterSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);

        if (stepCounterSensor != null) {
            sensorManager.registerListener(this, stepCounterSensor, SensorManager.SENSOR_DELAY_NORMAL);
        } else {
            Log.e("StepService", "Step Counter sensor not available");
            stopSelf(); // Stop service if sensor is unavailable
        }

        steps = loadStepCount();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY; // Restart if service is killed
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        sensorManager.unregisterListener(this);
        saveStepCount(steps);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null; // No binding needed
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_STEP_COUNTER) {
            if (isFirstReading) {
                initialStepCount = (int) event.values[0];
                isFirstReading = false;
            }
            steps = (int) event.values[0] - initialStepCount + loadStepCount();
            saveStepCount(steps);

            // Update notification
            updateNotification("Steps: " + steps);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // No action needed
    }

    private void saveStepCount(int steps) {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt(STEP_COUNT_KEY, steps);
        editor.apply();
    }

    private int loadStepCount() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        return prefs.getInt(STEP_COUNT_KEY, 0);
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "Step Counter", NotificationManager.IMPORTANCE_LOW);
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }
    }

    private Notification createNotification(String content) {
        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Step Counter Active")
                .setContentText(content)
                .setSmallIcon(R.drawable.ic_walk)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .build();
    }

    private void updateNotification(String content) {
        Notification notification = createNotification(content);
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        manager.notify(NOTIFICATION_ID, notification);
    }
}