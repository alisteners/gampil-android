package com.alisteners.pengaduan.presentation.operator.dashboard;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.alisteners.pengaduan.R;
import com.alisteners.pengaduan.data.repository.OperatorRepositoryImpl;
import com.alisteners.pengaduan.domain.operator.repository.OperatorRepository;
import com.alisteners.pengaduan.domain.operator.usecase.GetPengaduanOperatorUseCase;
import com.alisteners.pengaduan.presentation.masyarakat.beranda.PengaduanPublikAdapter;
import com.alisteners.pengaduan.presentation.operator.detail.DetailPengaduanOperatorActivity;

import java.util.ArrayList;

/**
 * Fragment yang bertanggung jawab untuk menampilkan daftar pengaduan berdasarkan status tertentu.
 * Fragment ini dirancang untuk dapat digunakan kembali (reusable) oleh ViewPager di
 * {@link DashboardOperatorFragment} untuk setiap tab (Baru, Diproses, Selesai).
 * Juga dilengkapi dengan BroadcastReceiver untuk me-refresh data secara otomatis
 * ketika ada pembaruan dari halaman lain.
 */
public class PengaduanListOperatorFragment extends Fragment {

    private static final String TAG = "PengaduanListFragment";
    private static final String ARG_STATUS = "arg_status";
    private String statusFilter;

    // --- Deklarasi View & Komponen ---
    private OperatorListViewModel viewModel;
    private RecyclerView recyclerView;
    private PengaduanPublikAdapter adapter;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ProgressBar progressBar;
    private TextView textViewPesan;
    private BroadcastReceiver updateReceiver; // Receiver untuk sinyal update

    /**
     * Metode pabrik (factory method) untuk membuat instance baru dari fragment ini.
     * Menggunakan metode ini adalah praktik terbaik untuk meneruskan argumen ke fragment.
     *
     * @param status Status pengaduan yang akan ditampilkan oleh fragment ini (misal: "menunggu").
     * @return Instance baru dari PengaduanListOperatorFragment.
     */
    public static PengaduanListOperatorFragment newInstance(String status) {
        PengaduanListOperatorFragment fragment = new PengaduanListOperatorFragment();
        Bundle args = new Bundle();
        args.putString(ARG_STATUS, status);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            statusFilter = getArguments().getString(ARG_STATUS);
        }

        // Inisialisasi BroadcastReceiver untuk mendengarkan sinyal "PENGADUAN_UPDATED"
        updateReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (isResumed() && viewModel != null) {
                    Log.d(TAG, "Menerima sinyal update untuk status: " + statusFilter + ". Memuat ulang...");
                    swipeRefreshLayout.setRefreshing(true);
                    viewModel.loadPengaduanByStatus(statusFilter);
                }
            }
        };
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_pengaduan_list_operator, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        inisialisasiView(view);
        setupRecyclerView();
        setupViewModel();
        setupListeners();
        observeViewModel();
    }

    /**
     * Mendaftarkan BroadcastReceiver dan memuat data awal saat fragment menjadi terlihat.
     */
    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume: Memuat data untuk status: " + statusFilter);
        viewModel.loadPengaduanByStatus(statusFilter);
        LocalBroadcastManager.getInstance(requireContext()).registerReceiver(
                updateReceiver, new IntentFilter("com.alisteners.pengaduan.PENGADUAN_UPDATED")
        );
    }

    /**
     * Melepaskan pendaftaran BroadcastReceiver saat fragment tidak lagi terlihat
     * untuk mencegah kebocoran memori (memory leak).
     */
    @Override
    public void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(updateReceiver);
    }

    /**
     * Menginisialisasi semua referensi ke komponen view dari layout XML.
     */
    private void inisialisasiView(View view) {
        recyclerView = view.findViewById(R.id.recyclerViewOperator);
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshOperatorList);
        progressBar = view.findViewById(R.id.progressBarOperatorList);
        textViewPesan = view.findViewById(R.id.textViewPesanOperator);
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
     * Menginisialisasi ViewModel menggunakan ViewModelFactory.
     */
    private void setupViewModel() {
        OperatorRepository repository = new OperatorRepositoryImpl(requireActivity().getApplicationContext());
        GetPengaduanOperatorUseCase useCase = new GetPengaduanOperatorUseCase(repository);
        OperatorListViewModelFactory factory = new OperatorListViewModelFactory(useCase);
        viewModel = new ViewModelProvider(this, factory).get(OperatorListViewModel.class);
    }

    /**
     * Mengatur semua listener untuk interaksi pengguna, seperti swipe-to-refresh dan klik item.
     */
    private void setupListeners() {
        swipeRefreshLayout.setOnRefreshListener(() -> {
            Log.d(TAG, "Swipe to refresh untuk status: " + statusFilter);
            viewModel.loadPengaduanByStatus(statusFilter);
        });

        adapter.setOnItemClickListener(item -> {
            Intent intent = new Intent(requireActivity(), DetailPengaduanOperatorActivity.class);
            intent.putExtra(DetailPengaduanOperatorActivity.EXTRA_ID_PENGADUAN, item.getIdPengaduan());
            startActivity(intent);
        });
    }

    /**
     * Mengamati LiveData dari ViewModel untuk memperbarui UI secara reaktif
     * terhadap perubahan data, status loading, atau pesan error.
     */
    @SuppressLint("SetTextI18n")
    private void observeViewModel() {
        viewModel.isLoading.observe(getViewLifecycleOwner(), isLoading -> {
            if (isLoading != null) {
                // Tampilkan progress bar hanya jika tidak sedang swipe-refresh
                if (isLoading && !swipeRefreshLayout.isRefreshing()) {
                    progressBar.setVisibility(View.VISIBLE);
                } else {
                    progressBar.setVisibility(View.GONE);
                }
                // Hentikan animasi swipe-refresh jika proses loading sudah selesai
                if(!isLoading) {
                    swipeRefreshLayout.setRefreshing(false);
                }
            }
        });

        viewModel.pengaduanList.observe(getViewLifecycleOwner(), list -> {
            boolean isEmpty = (list == null || list.isEmpty());
            textViewPesan.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
            recyclerView.setVisibility(isEmpty ? View.GONE : View.VISIBLE);

            if (!isEmpty) {
                adapter.updateData(list);
            } else {
                textViewPesan.setText("Belum ada pengaduan dengan status '" + statusFilter + "'.");
            }
        });

        viewModel.errorMessage.observe(getViewLifecycleOwner(), error -> {
            if (error != null && !error.isEmpty()) {
                textViewPesan.setText("Gagal memuat data: " + error);
                textViewPesan.setVisibility(View.VISIBLE);
                recyclerView.setVisibility(View.GONE);
            }
        });
    }
}