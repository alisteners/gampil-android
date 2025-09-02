package com.alisteners.pengaduan.presentation.operator.cari;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
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

public class CariFragment extends Fragment {

    private static final String TAG = "CariFragment";
    private CariViewModel viewModel;
    private androidx.appcompat.widget.SearchView searchView;
    private ChipGroup chipGroupFilter;
    private RecyclerView recyclerView;
    private PengaduanPublikAdapter adapter;
    private ProgressBar progressBar;
    private TextView textViewPesan;

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

    private void inisialisasiView(View view) {
        Toolbar toolbar = view.findViewById(R.id.toolbarCari);
        ((AppCompatActivity) requireActivity()).setSupportActionBar(toolbar);

        searchView = view.findViewById(R.id.searchViewOperator);
        chipGroupFilter = view.findViewById(R.id.chipGroupFilterStatus);
        recyclerView = view.findViewById(R.id.recyclerViewCari);
        progressBar = view.findViewById(R.id.progressBarCari);
        textViewPesan = view.findViewById(R.id.textViewPesanCari);

        // Setup SearchView hint - metode yang lebih reliable
        setupSearchViewHint();

        // Mengubah ukuran font di dalam EditText SearchView
        @SuppressLint("DiscouragedApi") int searchEditTextId = getResources().getIdentifier("androidx.appcompat:id/search_src_text", null, null);
        if (searchEditTextId != 0) {
            EditText searchEditText = searchView.findViewById(searchEditTextId);
            if (searchEditText != null) {
                searchEditText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
            }
        }

        // Memaksa SearchView untuk terbuka penuh
        searchView.setIconified(false);
    }

    // Method untuk setup hint
    private void setupSearchViewHint() {
        // Metode 1: Set hint secara programmatik
        searchView.setQueryHint("Cari berdasarkan kategori, deskripsi...");
    }

    private void setupViewModel() {
        OperatorRepository repository = new OperatorRepositoryImpl(requireContext());
        SearchPengaduanUseCase useCase = new SearchPengaduanUseCase(repository);
        CariViewModelFactory factory = new CariViewModelFactory(useCase);
        viewModel = new ViewModelProvider(this, factory).get(CariViewModel.class);
    }

    private void setupRecyclerView() {
        adapter = new PengaduanPublikAdapter(new ArrayList<>(), requireContext());
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(adapter);
    }

    private void setupListeners() {
        // Listener untuk SearchView dengan delay (debounce)
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                // Hentikan handler jika ada, lalu langsung cari
                searchHandler.removeCallbacks(searchRunnable);
                performSearch(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                // Hentikan pencarian sebelumnya, jadwalkan yang baru dalam 500ms
                searchHandler.removeCallbacks(searchRunnable);
                searchRunnable = () -> performSearch(newText);
                searchHandler.postDelayed(searchRunnable, 500); // Delay 500ms
                return true;
            }
        });

        // Listener untuk filter ChipGroup
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

        adapter.setOnItemClickListener(item -> {
            Intent intent = new Intent(requireActivity(), DetailPengaduanOperatorActivity.class);
            intent.putExtra(DetailPengaduanOperatorActivity.EXTRA_ID_PENGADUAN, item.getIdPengaduan());
            startActivity(intent);
        });
    }

    private void performSearch(String keyword) {
        currentKeyword = keyword;
        Log.d(TAG, "Mencari dengan keyword: '" + currentKeyword + "', status: '" + currentStatusFilter + "'");
        viewModel.cariPengaduan(currentKeyword, currentStatusFilter);
    }

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
                // Tampilkan pesan hanya jika tidak sedang loading
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
