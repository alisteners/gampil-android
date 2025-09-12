package com.alisteners.pengaduan.presentation.operator.detail;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DownloadManager;
import android.content.ActivityNotFoundException;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.alisteners.pengaduan.R;
import com.alisteners.pengaduan.data.remote.config.ApiConfig;
import com.alisteners.pengaduan.data.repository.OperatorRepositoryImpl;
import com.alisteners.pengaduan.data.repository.PengaduanRepositoryImpl;
import com.alisteners.pengaduan.domain.entity.DetailPengaduan;
import com.alisteners.pengaduan.domain.entity.LogStatusItem;
import com.alisteners.pengaduan.domain.operator.repository.OperatorRepository;
import com.alisteners.pengaduan.domain.operator.usecase.HapusPengaduanUseCase;
import com.alisteners.pengaduan.domain.operator.usecase.UpdatePengaduanUseCase;
import com.alisteners.pengaduan.domain.repository.PengaduanRepository;
import com.alisteners.pengaduan.domain.usecase.GetDetailPengaduanUseCase;
import com.alisteners.pengaduan.presentation.fullscreenimage.FullScreenImageActivity;
import com.alisteners.pengaduan.presentation.login.LoginActivity;
import com.alisteners.pengaduan.util.StringUtils;
import com.bumptech.glide.Glide;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * Activity yang menampilkan detail lengkap dari sebuah pengaduan untuk Operator.
 * Activity ini menyediakan fungsionalitas untuk:
 * - Melihat semua detail laporan.
 * - Memperbarui status laporan (Tindak Lanjut).
 * - Mengunggah foto bukti tindak lanjut.
 * - Menghapus laporan dengan alasan.
 * - Mencetak/mengunduh laporan dalam format PDF.
 */
public class DetailPengaduanOperatorActivity extends AppCompatActivity {

    public static final String EXTRA_ID_PENGADUAN = "extra_id_pengaduan";
    private static final String TAG = "DetailOperatorActivity";

    private DetailOperatorViewModel viewModel;
    private int idPengaduan;
    private String statusTerpilih;
    private DetailPengaduan detailPengaduan;

    private final List<Uri> listUriFotoBaru = new ArrayList<>();
    private ActivityResultLauncher<Intent> imagePickerLauncher;

