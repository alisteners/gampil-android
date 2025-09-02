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
import android.util.TypedValue;
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
import androidx.core.widget.NestedScrollView;
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
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
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

public class DetailPengaduanOperatorActivity extends AppCompatActivity {

    public static final String EXTRA_ID_PENGADUAN = "extra_id_pengaduan";
    private static final String TAG = "DetailOperatorActivity";

    private DetailOperatorViewModel viewModel;
    private int idPengaduan;
    private String statusTerpilih;
    private DetailPengaduan detailPengaduan;

    private final List<Uri> listUriFotoBaru = new ArrayList<>();
    private ActivityResultLauncher<Intent> imagePickerLauncher;

    // Views
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

    private void inisialisasiView() {
        imageViewDetailFotoOperator = findViewById(R.id.imageViewDetailFotoOperator);
        collapsingToolbar = findViewById(R.id.collapsingToolbarDetailOperator);
        AppBarLayout appBarLayout = findViewById(R.id.appBarLayoutDetailOperator);
        NestedScrollView nestedScrollView = findViewById(R.id.nestedScrollViewOperator);
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

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbarDetailOperator);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(null);
        }
    }

    private void setupViewModel() {
        PengaduanRepository pengaduanRepo = new PengaduanRepositoryImpl(this);
        OperatorRepository operatorRepo = new OperatorRepositoryImpl(this);
        GetDetailPengaduanUseCase getDetailUseCase = new GetDetailPengaduanUseCase(pengaduanRepo);
        UpdatePengaduanUseCase updateUseCase = new UpdatePengaduanUseCase(operatorRepo);
        HapusPengaduanUseCase hapusUseCase = new HapusPengaduanUseCase(operatorRepo);
        DetailOperatorViewModelFactory factory = new DetailOperatorViewModelFactory(getDetailUseCase, updateUseCase, hapusUseCase);
        viewModel = new ViewModelProvider(this, factory).get(DetailOperatorViewModel.class);
    }

    private void setupRecyclerView() {
        recyclerViewLogStatus.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewLogStatus.setNestedScrollingEnabled(false);
        logStatusAdapter = new LogStatusAdapter(new ArrayList<>(), this);
        recyclerViewLogStatus.setAdapter(logStatusAdapter);
    }

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

