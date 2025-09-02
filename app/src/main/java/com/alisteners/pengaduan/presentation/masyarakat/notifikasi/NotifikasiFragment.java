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
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NotifikasiFragment extends Fragment {

    private static final String TAG = "NotifikasiFragment";

    private RecyclerView recyclerViewNotifikasi;
    private ProgressBar progressBarNotifikasi;
    private SwipeRefreshLayout swipeRefreshLayout;
    private LinearLayout layoutPesanKosong;

    private NotifikasiAdapter notifikasiAdapter;
    private List<Notifikasi> notifikasiList;
    private RequestQueue requestQueue;
    private SharedPreferences sharedPreferences;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
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
        setupItemTouchHelper();
        swipeRefreshLayout.setOnRefreshListener(() -> fetchNotifikasi(true));

        fetchNotifikasi(false);
    }

    private void setupToolbar(View view) {
        Toolbar toolbar = view.findViewById(R.id.toolbarNotifikasi);
        ((AppCompatActivity) requireActivity()).setSupportActionBar(toolbar);
    }

    private void inisialisasiView(View view) {
        recyclerViewNotifikasi = view.findViewById(R.id.recyclerViewNotifikasi);
        progressBarNotifikasi = view.findViewById(R.id.progressBarNotifikasi);
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayoutNotifikasi);
        layoutPesanKosong = view.findViewById(R.id.layoutPesanKosong);
    }

    private void fetchNotifikasi(final boolean isFromSwipe) {
        if (!isFromSwipe) progressBarNotifikasi.setVisibility(View.VISIBLE);
        recyclerViewNotifikasi.setVisibility(View.GONE);
        layoutPesanKosong.setVisibility(View.GONE);

        int idUserInt = sharedPreferences.getInt(LoginActivity.KEY_ID_USER, 0);
        if (idUserInt == 0) {
            progressBarNotifikasi.setVisibility(View.GONE);
            if (isFromSwipe) swipeRefreshLayout.setRefreshing(false);
            layoutPesanKosong.setVisibility(View.VISIBLE);
            return;
        }

        String url = ApiConfig.URL_GET_NOTIFIKASI + "?idUser=" + idUserInt;

        @SuppressLint("NotifyDataSetChanged") JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        if ("success".equals(response.getString("status"))) {
                            JSONArray dataArray = response.getJSONArray("data");
                            notifikasiList.clear();

                            if (dataArray.length() == 0) {
                                layoutPesanKosong.setVisibility(View.VISIBLE);
                                recyclerViewNotifikasi.setVisibility(View.GONE);
                            } else {
                                layoutPesanKosong.setVisibility(View.GONE);
                                recyclerViewNotifikasi.setVisibility(View.VISIBLE);
                                tandaiSemuaDilihat();

                                Gson gson = new Gson();
                                for (int i = 0; i < dataArray.length(); i++) {
                                    JSONObject notifObject = dataArray.getJSONObject(i);
                                    Notifikasi notifikasi = gson.fromJson(notifObject.toString(), Notifikasi.class);
                                    notifikasiList.add(notifikasi);
                                }
                                notifikasiAdapter.notifyDataSetChanged();
                            }
                        } else {
                            layoutPesanKosong.setVisibility(View.VISIBLE);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        layoutPesanKosong.setVisibility(View.VISIBLE);
                    } finally {
                        progressBarNotifikasi.setVisibility(View.GONE);
                        if (isFromSwipe) swipeRefreshLayout.setRefreshing(false);
                    }
                },
                error -> {
                    Log.e(TAG, "Volley Error: " + error.toString());
                    layoutPesanKosong.setVisibility(View.VISIBLE);
                    progressBarNotifikasi.setVisibility(View.GONE);
                    if (isFromSwipe) swipeRefreshLayout.setRefreshing(false);
                });
        requestQueue.add(jsonObjectRequest);
    }

    private void setupRecyclerView() {
        notifikasiList = new ArrayList<>();
        notifikasiAdapter = new NotifikasiAdapter(requireContext(), notifikasiList);
        recyclerViewNotifikasi.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerViewNotifikasi.setAdapter(notifikasiAdapter);
        notifikasiAdapter.setOnItemClickListener(notifikasi -> {
            Intent intent = new Intent(getActivity(), DetailPengaduanActivity.class);
            intent.putExtra(DetailPengaduanActivity.EXTRA_PENGADUAN_ID, notifikasi.getIdPengaduan());
            startActivity(intent);
        });
    }

    private void setupItemTouchHelper() {
        ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                Notifikasi notifikasiDihapus = notifikasiList.get(position);

                // Kirim idNotifikasi, bukan idPengaduan
                int idNotifikasi = notifikasiDihapus.getIdNotifikasi();
                tandaiSatuDilihat(idNotifikasi);

                notifikasiList.remove(position);
                notifikasiAdapter.notifyItemRemoved(position);
                if (getActivity() instanceof MainActivity) {
                    ((MainActivity) getActivity()).updateNotificationBadge(notifikasiList.size());
                }
                if (notifikasiList.isEmpty()){
                    layoutPesanKosong.setVisibility(View.VISIBLE);
                }
            }
        };
        new ItemTouchHelper(simpleCallback).attachToRecyclerView(recyclerViewNotifikasi);
    }

    private void tandaiSemuaDilihat() {
        int idUserInt = sharedPreferences.getInt(LoginActivity.KEY_ID_USER, 0);
        if (idUserInt == 0) return;

        StringRequest stringRequest = new StringRequest(Request.Method.POST, ApiConfig.URL_TANDAI_SEMUA_DILIHAT,
                response -> Log.d(TAG, "Respon tandai semua dilihat: " + response),
                error -> Log.e(TAG, "Error tandai semua dilihat: " + error.toString())) {
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

    private void tandaiSatuDilihat(int idNotifikasi) {
        StringRequest stringRequest = new StringRequest(Request.Method.POST, ApiConfig.URL_TANDAI_SATU_DILIHAT,
                response -> Log.d(TAG, "Respon tandai satu dilihat: " + response),
                error -> Log.e(TAG, "Error tandai satu dilihat: " + error.toString())) {
            @NonNull
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                // Kirim parameter idNotifikasi
                params.put("idNotifikasi", String.valueOf(idNotifikasi));
                return params;
            }
        };
        requestQueue.add(stringRequest);
    }
}