    // --- Deklarasi Komponen UI ---
    private ProgressBar progressBar;
    private ImageView imageViewDetailFotoOperator;
    private CollapsingToolbarLayout collapsingToolbar;
    private TextView textViewDetailKategoriOperator, textViewDetailTanggalOperator, textViewDetailNamaPelapor, textViewDetailLokasiOperator, textViewDetailDeskripsiOperator;
    private Chip chipDetailStatusOperator;
    private RecyclerView recyclerViewLogStatus;
    private LogStatusAdapter logStatusAdapter;
    private MaterialCardView cardAksiBaru;
    private ChipGroup chipGroupStatus;
    private LinearLayout containerFormAksiBaru;
    private TextInputEditText editTextTindakLanjut;
    private Button buttonPilihFotoTindakLanjut;
    private HorizontalScrollView horizontalScrollViewPreview;
    private LinearLayout containerPreviewFoto;
    private Button buttonSimpanPerubahan;
    private Button buttonHapusPengaduan;
    private Button buttonCetakLaporan;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_pengaduan_operator);
        inisialisasiView();
        setupToolbar();
        setupViewModel();
        setupRecyclerView();
        setupImagePickerLauncher();
        setupListeners();
        observeViewModel();

        idPengaduan = getIntent().getIntExtra(EXTRA_ID_PENGADUAN, -1);
        if (idPengaduan != -1) {
            viewModel.loadDetail(idPengaduan);
        } else {
            Toast.makeText(this, "ID Pengaduan tidak valid.", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    /**
     * Menginisialisasi semua referensi ke komponen view dari layout XML.
     */
    private void inisialisasiView() {
        imageViewDetailFotoOperator = findViewById(R.id.imageViewDetailFotoOperator);
        collapsingToolbar = findViewById(R.id.collapsingToolbarDetailOperator);
        textViewDetailKategoriOperator = findViewById(R.id.textViewDetailKategoriOperator);
        textViewDetailTanggalOperator = findViewById(R.id.textViewDetailTanggalOperator);
        textViewDetailNamaPelapor = findViewById(R.id.textViewDetailNamaPelapor);
        chipDetailStatusOperator = findViewById(R.id.chipDetailStatusOperator);
        textViewDetailLokasiOperator = findViewById(R.id.textViewDetailLokasiOperator);
        textViewDetailDeskripsiOperator = findViewById(R.id.textViewDetailDeskripsiOperator);
        progressBar = findViewById(R.id.progressBarDetailOperator);
        recyclerViewLogStatus = findViewById(R.id.recyclerViewLogStatus);
        cardAksiBaru = findViewById(R.id.cardAksiBaru);
        chipGroupStatus = findViewById(R.id.chipGroupStatus);
        containerFormAksiBaru = findViewById(R.id.containerFormAksiBaru);
        editTextTindakLanjut = findViewById(R.id.editTextTindakLanjut);
        buttonPilihFotoTindakLanjut = findViewById(R.id.buttonPilihFotoTindakLanjut);
        buttonSimpanPerubahan = findViewById(R.id.buttonSimpanPerubahan);
        buttonHapusPengaduan = findViewById(R.id.buttonHapusPengaduan);
        buttonCetakLaporan = findViewById(R.id.buttonCetakLaporan);
        buttonCetakLaporan.setVisibility(View.GONE);
        horizontalScrollViewPreview = findViewById(R.id.horizontalScrollViewPreview);
        containerPreviewFoto = findViewById(R.id.containerPreviewFoto);
    }

    /**
     * Mengatur Toolbar, termasuk tombol kembali (up button).
     */
    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbarDetailOperator);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(null);
        }
    }

    /**
     * Menginisialisasi ViewModel menggunakan ViewModelFactory untuk menyuntikkan semua dependensi UseCase.
     */
    private void setupViewModel() {
        PengaduanRepository pengaduanRepo = new PengaduanRepositoryImpl(this);
        OperatorRepository operatorRepo = new OperatorRepositoryImpl(this);
        GetDetailPengaduanUseCase getDetailUseCase = new GetDetailPengaduanUseCase(pengaduanRepo);
        UpdatePengaduanUseCase updateUseCase = new UpdatePengaduanUseCase(operatorRepo);
        HapusPengaduanUseCase hapusUseCase = new HapusPengaduanUseCase(operatorRepo);
        DetailOperatorViewModelFactory factory = new DetailOperatorViewModelFactory(getDetailUseCase, updateUseCase, hapusUseCase);
        viewModel = new ViewModelProvider(this, factory).get(DetailOperatorViewModel.class);
    }

    /**
     * Mengatur RecyclerView untuk menampilkan timeline tindak lanjut.
     */
    private void setupRecyclerView() {
        recyclerViewLogStatus.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewLogStatus.setNestedScrollingEnabled(false);
        logStatusAdapter = new LogStatusAdapter(new ArrayList<>(), this);
        recyclerViewLogStatus.setAdapter(logStatusAdapter);
    }

    /**
     * Menginisialisasi ActivityResultLauncher untuk menangani hasil dari pemilihan gambar.
     */
    private void setupImagePickerLauncher() {
        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent data = result.getData();
                        if (data != null) {
                            listUriFotoBaru.clear();
                            if (data.getClipData() != null) {
                                for (int i = 0; i < data.getClipData().getItemCount(); i++) {
                                    listUriFotoBaru.add(data.getClipData().getItemAt(i).getUri());
                                }
                            } else if (data.getData() != null) {
                                listUriFotoBaru.add(data.getData());
                            }
                            tampilkanFotoPreviewBaru();
                        }
                    }
                });
    }

    /**
     * Mengatur semua listener untuk interaksi pengguna.
     */
    private void setupListeners() {
        buttonPilihFotoTindakLanjut.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
            imagePickerLauncher.launch(Intent.createChooser(intent, "Pilih beberapa foto"));
        });

        chipGroupStatus.setOnCheckedChangeListener((group, checkedId) -> {
            if (group.getCheckedChipId() == View.NO_ID) {
                statusTerpilih = null;
                containerFormAksiBaru.setVisibility(View.GONE);
            } else {
                statusTerpilih = (checkedId == R.id.chipProses) ? "diproses" : "selesai";
                containerFormAksiBaru.setVisibility(View.VISIBLE);
            }
        });

        buttonSimpanPerubahan.setOnClickListener(v -> simpanPerubahan());
        buttonHapusPengaduan.setOnClickListener(v -> tampilkanDialogHapus());
        textViewDetailLokasiOperator.setOnClickListener(v -> bukaLokasiDiPeta());
    }

    /**
     * Mengamati semua LiveData dari ViewModel untuk memperbarui UI secara reaktif.
     */
    private void observeViewModel() {
        viewModel.isLoading.observe(this, isLoading -> {
            if (isLoading != null) progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        });

        viewModel.detailPengaduan.observe(this, detail -> {
            if (detail != null) {
                this.detailPengaduan = detail;
                populateUi(detail);
            }
        });

        viewModel.isDeleting.observe(this, isDeleting -> {
            if (isDeleting != null)
                progressBar.setVisibility(isDeleting ? View.VISIBLE : View.GONE);
        });

        viewModel.deleteSuccessMessage.observe(this, message -> {
            if (message != null && !message.isEmpty()) {
                Toast.makeText(this, message, Toast.LENGTH_LONG).show();
                setResult(RESULT_OK);
                finish();
            }
        });

        viewModel.isUpdating.observe(this, isUpdating -> {
            progressBar.setVisibility(isUpdating ? View.VISIBLE : View.GONE);
            buttonSimpanPerubahan.setEnabled(!isUpdating);
        });

        viewModel.updateSuccessMessage.observe(this, message -> {
            if (message != null && !message.isEmpty()) {
                Toast.makeText(this, message, Toast.LENGTH_LONG).show();
                setResult(RESULT_OK);
                Intent intent = new Intent("com.alisteners.pengaduan.PENGADUAN_UPDATED");
                LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
                new Handler(Looper.getMainLooper()).postDelayed(this::finish, 200);
            }
        });

        viewModel.errorMessage.observe(this, error -> {
            if (error != null && !error.isEmpty()) {
                buttonSimpanPerubahan.setEnabled(true);
                Toast.makeText(this, "Error: " + error, Toast.LENGTH_LONG).show();
            }
        });
    }

    /**
     * Mengisi semua komponen UI dengan data dari objek {@link DetailPengaduan}.
     * @param detail Objek detail pengaduan yang diterima dari ViewModel.
     */
    @SuppressLint("SetTextI18n")
    private void populateUi(DetailPengaduan detail) {
        collapsingToolbar.setTitle(detail.kategori);
        textViewDetailKategoriOperator.setText(detail.kategori);
        textViewDetailTanggalOperator.setText(formatTanggal(detail.tanggalPengaduan));
        textViewDetailNamaPelapor.setText("Dilaporkan oleh " + detail.getNamaPelapor());
        textViewDetailLokasiOperator.setText(detail.lokasi);
        textViewDetailDeskripsiOperator.setText(detail.deskripsi);
        setupChipStatus(detail.status);

        if (detail.getRiwayatTindakLanjut() != null && !detail.getRiwayatTindakLanjut().isEmpty()) {
            List<LogStatusItem> riwayat = new ArrayList<>(detail.getRiwayatTindakLanjut());
            Collections.reverse(riwayat);
            logStatusAdapter.updateData(riwayat);
            recyclerViewLogStatus.setVisibility(View.VISIBLE);
        } else {
            recyclerViewLogStatus.setVisibility(View.GONE);
        }

        if ("selesai".equalsIgnoreCase(detail.status)) {
            cardAksiBaru.setVisibility(View.GONE);
            buttonCetakLaporan.setVisibility(View.VISIBLE);
            buttonCetakLaporan.setOnClickListener(v -> downloadLaporan(false));
            buttonCetakLaporan.setOnLongClickListener(v -> {
                downloadLaporan(true);
                return true;
            });
        } else {
            cardAksiBaru.setVisibility(View.VISIBLE);
            buttonCetakLaporan.setVisibility(View.GONE);
        }

        editTextTindakLanjut.setText("");
        editTextTindakLanjut.setError(null);
        chipGroupStatus.clearCheck();
        containerFormAksiBaru.setVisibility(View.GONE);
        listUriFotoBaru.clear();
        tampilkanFotoPreviewBaru();

        if (detail.pathImage != null && !detail.pathImage.isEmpty()) {
            String imageUrl = ApiConfig.BASE_URL + detail.pathImage;
            Glide.with(this).load(imageUrl).placeholder(R.drawable.ic_image_placeholder).error(R.drawable.ic_image_error).into(imageViewDetailFotoOperator);
            imageViewDetailFotoOperator.setOnClickListener(v -> {
                Intent intent = new Intent(this, FullScreenImageActivity.class);
                intent.putExtra(FullScreenImageActivity.EXTRA_IMAGE_URL, imageUrl);
                startActivity(intent);
            });
        }
    }

    /**
     * Memvalidasi dan memulai proses penyimpanan perubahan tindak lanjut.
     */
    private void simpanPerubahan() {
        if (!buttonSimpanPerubahan.isEnabled()) {
            return;
        }
        if (statusTerpilih == null || statusTerpilih.isEmpty()) {
            Toast.makeText(this, "Pilih status baru terlebih dahulu.", Toast.LENGTH_SHORT).show();
            return;
        }
        String catatan = Objects.requireNonNull(editTextTindakLanjut.getText()).toString().trim();
        if (statusTerpilih.equals("selesai") && catatan.isEmpty() && listUriFotoBaru.isEmpty()) {
            editTextTindakLanjut.setError("Catatan atau foto bukti wajib diisi untuk status 'Selesai'");
            editTextTindakLanjut.requestFocus();
            return;
        }
        buttonSimpanPerubahan.setEnabled(false);
        progressBar.setVisibility(View.VISIBLE);
        compressImagesAndUpload(catatan);
    }

    /**
     * Mengompres gambar yang dipilih oleh operator di background thread sebelum diunggah.
     * @param catatan Catatan yang akan dikirim bersamaan dengan file.
     */
    private void compressImagesAndUpload(String catatan) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());
        executor.execute(() -> {
            final List<File> compressedFiles = new ArrayList<>();
            try {
                for (Uri uri : listUriFotoBaru) {
                    InputStream inputStream = getContentResolver().openInputStream(uri);
                    Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                    File tempFile = new File(getCacheDir(), "upload_" + System.currentTimeMillis() + ".jpg");
                    FileOutputStream fos = new FileOutputStream(tempFile);
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 60, fos);
                    fos.flush();
                    fos.close();
                    if (inputStream != null) inputStream.close();
                    compressedFiles.add(tempFile);
                }
                handler.post(() -> kirimDataKeServer(compressedFiles, catatan));
            } catch (Exception e) {
                Log.e(TAG, "Gagal melakukan kompresi gambar", e);
                handler.post(() -> {
                    Toast.makeText(this, "Gagal memproses gambar.", Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.GONE);
                    buttonSimpanPerubahan.setEnabled(true);
                });
            }
        });
    }

    /**
     * Mengirim data yang sudah final (termasuk file terkompresi) ke ViewModel.
     */
    private void kirimDataKeServer(List<File> filesToUpload, String catatan) {
        String requestToken = UUID.randomUUID().toString();
        SharedPreferences sp = getSharedPreferences(LoginActivity.SHARED_PREFS, MODE_PRIVATE);
        int idOperator = sp.getInt(LoginActivity.KEY_ID_USER, -1);
        if (idOperator == -1) {
            Toast.makeText(this, "Sesi operator tidak valid.", Toast.LENGTH_SHORT).show();
            progressBar.setVisibility(View.GONE);
            buttonSimpanPerubahan.setEnabled(true);
            return;
        }
        viewModel.simpanPerubahan(idPengaduan, idOperator, statusTerpilih, catatan, filesToUpload, requestToken);
    }

    /**
     * Menampilkan pratinjau (preview) dari gambar-gambar yang dipilih untuk diunggah.
     */
    private void tampilkanFotoPreviewBaru() {
        containerPreviewFoto.removeAllViews();
        if (listUriFotoBaru.isEmpty()) {
            horizontalScrollViewPreview.setVisibility(View.GONE);
            return;
        }
        horizontalScrollViewPreview.setVisibility(View.VISIBLE);
        for (final Uri uri : listUriFotoBaru) {
            View itemView = getLayoutInflater().inflate(R.layout.item_image_preview, containerPreviewFoto, false);
            ImageView imageView = itemView.findViewById(R.id.imageViewPreview);
            ImageView buttonHapus = itemView.findViewById(R.id.buttonHapusPreview);
            Glide.with(this).load(uri).into(imageView);
            buttonHapus.setOnClickListener(v -> {
                listUriFotoBaru.remove(uri);
                tampilkanFotoPreviewBaru();
            });
            containerPreviewFoto.addView(itemView);
        }
    }

    /**
     * Membuka aplikasi peta (Google Maps) dengan koordinat lokasi pengaduan.
     */
    private void bukaLokasiDiPeta() {
        if (detailPengaduan != null && detailPengaduan.latitude != 0 && detailPengaduan.longitude != 0) {
            String label = "Lokasi: " + detailPengaduan.kategori;
            String uriString = String.format(Locale.US, "geo:%f,%f?q=%f,%f(%s)", detailPengaduan.latitude, detailPengaduan.longitude, detailPengaduan.latitude, detailPengaduan.longitude, Uri.encode(label));
            Uri gmmIntentUri = Uri.parse(uriString);
            Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
            mapIntent.setPackage("com.google.android.apps.maps");
            if (mapIntent.resolveActivity(getPackageManager()) != null) {
                startActivity(mapIntent);
            } else {
                Toast.makeText(this, "Aplikasi Google Maps tidak ditemukan.", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Data koordinat lokasi tidak tersedia.", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Mengatur tampilan Chip (teks, warna, ikon) berdasarkan status pengaduan.
     * @param status String status dari item pengaduan.
     */
    private void setupChipStatus(String status) {
        if (status == null) return;
        chipDetailStatusOperator.setText(StringUtils.capitalizeFirstLetter(status));
        int colorRes = switch (status.toLowerCase()) {
            case "diproses" -> R.color.status_diproses_bg;
            case "selesai" -> R.color.status_selesai_bg;
            default -> R.color.status_menunggu_bg;
        };
        chipDetailStatusOperator.setChipBackgroundColorResource(colorRes);
    }

    /**
     * Memformat string tanggal dari format "yyyy-MM-dd HH:mm:ss" menjadi "dd MMMM yy, HH:mm".
     * @param tanggalMySQL String tanggal dari server.
     * @return String tanggal yang sudah diformat.
     */
    private String formatTanggal(String tanggalMySQL) {
        if (tanggalMySQL == null || tanggalMySQL.isEmpty()) return "N/A";
        SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        SimpleDateFormat outputFormat = new SimpleDateFormat("dd MMMM yy, HH:mm", new Locale("id", "ID"));
        try {
            Date date = inputFormat.parse(tanggalMySQL);
            return date != null ? outputFormat.format(date) : "N/A";
        } catch (ParseException e) {
            return tanggalMySQL;
        }
    }

    /**
     * Menampilkan dialog konfirmasi untuk menghapus pengaduan.
     * Memerlukan input alasan penghapusan.
     */
    private void tampilkanDialogHapus() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_hapus_dengan_alasan, null);
        final TextInputEditText inputAlasan = dialogView.findViewById(R.id.inputAlasanHapus);
        AlertDialog dialog = new MaterialAlertDialogBuilder(this)
                .setTitle("Hapus Pengaduan")
                .setView(dialogView)
                .setNegativeButton("Batal", null)
                .setPositiveButton("Hapus", (d, which) -> {
                    String alasan = Objects.requireNonNull(inputAlasan.getText()).toString().trim();
                    viewModel.hapusPengaduan(idPengaduan, alasan);
                }).create();
        dialog.show();
        Button hapusButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
        hapusButton.setEnabled(false);
        inputAlasan.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                hapusButton.setEnabled(!s.toString().trim().isEmpty());
            }
            @Override public void afterTextChanged(Editable s) {}
        });
    }

    // === Bagian CETAK LAPORAN ===

    /**
     * Memulai proses pengunduhan laporan PDF.
     * @param force true jika ingin memaksa pembuatan ulang PDF di server.
     */
    private void downloadLaporan(boolean force) {
        SharedPreferences sp = getSharedPreferences(LoginActivity.SHARED_PREFS, MODE_PRIVATE);
        int idOperator = sp.getInt(LoginActivity.KEY_ID_USER, -1);
        if (idOperator == -1) {
            Toast.makeText(this, "Sesi operator tidak valid.", Toast.LENGTH_SHORT).show();
            return;
        }
        String url = ApiConfig.URL_CETAK_LAPORAN + "?idPengaduan=" + idPengaduan + "&idOperator=" + idOperator + (force ? "&force=1" : "");
        String fallbackName = "Laporan-Pengaduan-" + idPengaduan + ".pdf";

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // Panggil metode baru yang sudah dianotasi
            downloadLaporanForQ(url, fallbackName);
        } else {
            // Menggunakan DownloadManager untuk Android 9 ke bawah
            downloadWithDownloadManager(url, fallbackName);
        }
    }

    /**
     * [METODE BARU] Menangani logika unduh untuk Android 10 (Q) ke atas.
     * Dianotasi dengan @RequiresApi untuk mengatasi warning dari Linter.
     */
    @RequiresApi(Build.VERSION_CODES.Q)
    private void downloadLaporanForQ(String url, String fallbackName) {
        Toast.makeText(this, "Mengunduh ke Unduhan/Pengaduan…", Toast.LENGTH_SHORT).show();
        buttonCetakLaporan.setEnabled(false);
        progressBar.setVisibility(View.VISIBLE);
        new Thread(() -> {
            OkHttpClient client = new OkHttpClient();
            Request req = new Request.Builder().url(url).build();
            try (Response resp = client.newCall(req).execute()) {
                if (!resp.isSuccessful()) throw new IOException("Gagal mengunduh: " + resp.code());
                ResponseBody body = resp.body();
                if (body == null) throw new IOException("Body respons kosong");

                String fileName = parseFileNameFromContentDisposition(resp.header("Content-Disposition"), fallbackName);
                Uri savedUri = savePdfToPublicDownloads29(fileName, body.byteStream());

                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    buttonCetakLaporan.setEnabled(true);
                    Toast.makeText(this, "Tersimpan di Unduhan/Pengaduan", Toast.LENGTH_LONG).show();
                    openPdf(savedUri);
                });
            } catch (Exception e) {
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    buttonCetakLaporan.setEnabled(true);
                    Toast.makeText(this, "Kesalahan: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
            }
        }).start();
    }

    /**
     * Mengunduh file menggunakan DownloadManager bawaan Android.
     * Metode ini digunakan untuk Android 9 (API 28) ke bawah.
     */
    private void downloadWithDownloadManager(String url, String fallbackName) {
        try {
            File subDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "Pengaduan");
            if (!subDir.exists()) subDir.mkdirs();
            DownloadManager.Request req = new DownloadManager.Request(Uri.parse(url));
            req.setTitle("Laporan Pengaduan #" + idPengaduan);
            req.setDescription("Mengunduh PDF...");
            req.setMimeType("application/pdf");
            req.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
            req.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "Pengaduan/" + fallbackName);
            ((DownloadManager) getSystemService(DOWNLOAD_SERVICE)).enqueue(req);
            Toast.makeText(this, "Mengunduh ke Unduhan/Pengaduan…", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this, "Gagal memulai unduhan.", Toast.LENGTH_LONG).show();
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private Uri savePdfToPublicDownloads29(String fileName, InputStream in) throws IOException {
        ContentValues cv = new ContentValues();
        cv.put(MediaStore.MediaColumns.DISPLAY_NAME, fileName);
        cv.put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf");
        cv.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS + "/Pengaduan");
        cv.put(MediaStore.MediaColumns.IS_PENDING, 1);

        Uri uri = getContentResolver().insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, cv);
        if (uri == null) throw new IOException("Gagal insert MediaStore");

        try (OutputStream os = getContentResolver().openOutputStream(uri)) {
            if (os == null) throw new IOException("Gagal membuka output stream");
            byte[] buf = new byte[8192];
            int len;
            while ((len = in.read(buf)) > 0) {
                os.write(buf, 0, len);
            }
        } finally {
            ContentValues done = new ContentValues();
            done.put(MediaStore.MediaColumns.IS_PENDING, 0);
            getContentResolver().update(uri, done, null, null);
        }
        return uri;
    }

    private String parseFileNameFromContentDisposition(String cd, String fallback) {
        if (cd == null) return fallback;
        for (String part : cd.split(";")) {
            part = part.trim();
            if (part.toLowerCase().startsWith("filename=")) {
                String v = part.substring(9).trim().replace("\"", "");
                if (!v.isEmpty()) return v;
            }
        }
        return fallback;
    }

    private void openPdf(Uri uri) {
        Intent viewIntent = new Intent(Intent.ACTION_VIEW);
        viewIntent.setDataAndType(uri, "application/pdf");
        viewIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        try {
            startActivity(viewIntent);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, "PDF tersimpan. Buka dari folder Unduhan.", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}