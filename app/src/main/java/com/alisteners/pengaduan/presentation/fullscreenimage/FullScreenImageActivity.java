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

public class FullScreenImageActivity extends AppCompatActivity {

    public static final String EXTRA_IMAGE_URL = "extra_image_url";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_full_screen_image);

        // Atur agar layout menjadi fullscreen
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);

        final PhotoView photoView = findViewById(R.id.photo_view);
        final ProgressBar progressBar = findViewById(R.id.progressBarFullScreen);
        final ImageButton buttonClose = findViewById(R.id.buttonClose);

        buttonClose.setOnClickListener(v -> onBackPressed());

        String imageUrl = getIntent().getStringExtra(EXTRA_IMAGE_URL);

        if (imageUrl != null && !imageUrl.isEmpty()) {
            progressBar.setVisibility(View.VISIBLE);
            Glide.with(this)
                    .load(imageUrl)
                    .listener(new RequestListener<>() {
                        @Override
                        public boolean onLoadFailed(@Nullable GlideException e, Object model, @NonNull Target<Drawable> target, boolean isFirstResource) {
                            progressBar.setVisibility(View.GONE);
                            Toast.makeText(FullScreenImageActivity.this, "Gagal memuat gambar.", Toast.LENGTH_SHORT).show();
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(@NonNull Drawable resource, @NonNull Object model, Target<Drawable> target, @NonNull DataSource dataSource, boolean isFirstResource) {
                            progressBar.setVisibility(View.GONE);
                            return false;
                        }
                    })
                    .into(photoView);
        } else {
            Toast.makeText(this, "URL gambar tidak ditemukan.", Toast.LENGTH_SHORT).show();
            finish();
        }
    }
}