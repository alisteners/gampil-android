package com.alisteners.pengaduan.presentation.masyarakat.detailpengaduan;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
import com.alisteners.pengaduan.data.util.SizeFormatter;

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

/**
 * Activity yang menampilkan detail lengkap dari sebuah pengaduan.
 * Bertanggung jawab untuk mengambil data dari ViewModel, menampilkannya ke berbagai
 * komponen UI, termasuk informasi dasar, analisis kompresi, dan timeline tindak lanjut.
 */
public class DetailPengaduanActivity extends AppCompatActivity {

    /** Kunci (key) yang digunakan untuk mengirim ID pengaduan melalui Intent. */
    public static final String EXTRA_PENGADUAN_ID = "extra_pengaduan_id";
    private DetailPengaduanViewModel viewModel;

    // --- Deklarasi View ---
    private ProgressBar progressBar;
    private ImageView imageViewDetailFoto;
    private CollapsingToolbarLayout collapsingToolbar;
    private TextView textViewDetailKategori, textViewDetailTanggal, textViewDetailLokasi, textViewDetailDeskripsi;
    private Chip chipDetailStatus;
    private TextView textViewDetailDimensi, textViewDetailUkuranAsli,
            textViewDetailUkuranResize, textViewDetailUkuranGrayscale,
            textViewDetailUkuranRle, textViewDetailRasioHemat,
            textViewDetailRasioStandar, textViewDetailWaktu;
    private TextView textViewRasioVsAsli, textViewRasioVsResize, textViewRasioVsGrayscale;
    private TextView textViewSpaceSavingVsAsli, textViewSpaceSavingVsResize, textViewSpaceSavingVsGrayscale;
    private RecyclerView recyclerViewLogStatus;
    private LogStatusAdapter logStatusAdapter;

