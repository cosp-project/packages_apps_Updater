package com.updates.utils;

import android.content.Context;
import android.os.UpdateEngine;
import android.os.UpdateEngineCallback;
import android.util.Log;

import com.updates.R;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

public class ABUpdater {
    private static final String TAG = "ABUpdater";
    private static ABUpdater sInstance = null;
    private final Context mContext;
    private NotificationCompat.Builder builder;

    private NotificationManagerCompat notificationManager;
    private UpdateEngine updateEngine;
    private boolean updateDone;
    private final UpdateEngineCallback updateEngineCallback = new UpdateEngineCallback() {
        @Override
        public void onStatusUpdate(int status, float percent) {
            switch (status) {
                case UpdateEngine.UpdateStatusConstants.DOWNLOADING:
                case UpdateEngine.UpdateStatusConstants.FINALIZING: {
                    int progress = Math.round(percent * 100);
                    builder.setProgress(100, progress, false);
                    notificationManager.notify(6971, builder.build());

                }
                break;
                case UpdateEngine.UpdateStatusConstants.UPDATED_NEED_REBOOT: {
                    updateDone = true;
                    builder.setContentText("Update complete!")
                            .setProgress(0, 0, false);
                    notificationManager.notify(6971, builder.build());
                }
            }
        }

        @Override
        public void onPayloadApplicationComplete(int errorCode) {
            if (errorCode != UpdateEngine.ErrorCodeConstants.SUCCESS) {
                updateDone = false;
                builder.setContentText("Update failed!")
                        .setProgress(0, 0, false);
                notificationManager.notify(6971, builder.build());
            }
        }
    };
    private boolean bound;

    private ABUpdater(Context context) {
        this.mContext = context.getApplicationContext();
        updateEngine = new UpdateEngine();
        OTAUtils.createNotificationChannel(mContext, "com.updates.AB_UPDATE", "A/B Flash");
        notificationManager = NotificationManagerCompat.from(mContext);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(mContext, "com.updates.AB_UPDATE");
        builder.setContentTitle("A/B update")
                .setContentText("Flashing in progress")
                .setSmallIcon(R.drawable.ic_nav_settings)
                .setPriority(NotificationCompat.PRIORITY_LOW);
        builder.setProgress(100, 0, false);
    }

    public static synchronized ABUpdater getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new ABUpdater(context);
        }
        return sInstance;
    }

    public boolean install(String filePath) {
        File file = new File(filePath);
        if (!file.exists()) {
            Log.e(TAG, "The given update doesn't exist");
            return false;
        }

        long offset;
        String[] headerKeyValuePairs;
        try {
            ZipFile zipFile = new ZipFile(file);
            offset = OTAUtils.getZipEntryOffset(zipFile, Constants.AB_PAYLOAD_BIN_PATH);
            ZipEntry payloadPropEntry = zipFile.getEntry(Constants.AB_PAYLOAD_PROPERTIES_PATH);
            try (InputStream is = zipFile.getInputStream(payloadPropEntry);
                 InputStreamReader isr = new InputStreamReader(is);
                 BufferedReader br = new BufferedReader(isr)) {
                List<String> lines = new ArrayList<>();
                for (String line; (line = br.readLine()) != null; ) {
                    lines.add(line);
                }
                headerKeyValuePairs = new String[lines.size()];
                headerKeyValuePairs = lines.toArray(headerKeyValuePairs);
            }
            zipFile.close();
        } catch (IOException | IllegalArgumentException e) {
            Log.e(TAG, "Could not prepare " + file, e);
            return false;
        }

        if (!bound) {
            bound = updateEngine.bind(updateEngineCallback);
            if (!bound) {
                Log.e(TAG, "Could not bind");
                return false;
            }
        }
        String zipFileUri = "file://" + file.getAbsolutePath();
        updateEngine.applyPayload(zipFileUri, offset, 0, headerKeyValuePairs);
        return true;
    }
}
