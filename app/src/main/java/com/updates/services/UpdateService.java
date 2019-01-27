package com.updates.services;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import com.google.gson.Gson;
import com.pusher.client.Pusher;
import com.pusher.client.PusherOptions;
import com.pusher.client.channel.Channel;
import com.updates.MainActivity;
import com.updates.R;
import com.updates.models.PushNotification;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import static com.updates.utils.OTAUtils.getDeviceName;
import static com.updates.utils.SingletonServiceChecker.IS_SERVICE_RUNNING;

public class UpdateService extends Service {
    private static final String TAG = "UpdateService";

    public UpdateService() {
    }

    private void createNotificationChannel(Context context) {
        int importance = NotificationManager.IMPORTANCE_HIGH;
        NotificationChannel channel = new NotificationChannel(getString(R.string.updates_arrived_channel), "Update check", importance);
        NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
        notificationManager.createNotificationChannel(channel);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        Toast.makeText(this, getString(R.string.toast_update_service_stopped), Toast.LENGTH_LONG).show();
        Log.d(TAG, "onDestroy: ");
        IS_SERVICE_RUNNING = false;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        IS_SERVICE_RUNNING = true;
        Toast.makeText(this, getString(R.string.toast_update_service_started), Toast.LENGTH_SHORT).show();
        PusherOptions options = new PusherOptions();
        options.setCluster("EU");
        Pusher pusher = new Pusher(getString(R.string.api_key), options);
        Gson gson = new Gson();


        Channel channel = pusher.subscribe(getString(R.string.pusher_channel_name));
        channel.bind(getString(R.string.pusher_event), (s, s1, s2) -> {
            Log.d(TAG, "onCreate: " + s2);
            String device = gson.fromJson(s2, PushNotification.class).getDevice();
            Log.d(TAG, "onStartCommand: "+ device);
            if (device.equals(getDeviceName(getBaseContext()))) {
                Intent resultIntent = new Intent(getBaseContext(), MainActivity.class);
                TaskStackBuilder stackBuilder = TaskStackBuilder.create(getBaseContext());
                stackBuilder.addNextIntentWithParentStack(resultIntent);
                PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
                NotificationCompat.Builder builder = new NotificationCompat.Builder(getBaseContext(), getString(R.string.notif_channel_new_update))
                        .setContentIntent(resultPendingIntent)
                        .setContentTitle(getString(R.string.notif_update_available))
                        .setContentText(getString(R.string.notif_body_ota_available, device))
                        .setStyle(new NotificationCompat.BigTextStyle().bigText(getString(R.string.notif_body_ota_available, device)))
                        .setSmallIcon(R.drawable.ic_nav_settings);
                createNotificationChannel(getBaseContext());

                NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(getBaseContext());
                notificationManagerCompat.notify(6970, builder.build());
            }
        });
        pusher.connect();
        return START_STICKY;
    }
}
