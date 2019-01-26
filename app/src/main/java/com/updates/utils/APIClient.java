package com.updates.utils;

import com.updates.models.Update;

import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Query;

public class APIClient {
    private APIClient(String BASE_URL, Retrofit retrofit) {
    }

    public interface UpdatesApiInterface {
        @GET("checkUpdate")
        Call<Update> checkUpdate(@Query("device") String device, @Query("date") int date);
    }

    public static class Builder {
        private String BASE_URL;
        private Retrofit retrofit;

        public Builder(){}

        public Builder setBaseUrl(String BASE_URL) {
            this.BASE_URL = BASE_URL;
            return this;
        }


        public APIClient build() {
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();

            return new APIClient(BASE_URL, retrofit);
        }
    }
}
