package com.updates.fragments;


import android.app.DownloadManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.airbnb.lottie.LottieAnimationView;
import com.airbnb.lottie.LottieDrawable;
import com.updates.R;
import com.updates.models.RebootDialog;
import com.updates.models.Update;

import org.apache.commons.io.FilenameUtils;

import java.io.File;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.fragment.app.Fragment;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Query;

import static com.updates.utils.ORSUtils.InstallZip;
import static com.updates.utils.OTAUtils.getDeviceName;

public class OTAFragment extends Fragment {
    private static final String TAG = "OTAFragment";
    private Retrofit retrofit = new Retrofit.Builder()
            .baseUrl("https://cosp-webserver.herokuapp.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build();
    private UpdatesApiInterface apiInterface = retrofit.create(UpdatesApiInterface.class);
    private LottieAnimationView lottieAnimationView;
    private Button checkUpdateButton;
    private Button downloadButton, checkChangeLogButton;
    private TextView updateStatus, changeLogText, changeLogTitleText, upToDate;
    private String deviceName;

    public OTAFragment() {
        // Required empty public constructor
    }

    private String downloadUrl;
    private BroadcastReceiver onComplete = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "com.updates.downloadID")
                    .setContentTitle("Update finished.")
                    .setContentText(FilenameUtils.getName(Uri.parse(downloadUrl).getPath()))
                    .setStyle(new NotificationCompat.BigTextStyle().bigText("Updooot"))
                    .setSmallIcon(R.drawable.ic_nav_settings);
            createNotificationChannel(context);
            NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(context);
            notificationManagerCompat.notify(6969, builder.build());
            downloadButton.setText(getString(R.string.flash_update));
        }
    };
    private File file;

    private void checkUpdate() {
        updateStatus.setText(getString(R.string.update_checking));
        lottieAnimationView.setAnimation("loading.json");
        lottieAnimationView.playAnimation();
        lottieAnimationView.setRepeatCount(LottieDrawable.INFINITE);
        changeLogText.setVisibility(View.GONE);
        changeLogTitleText.setVisibility(View.GONE);
        downloadButton.setVisibility(View.INVISIBLE);
        downloadButton.setClickable(false);
        Call<Update> call = apiInterface.checkUpdate(deviceName, 20190121);

        call.enqueue(new Callback<Update>() {
            @Override
            public void onResponse(Call<Update> call, Response<Update> response) {
                downloadUrl = response.body().getDownload();
                file = new File(Environment.getExternalStorageDirectory() + "/OTA/" + FilenameUtils.getName(Uri.parse(downloadUrl).getPath()));
                if (file.exists()) {
                    downloadButton.setText(getString(R.string.flash_update));
                } else {
                    downloadButton.setText(getString(R.string.download_update));
                }
                if (response.body().getUpdate()) {
                    lottieAnimationView.pauseAnimation();
                    lottieAnimationView.setAnimation("newAnimation.json");
                    lottieAnimationView.playAnimation();
                    lottieAnimationView.setRepeatCount(0);
                    updateStatus.setText(getString(R.string.update_available, deviceName));
                    changeLogText.setText(response.body().getChangeLog());
                    changeLogText.setVisibility(View.VISIBLE);
                    changeLogTitleText.setVisibility(View.VISIBLE);
                    upToDate.setVisibility(View.GONE);
                    checkChangeLogButton.setVisibility(View.GONE);
                    downloadButton.setVisibility(View.VISIBLE);
                    downloadButton.setClickable(true);
                } else {
                    lottieAnimationView.pauseAnimation();
                    lottieAnimationView.setAnimation("success.json");
                    lottieAnimationView.playAnimation();
                    lottieAnimationView.setRepeatCount(0);
                    updateStatus.setText(getString(R.string.up_to_date_device));
                    changeLogText.setVisibility(View.GONE);
                    changeLogTitleText.setVisibility(View.GONE);
                    upToDate.setVisibility(View.VISIBLE);
                    checkChangeLogButton.setVisibility(View.GONE);
                    downloadButton.setVisibility(View.INVISIBLE);
                    downloadButton.setClickable(false);
                }
            }

            @Override
            public void onFailure(Call<Update> call, Throwable t) {
                Log.w(TAG, "onFailure: ", t);
            }
        });
    }

    private void createNotificationChannel(Context context) {
        int importance = NotificationManager.IMPORTANCE_HIGH;
        NotificationChannel channel = new NotificationChannel("com.updates.downloadID", "Download update", importance);
        NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
        notificationManager.createNotificationChannel(channel);
    }




    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_ota, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        lottieAnimationView = view.findViewById(R.id.lottie_anim);
        checkUpdateButton = view.findViewById(R.id.test_button);
        checkChangeLogButton = view.findViewById(R.id.changelog_button);
        downloadButton = view.findViewById(R.id.download_button);
        updateStatus = view.findViewById(R.id.update_status);
        changeLogText = view.findViewById(R.id.changelog_text);
        upToDate = view.findViewById(R.id.up_to_date);
        changeLogTitleText = view.findViewById(R.id.changelog_title_text);
        deviceName = getDeviceName(requireContext());

        changeLogText.setMovementMethod(new ScrollingMovementMethod());
        updateStatus.setText(getString(R.string.update_uptodate));


        checkUpdateButton.setOnClickListener(l -> {
            checkUpdate();
        });
        downloadButton.setOnClickListener(l -> {
            if (file.exists()) {
                downloadButton.setText("Flash update");
                InstallZip(file.getAbsolutePath());
                RebootDialog dialog = new RebootDialog();
                dialog.show(getFragmentManager(), null);

            } else {
                Uri url = Uri.parse(downloadUrl);
                DownloadManager.Request request = new DownloadManager.Request(url);
                File direct = new File(Environment.getExternalStorageDirectory() + "/OTA");
                if (!direct.exists()) {
                    direct.mkdirs();
                }
                request.setDescription("Downloading update...");
                request.setDestinationInExternalPublicDir("/OTA", FilenameUtils.getName(url.getPath()));
                request.setTitle(FilenameUtils.getName(url.getPath()));
                DownloadManager manager = (DownloadManager) requireContext().getSystemService(Context.DOWNLOAD_SERVICE);
                manager.enqueue(request);
                Toast.makeText(requireContext(), "Downloading...", Toast.LENGTH_SHORT).show();
                updateStatus.setText("Downloading...");
                lottieAnimationView.setAnimation("newAnimation.json");
                lottieAnimationView.playAnimation();

            }
        });
        requireContext().registerReceiver(onComplete, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
        checkUpdate();

    }

    public interface UpdatesApiInterface {
        @GET("checkUpdate")
        Call<Update> checkUpdate(@Query("device") String device, @Query("date") int date);
    }
}