    // --- View untuk Fitur "Mode Penelitian" ---
    private com.google.android.material.card.MaterialCardView cardViewAnalisis;
    private LinearLayout layoutDimensi, layoutUkuranResize, layoutUkuranGrayscale, layoutInfoTambahan;
    private int tapCount = 0;
    private final Handler tapHandler = new Handler(Looper.getMainLooper());
    private final Runnable tapResetRunnable = () -> tapCount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_pengaduan);

        inisialisasiView();
        setupToolbar();
        setupViewModel();
        setupRecyclerView();
        observeViewModel();
        setupListeners();

        int idPengaduan = getIntent().getIntExtra(EXTRA_PENGADUAN_ID, -1);
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
        progressBar = findViewById(R.id.progressBarDetail);
        imageViewDetailFoto = findViewById(R.id.imageViewDetailFoto);
        collapsingToolbar = findViewById(R.id.collapsingToolbarDetail);
        textViewDetailKategori = findViewById(R.id.textViewDetailKategori);
        textViewDetailTanggal = findViewById(R.id.textViewDetailTanggal);
        chipDetailStatus = findViewById(R.id.chipDetailStatus);
        textViewDetailLokasi = findViewById(R.id.textViewDetailLokasi);
        textViewDetailDeskripsi = findViewById(R.id.textViewDetailDeskripsi);
        textViewDetailDimensi = findViewById(R.id.textViewDetailDimensi);
        textViewDetailUkuranAsli = findViewById(R.id.textViewDetailUkuranAsli);
        textViewDetailUkuranResize = findViewById(R.id.textViewDetailUkuranResize);
        textViewDetailUkuranGrayscale = findViewById(R.id.textViewDetailUkuranGrayscale);
        textViewDetailUkuranRle = findViewById(R.id.textViewDetailUkuranRle);
        textViewDetailRasioHemat = findViewById(R.id.textViewDetailRasioHematRuang);
        textViewDetailRasioStandar = findViewById(R.id.textViewDetailRasioStandar);
        textViewDetailWaktu = findViewById(R.id.textViewDetailWaktu);
        cardViewAnalisis = findViewById(R.id.card_view_analisis);
        layoutDimensi = findViewById(R.id.layout_dimensi);
        layoutUkuranResize = findViewById(R.id.layout_ukuran_resize);
        layoutUkuranGrayscale = findViewById(R.id.layout_ukuran_grayscale);
        layoutInfoTambahan = findViewById(R.id.layout_info_tambahan);
        textViewRasioVsAsli = findViewById(R.id.textViewRasioVsAsli);
        textViewRasioVsResize = findViewById(R.id.textViewRasioVsResize);
        textViewRasioVsGrayscale = findViewById(R.id.textViewRasioVsGrayscale);
        textViewSpaceSavingVsAsli = findViewById(R.id.textViewSpaceSavingVsAsli);
        textViewSpaceSavingVsResize = findViewById(R.id.textViewSpaceSavingVsResize);
        textViewSpaceSavingVsGrayscale = findViewById(R.id.textViewSpaceSavingVsGrayscale);
        recyclerViewLogStatus = findViewById(R.id.recyclerViewLogStatusMasyarakat);
    }

    /**
     * Mengatur Toolbar, termasuk tombol kembali (up button).
     */
    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbarDetail);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("");
        }
    }

    /**
     * Menginisialisasi ViewModel menggunakan ViewModelFactory untuk menyuntikkan dependensi.
     */
    private void setupViewModel() {
        PengaduanRepository pengaduanRepo = new PengaduanRepositoryImpl(getApplicationContext());
        GetDetailPengaduanUseCase getDetailUseCase = new GetDetailPengaduanUseCase(pengaduanRepo);
        DetailPengaduanViewModelFactory factory = new DetailPengaduanViewModelFactory(getDetailUseCase);
        viewModel = new ViewModelProvider(this, factory).get(DetailPengaduanViewModel.class);
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
     * Mengatur listener untuk "Mode Penelitian", di mana double-tap pada kartu analisis
     * akan menampilkan/menyembunyikan data teknis tambahan.
     */
    private void setupListeners() {
        cardViewAnalisis.setOnClickListener(v -> {
            tapHandler.removeCallbacks(tapResetRunnable);
            tapCount++;
            if (tapCount == 2) {
                Toast.makeText(this, "Mode Penelitian Diaktifkan!", Toast.LENGTH_SHORT).show();
                int newVisibility = layoutDimensi.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE;
                layoutDimensi.setVisibility(newVisibility);
                layoutUkuranResize.setVisibility(newVisibility);
                layoutUkuranGrayscale.setVisibility(newVisibility);
                layoutInfoTambahan.setVisibility(newVisibility);
                tapCount = 0; // Reset
            }
            tapHandler.postDelayed(tapResetRunnable, 1000); // Reset dalam 1 detik
        });
    }

    /**
     * Mengamati LiveData dari ViewModel untuk memperbarui UI secara reaktif.
     */
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

    /**
     * Mengisi semua komponen UI dengan data dari objek {@link DetailPengaduan}.
     * @param detail Objek detail pengaduan yang diterima dari ViewModel.
     */
    @SuppressLint({"NotifyDataSetChanged", "SetTextI18n"})
    private void populateUi(DetailPengaduan detail) {
        if (detail == null) {
            Toast.makeText(this, "Gagal memuat detail data.", Toast.LENGTH_SHORT).show();
            return;
        }

        // --- Mengisi Data Utama ---
        collapsingToolbar.setTitle(detail.kategori);
        ((TextView) findViewById(R.id.textViewJudulToolbar)).setText(detail.kategori);
        textViewDetailKategori.setText(detail.kategori);
        textViewDetailTanggal.setText(formatTanggal(detail.tanggalPengaduan));
        textViewDetailLokasi.setText(detail.lokasi);
        textViewDetailDeskripsi.setText(detail.deskripsi);
        setupChipStatus(detail.status);

        // --- Mengisi Data Analisis Kompresi ---
        long jumlahPiksel = (long) detail.lebar * detail.tinggi;
        NumberFormat formatter = NumberFormat.getInstance(new Locale("id", "ID"));
        textViewDetailDimensi.setText(String.format(Locale.US, "%dx%d (%s Piksel)", detail.lebar, detail.tinggi, formatter.format(jumlahPiksel)));
        textViewDetailUkuranAsli.setText(formatUkuranBytes(detail.ukuranAsli));
        textViewDetailUkuranResize.setText(formatUkuranBytes(detail.ukuranSetelahResize));
        textViewDetailUkuranGrayscale.setText(formatUkuranBytes(detail.ukuranGrayscale));
        textViewDetailUkuranRle.setText(formatUkuranBytes(detail.ukuranFileRLEByte));
        textViewDetailRasioStandar.setText(String.format(Locale.US, "%.2f %%", detail.rasioKompresiStandar));
        textViewDetailRasioHemat.setText(String.format(Locale.US, "%.2f %%", detail.rasioKompresiRLE));
        ((ProgressBar) findViewById(R.id.progressBarRasio)).setProgress((int) Math.round(detail.rasioKompresiRLE));
        DecimalFormat df = new DecimalFormat("#.##");
        String detikFormatted = df.format(detail.waktuProsesGambarMs / 1000.0);
        textViewDetailWaktu.setText(String.format(Locale.US, "Waktu Proses Kompresi : %d ms (%s detik)", detail.waktuProsesGambarMs, detikFormatted));

        // --- Mengisi Data "Mode Penelitian" ---
        textViewRasioVsAsli.setText(String.format(Locale.US, "%.2f %%", hitungRasio(detail.ukuranFileRLEByte, detail.ukuranAsli)));
        textViewRasioVsResize.setText(String.format(Locale.US, "%.2f %%", hitungRasio(detail.ukuranFileRLEByte, detail.ukuranSetelahResize)));
        textViewRasioVsGrayscale.setText(String.format(Locale.US, "%.2f %%", hitungRasio(detail.ukuranFileRLEByte, detail.ukuranGrayscale)));
        textViewSpaceSavingVsAsli.setText(String.format(Locale.US, "%.2f %%", hitungSpaceSaving(detail.ukuranFileRLEByte, detail.ukuranAsli)));
        textViewSpaceSavingVsResize.setText(String.format(Locale.US, "%.2f %%", hitungSpaceSaving(detail.ukuranFileRLEByte, detail.ukuranSetelahResize)));
        textViewSpaceSavingVsGrayscale.setText(String.format(Locale.US, "%.2f %%", hitungSpaceSaving(detail.ukuranFileRLEByte, detail.ukuranGrayscale)));

        // --- Mengisi Timeline Tindak Lanjut ---
        if (detail.getRiwayatTindakLanjut() != null && !detail.getRiwayatTindakLanjut().isEmpty()) {
            List<LogStatusItem> riwayat = new ArrayList<>(detail.getRiwayatTindakLanjut());
            Collections.reverse(riwayat); // Balik urutan agar yang terbaru di atas.
            logStatusAdapter.updateData(riwayat);
            recyclerViewLogStatus.setVisibility(View.VISIBLE);
        } else {
            logStatusAdapter.updateData(new ArrayList<>());
            recyclerViewLogStatus.setVisibility(View.GONE);
        }

        // --- Memuat Gambar Utama ---
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

    // --- Metode Helper ---

    private double hitungRasio(long ukuranSetelah, long ukuranSebelum) {
        if (ukuranSebelum == 0) return 0.0;
        return ((double) ukuranSetelah / ukuranSebelum) * 100.0;
    }

    private double hitungSpaceSaving(long ukuranSetelah, long ukuranSebelum) {
        if (ukuranSebelum == 0) return 0.0;
        return (1.0 - ((double) ukuranSetelah / ukuranSebelum)) * 100.0;
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

    private String formatUkuranBytes(long bytes) {
        // Memanggil helper terpusat kita yang menggunakan Basis 1024
        return SizeFormatter.formatUkuranBiner(bytes);
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