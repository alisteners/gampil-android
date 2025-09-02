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

public class RiwayatFragment extends Fragment {
    private static final String TAG = "RiwayatFragment";

    private RiwayatPengaduanViewModel viewModel;
    private RecyclerView recyclerViewRiwayat;
    private RiwayatPengaduanAdapter adapter;
    private ProgressBar progressBarRiwayat;
    private SwipeRefreshLayout swipeRefreshLayoutRiwayat;
    private LinearLayout layoutPesanRiwayat;
    private TextView textViewPesanDiLayout;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_riwayat, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Inisialisasi view
        recyclerViewRiwayat       = view.findViewById(R.id.recyclerViewRiwayat);
        progressBarRiwayat        = view.findViewById(R.id.progressBarRiwayat);
        swipeRefreshLayoutRiwayat = view.findViewById(R.id.swipeRefreshLayoutRiwayat);
        layoutPesanRiwayat        = view.findViewById(R.id.layoutPesanRiwayat);
        textViewPesanDiLayout     = view.findViewById(R.id.textViewPesanDiLayout);

        // Setup RecyclerView & Adapter
        recyclerViewRiwayat.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new RiwayatPengaduanAdapter(new ArrayList<>(), requireContext());
        recyclerViewRiwayat.setAdapter(adapter);

        // Setup ViewModel
        PengaduanRepository repo = new PengaduanRepositoryImpl(requireContext());
        GetRiwayatPengaduanUseCase uc = new GetRiwayatPengaduanUseCase(repo);
        RiwayatPengaduanViewModelFactory factory =
                new RiwayatPengaduanViewModelFactory(uc);
        viewModel = new ViewModelProvider(this, factory)
                .get(RiwayatPengaduanViewModel.class);

        // Swipe-to-refresh
        swipeRefreshLayoutRiwayat.setOnRefreshListener(() -> {
            Log.d(TAG, "Swipe to refresh dipicu.");
            loadInitialData();
        });

        // Item click
        adapter.setOnItemClickListener(item -> {
            Log.d(TAG, "Klik ID Pengaduan: " + item.idPengaduan());
            Intent i = new Intent(requireContext(), DetailPengaduanActivity.class);
            i.putExtra(DetailPengaduanActivity.EXTRA_PENGADUAN_ID,
                    item.idPengaduan());
            startActivity(i);
        });

        // Observers
        observeViewModel();

        // Load pertama
        loadInitialData();
    }

    private void loadInitialData() {
        getContext();
        SharedPreferences prefs =
                requireContext().getSharedPreferences(LoginActivity.SHARED_PREFS,
                        Context.MODE_PRIVATE);
        int idUser = prefs.getInt(LoginActivity.KEY_ID_USER, -1);
        if (idUser != -1) {
            viewModel.loadRiwayat(idUser);
        } else {
            Toast.makeText(requireContext(),
                    "Sesi pengguna tidak valid.",
                    Toast.LENGTH_SHORT).show();
            if (swipeRefreshLayoutRiwayat.isRefreshing()) {
                swipeRefreshLayoutRiwayat.setRefreshing(false);
            }
            requireActivity().finish();
        }
    }

    // Di dalam RiwayatFragment.java

    @SuppressLint("SetTextI18n")
    private void observeViewModel() {
        // Observer untuk isLoading
        viewModel.isLoading.observe(getViewLifecycleOwner(), isLoading -> {
            if (isLoading != null) {
                if (isLoading && !swipeRefreshLayoutRiwayat.isRefreshing()) {
                    progressBarRiwayat.setVisibility(View.VISIBLE);
                    recyclerViewRiwayat.setVisibility(View.GONE);
                    layoutPesanRiwayat.setVisibility(View.GONE);
                } else if (!isLoading) {
                    progressBarRiwayat.setVisibility(View.GONE);
                    swipeRefreshLayoutRiwayat.setRefreshing(false);
                }
            }
        });

        // Observer untuk errorMessage
        viewModel.errorMessage.observe(getViewLifecycleOwner(), error -> {
            if (error != null && !error.isEmpty()) {
                textViewPesanDiLayout.setText(error);
                layoutPesanRiwayat.setVisibility(View.VISIBLE);
                recyclerViewRiwayat.setVisibility(View.GONE);
            }
        });

        // Menggabungkan logika penampilan data dan penanganan kasus kosong
        viewModel.riwayatList.observe(getViewLifecycleOwner(), items -> {
            // 1. Selalu cek null untuk keamanan
            if (items != null) {

                // 2. Cek apakah daftar yang diterima kosong
                if (items.isEmpty()) {
                    // Jika ya, tampilkan layout pesan "Riwayat Kosong"
                    // dan sembunyikan daftar RecyclerView
                    textViewPesanDiLayout.setText(getString(R.string.pesan_riwayat_kosong));
                    layoutPesanRiwayat.setVisibility(View.VISIBLE);
                    recyclerViewRiwayat.setVisibility(View.GONE);
                } else {
                    // Jika ada data, tampilkan daftar RecyclerView
                    // dan sembunyikan layout pesan
                    layoutPesanRiwayat.setVisibility(View.GONE);
                    recyclerViewRiwayat.setVisibility(View.VISIBLE);
                    adapter.updateData(items);
                }
            } else {
                // Sebagai pengaman jika 'items' null, tampilkan pesan error
                textViewPesanDiLayout.setText("Gagal memuat data riwayat.");
                layoutPesanRiwayat.setVisibility(View.VISIBLE);
                recyclerViewRiwayat.setVisibility(View.GONE);
            }
        });

    }
}
