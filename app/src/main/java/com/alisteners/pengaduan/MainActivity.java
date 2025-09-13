package com.alisteners.pengaduan;

import android.Manifest;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.provider.OpenableColumns;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import com.alisteners.pengaduan.data.remote.config.ApiConfig;
import com.alisteners.pengaduan.presentation.masyarakat.beranda.BerandaFragment;
import com.alisteners.pengaduan.presentation.masyarakat.lapor.BuatPengaduanActivity;
import com.alisteners.pengaduan.presentation.login.LoginActivity;
import com.alisteners.pengaduan.presentation.masyarakat.notifikasi.NotifikasiFragment;
import com.alisteners.pengaduan.presentation.masyarakat.profil.ProfilFragment;
import com.alisteners.pengaduan.presentation.masyarakat.riwayatpengaduan.RiwayatFragment;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.canhub.cropper.CropImageContract;
import com.canhub.cropper.CropImageContractOptions;
import com.canhub.cropper.CropImageOptions;
import com.canhub.cropper.CropImageView;
import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import org.json.JSONException;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Activity utama aplikasi untuk pengguna 'Masyarakat' setelah berhasil login.
 * (Dokumentasi Javadoc lainnya tetap sama)
 */
public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private static final int REQUEST_CAMERA_PERMISSION = 101;
    private static final int REQUEST_NOTIFICATION_PERMISSION = 102;
    private static final String KEY_PHOTO_URI = "key_photo_uri";
    private static final String CHANNEL_ID = "pengaduan_channel";
    private static final int NOTIFICATION_ID = 1;
    private static final long POLLING_INTERVAL = 30000; // 30 detik

    private BottomNavigationView bottomNavigationView;
    private Uri currentPhotoUri; // Menyimpan URI foto saat mengambil dari kamera

    // --- Deklarasi semua ActivityResultLauncher ---
    private ActivityResultLauncher<Uri> ambilFotoLauncher;
    private ActivityResultLauncher<String> pilihGambarLauncher;
    private ActivityResultLauncher<Intent> buatPengaduanLauncher;
    private ActivityResultLauncher<CropImageContractOptions> cropImageLauncher;

    // --- Variabel untuk Notifikasi ---
    private RequestQueue requestQueue;
    private SharedPreferences sharedPreferences;
    private final Handler pollingHandler = new Handler(Looper.getMainLooper());
    private Runnable pollingRunnable;
    private boolean isNotificationVisible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (savedInstanceState != null) {
            currentPhotoUri = savedInstanceState.getParcelable(KEY_PHOTO_URI);
        }

        bottomNavigationView = findViewById(R.id.bottom_navigation_view);
        requestQueue = Volley.newRequestQueue(this);
        sharedPreferences = getSharedPreferences(LoginActivity.SHARED_PREFS, Context.MODE_PRIVATE);

        initializeAllLaunchers();
        createNotificationChannel();
        checkAndRequestNotificationPermission();
        setupNavigation();
        setupPolling();

        // Navigasi awal saat Activity dibuka
        if (getIntent() != null && getIntent().getBooleanExtra("BUKA_NOTIFIKASI_FRAGMENT", false)) {
            bottomNavigationView.setSelectedItemId(R.id.nav_notifikasi);
            getIntent().removeExtra("BUKA_NOTIFIKASI_FRAGMENT");
        } else if (savedInstanceState == null) {
            bottomNavigationView.setSelectedItemId(R.id.nav_beranda);
        }
    }

    /**
     * Menginisialisasi semua {@link ActivityResultLauncher} yang digunakan dalam Activity ini.
     */
    private void initializeAllLaunchers() {
        ambilFotoLauncher = registerForActivityResult(new ActivityResultContracts.TakePicture(), sukses -> {
            if (sukses && currentPhotoUri != null) {
                launchCropper(currentPhotoUri);
            } else {
                Toast.makeText(this, "Pengambilan foto dibatalkan.", Toast.LENGTH_SHORT).show();
            }
        });

        pilihGambarLauncher = registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
            if (uri != null) {
                launchCropper(uri);
            } else {
                Toast.makeText(this, "Pemilihan gambar dibatalkan.", Toast.LENGTH_SHORT).show();
            }
        });

        cropImageLauncher = registerForActivityResult(new CropImageContract(), result -> {
            if (result.isSuccessful()) {
                Uri croppedUri = result.getUriContent();
                Uri originalUri = result.getOriginalUri();
                long originalFileSize = (originalUri != null) ? getFileSizeFromUri(originalUri) : 0;

                Intent intent = new Intent(this, BuatPengaduanActivity.class);
                if (croppedUri != null) {
                    intent.putExtra("PHOTO_URI", croppedUri.toString());
                    intent.putExtra("ORIGINAL_FILE_SIZE", originalFileSize);
                    buatPengaduanLauncher.launch(intent);
                }
            } else {
                Exception error = result.getError();
                Toast.makeText(this, "Gagal memotong gambar: " + (error != null ? error.getMessage() : "Error tidak diketahui"), Toast.LENGTH_SHORT).show();
            }
        });

        buatPengaduanLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    // Jika pengaduan berhasil dibuat, kirim sinyal ke fragment lain untuk refresh.
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        // Sinyal ini akan diterima oleh BerandaFragment untuk memuat ulang data.
                        getSupportFragmentManager().setFragmentResult("pengaduan_baru", new Bundle());
                    }
                }
        );
    }

    /**
     * Mengatur listener untuk BottomNavigationView dan menangani transaksi Fragment.
     */
    private void setupNavigation() {
        bottomNavigationView.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            int itemId = item.getItemId();

            if (itemId == R.id.nav_beranda) {
                selectedFragment = new BerandaFragment();
            } else if (itemId == R.id.nav_aktivitas) {
                selectedFragment = new RiwayatFragment();
            } else if (itemId == R.id.nav_profil) {
                selectedFragment = new ProfilFragment();
            } else if (itemId == R.id.nav_lapor) {
                // >> MODE 1: DEVELOPER (Untuk Analisis Skripsi)
                // Menampilkan dialog untuk memilih antara Kamera atau Galeri.
                tampilkanDialogPilihGambar();

                // >> MODE 2: PRODUKSI (Untuk Pengguna Akhir)
                // Langsung membuka kamera. Hapus atau beri komentar pada baris di atas,
                // lalu hapus komentar pada blok di bawah ini.
                /*
                if (checkAndRequestCameraPermission()) {
                    bukaKameraInternal();
                }
                */
                return false;
            } else if (itemId == R.id.nav_notifikasi) {
                selectedFragment = new NotifikasiFragment();
                hideNotificationBadge();
                isNotificationVisible = false;
            }

            if (selectedFragment != null) {
                loadFragment(selectedFragment);
            }
            return true;
        });
    }

    /**
     * Menampilkan dialog untuk memilih sumber gambar (Kamera/Galeri).
     */
    private void tampilkanDialogPilihGambar() {
        final CharSequence[] options = {"Ambil dari Kamera", "Pilih dari Galeri", "Batal"};
        new MaterialAlertDialogBuilder(this)
                .setTitle("Pilih Sumber Gambar")
                .setItems(options, (dialog, item) -> {
                    if (options[item].equals("Ambil dari Kamera")) {
                        if (checkAndRequestCameraPermission()) {
                            bukaKameraInternal();
                        }
                    } else if (options[item].equals("Pilih dari Galeri")) {
                        pilihGambarLauncher.launch("image/*");
                    } else {
                        dialog.dismiss();
                    }
                })
                .show();
    }

    /**
     * Meluncurkan activity cropping gambar dengan URI gambar sebagai input.
     * @param imageUri URI dari gambar yang akan di-crop.
     */
    private void launchCropper(Uri imageUri) {
        CropImageOptions cropOptions = new CropImageOptions();
        cropOptions.guidelines = CropImageView.Guidelines.ON;
        cropOptions.fixAspectRatio = true;
        cropOptions.aspectRatioX = 4;
        cropOptions.aspectRatioY = 3;

        // Logika penyesuaian resolusi output dinamis berdasarkan total RAM perangkat.
        // Ini adalah langkah krusial untuk mencegah OutOfMemoryError di perangkat low-end.
        ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        ActivityManager.MemoryInfo memoryInfo = new ActivityManager.MemoryInfo();
        activityManager.getMemoryInfo(memoryInfo);
        long totalRamMB = memoryInfo.totalMem / (1024 * 1024);

        int outputWidth, outputHeight;
        if (totalRamMB < 2000) { // RAM < 2GB
            outputWidth = 1024;
            outputHeight = 768;
        } else if (totalRamMB < 4000) { // RAM 2–4GB
            outputWidth = 1600;
            outputHeight = 1200;
        } else { // RAM ≥ 4GB
            outputWidth = 2000;
            outputHeight = 1500;
        }

        cropOptions.outputRequestWidth = outputWidth;
        cropOptions.outputRequestHeight = outputHeight;
        // Jika ingin resolusi asli hapus/komen line 267
        cropOptions.outputRequestSizeOptions = CropImageView.RequestSizeOptions.RESIZE_INSIDE;

        cropOptions.outputCompressFormat = Bitmap.CompressFormat.JPEG;
        cropOptions.outputCompressQuality = 90;

        CropImageContractOptions contractOptions = new CropImageContractOptions(imageUri, cropOptions);
        cropImageLauncher.launch(contractOptions);
    }

    /**
     * Membuka intent kamera internal perangkat.
     */
    private void bukaKameraInternal() {
        try {
            File photoFile = createImageFile();
            String authority = getPackageName() + ".fileprovider";
            currentPhotoUri = FileProvider.getUriForFile(this, authority, photoFile);
            ambilFotoLauncher.launch(currentPhotoUri);
        } catch (IOException ex) {
            Toast.makeText(this, "Gagal membuat file foto.", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Gagal membuat file foto", ex);
        }
    }

    /**
     * Membuat file gambar sementara.
     * @return Objek File yang baru dibuat.
     */
    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        return File.createTempFile("JPEG_" + timeStamp + "_", ".jpg", storageDir);
    }

    /**
     * Mengganti fragment yang sedang ditampilkan.
     * @param fragment Fragment yang akan ditampilkan.
     */
    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.main_frame_layout, fragment)
                .commit();
    }

    // --- Metode-Metode Notifikasi ---

    /**
     * Membuat Notification Channel. Diperlukan untuk Android Oreo (API 26) ke atas.
     */
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Notifikasi Pengaduan";
            String description = "Channel untuk notifikasi status laporan pengaduan";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            getSystemService(NotificationManager.class).createNotificationChannel(channel);
        }
    }

    /**
     * Mengatur mekanisme polling periodik untuk memeriksa notifikasi baru.
     */
    private void setupPolling() {
        pollingRunnable = () -> {
            if (isFinishing() || isDestroyed()) return;
            cekNotifikasiBaru();
            pollingHandler.postDelayed(pollingRunnable, POLLING_INTERVAL);
        };
    }

    /**
     * Mengirim request ke server untuk memeriksa jumlah notifikasi baru.
     */
    private void cekNotifikasiBaru() {
        int idUser = sharedPreferences.getInt(LoginActivity.KEY_ID_USER, 0);
        if (idUser == 0) return;

        String url = ApiConfig.URL_CEK_NOTIFIKASI + "?idUser=" + idUser;
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        if ("success".equals(response.getString("status"))) {
                            int jumlahBaru = response.getInt("jumlah_notifikasi_baru");
                            if (jumlahBaru > 0) {
                                showNotificationBadge(jumlahBaru);
                                if (!isNotificationVisible) {
                                    showSystemNotification(jumlahBaru);
                                    isNotificationVisible = true;
                                }
                            } else {
                                hideNotificationBadge();
                                isNotificationVisible = false;
                            }
                        }
                    } catch (JSONException e) {
                        Log.e(TAG, "Error parsing JSON notifikasi: " + e.getMessage());
                    }
                },
                error -> Log.e(TAG, "Volley Error cek notifikasi: " + error.toString()));
        requestQueue.add(request);
    }

    /**
     * Menampilkan notifikasi di status bar sistem Android.
     * @param jumlahBaru Jumlah notifikasi baru yang akan ditampilkan dalam teks notifikasi.
     */
    private void showSystemNotification(int jumlahBaru) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.putExtra("BUKA_NOTIFIKASI_FRAGMENT", true);

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.icon_gampil)
                .setContentTitle("Pembaruan Status Pengaduan")
                .setContentText("Anda memiliki " + jumlahBaru + " pembaruan status yang belum dilihat.")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        NotificationManagerCompat.from(this).notify(NOTIFICATION_ID, builder.build());
    }

    /** Menampilkan badge (lencana) notifikasi pada item BottomNavigationView. */
    private void showNotificationBadge(int count) {
        BadgeDrawable badge = bottomNavigationView.getOrCreateBadge(R.id.nav_notifikasi);
        badge.setVisible(true);
        badge.setNumber(count);
    }

    /** Menyembunyikan badge notifikasi. */
    private void hideNotificationBadge() {
        BadgeDrawable badge = bottomNavigationView.getOrCreateBadge(R.id.nav_notifikasi);
        badge.setVisible(false);
    }

    /**
     * Metode publik yang dapat dipanggil oleh fragment (misalnya {@link NotifikasiFragment})
     * untuk memperbarui tampilan badge.
     * @param count Jumlah notifikasi baru yang tersisa.
     */
    public void updateNotificationBadge(int count) {
        if (count > 0) {
            showNotificationBadge(count);
        } else {
            hideNotificationBadge();
        }
    }

    // --- Metode-Metode Izin (Permissions) ---
    private boolean checkAndRequestCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
            return false;
        }
        return true;
    }

    private void checkAndRequestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, REQUEST_NOTIFICATION_PERMISSION);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                bukaKameraInternal();
            } else {
                Toast.makeText(this, "Izin kamera diperlukan untuk dapat melapor.", Toast.LENGTH_LONG).show();
            }
        }
        if (requestCode == REQUEST_NOTIFICATION_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Izin diberikan, tidak perlu menampilkan toast agar tidak mengganggu.
            } else {
                Toast.makeText(this, "Anda mungkin tidak menerima notifikasi update status.", Toast.LENGTH_LONG).show();
            }
        }
    }

    // --- Metode Siklus Hidup (Lifecycle) & Utilitas ---
    @Override
    protected void onResume() {
        super.onResume();
        pollingHandler.post(pollingRunnable); // Mulai polling saat aplikasi aktif
        if (getIntent() != null && getIntent().getBooleanExtra("BUKA_NOTIFIKASI_FRAGMENT", false)) {
            bottomNavigationView.setSelectedItemId(R.id.nav_notifikasi);
            getIntent().removeExtra("BUKA_NOTIFIKASI_FRAGMENT");
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        pollingHandler.removeCallbacks(pollingRunnable); // Hentikan polling saat aplikasi tidak aktif
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if (currentPhotoUri != null) {
            outState.putParcelable(KEY_PHOTO_URI, currentPhotoUri);
        }
    }

    /**
     * Mendapatkan ukuran file (dalam bytes) dari sebuah URI.
     * @param uri URI dari file.
     * @return Ukuran file dalam bytes, atau 0 jika gagal.
     */
    private long getFileSizeFromUri(Uri uri) {
        try (Cursor cursor = getContentResolver().query(uri, null, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                int sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE);
                if (sizeIndex != -1) {
                    return cursor.getLong(sizeIndex);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Gagal mendapatkan ukuran file dari URI", e);
        }
        return 0;
    }
}