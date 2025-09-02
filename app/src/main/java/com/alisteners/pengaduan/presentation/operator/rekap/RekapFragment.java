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

public class RekapFragment extends Fragment {

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
        viewModel.loadRekapData();
    }

    private void inisialisasiView(View view) {
        tvTotal = view.findViewById(R.id.tvTotalLaporan);
        tvMenunggu = view.findViewById(R.id.tvStatusMenunggu);
        tvDiproses = view.findViewById(R.id.tvStatusDiproses);
        tvSelesai = view.findViewById(R.id.tvStatusSelesai);
        pieChart = view.findViewById(R.id.pieChartKategori);
        barChart = view.findViewById(R.id.barChartTren);
        progressBar = view.findViewById(R.id.progressBarRekap);
    }

    private void setupViewModel() {
        OperatorRepository repository = new OperatorRepositoryImpl(requireContext());
        GetRekapDataUseCase useCase = new GetRekapDataUseCase(repository);
        RekapViewModelFactory factory = new RekapViewModelFactory(useCase);
        viewModel = new ViewModelProvider(this, factory).get(RekapViewModel.class);
    }

    private void observeViewModel() {
        viewModel.isLoading.observe(getViewLifecycleOwner(), isLoading -> progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE));

        viewModel.errorMessage.observe(getViewLifecycleOwner(), error -> {
            if (error != null && !error.isEmpty()) {
                Toast.makeText(getContext(), error, Toast.LENGTH_LONG).show();
            }
        });

        viewModel.rekapData.observe(getViewLifecycleOwner(), rekapData -> {
            if (rekapData != null) {
                // Mengisi data summary (sekarang akan bekerja)
                populateSummary(rekapData.summary);

                // Mengisi data pie chart (sekarang akan bekerja)
                setupPieChart(rekapData.distribusiKategori);

                // Mengisi data bar chart (sekarang akan bekerja)
                setupBarChart(rekapData.trenBulanan);
            }
        });
    }

    private void populateSummary(Summary summary) {
        if(summary == null) return;
        tvTotal.setText(String.valueOf(summary.total));
        tvMenunggu.setText(String.valueOf(summary.menunggu));
        tvDiproses.setText(String.valueOf(summary.diproses));
        tvSelesai.setText(String.valueOf(summary.selesai));
    }

    private void setupPieChart(List<KategoriDistribusi> data) {
        if (data == null || data.isEmpty()) {
            pieChart.setVisibility(View.GONE);
            return;
        }
        pieChart.setVisibility(View.VISIBLE);

        // Pengaturan Dasar Chart
        pieChart.setUsePercentValues(true);
        pieChart.getDescription().setEnabled(false);
        pieChart.setExtraOffsets(5, 10, 5, 5);
        pieChart.setDragDecelerationFrictionCoef(0.95f);

        // Pengaturan Lubang Tengah
        pieChart.setDrawHoleEnabled(true);
        pieChart.setHoleColor(Color.WHITE);
        pieChart.setTransparentCircleRadius(61f);
        pieChart.setCenterText("Distribusi Kategori"); // Teks default
        pieChart.setCenterTextSize(14f);

        // Sembunyikan label nama kategori dari atas slice
        pieChart.setDrawEntryLabels(false);

        // Mempersiapkan Data dan Warna
        ArrayList<PieEntry> entries = new ArrayList<>();
        for (KategoriDistribusi item : data) {
            entries.add(new PieEntry(item.jumlah, item.kategori));
        }

        PieData pieData = getPieData(entries);
        pieChart.setData(pieData);

        // Pengaturan Legenda di Bawah (Tidak Berubah)
        Legend l = pieChart.getLegend();
        l.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
        l.setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);
        l.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        l.setDrawInside(false);
        l.setWordWrapEnabled(true);
        l.setXEntrySpace(10f);
        l.setYEntrySpace(5f);


        // LISTENER KLIK INTERAKTIF
        pieChart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(Entry e, Highlight h) {
                if (e == null) return;

                PieEntry pieEntry = (PieEntry) e;
                // Tampilkan label dan persen di tengah chart saat diklik
                // Menggunakan getX() untuk nilai mentah dan getY() untuk persen jika chart dikonfigurasi
                // Tapi untuk amannya, kita hitung manual persennya dari nilai mentah
                String centerText = pieEntry.getLabel() + "\n" + String.format(Locale.US, "%.1f%%", h.getY());
                pieChart.setCenterText(centerText);
            }

            @Override
            public void onNothingSelected() {
                // Kembalikan ke teks default jika tidak ada yang dipilih
                pieChart.setCenterText("Distribusi Kategori");
            }
        });

        // Animasi dan Refresh
        pieChart.animateY(1400, Easing.EaseInOutQuad);
        pieChart.invalidate();
    }

    @NonNull
    private static PieData getPieData(ArrayList<PieEntry> entries) {
        ArrayList<Integer> colors = new ArrayList<>();
        for (int color : ColorTemplate.MATERIAL_COLORS) { colors.add(color); }
        for (int color : ColorTemplate.VORDIPLOM_COLORS) { colors.add(color); }
        for (int color : ColorTemplate.JOYFUL_COLORS) { colors.add(color); }

        // Pengaturan Tampilan Data (PieDataSet)
        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setColors(colors);
        dataSet.setSliceSpace(3f);

        // Jangan gambar nilai (persentase dan garis) di chart sama sekali
        dataSet.setDrawValues(false);

        return new PieData(dataSet);
    }

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
            // Format bulan dari "2025-06" menjadi "Jun 25"
            labels.add(formatLabelBulan(data.get(i).bulan));
        }

        BarDataSet dataSet = new BarDataSet(entries, "Jumlah Laporan");
        dataSet.setColor(ContextCompat.getColor(requireContext(), R.color.app_primer));

        BarData barData = new BarData(dataSet);
        barData.setBarWidth(0.5f);
        barData.setValueTextSize(10f);

        XAxis xAxis = barChart.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(labels));
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setGranularityEnabled(true);
        xAxis.setLabelRotationAngle(-45); // Rotasi label agar tidak tumpang tindih

        barChart.setData(barData);
        barChart.getDescription().setEnabled(false);
        barChart.getAxisRight().setEnabled(false); // Sembunyikan sumbu Y kanan
        barChart.setFitBars(true);
        barChart.animateY(1000);
        barChart.invalidate();
    }

    private String formatLabelBulan(String bulanTahun) { // Contoh: "2025-06"
        try {
            SimpleDateFormat input = new SimpleDateFormat("yyyy-MM", Locale.getDefault());
            SimpleDateFormat output = new SimpleDateFormat("MMM yy", new Locale("id", "ID"));
            Date date = input.parse(bulanTahun);
            assert date != null;
            return output.format(date);
        } catch (ParseException e) {
            return bulanTahun;
        }
    }
}
