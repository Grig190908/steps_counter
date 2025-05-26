package com.example.stepscounter;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ResetStepsReceiver extends BroadcastReceiver {

    private static final String PREFS_NAME = "step_prefs";
    private static final String STEP_COUNT_KEY = "step_count";
    private static final String LAST_RESET_DATE_KEY = "last_reset_date";
    private static final String MONTHLY_STEPS_KEY_PREFIX = "monthly_steps_";

    @Override
    public void onReceive(Context context, Intent intent) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        int currentSteps = sharedPreferences.getInt(STEP_COUNT_KEY, 0);

        // Save to monthly total
        saveToMonthlyTotal(context, currentSteps);

        // Reset daily steps
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(STEP_COUNT_KEY, 0);
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        editor.putString(LAST_RESET_DATE_KEY, today);
        editor.apply();

        // Notify the app to update UI
        Intent updateIntent = new Intent("com.example.stepscounter.STEP_UPDATE");
        updateIntent.putExtra("steps", 0);
        context.sendBroadcast(updateIntent);
    }

    private void saveToMonthlyTotal(Context context, int dailySteps) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String monthKey = MONTHLY_STEPS_KEY_PREFIX + new SimpleDateFormat("yyyy_MM", Locale.getDefault()).format(new Date());
        int currentMonthlySteps = sharedPreferences.getInt(monthKey, 0);
        currentMonthlySteps += dailySteps;
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(monthKey, currentMonthlySteps);
        editor.apply();
    }
}