package com.updates;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.updates.fragments.OTAFragment;
import com.updates.services.UpdateService;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import static com.updates.utils.SingletonServiceChecker.IS_SERVICE_RUNNING;

public class MainActivity extends AppCompatActivity {
    private static final int STORAGE_PERMISSION_CODE = 200;
    private static final String TAG = "MainActivity";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        checkStoragePermissions();
        getSupportFragmentManager().beginTransaction().replace(R.id.container, new OTAFragment()).commitNow();
        Context context = this;
        if (!IS_SERVICE_RUNNING) {
            Intent intent1 = new Intent(context, UpdateService.class);
            context.startService(intent1);
            Log.i(TAG, "Started");
        }
    }

    private void checkStoragePermissions() {
        if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
            requestPermissions(new String[]{ Manifest.permission.WRITE_EXTERNAL_STORAGE}, STORAGE_PERMISSION_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (!(requestCode == STORAGE_PERMISSION_CODE && grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
            Toast.makeText(MainActivity.this,  R.string.permission_not_enabled, Toast.LENGTH_LONG)
                    .show();
            finish();
        }
    }
}
