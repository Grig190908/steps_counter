package com.example.stepscounter;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

public class ResetStepsReceiver extends BroadcastReceiver {

    private static final String PREFS_NAME = "step_prefs";
    private static final String STEP_COUNT_KEY = "step_count";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("ResetStepsReceiver", "Resetting steps to zero.");

        // Reset the step count to zero
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(STEP_COUNT_KEY, 0);
        editor.apply();
    }
}