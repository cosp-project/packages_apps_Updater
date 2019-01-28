/*
 * Copyright (C) 2018 Chandra Poerwanto
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.updates.utils;

import android.app.Activity;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.PowerManager;
import android.os.SystemProperties;
import android.util.Log;
import android.widget.Toast;

import com.updates.R;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.text.DecimalFormat;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import androidx.annotation.NonNull;

public final class OTAUtils {

    private static final String TAG = "OTA";
    private static final boolean DEBUG = true;

    private OTAUtils() {
    }

    public static void logError(Exception e) {
        if (DEBUG) {
            Log.e(TAG, e.getMessage(), e);
        }
    }

    public static void logError(String e) {
        if (DEBUG) {
            Log.e(TAG, e);
        }
    }

    public static void logInfo(String message) {
        if (DEBUG) {
            Log.i(TAG, message);
        }
    }

    public static void toast(int messageId, Context context) {
        if (context != null) {
            Toast.makeText(context, context.getResources().getString(messageId),
                    Toast.LENGTH_LONG).show();
        }
    }

    public static String getDeviceName(Context context) {
        return OTAUtils.getProp(context.getString(R.string.prop_device_name));
    }

    public static String getProp(String propName) {
        Process p;
        String result = "";
        try {
            p = new ProcessBuilder("/system/bin/getprop", propName).redirectErrorStream(true).start();
            BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String line;
            while ((line=br.readLine()) != null) {
                result = line;
            }
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    public static String runCommand(String command) {
        try {
            StringBuilder output = new StringBuilder();
            Process p = Runtime.getRuntime().exec(command);
            BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }
            reader.close();
            p.waitFor();
            return output.toString();
        } catch (InterruptedException | IOException e) {
            logError(e);
        }
        return "";
    }

    public static InputStream downloadURL(String link) throws IOException {
        URL url = new URL(link);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setReadTimeout(10000);
        conn.setConnectTimeout(15000);
        conn.setRequestMethod("GET");
        conn.setDoInput(true);
        conn.connect();
        logInfo("downloadStatus: " + conn.getResponseCode());
        return conn.getInputStream();
    }

    public static void launchURL(Context context, String URL) {
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(URL));
        PackageManager pm = context.getPackageManager();
        if (browserIntent.resolveActivity(pm) != null) {
            context.startActivity(browserIntent);
        } else {
            Toast toast = Toast.makeText(context, R.string.toast_message, Toast.LENGTH_LONG);
            toast.show();
        }
    }

    public static void CopyStream(InputStream is, OutputStream os)
    {
        final int buffer_size=1024;
        try
        {
            byte[] bytes=new byte[buffer_size];
            for(;;)
            {
                int count=is.read(bytes, 0, buffer_size);
                if(count==-1)
                    break;
                os.write(bytes, 0, count);
            }
        }
        catch(Exception ex){}
    }

    public static String getSizeFromURL(@NonNull final Context context, final String URL) {
        int size = 0;
        try {
            URLConnection urlConnection = (new URL(URL)).openConnection();
            urlConnection.connect();
            size = urlConnection.getContentLength();
        } catch (Exception ex) {
            logError("Unable to get size from URL.");
        }
        return getSizeString(context, size);
    }

    public static void rebootRecovery(Context context){
        ((PowerManager) context.getApplicationContext().getSystemService(Activity.POWER_SERVICE)).reboot("recovery-update");
    }

    @NonNull
    public static String getSizeString(@NonNull final Context context, long bytes) {
        if (bytes < 0) bytes = 0;
        double kb = (double) bytes / (double) 1000;
        double mb = kb / (double) 1000;
        final DecimalFormat decimalFormat = new DecimalFormat(".##");
        if (mb >= 1) {
            return context.getString(R.string.size_mb, decimalFormat.format(mb));
        } else if (kb >= 1) {
            return context.getString(R.string.size_kb, decimalFormat.format(kb));
        } else {
            return context.getString(R.string.size_bytes, bytes);
        }
    }

    /**
     * Get the offset to the compressed data of a file inside the given zip
     *
     * @param zipFile   input zip file
     * @param entryPath full path of the entry
     * @return the offset of the compressed, or -1 if not found
     * @throws IllegalArgumentException if the given entry is not found
     */
    public static long getZipEntryOffset(ZipFile zipFile, String entryPath) {
        // Each entry has an header of (30 + n + m) bytes
        // 'n' is the length of the file name
        // 'm' is the length of the extra field
        final int FIXED_HEADER_SIZE = 30;
        Enumeration<? extends ZipEntry> zipEntries = zipFile.entries();
        long offset = 0;
        while (zipEntries.hasMoreElements()) {
            ZipEntry entry = zipEntries.nextElement();
            int n = entry.getName().length();
            int m = entry.getExtra() == null ? 0 : entry.getExtra().length;
            int headerSize = FIXED_HEADER_SIZE + n + m;
            offset += headerSize;
            if (entry.getName().equals(entryPath)) {
                return offset;
            }
            offset += entry.getCompressedSize();
        }
        Log.e(TAG, "Entry " + entryPath + " not found");
        throw new IllegalArgumentException("The given entry was not found");
    }

    public static void createNotificationChannel(Context context, String channelId, String channelName) {
        int importance = NotificationManager.IMPORTANCE_HIGH;
        NotificationChannel channel = new NotificationChannel(channelId, channelName, importance);
        NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
        notificationManager.createNotificationChannel(channel);
    }

    public static boolean isABDevice() {
        return SystemProperties.getBoolean(Constants.PROP_AB_DEVICE, false);
    }

    public static boolean isABUpdate(ZipFile zipFile) {
        return zipFile.getEntry(Constants.AB_PAYLOAD_BIN_PATH) != null &&
                zipFile.getEntry(Constants.AB_PAYLOAD_PROPERTIES_PATH) != null;
    }

    public static boolean isABUpdate(File file) throws IOException {
        ZipFile zipFile = new ZipFile(file);
        boolean isAB = isABUpdate(zipFile);
        zipFile.close();
        return isAB;
    }
}