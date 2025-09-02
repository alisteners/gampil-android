package com.alisteners.pengaduan.presentation.masyarakat.detailpengaduan;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.alisteners.pengaduan.R;
import com.alisteners.pengaduan.data.remote.config.ApiConfig;
import com.alisteners.pengaduan.data.repository.PengaduanRepositoryImpl;
import com.alisteners.pengaduan.domain.entity.DetailPengaduan;
import com.alisteners.pengaduan.domain.entity.LogStatusItem;
import com.alisteners.pengaduan.domain.repository.PengaduanRepository;
import com.alisteners.pengaduan.domain.usecase.GetDetailPengaduanUseCase;
import com.alisteners.pengaduan.presentation.fullscreenimage.FullScreenImageActivity;

// Import adapter yang sudah dibuat dari package operator
import com.alisteners.pengaduan.presentation.operator.detail.LogStatusAdapter;
import com.alisteners.pengaduan.util.StringUtils;
import com.bumptech.glide.Glide;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.chip.Chip;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DetailPengaduanActivity extends AppCompatActivity {

    public static final String EXTRA_PENGADUAN_ID = "extra_pengaduan_id";
    private DetailPengaduanViewModel viewModel;

    // --- Deklarasi View ---
    private ProgressBar progressBar;
    private ImageView imageViewDetailFoto;
    private CollapsingToolbarLayout collapsingToolbar;
    private TextView textViewDetailKategori, textViewDetailTanggal, textViewDetailLokasi, textViewDetailDeskripsi;
    private Chip chipDetailStatus;
    private TextView textViewDetailDimensi, textViewDetailUkuranAsli, textViewDetailUkuranResize, textViewDetailUkuranGrayscale, textViewDetailUkuranRle, textViewDetailRasioHemat, textViewDetailRasioStandar, textViewDetailWaktu;

    // --- Deklarasi untuk Timeline ---
    private RecyclerView recyclerViewLogStatus;
    private LogStatusAdapter logStatusAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_pengaduan);

        inisialisasiView();
        setupToolbar();
        setupViewModel();
        setupRecyclerView();
        observeViewModel();

        int idPengaduan = getIntent().getIntExtra(EXTRA_PENGADUAN_ID, -1);
        if (idPengaduan != -1) {
            viewModel.loadDetail(idPengaduan);
        } else {
            Toast.makeText(this, "ID Pengaduan tidak valid.", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void inisialisasiView() {
        progressBar = findViewById(R.id.progressBarDetail);
        imageViewDetailFoto = findViewById(R.id.imageViewDetailFoto);
        collapsingToolbar = findViewById(R.id.collapsingToolbarDetail);
        textViewDetailKategori = findViewById(R.id.textViewDetailKategori);
        textViewDetailTanggal = findViewById(R.id.textViewDetailTanggal);
        chipDetailStatus = findViewById(R.id.chipDetailStatus);
        textViewDetailLokasi = findViewById(R.id.textViewDetailLokasi);
        textViewDetailDeskripsi = findViewById(R.id.textViewDetailDeskripsi);

        // Inisialisasi TextView untuk data analisis
//        textViewDetailDimensi = findViewById(R.id.textViewDetailDimensi);
        textViewDetailUkuranAsli = findViewById(R.id.textViewDetailUkuranAsli);
//        textViewDetailUkuranResize = findViewById(R.id.textViewDetailUkuranResize);
//        textViewDetailUkuranGrayscale = findViewById(R.id.textViewDetailUkuranGrayscale);
        textViewDetailUkuranRle = findViewById(R.id.textViewDetailUkuranRle);
        textViewDetailRasioHemat = findViewById(R.id.textViewDetailRasioHematRuang);
        textViewDetailRasioStandar = findViewById(R.id.textViewDetailRasioStandar);
        textViewDetailWaktu = findViewById(R.id.textViewDetailWaktu);
        ProgressBar progressBarRasio = findViewById(R.id.progressBarRasio);

        // Inisialisasi RecyclerView untuk timeline
        recyclerViewLogStatus = findViewById(R.id.recyclerViewLogStatusMasyarakat);
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbarDetail);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("");
        }
    }

    private void setupViewModel() {
        PengaduanRepository pengaduanRepo = new PengaduanRepositoryImpl(getApplicationContext());
        GetDetailPengaduanUseCase getDetailUseCase = new GetDetailPengaduanUseCase(pengaduanRepo);
        DetailPengaduanViewModelFactory factory = new DetailPengaduanViewModelFactory(getDetailUseCase);
        viewModel = new ViewModelProvider(this, factory).get(DetailPengaduanViewModel.class);
    }

    private void setupRecyclerView() {
        recyclerViewLogStatus.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewLogStatus.setNestedScrollingEnabled(false);
        // Gunakan kembali adapter yang sama dari sisi operator
        logStatusAdapter = new LogStatusAdapter(new ArrayList<>(), this);
        recyclerViewLogStatus.setAdapter(logStatusAdapter);
    }

    private void observeViewModel() {
        viewModel.isLoading.observe(this, isLoading -> {
            if (isLoading != null) progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        });
        viewModel.errorMessage.observe(this, error -> {
            if (error != null && !error.isEmpty()) {
                Toast.makeText(this, "Error: " + error, Toast.LENGTH_LONG).show();
            }
        });
        viewModel.detailPengaduan.observe(this, this::populateUi);
    }

    @SuppressLint({"NotifyDataSetChanged", "SetTextI18n"})
    private void populateUi(DetailPengaduan detail) {
        if (detail == null) {
            Toast.makeText(this, "Gagal memuat detail data.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Mengisi data utama
        collapsingToolbar.setTitle(detail.kategori);
        TextView textViewJudulToolbar = findViewById(R.id.textViewJudulToolbar);
        textViewJudulToolbar.setText(detail.kategori);
        textViewDetailKategori.setText(detail.kategori);
        textViewDetailTanggal.setText(formatTanggal(detail.tanggalPengaduan));
        textViewDetailLokasi.setText(detail.lokasi);
        textViewDetailDeskripsi.setText(detail.deskripsi);
        setupChipStatus(detail.status);

        // Mengisi data analisis kompresi
        long jumlahPiksel = (long) detail.lebar * detail.tinggi;
        NumberFormat formatter = NumberFormat.getInstance(new Locale("id", "ID"));
//        textViewDetailDimensi.setText(String.format(Locale.US, "%dx%d (%s Piksel)",
//                detail.lebar,
//                detail.tinggi,
//                formatter.format(jumlahPiksel)));
        textViewDetailUkuranAsli.setText(formatUkuranBytes(detail.ukuranAsli));
//        textViewDetailUkuranResize.setText(formatUkuranBytes(detail.ukuranSetelahResize));
//        textViewDetailUkuranGrayscale.setText(formatUkuranBytes(detail.ukuranGrayscale));
        textViewDetailUkuranRle.setText(formatUkuranBytes(detail.ukuranFileRLEByte));
        textViewDetailRasioStandar.setText(String.format(Locale.US, "%.2f %%", detail.rasioKompresiStandar));
        textViewDetailRasioHemat.setText(String.format(Locale.US, "%.2f %%", detail.rasioKompresiRLE));
        ((ProgressBar) findViewById(R.id.progressBarRasio)).setProgress((int) Math.round(detail.rasioKompresiRLE));
        DecimalFormat df = new DecimalFormat("#.##"); // Tampilkan hingga 2 desimal jika ada
        String detikFormatted = df.format(detail.waktuProsesGambarMs / 1000.0);
        textViewDetailWaktu.setText(String.format(Locale.US, "Waktu Proses Kompresi : %d ms (%s detik)", detail.waktuProsesGambarMs, detikFormatted));

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

        // Memuat gambar utama pengaduan
        if (detail.pathImage != null && !detail.pathImage.isEmpty()) {
            final String imageUrl = ApiConfig.BASE_URL + detail.pathImage;
            Glide.with(this).load(imageUrl).placeholder(R.drawable.ic_image_placeholder).error(R.drawable.ic_image_error).into(imageViewDetailFoto);
            imageViewDetailFoto.setOnClickListener(v -> {
                Intent intent = new Intent(this, FullScreenImageActivity.class);
                intent.putExtra(FullScreenImageActivity.EXTRA_IMAGE_URL, imageUrl);
                startActivity(intent);
            });
        }
    }

    private int dpToPx(int dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, getResources().getDisplayMetrics());
    }

    private void setupChipStatus(String status) {
        if (status == null) return;
        chipDetailStatus.setText(StringUtils.capitalizeFirstLetter(status));
        int colorRes = switch (status.toLowerCase()) {
            case "diproses" -> R.color.status_diproses_bg;
            case "selesai" -> R.color.status_selesai_bg;
            default -> R.color.status_menunggu_bg;
        };
        chipDetailStatus.setChipBackgroundColorResource(colorRes);
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

    private String formatUkuranBytes(long ukuranDalamBytes) {
        // Jika kurang dari 1 KB, tampilkan sebagai Bytes
        if (ukuranDalamBytes < 1024) {
            return ukuranDalamBytes + " B";
        }

        // Jika kurang dari 1 MB, tampilkan sebagai KiloBytes
        final double kilobyte = ukuranDalamBytes / 1024.0;
        if (kilobyte < 1024) {
            // Gunakan pola ini untuk selalu menampilkan 2 angka desimal
            DecimalFormat df = new DecimalFormat("#,##0.00");
            return df.format(kilobyte) + " KB";
        }

        // Jika lebih dari 1 MB, tampilkan sebagai MegaBytes
        final double megabyte = kilobyte / 1024.0;
        // Gunakan pola ini untuk selalu menampilkan 2 angka desimal
        DecimalFormat df = new DecimalFormat("#,##0.00");
        return df.format(megabyte) + " MB";
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