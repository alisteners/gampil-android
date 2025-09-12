package com.alisteners.pengaduan.presentation.masyarakat.notifikasi;

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

import androidx.appcompat.widget.Toolbar;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.alisteners.pengaduan.presentation.login.LoginActivity;
import com.alisteners.pengaduan.MainActivity;
import com.alisteners.pengaduan.R;
import com.alisteners.pengaduan.data.remote.config.ApiConfig;
import com.alisteners.pengaduan.domain.entity.Notifikasi;
import com.alisteners.pengaduan.presentation.masyarakat.detailpengaduan.DetailPengaduanActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Fragment yang bertanggung jawab untuk menampilkan daftar notifikasi kepada pengguna.
 * Fragment ini menangani pengambilan data notifikasi dari server, menampilkannya dalam
 * RecyclerView, serta mengelola interaksi seperti swipe-to-dismiss dan klik untuk
 * melihat detail pengaduan.
 */
public class NotifikasiFragment extends Fragment {

    private static final String TAG = "NotifikasiFragment";

    // --- Deklarasi View & Adapter ---
    private RecyclerView recyclerViewNotifikasi;
    private ProgressBar progressBarNotifikasi;
    private SwipeRefreshLayout swipeRefreshLayout;
    private LinearLayout layoutPesanKosong;
    private NotifikasiAdapter notifikasiAdapter;
    private List<Notifikasi> notifikasiList;

    // --- Deklarasi Komponen Lain ---
    private RequestQueue requestQueue;
    private SharedPreferences sharedPreferences;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate layout untuk fragment ini
        return inflater.inflate(R.layout.fragment_notifikasi, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        inisialisasiView(view);
        requestQueue = Volley.newRequestQueue(requireContext());
        sharedPreferences = requireActivity().getSharedPreferences(LoginActivity.SHARED_PREFS, Context.MODE_PRIVATE);

        setupToolbar(view);
        setupRecyclerView();
        setupItemTouchHelper(); // Mengatur fungsi swipe-to-dismiss
        swipeRefreshLayout.setOnRefreshListener(() -> fetchNotifikasi(true));

        // Memuat data notifikasi saat fragment pertama kali dibuat
        fetchNotifikasi(false);
    }

    /**
     * Mengatur Toolbar sebagai ActionBar untuk Activity induk.
     */
    private void setupToolbar(View view) {
        Toolbar toolbar = view.findViewById(R.id.toolbarNotifikasi);
        ((AppCompatActivity) requireActivity()).setSupportActionBar(toolbar);
    }

