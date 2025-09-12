package com.alisteners.pengaduan.presentation.masyarakat.riwayatpengaduan;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.alisteners.pengaduan.presentation.login.LoginActivity;
import com.alisteners.pengaduan.R;
import com.alisteners.pengaduan.data.repository.PengaduanRepositoryImpl;
import com.alisteners.pengaduan.domain.repository.PengaduanRepository;
import com.alisteners.pengaduan.domain.usecase.GetRiwayatPengaduanUseCase;
import com.alisteners.pengaduan.presentation.masyarakat.detailpengaduan.DetailPengaduanActivity;

import java.util.ArrayList;

/**
 * Fragment yang bertanggung jawab untuk menampilkan halaman Riwayat Pengaduan pengguna.
 * Halaman ini menampilkan daftar semua pengaduan yang pernah dibuat oleh pengguna yang sedang login.
 * Fragment ini mengimplementasikan arsitektur MVVM dengan berkomunikasi melalui {@link RiwayatPengaduanViewModel}.
 */
public class RiwayatFragment extends Fragment {
    private static final String TAG = "RiwayatFragment";

    // --- Deklarasi View & Adapter ---
    private RiwayatPengaduanViewModel viewModel;
    private RecyclerView recyclerViewRiwayat;
    private RiwayatPengaduanAdapter adapter;
    private ProgressBar progressBarRiwayat;
    private SwipeRefreshLayout swipeRefreshLayoutRiwayat;
    private LinearLayout layoutPesanRiwayat; // Layout untuk menampilkan pesan (error/kosong)
    private TextView textViewPesanDiLayout;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // Inflate layout untuk fragment ini
        return inflater.inflate(R.layout.fragment_riwayat, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        inisialisasiView(view);
        setupRecyclerView();
        setupViewModel();
        setupListeners();
        observeViewModel();

        // Memuat data riwayat saat fragment pertama kali dibuat
        loadInitialData();
    }

    /**
     * Menginisialisasi semua referensi ke komponen view dari layout XML.
     */
    private void inisialisasiView(@NonNull View view) {
        recyclerViewRiwayat       = view.findViewById(R.id.recyclerViewRiwayat);
        progressBarRiwayat        = view.findViewById(R.id.progressBarRiwayat);
        swipeRefreshLayoutRiwayat = view.findViewById(R.id.swipeRefreshLayoutRiwayat);
        layoutPesanRiwayat        = view.findViewById(R.id.layoutPesanRiwayat);
        textViewPesanDiLayout     = view.findViewById(R.id.textViewPesanDiLayout);
    }

    /**
     * Mengatur RecyclerView, termasuk adapter dan layout manager.
     */
    private void setupRecyclerView() {
        recyclerViewRiwayat.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new RiwayatPengaduanAdapter(new ArrayList<>(), requireContext());
        recyclerViewRiwayat.setAdapter(adapter);
    }

    /**
     * Menginisialisasi ViewModel menggunakan ViewModelFactory untuk menyuntikkan dependensi (UseCase).
     */
    private void setupViewModel() {
        PengaduanRepository repo = new PengaduanRepositoryImpl(requireContext());
        GetRiwayatPengaduanUseCase uc = new GetRiwayatPengaduanUseCase(repo);
        RiwayatPengaduanViewModelFactory factory = new RiwayatPengaduanViewModelFactory(uc);
        viewModel = new ViewModelProvider(this, factory).get(RiwayatPengaduanViewModel.class);
    }

    /**
     * Mengatur semua listener untuk interaksi pengguna, seperti swipe-to-refresh dan klik item.
     */
    private void setupListeners() {
        swipeRefreshLayoutRiwayat.setOnRefreshListener(() -> {
            Log.d(TAG, "Swipe to refresh dipicu.");
            loadInitialData();
        });

        adapter.setOnItemClickListener(item -> {
            Log.d(TAG, "Klik ID Pengaduan: " + item.idPengaduan());
            Intent i = new Intent(requireContext(), DetailPengaduanActivity.class);
            i.putExtra(DetailPengaduanActivity.EXTRA_PENGADUAN_ID, item.idPengaduan());
            startActivity(i);
        });
    }

    /**
     * Memulai proses pemuatan data riwayat dari ViewModel.
     * Metode ini mengambil ID pengguna dari SharedPreferences sebelum memanggil ViewModel.
     */
    private void loadInitialData() {
        SharedPreferences prefs = requireContext().getSharedPreferences(LoginActivity.SHARED_PREFS, Context.MODE_PRIVATE);
        int idUser = prefs.getInt(LoginActivity.KEY_ID_USER, -1);

        if (idUser != -1) {
            viewModel.loadRiwayat(idUser);
        } else {
            // Menangani kasus jika sesi pengguna tidak valid atau telah berakhir
            Toast.makeText(requireContext(), "Sesi pengguna tidak valid.", Toast.LENGTH_SHORT).show();
            if (swipeRefreshLayoutRiwayat.isRefreshing()) {
                swipeRefreshLayoutRiwayat.setRefreshing(false);
            }
            // Pertimbangkan untuk mengarahkan pengguna kembali ke halaman login
            // requireActivity().finish();
        }
    }

    /**
     * Mengamati LiveData dari ViewModel untuk memperbarui UI secara reaktif
     * ketika ada perubahan data, status loading, atau pesan error.
     */
    @SuppressLint("SetTextI18n")
    private void observeViewModel() {
        viewModel.isLoading.observe(getViewLifecycleOwner(), isLoading -> {
            if (isLoading != null) {
                if (isLoading && !swipeRefreshLayoutRiwayat.isRefreshing()) {
                    // Tampilkan progress bar utama saat loading awal
                    progressBarRiwayat.setVisibility(View.VISIBLE);
                    recyclerViewRiwayat.setVisibility(View.GONE);
                    layoutPesanRiwayat.setVisibility(View.GONE);
                } else if (!isLoading) {
                    // Sembunyikan semua indikator loading setelah selesai
                    progressBarRiwayat.setVisibility(View.GONE);
                    swipeRefreshLayoutRiwayat.setRefreshing(false);
                }
            }
        });

        viewModel.errorMessage.observe(getViewLifecycleOwner(), error -> {
            if (error != null && !error.isEmpty()) {
                // Tampilkan pesan error jika terjadi kegagalan
                textViewPesanDiLayout.setText(error);
                layoutPesanRiwayat.setVisibility(View.VISIBLE);
                recyclerViewRiwayat.setVisibility(View.GONE);
            }
        });

        viewModel.riwayatList.observe(getViewLifecycleOwner(), items -> {
            if (items != null) {
                if (items.isEmpty()) {
                    // Tampilkan pesan "Riwayat Kosong" jika tidak ada data
                    textViewPesanDiLayout.setText(getString(R.string.pesan_riwayat_kosong));
                    layoutPesanRiwayat.setVisibility(View.VISIBLE);
                    recyclerViewRiwayat.setVisibility(View.GONE);
                } else {
                    // Tampilkan data ke RecyclerView jika data tersedia
                    layoutPesanRiwayat.setVisibility(View.GONE);
                    recyclerViewRiwayat.setVisibility(View.VISIBLE);
                    adapter.updateData(items);
                }
            }
        });
    }
}