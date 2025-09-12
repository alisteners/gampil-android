package com.alisteners.pengaduan.presentation.operator.rekap;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.alisteners.pengaduan.R;
import com.alisteners.pengaduan.data.repository.OperatorRepositoryImpl;
import com.alisteners.pengaduan.domain.operator.entity.KategoriDistribusi;
import com.alisteners.pengaduan.domain.operator.entity.Summary;
import com.alisteners.pengaduan.domain.operator.entity.TrenBulanan;
import com.alisteners.pengaduan.domain.operator.repository.OperatorRepository;
import com.alisteners.pengaduan.domain.operator.usecase.GetRekapDataUseCase;

import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Fragment yang bertanggung jawab untuk menampilkan halaman rekapitulasi dan statistik
 * untuk pengguna operator. Menampilkan data dalam bentuk ringkasan, diagram lingkaran (Pie Chart)
 * untuk distribusi kategori, dan diagram batang (Bar Chart) untuk tren bulanan.
 */
public class RekapFragment extends Fragment {

    // --- Deklarasi View & Komponen ---
    private RekapViewModel viewModel;
    private TextView tvTotal, tvMenunggu, tvDiproses, tvSelesai;
    private PieChart pieChart;
    private BarChart barChart;
    private ProgressBar progressBar;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_rekap, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Toolbar toolbar = view.findViewById(R.id.toolbarRekap);
        ((AppCompatActivity)requireActivity()).setSupportActionBar(toolbar);

        inisialisasiView(view);
        setupViewModel();
        observeViewModel();

