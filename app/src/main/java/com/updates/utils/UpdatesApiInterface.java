package com.updates.utils;

import com.updates.models.Update;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface UpdatesApiInterface {
    @GET("checkUpdate")
    Call<Update> checkUpdate(@Query("device") String device, @Query("date") int date);
}