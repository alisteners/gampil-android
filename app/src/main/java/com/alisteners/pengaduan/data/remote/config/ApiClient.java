package com.alisteners.pengaduan.data.remote.config;

import com.alisteners.pengaduan.data.remote.service.ApiService;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Kelas singleton yang bertanggung jawab untuk membuat dan menyediakan instance Retrofit ApiService.
 * Kelas ini mengonfigurasi koneksi ke server, termasuk base URL dan logging untuk-
 * proses debugging jaringan.
 * <p>
 * Penggunaan pola singleton (hanya ada satu instance ApiService) memastikan efisiensi
 * dengan tidak berulang kali membuat objek Retrofit yang berat.
 */
public class ApiClient {
    // Instance ApiService dibuat sekali dan digunakan kembali (pola singleton).
    private static ApiService apiService = null;

    /**
     * Mendapatkan instance singleton dari ApiService.
     * Jika instance belum ada, metode ini akan menginisialisasinya terlebih dahulu.
     * <p>
     * Metode ini juga mengonfigurasi HttpLoggingInterceptor untuk mencatat semua
     * request dan response HTTP ke Logcat. Ini sangat berguna selama tahap
     * pengembangan dan debugging untuk memantau komunikasi data dengan server.
     *
     * @return Instance ApiService yang siap digunakan.
     */
    public static ApiService getApiService() {
        if (apiService == null) {

            // 1. Buat Interceptor untuk logging. Ini akan menampilkan detail request/response di Logcat.
            HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
            loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

            // 2. Buat OkHttpClient dan tambahkan interceptor logging.
            OkHttpClient okHttpClient = new OkHttpClient.Builder()
                    .addInterceptor(loggingInterceptor)
                    .build();

            // 3. Bangun objek Retrofit, konfigurasikan dengan base URL, client, dan converter.
            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(ApiConfig.BASE_URL)
                    .client(okHttpClient) // Gunakan client yang sudah dikonfigurasi dengan logging
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();

            // 4. Buat implementasi dari interface ApiService.
            apiService = retrofit.create(ApiService.class);
        }
        return apiService;
    }
}