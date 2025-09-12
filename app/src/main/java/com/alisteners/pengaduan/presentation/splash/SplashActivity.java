package com.alisteners.pengaduan.presentation.splash;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AppCompatActivity;

import com.alisteners.pengaduan.R;
import com.alisteners.pengaduan.presentation.login.LoginActivity;

/**
 * Activity yang berfungsi sebagai Splash Screen (layar pembuka).
 * Layar ini akan tampil selama beberapa saat saat aplikasi pertama kali dijalankan,
 * sebelum secara otomatis mengarahkan pengguna ke {@link LoginActivity}.
 */
@SuppressLint("CustomSplashScreen")
public class SplashActivity extends AppCompatActivity {

    /**
     * Durasi tampilan splash screen dalam milidetik (ms) sebelum berpindah ke activity berikutnya.
     */
    private static final int SPLASH_DELAY = 3000;

    /**
     * Metode yang dipanggil saat Activity pertama kali dibuat.
     * Mengatur tampilan layout dan memulai Handler untuk menunda transisi ke LoginActivity.
     *
     * @param savedInstanceState Jika Activity diinisialisasi ulang, Bundle ini berisi data terakhir.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // Menggunakan Handler untuk menunda eksekusi kode
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            // Kode di dalam blok ini akan dieksekusi setelah SPLASH_DELAY berlalu
            Intent intent = new Intent(SplashActivity.this, LoginActivity.class);
            startActivity(intent);
            finish(); // Menutup SplashActivity agar pengguna tidak bisa kembali ke layar ini
        }, SPLASH_DELAY);
    }
}