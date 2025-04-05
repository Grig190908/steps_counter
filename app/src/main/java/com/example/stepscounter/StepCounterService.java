package com.example.stepscounter;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
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

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class StepCounterService extends Service implements SensorEventListener {

    private SensorManager sensorManager;
    private Sensor stepDetectorSensor;
    private int steps = 0;
    private static final String PREFS_NAME = "step_prefs";
    private static final String STEP_COUNT_KEY = "step_count";
    private static final String LAST_RESET_DATE_KEY = "last_reset_date";
    private static final String STEP_UPDATE_ACTION = "com.example.stepscounter.STEP_UPDATE";
    private static final int NOTIFICATION_ID = 1;

    @Override
    public void onCreate() {
        super.onCreate();
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        steps = loadStepCount();
        initStepDetector();
        startForeground(NOTIFICATION_ID, createNotification());
    }

    private void initStepDetector() {
        if (sensorManager != null) {
            stepDetectorSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR);
            if (stepDetectorSensor == null) {
                Log.e("StepCounterService", "Step Detector not available!");
                stopSelf();
            } else {
                sensorManager.registerListener(this, stepDetectorSensor, SensorManager.SENSOR_DELAY_UI);
            }
        }
    }

    private Notification createNotification() {
        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        return new NotificationCompat.Builder(this, NotificationHelper.CHANNEL_ID)
                .setContentTitle("Step Counter Running")
                .setContentText("Steps: " + steps)
                .setSmallIcon(R.drawable.ic_notification) // Replace with your notification icon
                .setContentIntent(pendingIntent)
                .setOngoing(true)
                .build();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_STEP_DETECTOR) {
            steps++;
            saveStepCount(steps);
            sendStepUpdateBroadcast();
            Log.d("StepCounterService", "Step detected. Total steps: " + steps);

            if (steps % 5000 == 0) {
                NotificationHelper notificationHelper = new NotificationHelper(this);
                notificationHelper.sendNotification("Great job!", "You've walked " + steps + " steps! Keep going!");
            }
            // Update notification
            startForeground(NOTIFICATION_ID, createNotification());
        }
    }

    private void sendStepUpdateBroadcast() {
        Intent intent = new Intent(STEP_UPDATE_ACTION);
        intent.putExtra("steps", steps);
        sendBroadcast(intent);
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
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putInt(STEP_COUNT_KEY, 0);
            editor.putString(LAST_RESET_DATE_KEY, today);
            editor.apply();
            return 0;
        }
        return sharedPreferences.getInt(STEP_COUNT_KEY, 0);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {}

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (sensorManager != null) {
            sensorManager.unregisterListener(this);
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}