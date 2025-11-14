package com.alisteners.pengaduan.presentation.masyarakat.lapor;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.exifinterface.media.ExifInterface;
import androidx.lifecycle.ViewModelProvider;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.alisteners.pengaduan.presentation.login.LoginActivity;
import com.alisteners.pengaduan.R;
import com.alisteners.pengaduan.data.repository.PengaduanRepositoryImpl;
import com.alisteners.pengaduan.data.util.ImageProcessingManager;
import com.alisteners.pengaduan.data.util.SizeFormatter;
import com.alisteners.pengaduan.domain.entity.PengaduanData;
import com.alisteners.pengaduan.domain.repository.PengaduanRepository;
import com.alisteners.pengaduan.domain.usecase.KirimPengaduanUseCase;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.tasks.CancellationTokenSource;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Activity yang bertanggung jawab untuk menangani pembuatan pengaduan baru oleh pengguna.
 * Alur kerja utama meliputi:
 * 1. Menampilkan form input (foto, lokasi, deskripsi).
 * 2. Mengambil lokasi GPS pengguna secara otomatis.
 * 3. Memvalidasi semua input dari pengguna.
 * 4. Memulai proses kompresi gambar di background thread.
 * 5. Mengirim data yang telah diproses ke ViewModel untuk diteruskan ke server.
 */
public class BuatPengaduanActivity extends AppCompatActivity {

    private static final String TAG = "BuatPengaduanActivity";
    private static final int REQUEST_LOCATION_PERMISSION = 102;
    private final CancellationTokenSource cancellationTokenSource = new CancellationTokenSource();

    // --- Deklarasi Komponen UI ---
    private ImageView imageViewFotoPengaduan;
    private TextView tvUkuranAsliPreview;
    private EditText editTextLokasiOtomatis, editTextDetailLokasiManual, editTextDeskripsi;
    private Spinner spinnerKategori;
    private CheckBox checkboxPernyataan;
    private Button buttonKirimPengaduan;
    private ProgressBar progressBarBuatPengaduan;

