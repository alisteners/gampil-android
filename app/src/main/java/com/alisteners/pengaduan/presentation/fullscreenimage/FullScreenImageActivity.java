package com.alisteners.pengaduan.presentation.fullscreenimage;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.alisteners.pengaduan.R;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.github.chrisbanes.photoview.PhotoView;

/**
 * Activity yang bertanggung jawab untuk menampilkan sebuah gambar dalam mode layar penuh (fullscreen).
 * Activity ini menggunakan library PhotoView untuk memungkinkan interaksi zoom (pinch-to-zoom)
 * dan pan pada gambar.
 */
public class FullScreenImageActivity extends AppCompatActivity {

    /**
     * Kunci (key) yang digunakan untuk mengirim URL gambar melalui Intent.
     */
    public static final String EXTRA_IMAGE_URL = "extra_image_url";

    /**
     * Metode yang dipanggil saat Activity pertama kali dibuat.
     * Bertanggung jawab untuk inisialisasi layout, view, dan memuat gambar dari URL
     * yang diterima melalui Intent.
     *
     * @param savedInstanceState Jika Activity diinisialisasi ulang setelah sebelumnya dihancurkan,
     * Bundle ini berisi data yang terakhir disimpan.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_full_screen_image);

        // Mengatur UI flag untuk membuat layout menjadi fullscreen, menyembunyikan status bar.
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);

        final PhotoView photoView = findViewById(R.id.photo_view);
        final ProgressBar progressBar = findViewById(R.id.progressBarFullScreen);
        final ImageButton buttonClose = findViewById(R.id.buttonClose);

        buttonClose.setOnClickListener(v -> onBackPressed());

        String imageUrl = getIntent().getStringExtra(EXTRA_IMAGE_URL);

        // Memvalidasi dan memuat gambar menggunakan Glide.
        if (imageUrl != null && !imageUrl.isEmpty()) {
            progressBar.setVisibility(View.VISIBLE);
            Glide.with(this)
                    .load(imageUrl)
                    .listener(new RequestListener<>() {
                        /**
                         * Callback yang dipanggil jika Glide gagal memuat gambar.
                         */
                        @Override
                        public boolean onLoadFailed(@Nullable GlideException e, Object model, @NonNull Target<Drawable> target, boolean isFirstResource) {
                            progressBar.setVisibility(View.GONE);
                            Toast.makeText(FullScreenImageActivity.this, "Gagal memuat gambar.", Toast.LENGTH_SHORT).show();
                            return false; // Mengizinkan Glide untuk menampilkan error placeholder jika ada.
                        }

                        /**
                         * Callback yang dipanggil saat gambar berhasil dimuat.
                         */
                        @Override
                        public boolean onResourceReady(@NonNull Drawable resource, @NonNull Object model, Target<Drawable> target, @NonNull DataSource dataSource, boolean isFirstResource) {
                            progressBar.setVisibility(View.GONE);
                            return false; // Mengizinkan Glide untuk menampilkan gambar ke target.
                        }
                    })
                    .into(photoView);
        } else {
            Toast.makeText(this, "URL gambar tidak ditemukan.", Toast.LENGTH_SHORT).show();
            finish(); // Tutup activity jika tidak ada URL gambar.
        }
    }
}