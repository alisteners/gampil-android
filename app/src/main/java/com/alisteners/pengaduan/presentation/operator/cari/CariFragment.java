package com.alisteners.pengaduan.presentation.operator.cari;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import androidx.appcompat.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.alisteners.pengaduan.R;
import com.alisteners.pengaduan.data.repository.OperatorRepositoryImpl;
import com.alisteners.pengaduan.domain.operator.repository.OperatorRepository;
import com.alisteners.pengaduan.domain.operator.usecase.SearchPengaduanUseCase;
import com.alisteners.pengaduan.presentation.masyarakat.beranda.PengaduanPublikAdapter;
import com.alisteners.pengaduan.presentation.operator.detail.DetailPengaduanOperatorActivity;

import com.google.android.material.chip.ChipGroup;

import java.util.ArrayList;

/**
 * Fragment yang bertanggung jawab untuk fungsionalitas pencarian pengaduan bagi operator.
 * Memungkinkan operator untuk mencari pengaduan berdasarkan kata kunci dan memfilternya
 * berdasarkan status. Mengimplementasikan logika 'debounce' untuk efisiensi pencarian.
 */
public class CariFragment extends Fragment {

    private static final String TAG = "CariFragment";

    // --- Deklarasi View & Adapter ---
    private CariViewModel viewModel;
    private androidx.appcompat.widget.SearchView searchView;
    private ChipGroup chipGroupFilter;
    private RecyclerView recyclerView;
    private PengaduanPublikAdapter adapter; // Menggunakan adapter yang sama dengan Beranda
    private ProgressBar progressBar;
    private TextView textViewPesan;

    // --- Deklarasi Properti Lain ---
    private String currentKeyword = "";
    private String currentStatusFilter = ""; // "" berarti semua status
    private final Handler searchHandler = new Handler(Looper.getMainLooper());
    private Runnable searchRunnable;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_cari, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        inisialisasiView(view);
        setupViewModel();
        setupRecyclerView();
        setupListeners();
        observeViewModel();
    }

    /**
     * Menginisialisasi semua referensi ke komponen view dari layout XML dan melakukan setup awal.
     */
    private void inisialisasiView(View view) {
        Toolbar toolbar = view.findViewById(R.id.toolbarCari);
        ((AppCompatActivity) requireActivity()).setSupportActionBar(toolbar);

        searchView = view.findViewById(R.id.searchViewOperator);
        chipGroupFilter = view.findViewById(R.id.chipGroupFilterStatus);
        recyclerView = view.findViewById(R.id.recyclerViewCari);
        progressBar = view.findViewById(R.id.progressBarCari);
        textViewPesan = view.findViewById(R.id.textViewPesanCari);

        searchView.setQueryHint("Cari berdasarkan kategori, deskripsi...");
        searchView.setIconified(false); // Otomatis membuka keyboard saat fragment tampil
    }

    /**
     * Menginisialisasi ViewModel menggunakan ViewModelFactory untuk menyuntikkan dependensi.
     */
    private void setupViewModel() {
        OperatorRepository repository = new OperatorRepositoryImpl(requireContext());
        SearchPengaduanUseCase useCase = new SearchPengaduanUseCase(repository);
        CariViewModelFactory factory = new CariViewModelFactory(useCase);
        viewModel = new ViewModelProvider(this, factory).get(CariViewModel.class);
    }

    /**
     * Mengatur RecyclerView, termasuk adapter dan layout manager.
     */
    private void setupRecyclerView() {
        adapter = new PengaduanPublikAdapter(new ArrayList<>(), requireContext());
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(adapter);
    }

    /**
     * Mengatur semua listener untuk interaksi pengguna, termasuk input pencarian,
     * perubahan filter, dan klik pada item hasil pencarian.
     */
    private void setupListeners() {
        // Listener untuk SearchView dengan mekanisme debounce (jeda)
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            /** Dipanggil saat pengguna menekan tombol submit (enter) pada keyboard. */
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchHandler.removeCallbacks(searchRunnable); // Hentikan pencarian terjadwal
                performSearch(query); // Langsung lakukan pencarian
                return true;
            }

            /** Dipanggil setiap kali teks di dalam SearchView berubah. */
            @Override
            public boolean onQueryTextChange(String newText) {
                searchHandler.removeCallbacks(searchRunnable); // Batalkan pencarian sebelumnya
                // Jadwalkan pencarian baru setelah jeda 500ms untuk mencegah request berlebihan
                searchRunnable = () -> performSearch(newText);
                searchHandler.postDelayed(searchRunnable, 500);
                return true;
            }
        });

        // Listener untuk filter status
        chipGroupFilter.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.chipFilterMenunggu) {
                currentStatusFilter = "menunggu";
            } else if (checkedId == R.id.chipFilterDiproses) {
                currentStatusFilter = "diproses";
            } else if (checkedId == R.id.chipFilterSelesai) {
                currentStatusFilter = "selesai";
            } else { // Termasuk chipFilterSemua
                currentStatusFilter = "";
            }
            performSearch(currentKeyword); // Lakukan pencarian ulang dengan filter baru
        });

        // Listener untuk klik item di hasil pencarian
        adapter.setOnItemClickListener(item -> {
            Intent intent = new Intent(requireActivity(), DetailPengaduanOperatorActivity.class);
            intent.putExtra(DetailPengaduanOperatorActivity.EXTRA_ID_PENGADUAN, item.getIdPengaduan());
            startActivity(intent);
        });
    }

    /**
     * Memulai proses pencarian data dengan memanggil metode di ViewModel.
     * @param keyword Kata kunci yang dimasukkan oleh pengguna.
     */
    private void performSearch(String keyword) {
        currentKeyword = keyword;
        Log.d(TAG, "Mencari dengan keyword: '" + currentKeyword + "', status: '" + currentStatusFilter + "'");
        viewModel.cariPengaduan(currentKeyword, currentStatusFilter);
    }

    /**
     * Mengamati LiveData dari ViewModel untuk memperbarui UI secara reaktif
     * terhadap perubahan hasil pencarian, status loading, atau pesan error.
     */
    @SuppressLint("SetTextI18n")
    private void observeViewModel() {
        viewModel.isLoading.observe(getViewLifecycleOwner(), isLoading -> {
            progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            if (isLoading) textViewPesan.setVisibility(View.GONE);
        });

        viewModel.hasilPencarian.observe(getViewLifecycleOwner(), list -> {
            boolean isEmpty = (list == null || list.isEmpty());
            if (!isEmpty) {
                textViewPesan.setVisibility(View.GONE);
                recyclerView.setVisibility(View.VISIBLE);
                adapter.updateData(list);
            } else {
                recyclerView.setVisibility(View.GONE);
                // Hanya tampilkan pesan "kosong" jika proses loading sudah selesai
                if (viewModel.isLoading.getValue() != null && !viewModel.isLoading.getValue()) {
                    textViewPesan.setVisibility(View.VISIBLE);
                    textViewPesan.setText("Tidak ada hasil yang ditemukan.");
                }
            }
        });

        viewModel.errorMessage.observe(getViewLifecycleOwner(), error -> {
            if (error != null && !error.isEmpty()) {
                Toast.makeText(getContext(), error, Toast.LENGTH_SHORT).show();
            }
        });
    }
}