        // Memuat data rekap saat fragment pertama kali dibuat
        viewModel.loadRekapData();
    }

    /**
     * Menginisialisasi semua referensi ke komponen view dari layout XML.
     */
    private void inisialisasiView(View view) {
        tvTotal = view.findViewById(R.id.tvTotalLaporan);
        tvMenunggu = view.findViewById(R.id.tvStatusMenunggu);
        tvDiproses = view.findViewById(R.id.tvStatusDiproses);
        tvSelesai = view.findViewById(R.id.tvStatusSelesai);
        pieChart = view.findViewById(R.id.pieChartKategori);
        barChart = view.findViewById(R.id.barChartTren);
        progressBar = view.findViewById(R.id.progressBarRekap);
    }

    /**
     * Menginisialisasi ViewModel menggunakan ViewModelFactory untuk menyuntikkan dependensi.
     */
    private void setupViewModel() {
        OperatorRepository repository = new OperatorRepositoryImpl(requireContext());
        GetRekapDataUseCase useCase = new GetRekapDataUseCase(repository);
        RekapViewModelFactory factory = new RekapViewModelFactory(useCase);
        viewModel = new ViewModelProvider(this, factory).get(RekapViewModel.class);
    }

    /**
     * Mengamati LiveData dari ViewModel untuk memperbarui UI secara reaktif.
     */
    private void observeViewModel() {
        viewModel.isLoading.observe(getViewLifecycleOwner(), isLoading ->
                progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE));

        viewModel.errorMessage.observe(getViewLifecycleOwner(), error -> {
            if (error != null && !error.isEmpty()) {
                Toast.makeText(getContext(), error, Toast.LENGTH_LONG).show();
            }
        });

        viewModel.rekapData.observe(getViewLifecycleOwner(), rekapData -> {
            if (rekapData != null) {
                populateSummary(rekapData.summary);
                setupPieChart(rekapData.distribusiKategori);
                setupBarChart(rekapData.trenBulanan);
            }
        });
    }

    /**
     * Mengisi data ringkasan (summary) ke dalam TextViews.
     * @param summary Objek {@link Summary} yang berisi data jumlah laporan.
     */
    private void populateSummary(Summary summary) {
        if(summary == null) return;
        tvTotal.setText(String.valueOf(summary.total));
        tvMenunggu.setText(String.valueOf(summary.menunggu));
        tvDiproses.setText(String.valueOf(summary.diproses));
        tvSelesai.setText(String.valueOf(summary.selesai));
    }

    /**
     * Mengatur dan menampilkan data distribusi kategori ke dalam PieChart.
     * @param data Daftar {@link KategoriDistribusi} dari ViewModel.
     */
    private void setupPieChart(List<KategoriDistribusi> data) {
        if (data == null || data.isEmpty()) {
            pieChart.setVisibility(View.GONE);
            return;
        }
        pieChart.setVisibility(View.VISIBLE);

        pieChart.setUsePercentValues(true);
        pieChart.getDescription().setEnabled(false);
        pieChart.setExtraOffsets(5, 10, 5, 5);
        pieChart.setDrawHoleEnabled(true);
        pieChart.setHoleColor(Color.WHITE);
        pieChart.setCenterText("Distribusi Kategori");
        pieChart.setCenterTextSize(14f);
        pieChart.setDrawEntryLabels(false);

        ArrayList<PieEntry> entries = new ArrayList<>();
        for (KategoriDistribusi item : data) {
            entries.add(new PieEntry(item.jumlah, item.kategori));
        }

        pieChart.setData(getPieData(entries));

        Legend l = pieChart.getLegend();
        l.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
        l.setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);
        l.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        l.setWordWrapEnabled(true);

        // Listener untuk interaksi klik pada chart
        pieChart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(Entry e, Highlight h) {
                if (e instanceof PieEntry pieEntry) {
                    String centerText = pieEntry.getLabel() + "\n" + String.format(Locale.US, "%.1f%%", h.getY());
                    pieChart.setCenterText(centerText);
                }
            }
            @Override
            public void onNothingSelected() {
                pieChart.setCenterText("Distribusi Kategori");
            }
        });

        pieChart.animateY(1400, Easing.EaseInOutQuad);
        pieChart.invalidate();
    }

    /**
     * Membuat objek PieData dengan dataset dan konfigurasi warna.
     */
    @NonNull
    private static PieData getPieData(ArrayList<PieEntry> entries) {
        ArrayList<Integer> colors = new ArrayList<>();
        for (int color : ColorTemplate.MATERIAL_COLORS) { colors.add(color); }
        for (int color : ColorTemplate.VORDIPLOM_COLORS) { colors.add(color); }
        for (int color : ColorTemplate.JOYFUL_COLORS) { colors.add(color); }

        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setColors(colors);
        dataSet.setSliceSpace(3f);
        dataSet.setDrawValues(false); // Sembunyikan nilai persen di atas slice
        return new PieData(dataSet);
    }

    /**
     * Mengatur dan menampilkan data tren bulanan ke dalam BarChart.
     * @param data Daftar {@link TrenBulanan} dari ViewModel.
     */
    private void setupBarChart(List<TrenBulanan> data) {
        if (data == null || data.isEmpty()) {
            barChart.setVisibility(View.GONE);
            return;
        }
        barChart.setVisibility(View.VISIBLE);

        ArrayList<BarEntry> entries = new ArrayList<>();
        ArrayList<String> labels = new ArrayList<>();
        for (int i = 0; i < data.size(); i++) {
            entries.add(new BarEntry(i, data.get(i).jumlah));
            labels.add(formatLabelBulan(data.get(i).bulan));
        }

        BarDataSet dataSet = new BarDataSet(entries, "Jumlah Laporan");
        dataSet.setColor(ContextCompat.getColor(requireContext(), R.color.app_primer));
        BarData barData = new BarData(dataSet);
        barData.setBarWidth(0.5f);

        XAxis xAxis = barChart.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(labels));
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setLabelRotationAngle(-45);

        barChart.setData(barData);
        barChart.getDescription().setEnabled(false);
        barChart.getAxisRight().setEnabled(false);
        barChart.setFitBars(true);
        barChart.animateY(1000);
        barChart.invalidate();
    }

    /**
     * Memformat label bulan dari "yyyy-MM" menjadi "MMM yy" (misal: "Jun 25").
     */
    private String formatLabelBulan(String bulanTahun) {
        try {
            SimpleDateFormat input = new SimpleDateFormat("yyyy-MM", Locale.getDefault());
            SimpleDateFormat output = new SimpleDateFormat("MMM yy", new Locale("id", "ID"));
            Date date = input.parse(bulanTahun);
            return (date != null) ? output.format(date) : bulanTahun;
        } catch (ParseException e) {
            return bulanTahun;
        }
    }
}