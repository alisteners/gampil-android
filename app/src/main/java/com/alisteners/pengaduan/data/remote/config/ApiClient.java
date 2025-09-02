package com.alisteners.pengaduan.data.remote.config;

import com.alisteners.pengaduan.data.remote.service.ApiService;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {
    private static ApiService apiService = null;

    public static ApiService getApiService() {
        if (apiService == null) {

            // 1. Buat Interceptor untuk logging
            HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
            loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY); // Level BODY untuk melihat semua detail

            // 2. Buat OkHttpClient dan tambahkan interceptor
            OkHttpClient okHttpClient = new OkHttpClient.Builder()
                    .addInterceptor(loggingInterceptor)
                    .build();

            // 3. Bangun Retrofit dengan OkHttpClient yang baru
            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(ApiConfig.BASE_URL)
                    .client(okHttpClient) // Gunakan client yang sudah ada logging-nya
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();

            apiService = retrofit.create(ApiService.class);
        }
        return apiService;
    }
}