    /**
     * Menginisialisasi semua referensi ke komponen view dari layout XML.
     */
    private void inisialisasiView(View view) {
        recyclerViewNotifikasi = view.findViewById(R.id.recyclerViewNotifikasi);
        progressBarNotifikasi = view.findViewById(R.id.progressBarNotifikasi);
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayoutNotifikasi);
        layoutPesanKosong = view.findViewById(R.id.layoutPesanKosong);
    }

    /**
     * Mengambil daftar notifikasi dari server menggunakan Volley.
     * @param isFromSwipe Menandakan apakah permintaan berasal dari aksi swipe-to-refresh.
     * Ini digunakan untuk menentukan apakah progress bar utama atau
     * indikator refresh dari swipe layout yang harus ditampilkan.
     */
    private void fetchNotifikasi(final boolean isFromSwipe) {
        if (!isFromSwipe) progressBarNotifikasi.setVisibility(View.VISIBLE);
        recyclerViewNotifikasi.setVisibility(View.GONE);
        layoutPesanKosong.setVisibility(View.GONE);

        int idUserInt = sharedPreferences.getInt(LoginActivity.KEY_ID_USER, 0);
        if (idUserInt == 0) {
            // Handle kasus jika ID pengguna tidak ditemukan (misalnya, sesi berakhir)
            progressBarNotifikasi.setVisibility(View.GONE);
            if (isFromSwipe) swipeRefreshLayout.setRefreshing(false);
            layoutPesanKosong.setVisibility(View.VISIBLE);
            return;
        }

        String url = ApiConfig.URL_GET_NOTIFIKASI + "?idUser=" + idUserInt;

        @SuppressLint("NotifyDataSetChanged")
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        if ("success".equals(response.getString("status"))) {
                            JSONArray dataArray = response.getJSONArray("data");
                            notifikasiList.clear();

                            if (dataArray.length() == 0) {
                                layoutPesanKosong.setVisibility(View.VISIBLE);
                            } else {
                                recyclerViewNotifikasi.setVisibility(View.VISIBLE);
                                tandaiSemuaDilihat(); // Otomatis tandai semua saat daftar dibuka

                                Gson gson = new Gson();
                                for (int i = 0; i < dataArray.length(); i++) {
                                    notifikasiList.add(gson.fromJson(dataArray.getJSONObject(i).toString(), Notifikasi.class));
                                }
                                notifikasiAdapter.notifyDataSetChanged();
                            }
                        } else {
                            layoutPesanKosong.setVisibility(View.VISIBLE);
                        }
                    } catch (JSONException e) {
                        Log.e(TAG, "JSONException saat parsing notifikasi: ", e);
                        layoutPesanKosong.setVisibility(View.VISIBLE);
                    } finally {
                        progressBarNotifikasi.setVisibility(View.GONE);
                        if (isFromSwipe) swipeRefreshLayout.setRefreshing(false);
                    }
                },
                error -> {
                    Log.e(TAG, "Volley Error saat fetch notifikasi: ", error);
                    layoutPesanKosong.setVisibility(View.VISIBLE);
                    progressBarNotifikasi.setVisibility(View.GONE);
                    if (isFromSwipe) swipeRefreshLayout.setRefreshing(false);
                });
        requestQueue.add(jsonObjectRequest);
    }

    /**
     * Mengatur RecyclerView, termasuk inisialisasi adapter, layout manager, dan item click listener.
     */
    private void setupRecyclerView() {
        notifikasiList = new ArrayList<>();
        notifikasiAdapter = new NotifikasiAdapter(requireContext(), notifikasiList);
        recyclerViewNotifikasi.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerViewNotifikasi.setAdapter(notifikasiAdapter);
        notifikasiAdapter.setOnItemClickListener(notifikasi -> {
            // Aksi saat item di-klik: buka detail pengaduan terkait
            Intent intent = new Intent(getActivity(), DetailPengaduanActivity.class);
            intent.putExtra(DetailPengaduanActivity.EXTRA_PENGADUAN_ID, notifikasi.getIdPengaduan());
            startActivity(intent);
        });
    }

    /**
     * Mengatur fungsionalitas swipe-to-dismiss pada item RecyclerView.
     * Saat item di-swipe, notifikasi akan ditandai sebagai "dilihat" di server.
     */
    private void setupItemTouchHelper() {
        ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false; // Tidak mendukung drag-and-drop
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                Notifikasi notifikasiDihapus = notifikasiList.get(position);

                tandaiSatuDilihat(notifikasiDihapus.getIdNotifikasi()); // Tandai satu notif sebagai dilihat

                // Hapus dari list dan update UI
                notifikasiList.remove(position);
                notifikasiAdapter.notifyItemRemoved(position);

                // Update badge di MainActivity
                if (getActivity() instanceof MainActivity) {
                    ((MainActivity) getActivity()).updateNotificationBadge(notifikasiList.size());
                }

                // Tampilkan pesan kosong jika list menjadi kosong
                if (notifikasiList.isEmpty()){
                    layoutPesanKosong.setVisibility(View.VISIBLE);
                }
            }
        };
        new ItemTouchHelper(simpleCallback).attachToRecyclerView(recyclerViewNotifikasi);
    }

    /**
     * Mengirim request ke server untuk menandai semua notifikasi sebagai "telah dilihat".
     * Ini dipanggil saat pengguna membuka halaman notifikasi.
     */
    private void tandaiSemuaDilihat() {
        int idUserInt = sharedPreferences.getInt(LoginActivity.KEY_ID_USER, 0);
        if (idUserInt == 0) return;

        StringRequest stringRequest = new StringRequest(Request.Method.POST, ApiConfig.URL_TANDAI_SEMUA_DILIHAT,
                response -> Log.d(TAG, "Respon tandai semua dilihat: " + response),
                error -> Log.e(TAG, "Error tandai semua dilihat: ", error)) {
            @NonNull
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("idUser", String.valueOf(idUserInt));
                return params;
            }
        };
        requestQueue.add(stringRequest);
    }

    /**
     * Mengirim request ke server untuk menandai satu notifikasi spesifik sebagai "telah dilihat".
     * Ini dipanggil saat pengguna melakukan swipe-to-dismiss pada sebuah item.
     * @param idNotifikasi ID dari notifikasi yang akan ditandai.
     */
    private void tandaiSatuDilihat(int idNotifikasi) {
        StringRequest stringRequest = new StringRequest(Request.Method.POST, ApiConfig.URL_TANDAI_SATU_DILIHAT,
                response -> Log.d(TAG, "Respon tandai satu dilihat: " + response),
                error -> Log.e(TAG, "Error tandai satu dilihat: ", error)) {
            @NonNull
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("idNotifikasi", String.valueOf(idNotifikasi));
                return params;
            }
        };
        requestQueue.add(stringRequest);
    }
}