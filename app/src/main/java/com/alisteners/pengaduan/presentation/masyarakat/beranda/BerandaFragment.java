package com.alisteners.pengaduan.presentation.masyarakat.beranda;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.alisteners.pengaduan.R;
import com.alisteners.pengaduan.data.repository.PengaduanRepositoryImpl;
import com.alisteners.pengaduan.domain.repository.PengaduanRepository;
import com.alisteners.pengaduan.domain.usecase.GetPengaduanPublikUseCase;
import com.alisteners.pengaduan.presentation.masyarakat.detailpengaduan.DetailPengaduanActivity;
import com.google.android.material.chip.ChipGroup;

import java.util.ArrayList;

public class BerandaFragment extends Fragment {

    private static final String TAG = "BerandaFragment";

    private BerandaViewModel viewModel;
    private RecyclerView recyclerView;
    private PengaduanPublikAdapter adapter;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ProgressBar progressBar;
    private LinearLayout layoutPesan;
    private TextView textViewPesanMain;
    private ImageView imageViewPesanIcon;
    private Button buttonCobaLagi;
    private TextView textViewJumlahLaporan;

    // Variabel untuk Filter
    private ChipGroup chipGroupFilter;
    private String filterStatusAktif = "semua";

    public BerandaFragment() { /* Diperlukan konstruktor kosong */ }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_beranda, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setupToolbar(view);
        inisialisasiView(view);
        setupRecyclerView();
        setupViewModel();
        setupListeners();
        observeViewModel();

        if (savedInstanceState == null) {
            loadData(true);
        }
    }

    private void setupToolbar(View view) {
        Toolbar toolbar = view.findViewById(R.id.toolbarMain);
        ((AppCompatActivity) requireActivity()).setSupportActionBar(toolbar);
    }

    private void inisialisasiView(View view) {
        recyclerView = view.findViewById(R.id.recyclerViewPublik);
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshMain);
        progressBar = view.findViewById(R.id.progressBarMain);
        layoutPesan = view.findViewById(R.id.layoutPesanMain);
        textViewPesanMain = view.findViewById(R.id.textViewPesanMain);
        imageViewPesanIcon = view.findViewById(R.id.imageViewPesanIcon);
        buttonCobaLagi = view.findViewById(R.id.buttonCobaLagi);
        textViewJumlahLaporan = view.findViewById(R.id.textViewJumlahLaporan);
        chipGroupFilter = view.findViewById(R.id.chipGroupFilter);
    }

    private void setupRecyclerView() {
        adapter = new PengaduanPublikAdapter(new ArrayList<>(), requireContext());
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(adapter);
    }

    private void setupViewModel() {
        PengaduanRepository repository = new PengaduanRepositoryImpl(requireActivity().getApplicationContext());
        GetPengaduanPublikUseCase useCase = new GetPengaduanPublikUseCase(repository);
        BerandaViewModelFactory factory = new BerandaViewModelFactory(useCase);
        viewModel = new ViewModelProvider(this, factory).get(BerandaViewModel.class);
    }

    private void setupListeners() {
        swipeRefreshLayout.setOnRefreshListener(() -> loadData(false));

        adapter.setOnItemClickListener(item -> {
            Intent intent = new Intent(requireActivity(), DetailPengaduanActivity.class);
            intent.putExtra(DetailPengaduanActivity.EXTRA_PENGADUAN_ID, item.getIdPengaduan());
            startActivity(intent);
        });

        chipGroupFilter.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.chipSemua) {
                filterStatusAktif = "semua";
            } else if (checkedId == R.id.chipMenunggu) {
                filterStatusAktif = "menunggu";
            } else if (checkedId == R.id.chipDiproses) {
                filterStatusAktif = "diproses";
            } else if (checkedId == R.id.chipSelesai) {
                filterStatusAktif = "selesai";
            }
            Log.d(TAG, "Filter diubah menjadi: " + filterStatusAktif);
            loadData(true);
        });

        buttonCobaLagi.setOnClickListener(v -> loadData(true));
    }

    private void loadData(boolean showLoading) {
        viewModel.loadPengaduanPublik(filterStatusAktif, showLoading);
    }

    private void observeViewModel() {
        viewModel.isLoading.observe(getViewLifecycleOwner(), isLoading -> {
            if (isLoading == null) return;
            // Saat loading, sembunyikan semuanya kecuali progress bar
            if (isLoading && !swipeRefreshLayout.isRefreshing()) {
                progressBar.setVisibility(View.VISIBLE);
                recyclerView.setVisibility(View.GONE);
                layoutPesan.setVisibility(View.GONE);
                textViewJumlahLaporan.setVisibility(View.GONE);
            } else if (!isLoading) {
                progressBar.setVisibility(View.GONE);
                swipeRefreshLayout.setRefreshing(false);
            }
        });

        viewModel.pengaduanList.observe(getViewLifecycleOwner(), pengaduanList -> {
            if (pengaduanList != null) {
                adapter.updateData(pengaduanList);

                if (pengaduanList.isEmpty()) {
                    // Jika KOSONG, tampilkan pesan dan sembunyikan yang lain
                    recyclerView.setVisibility(View.GONE);
                    textViewJumlahLaporan.setVisibility(View.GONE);
                    layoutPesan.setVisibility(View.VISIBLE);
                    textViewPesanMain.setText(R.string.pesan_tidak_ada_laporan_publik_filter);
                    imageViewPesanIcon.setImageResource(R.drawable.ic_search_off);
                    buttonCobaLagi.setVisibility(View.GONE);
                } else {
                    // Jika ADA DATA, tampilkan daftar dan jumlahnya
                    recyclerView.setVisibility(View.VISIBLE);
                    layoutPesan.setVisibility(View.GONE);
                    String jumlahText = "Menampilkan " + pengaduanList.size() + " Pengaduan Kerusakan Infrastruktur Desa";
                    textViewJumlahLaporan.setText(jumlahText);
                    textViewJumlahLaporan.setVisibility(View.VISIBLE); // Ini kuncinya
                }
            }
        });

        viewModel.errorMessage.observe(getViewLifecycleOwner(), error -> {
            if (error != null && !error.isEmpty()) {
                Log.e(TAG, "Observer: Menerima pesan error: " + error);
                recyclerView.setVisibility(View.GONE);
                textViewJumlahLaporan.setVisibility(View.GONE); // Sembunyikan juga saat error
                layoutPesan.setVisibility(View.VISIBLE);
                textViewPesanMain.setText(error);
                imageViewPesanIcon.setImageResource(R.drawable.ic_error_outline);
                buttonCobaLagi.setVisibility(View.VISIBLE);
            }
        });
    }
}