    // --- Deklarasi Properti Lain ---
    private Bitmap bitmapFotoPengaduan;
    private FusedLocationProviderClient fusedLocationClient;
    private double latitude = 0.0, longitude = 0.0;
    private ExecutorService executorService;
    private Handler mainThreadHandler;
    private BuatPengaduanViewModel viewModel;
    private long ukuranFileAsliBytes = 0;
    private ImageProcessingManager imageProcessingManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_buat_pengaduan);

        // Inisialisasi komponen-komponen penting
        executorService = Executors.newSingleThreadExecutor();
        mainThreadHandler = new Handler(Looper.getMainLooper());
        imageProcessingManager = new ImageProcessingManager(this);

        // Inisialisasi ViewModel
        PengaduanRepository pengaduanRepository = new PengaduanRepositoryImpl(getApplicationContext());
        KirimPengaduanUseCase kirimPengaduanUseCase = new KirimPengaduanUseCase(pengaduanRepository);
        BuatPengaduanViewModelFactory factory = new BuatPengaduanViewModelFactory(kirimPengaduanUseCase);
        viewModel = new ViewModelProvider(this, factory).get(BuatPengaduanViewModel.class);

        inisialisasiView();
        setupSpinnerKategori();
        setupListeners();
        observeViewModel();

        // Memproses data gambar dan lokasi yang diterima dari Activity sebelumnya
        handleIncomingPhotoAndLocation();
    }

    /**
     * Menginisialisasi semua referensi ke komponen view dari layout XML.
     */
    private void inisialisasiView() {
        ImageButton buttonKembali = findViewById(R.id.buttonKembali);
        imageViewFotoPengaduan = findViewById(R.id.imageViewFotoPengaduan);
        tvUkuranAsliPreview = findViewById(R.id.tv_ukuran_asli_preview);
        editTextLokasiOtomatis = findViewById(R.id.editTextLokasiOtomatis);
        ImageButton buttonRefreshLokasi = findViewById(R.id.buttonRefreshLokasi);
        editTextDetailLokasiManual = findViewById(R.id.editTextDetailLokasiManual);
        spinnerKategori = findViewById(R.id.spinnerKategori);
        editTextDeskripsi = findViewById(R.id.editTextDeskripsi);
        checkboxPernyataan = findViewById(R.id.checkboxPernyataan);
        buttonKirimPengaduan = findViewById(R.id.buttonKirimPengaduan);
        progressBarBuatPengaduan = findViewById(R.id.progressBarBuatPengaduan);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
    }

    /**
     * Mengatur semua listener untuk interaksi pengguna pada Activity ini.
     */
    private void setupListeners() {
        // Listener untuk tombol kembali
        findViewById(R.id.buttonKembali).setOnClickListener(v -> finish());

        // Listener untuk tombol refresh lokasi
        ImageButton buttonRefreshLokasi = findViewById(R.id.buttonRefreshLokasi);
        if (buttonRefreshLokasi != null) {
            buttonRefreshLokasi.setOnClickListener(v -> {
                Toast.makeText(BuatPengaduanActivity.this, getString(R.string.memperbarui_lokasi), Toast.LENGTH_SHORT).show();
                editTextLokasiOtomatis.setText(getString(R.string.hint_lokasi_otomatis_mencari));
                requestLocationPermissionAndGetLocation();
            });
        }

        // Listener untuk tombol kirim pengaduan
        buttonKirimPengaduan.setOnClickListener(v -> attemptKirimPengaduan());
    }

    /**
     * Mengamati LiveData dari ViewModel untuk memperbarui UI secara reaktif
     * terhadap perubahan state (loading, sukses, error).
     */
    private void observeViewModel() {
        viewModel.isLoading.observe(this, isLoading -> {
            if (isLoading != null) {
                buttonKirimPengaduan.setEnabled(!isLoading);
                progressBarBuatPengaduan.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            }
        });

        viewModel.pesanSukses.observe(this, message -> {
            if (message != null && !message.isEmpty()) {
                Toast.makeText(BuatPengaduanActivity.this, message, Toast.LENGTH_LONG).show();
                viewModel.clearPesanSukses();
                setResult(RESULT_OK); // Mengirim sinyal sukses ke Activity sebelumnya
                finish();
            }
        });

        viewModel.pesanError.observe(this, message -> {
            if (message != null && !message.isEmpty()) {
                Toast.makeText(BuatPengaduanActivity.this, message, Toast.LENGTH_LONG).show();
                viewModel.clearPesanError();
            }
        });
    }

    /**
     * Memproses data (URI gambar dan ukuran file) yang dikirim dari Activity sebelumnya,
     * lalu memulai permintaan lokasi.
     */
    @SuppressLint("SetTextI18n")
    private void handleIncomingPhotoAndLocation() {
        Intent intent = getIntent();
        if (intent != null) {
            ukuranFileAsliBytes = intent.getLongExtra("ORIGINAL_FILE_SIZE", 0);
            Log.d(TAG, "Ukuran File Asli diterima: " + ukuranFileAsliBytes + " bytes");

            // --- REVISI SHP: TAMPILKAN UKURAN ASLI ---
            String ukuranAsliFormatted = SizeFormatter.formatUkuranBiner(ukuranFileAsliBytes);
            tvUkuranAsliPreview.setText("Ukuran Sebelum Upload : " + ukuranAsliFormatted);
            tvUkuranAsliPreview.setVisibility(View.VISIBLE);
            // --- AKHIR REVISI ---

            if (intent.hasExtra("PHOTO_URI")) {
                Uri photoUri = Uri.parse(intent.getStringExtra("PHOTO_URI"));
                try {
                    Bitmap originalBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), photoUri);
                    if (originalBitmap != null) {
                        int rotation = getRotationFromExif(photoUri);
                        bitmapFotoPengaduan = rotateBitmap(originalBitmap, rotation);
                        imageViewFotoPengaduan.setImageBitmap(bitmapFotoPengaduan);
                    }
                } catch (IOException e) {
                    setDefaultImageAndMessage(getString(R.string.gagal_memuat_gambar));
                }
            }
        }
        requestLocationPermissionAndGetLocation();
    }

    /**
     * Titik awal saat tombol "Kirim Pengaduan" di-klik.
     * Metode ini melakukan validasi input UI dan sesi pengguna sebelum
     * memulai proses pengiriman yang berat.
     */
    private void attemptKirimPengaduan() {
        hideKeyboard();

        // Validasi input form
        if (!validateInputFields()) {
            return;
        }

        final SharedPreferences sp = getSharedPreferences(LoginActivity.SHARED_PREFS, MODE_PRIVATE);
        final int idUser = sp.getInt(LoginActivity.KEY_ID_USER, -1);
        if (idUser == -1) {
            Toast.makeText(this, getString(R.string.error_sesi_tidak_valid), Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        // Menyiapkan UI untuk proses loading
        progressBarBuatPengaduan.setVisibility(View.VISIBLE);
        buttonKirimPengaduan.setEnabled(false);

        // Mengambil nilai final dari UI sebelum masuk ke background thread
        final String lokasiOtomatisValue = editTextLokasiOtomatis.getText().toString().trim();
        final String detailLokasiManualValue = editTextDetailLokasiManual.getText().toString().trim();
        final String kategoriValue = spinnerKategori.getSelectedItem().toString();
        final String deskripsiValue = editTextDeskripsi.getText().toString().trim();

        // Memulai proses di background thread
        prosesDanKirimPengaduan(lokasiOtomatisValue, detailLokasiManualValue, kategoriValue, deskripsiValue, idUser);
    }

    /**
     * Mengeksekusi proses kompresi gambar dan pengiriman data di background thread
     * untuk mencegah pemblokiran UI thread (ANR).
     */
    private void prosesDanKirimPengaduan(String lokasiOtomatis, String detailLokasi, String kategori, String deskripsi, int idUser) {
        executorService.execute(() -> {
            try {
                // Gabungkan lokasi otomatis dan manual
                String lokasiFinal = lokasiOtomatis;
                if (!detailLokasi.isEmpty()) {
                    lokasiFinal = lokasiOtomatis.isEmpty() ? detailLokasi : lokasiOtomatis + " (Patokan : " + detailLokasi + ")";
                }

                // Buat objek data awal
                PengaduanData dataAwal = new PengaduanData(idUser, lokasiFinal, kategori, deskripsi, false, latitude, longitude, checkboxPernyataan.isChecked());

                // Panggil ImageProcessingManager untuk melakukan semua pekerjaan berat
                ImageProcessingManager.ProcessingResult result = imageProcessingManager.processAndCompressImage(bitmapFotoPengaduan, ukuranFileAsliBytes, dataAwal);

                // Kirim hasil kembali ke Main Thread untuk diperbarui di ViewModel
                mainThreadHandler.post(() -> viewModel.prosesKirimPengaduan(result.pengaduanData(), result.fileDataUntukUpload(), result.fileThumbnail()));

            } catch (Exception e) {
                Log.e(TAG, "Gagal memproses pengaduan di background thread", e);
                mainThreadHandler.post(() -> {
                    Toast.makeText(BuatPengaduanActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    progressBarBuatPengaduan.setVisibility(View.GONE);
                    buttonKirimPengaduan.setEnabled(true);
                });
            }
        });
    }

    /**
     * Membersihkan resource seperti ExecutorService saat Activity dihancurkan.
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
    }

    // --- Metode-Metode Helper ---

    /**
     * Memeriksa izin lokasi. Jika sudah diberikan, langsung ambil lokasi.
     * Jika belum, minta izin kepada pengguna.
     */
    private void requestLocationPermissionAndGetLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            getCurrentLocation();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION_PERMISSION);
        }
    }

    /**
     * Callback yang dipanggil setelah pengguna merespons dialog permintaan izin.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getCurrentLocation();
            } else {
                Toast.makeText(this, getString(R.string.izin_lokasi_ditolak_tidak_bisa_otomatis), Toast.LENGTH_LONG).show();
            }
        }
    }

    /**
     * Mengambil lokasi terkini pengguna menggunakan FusedLocationProviderClient.
     */
    @SuppressLint("MissingPermission")
    private void getCurrentLocation() {
        fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, cancellationTokenSource.getToken())
                .addOnSuccessListener(this, location -> {
                    if (location != null) {
                        latitude = location.getLatitude();
                        longitude = location.getLongitude();
                        getAddressFromLatLng(latitude, longitude);
                    } else {
                        Toast.makeText(BuatPengaduanActivity.this, getString(R.string.gagal_mendapatkan_lokasi_gps_pastikan_aktif), Toast.LENGTH_LONG).show();
                    }
                })
                .addOnFailureListener(this, e -> Log.e(TAG, "Gagal mendapatkan lokasi", e));
    }

    /**
     * Mengonversi koordinat (latitude, longitude) menjadi alamat yang dapat dibaca manusia
     * menggunakan Geocoder.
     */
    private void getAddressFromLatLng(double lat, double lon) {
        if (!Geocoder.isPresent()) {
            editTextLokasiOtomatis.setText(String.format(Locale.US, "Lat: %.5f, Lon: %.5f", lat, lon));
            return;
        }
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(lat, lon, 1);
            if (addresses != null && !addresses.isEmpty()) {
                editTextLokasiOtomatis.setText(formatAddress(addresses));
            } else {
                editTextLokasiOtomatis.setText(String.format(Locale.US, "Lat: %.5f, Lon: %.5f", lat, lon));
            }
        } catch (IOException e) {
            Log.e(TAG, "Layanan Geocoder error: " + e.getMessage());
        }
    }

    /**
     * Memformat objek Address menjadi satu baris string alamat.
     */
    @NonNull
    private static String formatAddress(List<Address> addresses) {
        Address address = addresses.get(0);
        return address.getAddressLine(0) != null ? address.getAddressLine(0) : "";
    }

    /**
     * Mengatur data dan adapter untuk Spinner kategori.
     */
    private void setupSpinnerKategori() {
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.kategori_pengaduan_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerKategori.setAdapter(adapter);
    }

    /**
     * Membaca metadata EXIF dari gambar untuk mendapatkan orientasi rotasinya.
     * @param imageUri URI dari gambar.
     * @return Sudut rotasi dalam derajat (0, 90, 180, 270).
     */
    private int getRotationFromExif(Uri imageUri) {
        try (InputStream inputStream = getContentResolver().openInputStream(imageUri)) {
            if (inputStream == null) return 0;
            ExifInterface exifInterface = new ExifInterface(inputStream);
            int orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
            return switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90 -> 90;
                case ExifInterface.ORIENTATION_ROTATE_180 -> 180;
                case ExifInterface.ORIENTATION_ROTATE_270 -> 270;
                default -> 0;
            };
        } catch (IOException e) {
            return 0;
        }
    }

    /**
     * Memutar (rotate) objek Bitmap sesuai dengan sudut yang ditentukan.
     * @param source Bitmap asli.
     * @param angle Sudut rotasi.
     * @return Bitmap baru yang sudah dirotasi.
     */
    public static Bitmap rotateBitmap(Bitmap source, float angle) {
        if (source == null || angle == 0) return source;
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
    }

    /**
     * Menyembunyikan soft keyboard dari layar.
     */
    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        View currentFocus = getCurrentFocus();
        if (currentFocus != null) {
            imm.hideSoftInputFromWindow(currentFocus.getWindowToken(), 0);
        }
        if (editTextDeskripsi != null) editTextDeskripsi.clearFocus();
        if (editTextDetailLokasiManual != null) editTextDetailLokasiManual.clearFocus();
    }

    /**
     * Menampilkan gambar placeholder dan pesan Toast jika terjadi error.
     */
    private void setDefaultImageAndMessage(String message) {
        if (imageViewFotoPengaduan != null) {
            imageViewFotoPengaduan.setImageResource(R.drawable.ic_image_placeholder);
        }
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    /**
     * Melakukan validasi pada semua field input di form.
     * @return true jika semua input valid, false jika ada yang tidak valid.
     */
    private boolean validateInputFields() {
        if (bitmapFotoPengaduan == null) {
            Toast.makeText(this, getString(R.string.foto_bukti_wajib), Toast.LENGTH_SHORT).show();
            return false;
        }
        if (spinnerKategori.getSelectedItem() == null || spinnerKategori.getSelectedItemPosition() <= 0) {
            Toast.makeText(this, getString(R.string.kategori_harus_dipilih), Toast.LENGTH_SHORT).show();
            return false;
        }
        if (editTextDeskripsi.getText().toString().trim().isEmpty()) {
            editTextDeskripsi.setError(getString(R.string.error_deskripsi_kosong));
            editTextDeskripsi.requestFocus();
            return false;
        }
        if (!checkboxPernyataan.isChecked()) {
            Toast.makeText(this, getString(R.string.error_pernyataan_belum_disetujui), Toast.LENGTH_LONG).show();
            return false;
        }
        return true;
    }
}