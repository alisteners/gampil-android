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

import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.json.JSONException;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import com.canhub.cropper.CropImageContract;
import com.canhub.cropper.CropImageContractOptions;
import com.canhub.cropper.CropImageOptions;
import com.canhub.cropper.CropImageView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private static final int REQUEST_CAMERA_PERMISSION = 101;
    private static final int REQUEST_NOTIFICATION_PERMISSION = 102;
    private static final String KEY_PHOTO_URI = "key_photo_uri";
    private static final String CHANNEL_ID = "pengaduan_channel";
    private static final int NOTIFICATION_ID = 1;
    private static final long POLLING_INTERVAL = 30000; // 30 detik

    private BottomNavigationView bottomNavigationView;
    private Uri currentPhotoUri;

    // --- Deklarasi semua ActivityResultLauncher ---
    private ActivityResultLauncher<Uri> ambilFotoLauncher;
    private ActivityResultLauncher<String> pilihGambarLauncher;
    private ActivityResultLauncher<Intent> buatPengaduanLauncher;
    private ActivityResultLauncher<CropImageContractOptions> cropImageLauncher;

    // Variabel untuk Notifikasi
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

        // Inisialisasi komponen
        bottomNavigationView = findViewById(R.id.bottom_navigation_view);
        requestQueue = Volley.newRequestQueue(this);
        sharedPreferences = getSharedPreferences(LoginActivity.SHARED_PREFS, Context.MODE_PRIVATE);

        // Panggil semua setup method
        initializeAllLaunchers();
        createNotificationChannel();
        checkAndRequestNotificationPermission();
        setupNavigation();
        setupPolling();

        // Atur halaman awal saat aplikasi pertama kali dibuka
        if (getIntent() != null && getIntent().getBooleanExtra("BUKA_NOTIFIKASI_FRAGMENT", false)) {
            bottomNavigationView.setSelectedItemId(R.id.nav_notifikasi);
            getIntent().removeExtra("BUKA_NOTIFIKASI_FRAGMENT");
        } else if (savedInstanceState == null) {
            bottomNavigationView.setSelectedItemId(R.id.nav_beranda);
        }
    }

    /**
     * Menginisialisasi semua ActivityResultLauncher di satu tempat.
     * Ini adalah praktik terbaik untuk memastikan semua callback terdaftar dengan benar.
     */
    private void initializeAllLaunchers() {

        // Launcher untuk menerima hasil dari kamera
        ambilFotoLauncher = registerForActivityResult(new ActivityResultContracts.TakePicture(), sukses -> {
            if (sukses && currentPhotoUri != null) {
                launchCropper(currentPhotoUri);
            } else {
                Toast.makeText(this, "Pengambilan foto dibatalkan.", Toast.LENGTH_SHORT).show();
            }
        });

        // Launcher untuk menerima hasil dari galeri
        pilihGambarLauncher = registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
            if (uri != null) {
                launchCropper(uri);
            } else {
                Toast.makeText(this, "Pemilihan gambar dibatalkan.", Toast.LENGTH_SHORT).show();
            }
        });

        // Launcher untuk menerima hasil dari activity cropping gambar
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
                Toast.makeText(this, "Gagal memotong gambar: " + (error != null ? error.getMessage() : "Unknown error"), Toast.LENGTH_SHORT).show();
            }
        });

        // Launcher untuk menerima hasil setelah pengaduan berhasil dibuat
        buatPengaduanLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Log.d(TAG, "Pengaduan berhasil dibuat, mengirim sinyal 'pengaduan_baru'.");
                        getSupportFragmentManager().setFragmentResult("pengaduan_baru", new Bundle());
                    }
                }
        );
    }

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

                // --- PENGATURAN MODE APLIKASI ---

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

                return false; // Mencegah pemilihan item navigasi & menjaga fragment tetap sama

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
     * [KHUSUS MODE DEVELOPER] Menampilkan dialog untuk memilih sumber gambar.
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
                    } else if (options[item].equals("Batal")) {
                        dialog.dismiss();
                    }
                })
                .show();
    }

    private void launchCropper(Uri imageUri) {
        CropImageOptions cropOptions = new CropImageOptions();
        cropOptions.guidelines = CropImageView.Guidelines.ON;
        cropOptions.fixAspectRatio = true;
        cropOptions.aspectRatioX = 4;
        cropOptions.aspectRatioY = 3;

        // Logika penyesuaian resolusi berdasarkan RAM perangkat
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
        cropOptions.outputRequestSizeOptions = CropImageView.RequestSizeOptions.RESIZE_INSIDE;

        cropOptions.outputCompressFormat = Bitmap.CompressFormat.JPEG;
        cropOptions.outputCompressQuality = 90;

        CropImageContractOptions contractOptions = new CropImageContractOptions(imageUri, cropOptions);
        cropImageLauncher.launch(contractOptions);
    }

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

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        return File.createTempFile("JPEG_" + timeStamp + "_", ".jpg", storageDir);
    }

    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.main_frame_layout, fragment)
                .commit();
    }

    // --- Metode-Metode Notifikasi ---
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

    private void setupPolling() {
        pollingRunnable = () -> {
            if (isFinishing() || isDestroyed()) return;
            cekNotifikasiBaru();
            pollingHandler.postDelayed(pollingRunnable, POLLING_INTERVAL);
        };
    }

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

    private void showNotificationBadge(int count) {
        BadgeDrawable badge = bottomNavigationView.getOrCreateBadge(R.id.nav_notifikasi);
        badge.setVisible(true);
        badge.setNumber(count);
    }

    private void hideNotificationBadge() {
        BadgeDrawable badge = bottomNavigationView.getOrCreateBadge(R.id.nav_notifikasi);
        badge.setVisible(false);
    }

    /**
     * Metode ini dipanggil oleh NotifikasiFragment untuk memperbarui badge
     * setelah notifikasi selesai dimuat atau dibaca.
     * @param count Jumlah notifikasi baru.
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
                Toast.makeText(this, "Izin notifikasi diberikan.", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Anda mungkin tidak menerima notifikasi update status.", Toast.LENGTH_LONG).show();
            }
        }
    }

    // --- Metode Siklus Hidup (Lifecycle) & Utilitas ---
    @Override
    protected void onResume() {
        super.onResume();
        pollingHandler.post(pollingRunnable);
        if (getIntent() != null && getIntent().getBooleanExtra("BUKA_NOTIFIKASI_FRAGMENT", false)) {
            bottomNavigationView.setSelectedItemId(R.id.nav_notifikasi);
            getIntent().removeExtra("BUKA_NOTIFIKASI_FRAGMENT");
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        pollingHandler.removeCallbacks(pollingRunnable);
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if (currentPhotoUri != null) {
            outState.putParcelable(KEY_PHOTO_URI, currentPhotoUri);
        }
    }

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