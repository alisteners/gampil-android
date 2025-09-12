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

/**
 * Fragment yang bertanggung jawab untuk menampilkan halaman Beranda (Home).
 * Halaman ini menampilkan daftar pengaduan publik yang dapat difilter berdasarkan status.
 * Fragment ini mengimplementasikan arsitektur MVVM dengan berkomunikasi melalui {@link BerandaViewModel}.
 */
public class BerandaFragment extends Fragment {

    private static final String TAG = "BerandaFragment";

    private BerandaViewModel viewModel;
    private RecyclerView recyclerView;
    private PengaduanPublikAdapter adapter;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ProgressBar progressBar;
    private LinearLayout layoutPesan; // Layout untuk menampilkan pesan (error/kosong)
    private TextView textViewPesanMain;
    private ImageView imageViewPesanIcon;
    private Button buttonCobaLagi;
    private TextView textViewJumlahLaporan;
    private ChipGroup chipGroupFilter;
    private String filterStatusAktif = "semua"; // Status filter default

    /**
     * Konstruktor default yang diperlukan untuk Fragment.
     */
    public BerandaFragment() { /* Diperlukan konstruktor kosong */ }

    /**
     * Dipanggil untuk membuat dan meng-inflate layout view dari fragment.
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_beranda, container, false);
    }

    /**
     * Dipanggil setelah view dari fragment selesai dibuat.
     * Metode ini adalah tempat utama untuk inisialisasi view, ViewModel, listener,
     * dan memuat data awal.
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setupToolbar(view);
        inisialisasiView(view);
        setupRecyclerView();
        setupViewModel();
        setupListeners();
        observeViewModel();
        setupFragmentResultListener();

        // Memuat data hanya saat fragment pertama kali dibuat, bukan saat re-creation.
        if (savedInstanceState == null) {
            loadData(true);
        }
    }

    /**
     * Mengatur Toolbar sebagai ActionBar untuk Activity induk.
     */
    private void setupToolbar(View view) {
        Toolbar toolbar = view.findViewById(R.id.toolbarMain);
        ((AppCompatActivity) requireActivity()).setSupportActionBar(toolbar);
    }

    /**
     * Menginisialisasi semua komponen view dari layout.
     */
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

    /**
     * Mengatur RecyclerView, termasuk adapter dan layout manager.
     */
    private void setupRecyclerView() {
        adapter = new PengaduanPublikAdapter(new ArrayList<>(), requireContext());
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(adapter);
    }

    /**
     * Menginisialisasi ViewModel menggunakan ViewModelFactory untuk menyuntikkan dependensi (UseCase).
     */
    private void setupViewModel() {
        PengaduanRepository repository = new PengaduanRepositoryImpl(requireActivity().getApplicationContext());
        GetPengaduanPublikUseCase useCase = new GetPengaduanPublikUseCase(repository);
        BerandaViewModelFactory factory = new BerandaViewModelFactory(useCase);
        viewModel = new ViewModelProvider(this, factory).get(BerandaViewModel.class);
    }

    /**
     * Mengatur semua listener untuk interaksi pengguna, seperti swipe-to-refresh,
     * klik item, dan perubahan filter.
     */
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
            loadData(true); // Muat ulang data dengan filter baru dan tampilkan loading.
        });

        buttonCobaLagi.setOnClickListener(v -> loadData(true));
    }

    /**
     * Memulai proses pemuatan data dari ViewModel.
     * @param showLoading Menentukan apakah ProgressBar awal harus ditampilkan.
     * Bernilai false saat melakukan swipe-to-refresh.
     */
    private void loadData(boolean showLoading) {
        viewModel.loadPengaduanPublik(filterStatusAktif, showLoading);
    }

    /**
     * Mengamati LiveData dari ViewModel untuk memperbarui UI secara reaktif
     * ketika ada perubahan data, status loading, atau pesan error.
     */
    private void observeViewModel() {
        viewModel.isLoading.observe(getViewLifecycleOwner(), isLoading -> {
            if (isLoading == null) return;
            if (isLoading && !swipeRefreshLayout.isRefreshing()) {
                // Tampilkan progress bar utama jika sedang loading (bukan dari swipe refresh)
                progressBar.setVisibility(View.VISIBLE);
                recyclerView.setVisibility(View.GONE);
                layoutPesan.setVisibility(View.GONE);
                textViewJumlahLaporan.setVisibility(View.GONE);
            } else if (!isLoading) {
                // Sembunyikan semua indikator loading setelah selesai.
                progressBar.setVisibility(View.GONE);
                swipeRefreshLayout.setRefreshing(false);
            }
        });

        viewModel.pengaduanList.observe(getViewLifecycleOwner(), pengaduanList -> {
            if (pengaduanList != null) {
                adapter.updateData(pengaduanList);

                if (pengaduanList.isEmpty()) {
                    // Kondisi jika tidak ada data yang sesuai dengan filter.
                    recyclerView.setVisibility(View.GONE);
                    textViewJumlahLaporan.setVisibility(View.GONE);
                    layoutPesan.setVisibility(View.VISIBLE);
                    textViewPesanMain.setText(R.string.pesan_tidak_ada_laporan_publik_filter);
                    imageViewPesanIcon.setImageResource(R.drawable.ic_search_off);
                    buttonCobaLagi.setVisibility(View.GONE);
                } else {
                    // Kondisi jika data berhasil dimuat.
                    recyclerView.setVisibility(View.VISIBLE);
                    layoutPesan.setVisibility(View.GONE);
                    String jumlahText = "Menampilkan " + pengaduanList.size() + " Laporan";
                    textViewJumlahLaporan.setText(jumlahText);
                    textViewJumlahLaporan.setVisibility(View.VISIBLE);
                }
            }
        });

        viewModel.errorMessage.observe(getViewLifecycleOwner(), error -> {
            if (error != null && !error.isEmpty()) {
                // Kondisi jika terjadi error saat mengambil data.
                Log.e(TAG, "Observer: Menerima pesan error: " + error);
                recyclerView.setVisibility(View.GONE);
                textViewJumlahLaporan.setVisibility(View.GONE);
                layoutPesan.setVisibility(View.VISIBLE);
                textViewPesanMain.setText(error);
                imageViewPesanIcon.setImageResource(R.drawable.ic_error_outline);
                buttonCobaLagi.setVisibility(View.VISIBLE);
            }
        });
    }

    /**
     * Mengatur listener untuk menerima sinyal dari fragment lain (misalnya setelah membuat pengaduan baru).
     * Ketika sinyal diterima, data di halaman ini akan dimuat ulang secara otomatis.
     */
    private void setupFragmentResultListener() {
        getParentFragmentManager().setFragmentResultListener("pengaduan_baru", this, (requestKey, bundle) -> {
            Log.d(TAG, "Menerima sinyal 'pengaduan_baru', memuat ulang data.");
            if (chipGroupFilter != null) {
                chipGroupFilter.check(R.id.chipSemua); // Kembali ke filter "Semua".
            }
            loadData(true);
        });
    }
}