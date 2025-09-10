package com.alisteners.pengaduan.presentation.masyarakat.lapor;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.exifinterface.media.ExifInterface;
import androidx.lifecycle.ViewModelProvider;

import android.Manifest;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Matrix;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import com.alisteners.pengaduan.presentation.login.LoginActivity;
import com.alisteners.pengaduan.R;
import com.alisteners.pengaduan.data.repository.PengaduanRepositoryImpl;
import com.alisteners.pengaduan.data.util.RLECompressor;
import com.alisteners.pengaduan.domain.entity.PengaduanData;
import com.alisteners.pengaduan.domain.repository.PengaduanRepository;
import com.alisteners.pengaduan.domain.usecase.KirimPengaduanUseCase;
import com.alisteners.pengaduan.util.MyPair;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.tasks.CancellationTokenSource;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BuatPengaduanActivity extends AppCompatActivity {

    private static final String TAG = "BuatPengaduanActivity";
    private static final int REQUEST_LOCATION_PERMISSION = 102;
    private final CancellationTokenSource cancellationTokenSource = new CancellationTokenSource();

    private ImageView imageViewFotoPengaduan;
    private EditText editTextLokasiOtomatis;
    private EditText editTextDetailLokasiManual;
    private Spinner spinnerKategori;
    private EditText editTextDeskripsi;
    private CheckBox checkboxPernyataan;
    private Button buttonKirimPengaduan;
    private ProgressBar progressBarBuatPengaduan;
    private Bitmap bitmapFotoPengaduan;
    private FusedLocationProviderClient fusedLocationClient;
    private double latitude = 0.0, longitude = 0.0;
    private ExecutorService executorService;
    private Handler mainThreadHandler;
    private BuatPengaduanViewModel viewModel;
    private long ukuranFileAsliBytes = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_buat_pengaduan);

        executorService = Executors.newSingleThreadExecutor();
        mainThreadHandler = new Handler(Looper.getMainLooper());

        PengaduanRepository pengaduanRepository = new PengaduanRepositoryImpl(getApplicationContext());
        KirimPengaduanUseCase kirimPengaduanUseCase = new KirimPengaduanUseCase(pengaduanRepository);
        BuatPengaduanViewModelFactory factory = new BuatPengaduanViewModelFactory(kirimPengaduanUseCase);
        viewModel = new ViewModelProvider(this, factory).get(BuatPengaduanViewModel.class);

        ImageButton buttonKembali = findViewById(R.id.buttonKembali);
        imageViewFotoPengaduan = findViewById(R.id.imageViewFotoPengaduan);
        editTextLokasiOtomatis = findViewById(R.id.editTextLokasiOtomatis);
        ImageButton buttonRefreshLokasi = findViewById(R.id.buttonRefreshLokasi);
        editTextDetailLokasiManual = findViewById(R.id.editTextDetailLokasiManual);
        spinnerKategori = findViewById(R.id.spinnerKategori);
        editTextDeskripsi = findViewById(R.id.editTextDeskripsi);
        checkboxPernyataan = findViewById(R.id.checkboxPernyataan);
        buttonKirimPengaduan = findViewById(R.id.buttonKirimPengaduan);
        progressBarBuatPengaduan = findViewById(R.id.progressBarBuatPengaduan);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        setupSpinnerKategori();
        handleIncomingPhotoAndLocation();

        buttonKembali.setOnClickListener(v -> finish());
        if (buttonRefreshLokasi != null) {
            buttonRefreshLokasi.setOnClickListener(v -> {
                Toast.makeText(BuatPengaduanActivity.this, getString(R.string.memperbarui_lokasi), Toast.LENGTH_SHORT).show();
                editTextLokasiOtomatis.setText(getString(R.string.hint_lokasi_otomatis_mencari));
                requestLocationPermissionAndGetLocation();
            });
        }
        buttonKirimPengaduan.setOnClickListener(v -> attemptKirimPengaduan());
        observeViewModel();
    }

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
                setResult(RESULT_OK);
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


    private void handleIncomingPhotoAndLocation() {
        if (getIntent() != null) {
            ukuranFileAsliBytes = getIntent().getLongExtra("ORIGINAL_FILE_SIZE", 0);
            Log.d(TAG, "Ukuran File Asli diterima: " + ukuranFileAsliBytes + " bytes");

            if (getIntent().hasExtra("PHOTO_URI")) {
                Uri photoUri = Uri.parse(getIntent().getStringExtra("PHOTO_URI"));
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

    private void attemptKirimPengaduan() {
        if (bitmapFotoPengaduan == null) {
            Toast.makeText(this, getString(R.string.foto_bukti_wajib), Toast.LENGTH_SHORT).show();
            return;
        }
        final String lokasiOtomatisValue = editTextLokasiOtomatis.getText().toString().trim();
        final String detailLokasiManualValue = editTextDetailLokasiManual.getText().toString().trim();
        final String kategoriValue;
        if (spinnerKategori.getSelectedItem() != null && spinnerKategori.getSelectedItemPosition() > 0) {
            kategoriValue = spinnerKategori.getSelectedItem().toString();
        } else {
            Toast.makeText(this, getString(R.string.kategori_harus_dipilih), Toast.LENGTH_SHORT).show();
            return;
        }
        final String deskripsiValue = editTextDeskripsi.getText().toString().trim();
        if (deskripsiValue.isEmpty()) {
            editTextDeskripsi.setError(getString(R.string.error_deskripsi_kosong));
            editTextDeskripsi.requestFocus();
            return;
        }
        if (!checkboxPernyataan.isChecked()) {
            Toast.makeText(this, getString(R.string.error_pernyataan_belum_disetujui), Toast.LENGTH_LONG).show();
            return;
        }

        final SharedPreferences sp = getSharedPreferences(LoginActivity.SHARED_PREFS, MODE_PRIVATE);
        final int idUser = sp.getInt(LoginActivity.KEY_ID_USER, -1);
        if (idUser == -1) {
            Toast.makeText(this, getString(R.string.error_sesi_tidak_valid), Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        progressBarBuatPengaduan.setVisibility(View.VISIBLE);
        buttonKirimPengaduan.setEnabled(false);

        prosesDanKirimPengaduan(lokasiOtomatisValue, detailLokasiManualValue, kategoriValue, deskripsiValue, idUser);
    }

    private void prosesDanKirimPengaduan(String lokasiOtomatis, String detailLokasi, String kategori, String deskripsi, int idUser) {
        final Bitmap bitmapUntukDiolah = bitmapFotoPengaduan;
        final double currentLatitude = latitude;
        final double currentLongitude = longitude;

        executorService.execute(() -> {
            try {
                long startTime = System.currentTimeMillis();
                final int bitmapWidth = bitmapUntukDiolah.getWidth();
                final int bitmapHeight = bitmapUntukDiolah.getHeight();
                final int ukuranAsli = (int) ukuranFileAsliBytes;
                final int ukuranBitmapSetelahResize = bitmapUntukDiolah.getByteCount();

                Log.d(TAG, "Background: Ukuran File Asli: " + ukuranAsli + " bytes");
                Log.d(TAG, "Background: Ukuran Bitmap Setelah Crop: " + ukuranBitmapSetelahResize + " bytes");

                // Buat ID unik untuk folder debug berdasarkan waktu saat ini
                String reportIdForFolder = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());

                // --- LOGIKA PENYIMPANAN & PROSES GAMBAR ---

                // 1. Simpan file thumbnail (cropped) untuk di-upload (Format JPG)
                File fileThumbnail = simpanBitmapKeFileSementara(bitmapUntukDiolah);
                if (fileThumbnail == null) throw new IOException("Gagal menyimpan file thumbnail.");

                // 2. Simpan file thumbnail (cropped) untuk DEBUG (Format PNG)
                saveBitmapToStorage(bitmapUntukDiolah, "cropped", reportIdForFolder);

                // 3. Konversi ke Grayscale
                int[] grayscalePixels = RLECompressor.convertBitmapToGrayscale(bitmapUntukDiolah);
                if (grayscalePixels.length == 0) throw new IOException("Gagal konversi ke grayscale.");

                // 4. Buat & Simpan Bitmap Grayscale (SEBELUM Kuantisasi) untuk DEBUG
                Bitmap bitmapGrayscale = Bitmap.createBitmap(bitmapWidth, bitmapHeight, Bitmap.Config.ARGB_8888);
                int[] colorPixelsGrayscale = new int[grayscalePixels.length];
                for (int i = 0; i < grayscalePixels.length; i++) {
                    int gray = grayscalePixels[i];
                    colorPixelsGrayscale[i] = Color.rgb(gray, gray, gray);
                }
                bitmapGrayscale.setPixels(colorPixelsGrayscale, 0, bitmapWidth, 0, 0, bitmapWidth, bitmapHeight);
                saveBitmapToStorage(bitmapGrayscale, "grayscale", reportIdForFolder);

                // 5. Lanjutkan ke Kuantisasi
                int jumlahLevelQuantisasi = 2;
                int[] quantizedPixels = RLECompressor.quantizeGrayscale(grayscalePixels, jumlahLevelQuantisasi);
                if (quantizedPixels.length == 0) throw new IOException("Gagal melakukan kuantisasi.");

                // 6. Buat & Simpan Bitmap Hasil Kuantisasi untuk DEBUG
                Bitmap bmQuantized = Bitmap.createBitmap(bitmapWidth, bitmapHeight, Bitmap.Config.ARGB_8888);
                int[] colorPixelsQuantized = new int[quantizedPixels.length];
                for (int i = 0; i < quantizedPixels.length; i++) {
                    int gray = quantizedPixels[i];
                    colorPixelsQuantized[i] = Color.rgb(gray, gray, gray);
                }
                bmQuantized.setPixels(colorPixelsQuantized, 0, bmQuantized.getWidth(), 0, 0, bmQuantized.getWidth(), bmQuantized.getHeight());
                saveBitmapToStorage(bmQuantized, "foto_setelah_quantisasi_" + jumlahLevelQuantisasi + "level", reportIdForFolder);

                // --- PERUBAHAN UTAMA DIMULAI DI SINI ---
                final int ukuranByteDataGrayscale = grayscalePixels.length;
                final List<MyPair<Integer, Integer>> compressedDataRLE = RLECompressor.compressRLE(quantizedPixels);

                // A. Buat dan simpan file BINER (.rle) yang EFISIEN untuk DIKIRIM KE SERVER
                final byte[] dataRLEBytes = RLECompressor.convertRleListToByteArray(compressedDataRLE);
                final File fileRleData = simpanByteKeFileSementara(dataRLEBytes, "rle_data_" + System.currentTimeMillis() + ".rle");
                if (fileRleData == null) throw new IOException("Gagal menyimpan RLE biner ke file.");

                // B. Buat dan simpan file TEKS (.txt) yang BISA ANDA BACA, HANYA UNTUK DEBUGGING
                final String dataRLEString = RLECompressor.convertRleListToEfficientString(compressedDataRLE);
                saveStringToStorage(dataRLEString, "rle_data_readable.txt", reportIdForFolder);
                // --- PERUBAHAN UTAMA SELESAI ---

                // --- INILAH OPTIMISASI UTAMANYA ---

                // 1. Buat representasi string RLE DI DALAM MEMORI (RAM)
                final String dataRLEStringEfisien = RLECompressor.convertRleListToEfficientString(compressedDataRLE);

                // 2. Hitung ukurannya DI DALAM MEMORI (Sangat Cepat)
                final int ukuranFileRLEByte = dataRLEStringEfisien.getBytes().length;


                final File fileRleUntukDikirim;
                final boolean isRleCompressed;

                // 3. Bandingkan ukuran di memori (RLE string vs Grayscale bitmap)
                if (ukuranFileRLEByte < ukuranByteDataGrayscale) {
                    // KASUS 1: RLE Efektif!
                    Log.d(TAG, "Kompresi RLE Efektif (vs Grayscale). Menyimpan file RLE untuk dikirim.");
                    // HANYA JIKA EFEKTIF, kita tulis filenya ke penyimpanan (satu kali I/O)
                    fileRleUntukDikirim = simpanStringKeFileSementara(dataRLEStringEfisien);
                    if (fileRleUntukDikirim == null) throw new IOException("Gagal menyimpan RLE yang efisien ke file.");
                    isRleCompressed = true;
                } else {
                    // KASUS 2: Negative Compression.
                    Log.w(TAG, "Negative Compression terdeteksi (vs Grayscale). Tidak ada file RLE yang dibuat.");
                    fileRleUntukDikirim = null;
                    isRleCompressed = false;
                    // Tidak ada file yang perlu dihapus karena memang tidak pernah dibuat!
                }
                // --- AKHIR DARI OPTIMISASI ---

                final double rasioHematRuang = (ukuranAsli > 0) ? (1.0 - ((double) ukuranFileRLEByte / (double) ukuranAsli)) * 100.0 : 0.0;
                final double rasioKompresiStandar = (ukuranAsli > 0) ? ((double) ukuranFileRLEByte / (double) ukuranAsli) * 100.0 : 0.0;
                long endTime = System.currentTimeMillis();
                long durationMs = endTime - startTime;

                mainThreadHandler.post(() -> {
                    String lokasiFinal = lokasiOtomatis;
                    if (!detailLokasi.isEmpty()) {
                        lokasiFinal = lokasiOtomatis.isEmpty() ? detailLokasi : lokasiOtomatis + " (Patokan : " + detailLokasi + ")";
                    }
                    PengaduanData pengaduanData = new PengaduanData(
                            idUser, lokasiFinal, kategori, deskripsi, true,
                            currentLatitude, currentLongitude,
                            bitmapWidth, bitmapHeight, ukuranAsli, ukuranBitmapSetelahResize,
                            ukuranByteDataGrayscale, ukuranFileRLEByte, rasioHematRuang,
                            rasioKompresiStandar, durationMs, isRleCompressed);
                    viewModel.prosesKirimPengaduan(pengaduanData, fileRleUntukDikirim, fileThumbnail);
                });

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

    private void saveBitmapToStorage(Bitmap bitmap, String fileNamePrefix, String folderId) {
        if (bitmap == null) {
            Log.e(TAG, "Bitmap null, tidak bisa disimpan (debug): " + fileNamePrefix);
            return;
        }
        File directory = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        if (directory == null) {
            Log.e(TAG, "External pictures directory not available for app (debug).");
            return;
        }

        // Buat sub-folder RLE_Debug jika belum ada
        File rleDebugDir = new File(directory, "RLE_Debug");
        if (!rleDebugDir.exists()) {
            if (!rleDebugDir.mkdirs()) {
                Log.e(TAG, "Gagal membuat direktori RLE_Debug.");
                return; // Hentikan jika gagal membuat folder
            }
        }

        // Buat folder unik untuk setiap laporan di dalam RLE_Debug
        File reportDir = new File(rleDebugDir, folderId);
        if (!reportDir.exists()) {
            if (!reportDir.mkdirs()) {
                Log.e(TAG, "Gagal membuat direktori untuk laporan: " + folderId);
                return; // Hentikan jika gagal membuat folder
            }
        }

        String fileName = fileNamePrefix + ".png"; // Nama file sekarang lebih simpel
        File file = new File(reportDir, fileName); // Simpan di dalam folder laporan

        try (FileOutputStream out = new FileOutputStream(file)) {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
            Log.d(TAG, "DEBUG Bitmap berhasil disimpan ke: " + file.getAbsolutePath());
        } catch (IOException e) {
            Log.e(TAG, "DEBUG Error saat menyimpan bitmap " + fileName, e);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
    }

    private void setDefaultImageAndMessage(String message) {
        if (imageViewFotoPengaduan != null) {
            imageViewFotoPengaduan.setImageResource(R.drawable.ic_image_placeholder);
        }
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    private void requestLocationPermissionAndGetLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            getCurrentLocation();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION_PERMISSION);
        }
    }

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

    private void getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
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

    private void getAddressFromLatLng(double lat, double lon) {
        if (!Geocoder.isPresent()) {
            editTextLokasiOtomatis.setText(String.format(Locale.US, "Lat: %.5f, Lon: %.5f", lat, lon));
            return;
        }
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(lat, lon, 1);
            if (addresses != null && !addresses.isEmpty()) {
                String alamatLengkap = formatAddress(addresses);
                editTextLokasiOtomatis.setText(alamatLengkap);
            } else {
                editTextLokasiOtomatis.setText(String.format(Locale.US, "Lat: %.5f, Lon: %.5f", lat, lon));
            }
        } catch (IOException e) {
            Log.e(TAG, "Layanan Geocoder error: " + e.getMessage());
        }
    }

    @NonNull
    private static String formatAddress(List<Address> addresses) {
        Address address = addresses.get(0);
        return address.getAddressLine(0) != null ? address.getAddressLine(0) : "";
    }

    private void setupSpinnerKategori() {
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.kategori_pengaduan_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerKategori.setAdapter(adapter);
    }

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

    public static Bitmap rotateBitmap(Bitmap source, float angle) {
        if (source == null || angle == 0) return source;
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
    }

    private File simpanStringKeFileSementara(String kontenString) {
        File fileOutput = new File(getCacheDir(), "rle_data_" + System.currentTimeMillis() + ".txt");
        try (FileOutputStream fos = new FileOutputStream(fileOutput);
             OutputStreamWriter osw = new OutputStreamWriter(fos)) {
            osw.write(kontenString);
            return fileOutput;
        } catch (IOException e) {
            return null;
        }
    }

    private File simpanBitmapKeFileSementara(Bitmap bitmap) {
        File file = new File(getCacheDir(), "thumbnail_" + System.currentTimeMillis() + ".jpg");
        try (FileOutputStream out = new FileOutputStream(file)) {
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
            return file;
        } catch (IOException e) {
            return null;
        }
    }

    private File simpanByteKeFileSementara(byte[] data, String fileName) {
        File fileOutput = new File(getCacheDir(), fileName);
        try (FileOutputStream fos = new FileOutputStream(fileOutput)) {
            fos.write(data);
            return fileOutput;
        } catch (IOException e) {
            Log.e(TAG, "Gagal menyimpan byte array ke file sementara", e);
            return null;
        }
    }

    private void saveStringToStorage(String content, String fileName, String folderId) {
        File directory = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        if (directory == null) return;
        File rleDebugDir = new File(directory, "RLE_Debug");
        if (!rleDebugDir.exists()) rleDebugDir.mkdirs();
        File reportDir = new File(rleDebugDir, folderId);
        if (!reportDir.exists()) reportDir.mkdirs();
        File file = new File(reportDir, fileName);
        try (FileOutputStream fos = new FileOutputStream(file);
             OutputStreamWriter osw = new OutputStreamWriter(fos)) {
            osw.write(content);
            Log.d(TAG, "DEBUG String berhasil disimpan ke: " + file.getAbsolutePath());
        } catch (IOException e) {
            Log.e(TAG, "DEBUG Error saat menyimpan string", e);
        }
    }
}