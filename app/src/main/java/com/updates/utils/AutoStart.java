package com.updates.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.updates.services.UpdateService;

public class AutoStart extends BroadcastReceiver {
    private static final String TAG = "AutoStart";
    @Override
    public void onReceive(Context context, Intent intent) {
        Intent intent1 = new Intent(context, UpdateService.class);
        context.startService(intent1);
        Log.i(TAG, "Started");
    }
}
