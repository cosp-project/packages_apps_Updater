package com.updates.fragments;


import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.airbnb.lottie.LottieAnimationView;
import com.airbnb.lottie.LottieDrawable;
import com.updates.R;
import com.updates.models.Update;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Query;

import static com.updates.utils.OTAUtils.getDeviceName;

public class OTAFragment extends Fragment {
    private static final String TAG = "OTAFragment";
    private Retrofit retrofit = new Retrofit.Builder()
            .baseUrl("https://cosp-webserver.herokuapp.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build();
    private UpdatesApiInterface apiInterface = retrofit.create(UpdatesApiInterface.class);
    private LottieAnimationView lottieAnimationView;
    public OTAFragment() {
        // Required empty public constructor
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
        Button checkUpdateButton = view.findViewById(R.id.test_button);
        Button checkChangeLogButton = view.findViewById(R.id.changelog_button);
        TextView updateStatus = view.findViewById(R.id.update_status);
        TextView changeLogText = view.findViewById(R.id.changelog_text);
        TextView upToDate = view.findViewById(R.id.up_to_date);
        TextView changeLogTitleText = view.findViewById(R.id.changelog_title_text);
        String deviceName = getDeviceName(requireContext());

        changeLogText.setMovementMethod(new ScrollingMovementMethod());
        updateStatus.setText(getString(R.string.update_uptodate));
        checkUpdateButton.setOnClickListener(l -> {
            updateStatus.setText(getString(R.string.update_checking));
            lottieAnimationView.setAnimation("loading.json");
            lottieAnimationView.playAnimation();
            lottieAnimationView.setRepeatCount(LottieDrawable.INFINITE);
            Call<Update> call = apiInterface.checkUpdate(deviceName, 20190121);

            call.enqueue(new Callback<Update>() {
                @Override
                public void onResponse(Call<Update> call, Response<Update> response) {
                    if (response.body().getUpdate()) {
                        lottieAnimationView.pauseAnimation();
                        lottieAnimationView.setAnimation("newAnimation.json");
                        lottieAnimationView.playAnimation();
                        lottieAnimationView.setRepeatCount(LottieDrawable.INFINITE);
                        updateStatus.setText(getString(R.string.update_available, deviceName));
                        changeLogText.setText(response.body().getChangeLog());
                        changeLogText.setVisibility(View.VISIBLE);
                        changeLogTitleText.setVisibility(View.VISIBLE);
                        upToDate.setVisibility(View.GONE);
                        checkChangeLogButton.setVisibility(View.GONE);
                    } else {
                        lottieAnimationView.pauseAnimation();
                        lottieAnimationView.setAnimation("boom.json");
                        lottieAnimationView.playAnimation();
                        updateStatus.setText(getString(R.string.up_to_date_device));
                        changeLogText.setVisibility(View.GONE);
                        changeLogTitleText.setVisibility(View.GONE  );
                        upToDate.setVisibility(View.VISIBLE);
                        checkChangeLogButton.setVisibility(View.GONE);
                    }
                }

                @Override
                public void onFailure(Call<Update> call, Throwable t) {
                    Log.w(TAG, "onFailure: ", t);
                }
            });
        });

    }

    public interface UpdatesApiInterface {
        @GET("checkUpdate")
        Call<Update> checkUpdate(@Query("device") String device, @Query("date") int date);
    }
}