// Tunda finish() selama sepersekian detik untuk memastikan siaran terkirim
                new Handler(Looper.getMainLooper()).postDelayed(this::finish, 200); // 200 milidetik
            }
        });

        viewModel.errorMessage.observe(this, error -> {
            if (error != null && !error.isEmpty()) {
                buttonSimpanPerubahan.setEnabled(true); // Aktifkan lagi tombolnya jika error
                Toast.makeText(this, "Error: " + error, Toast.LENGTH_LONG).show();
            }
        });
    }

    @SuppressLint("SetTextI18n")
    private void populateUi(DetailPengaduan detail) {
        collapsingToolbar.setTitle(detail.kategori);
        textViewDetailKategoriOperator.setText(detail.kategori);
        textViewDetailTanggalOperator.setText(formatTanggal(detail.tanggalPengaduan));
        textViewDetailNamaPelapor.setText("Dilaporkan oleh " + detail.getNamaPelapor());
        textViewDetailLokasiOperator.setText(detail.lokasi);
        textViewDetailDeskripsiOperator.setText(detail.deskripsi);
        setupChipStatus(detail.status);

        // Mengisi RecyclerView Timeline
        if (detail.getRiwayatTindakLanjut() != null && !detail.getRiwayatTindakLanjut().isEmpty()) {
            List<LogStatusItem> riwayat = new ArrayList<>(detail.getRiwayatTindakLanjut());
            Collections.reverse(riwayat);
            logStatusAdapter.updateData(riwayat);
            recyclerViewLogStatus.setVisibility(View.VISIBLE);
        } else {
            logStatusAdapter.updateData(new ArrayList<>());
            recyclerViewLogStatus.setVisibility(View.GONE);
        }

        // Sembunyikan Form Aksi Jika Sudah Selesai
        if ("selesai".equalsIgnoreCase(detail.status)) {
            cardAksiBaru.setVisibility(View.GONE);
        } else {
            cardAksiBaru.setVisibility(View.VISIBLE);
        }

        // Reset form aksi baru
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

        // Tampilkan tombol Cetak Laporan hanya jika status sudah 'selesai'
        if ("selesai".equalsIgnoreCase(detail.status)) {
            buttonCetakLaporan.setVisibility(View.VISIBLE);
            buttonCetakLaporan.setOnClickListener(v -> downloadLaporan(false));
            buttonCetakLaporan.setOnLongClickListener(v -> {
                downloadLaporan(true);
                return true;
            });
        } else {
            buttonCetakLaporan.setVisibility(View.GONE);
        }
    }

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

    private int dpToPx(int dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, getResources().getDisplayMetrics());
    }

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

    private void setupChipStatus(String status) {
        if (status == null) {
            chipDetailStatusOperator.setVisibility(View.GONE);
            return;
        }
        chipDetailStatusOperator.setVisibility(View.VISIBLE);
        chipDetailStatusOperator.setText(StringUtils.capitalizeFirstLetter(status));
        int colorRes;
        int iconRes = switch (status.toLowerCase()) {
            case "diproses" -> {
                colorRes = R.color.status_diproses_bg;
                yield R.drawable.ic_status_diproses;
            }
            case "selesai" -> {
                colorRes = R.color.status_selesai_bg;
                yield R.drawable.ic_status_selesai;
            }
            default -> {
                colorRes = R.color.status_menunggu_bg;
                yield R.drawable.ic_status_menunggu;
            }
        };
        chipDetailStatusOperator.setChipBackgroundColorResource(colorRes);
        if (iconRes != 0) {
            chipDetailStatusOperator.setChipIconResource(iconRes);
            chipDetailStatusOperator.setChipIconVisible(true);
        } else {
            chipDetailStatusOperator.setChipIconVisible(false);
        }
    }

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

    private void tampilkanDialogHapus() {
        // Buat custom view untuk dialog
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_hapus_dengan_alasan, null);
        final TextInputEditText inputAlasan = dialogView.findViewById(R.id.inputAlasanHapus);

        MaterialAlertDialogBuilder dialogBuilder = new MaterialAlertDialogBuilder(this)
                .setTitle("Hapus Pengaduan")
                .setView(dialogView)
                .setNegativeButton("Batal", null)
                .setPositiveButton("Hapus", (dialog, which) -> {
                    String alasan = Objects.requireNonNull(inputAlasan.getText()).toString().trim();
                    viewModel.hapusPengaduan(idPengaduan, alasan);
                });

        AlertDialog dialog = dialogBuilder.create();
        dialog.show();

        // Tombol "Hapus" awalnya dinonaktifkan
        Button hapusButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
        hapusButton.setEnabled(false);

        // Tambahkan TextWatcher untuk mengaktifkan tombol jika alasan sudah diisi
        inputAlasan.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                hapusButton.setEnabled(!s.toString().trim().isEmpty());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    // === Bagian CETAK LAPORAN ===
    private void downloadLaporan(boolean force) {
        SharedPreferences sp = getSharedPreferences(LoginActivity.SHARED_PREFS, MODE_PRIVATE);
        int idOperator = sp.getInt(LoginActivity.KEY_ID_USER, -1);
        if (idOperator == -1) {
            Toast.makeText(this, "Sesi operator tidak valid.", Toast.LENGTH_SHORT).show();
            return;
        }

        String url = ApiConfig.URL_CETAK_LAPORAN
                + "?idPengaduan=" + idPengaduan
                + "&idOperator=" + idOperator
                + (force ? "&force=1" : "");

        String fallbackName = "Laporan-Pengaduan-" + idPengaduan + ".pdf";

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            Toast.makeText(this, "Mengunduh ke Unduhan/Pengaduan…", Toast.LENGTH_SHORT).show();
            buttonCetakLaporan.setEnabled(false);
            progressBar.setVisibility(View.VISIBLE);

            new Thread(() -> {
                OkHttpClient client = new OkHttpClient.Builder().build();
                Request req = new Request.Builder().url(url).build();
                try (Response resp = client.newCall(req).execute()) {
                    if (!resp.isSuccessful()) {
                        runOnUiThread(() -> {
                            progressBar.setVisibility(View.GONE);
                            buttonCetakLaporan.setEnabled(true);
                            Toast.makeText(this, "Gagal mengunduh (" + resp.code() + ")", Toast.LENGTH_LONG).show();
                        });
                        return;
                    }
                    ResponseBody body = resp.body();
                    if (body == null) throw new IOException("Body kosong");

                    String cd = resp.header("Content-Disposition");
                    String fileName = parseFileNameFromContentDisposition(cd, fallbackName);

                    Uri saved = savePdfToPublicDownloads29(fileName, body.byteStream());
                    runOnUiThread(() -> {
                        progressBar.setVisibility(View.GONE);
                        buttonCetakLaporan.setEnabled(true);
                        Toast.makeText(this, "Tersimpan di Unduhan/Pengaduan", Toast.LENGTH_LONG).show();
                        openPdf(saved);
                    });
                } catch (Exception e) {
                    runOnUiThread(() -> {
                        progressBar.setVisibility(View.GONE);
                        buttonCetakLaporan.setEnabled(true);
                        Toast.makeText(this, "Kesalahan jaringan/IO: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    });
                }
            }).start();

        } else {
            try {
                File sub = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "Pengaduan");
                sub.mkdirs();

                DownloadManager.Request req = new DownloadManager.Request(Uri.parse(url));
                req.setTitle("Laporan Pengaduan #" + idPengaduan);
                req.setDescription("Mengunduh PDF ke folder Unduhan");
                req.setMimeType("application/pdf");
                req.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                req.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "Pengaduan/" + fallbackName);

                DownloadManager dm = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
                dm.enqueue(req);
                Toast.makeText(this, "Mengunduh ke Unduhan/Pengaduan…", Toast.LENGTH_SHORT).show();

            } catch (Exception e) {
                try { startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url))); }
                catch (ActivityNotFoundException ex) { Toast.makeText(this, "Gagal memulai unduhan.", Toast.LENGTH_LONG).show(); }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private Uri savePdfToPublicDownloads29(String fileName, InputStream in) throws IOException {
        Uri collection = MediaStore.Downloads.EXTERNAL_CONTENT_URI;
        ContentValues cv = new ContentValues();
        cv.put(MediaStore.MediaColumns.DISPLAY_NAME, fileName);
        cv.put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf");
        cv.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS + "/Pengaduan");
        cv.put(MediaStore.MediaColumns.IS_PENDING, 1);

        Uri uri = getContentResolver().insert(collection, cv);
        if (uri == null) throw new IOException("Gagal insert MediaStore");

        try (OutputStream os = getContentResolver().openOutputStream(uri);
             BufferedInputStream bis = new BufferedInputStream(in);
             BufferedOutputStream bos = new BufferedOutputStream(os)) {

            byte[] buf = new byte[8192];
            int r;
            while ((r = bis.read(buf)) != -1) {
                bos.write(buf, 0, r);
            }
            bos.flush();
        } finally {
            ContentValues done = new ContentValues();
            done.put(MediaStore.MediaColumns.IS_PENDING, 0);
            getContentResolver().update(uri, done, null, null);
        }
        return uri;
    }

    private String parseFileNameFromContentDisposition(String cd, String fallback) {
        if (cd == null) return fallback;
        try {
            for (String part : cd.split(";")) {
                part = part.trim();
                if (part.toLowerCase().startsWith("filename=")) {
                    String v = part.substring(9).trim();
                    if (v.startsWith("\"") && v.endsWith("\"")) v = v.substring(1, v.length() - 1);
                    if (!v.isEmpty()) return v;
                }
            }
        } catch (Exception ignored) {}
        return fallback;
    }

    private void openPdf(Uri uri) {
        Intent view = new Intent(Intent.ACTION_VIEW);
        view.setDataAndType(uri, "application/pdf");
        view.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        try {
            startActivity(view);
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
