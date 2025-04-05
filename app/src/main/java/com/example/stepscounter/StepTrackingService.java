package com.example.stepscounter;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

public class StepTrackingService extends Service {

    private static final String CHANNEL_ID = "StepTrackingChannel";

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // Только здесь запускаем foreground
        startForegroundServiceWithNotification();
        return START_STICKY;
    }

    @SuppressLint("ForegroundServiceType")
    private void startForegroundServiceWithNotification() {
        createNotificationChannel();
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Подсчёт шагов")
                .setContentText("Приложение считает ваши шаги...")
                .setSmallIcon(R.drawable.ic_walk) // Убедись, что иконка есть!
                .setOngoing(true)
                .build();

        // ⚠️ Вот тут начинается foreground, и именно здесь нужно вызывать startForeground
        startForeground(1, notification);
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Step Tracking Service Channel",
                    NotificationManager.IMPORTANCE_LOW
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}