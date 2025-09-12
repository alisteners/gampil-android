package com.alisteners.pengaduan.presentation.operator.profiloperator;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import com.alisteners.pengaduan.presentation.login.LoginActivity;
import com.alisteners.pengaduan.R;
import com.alisteners.pengaduan.data.remote.config.ApiConfig;
import com.alisteners.pengaduan.presentation.masyarakat.profil.UbahPasswordActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Fragment yang bertanggung jawab untuk menampilkan halaman profil untuk pengguna operator.
 * Halaman ini menampilkan data diri operator, statistik kinerjanya, serta menyediakan
 * menu untuk mengubah password, melihat info aplikasi, dan logout.
 */
public class ProfilOperatorFragment extends Fragment {
    private static final String TAG = "ProfilOperatorFragment";

    // --- Deklarasi View ---
    private TextView textViewNama;
    private TextView textViewEmail;
    private TextView textViewTelepon;
    private TextView textViewStatDitangani, textViewStatDiproses, textViewStatSelesai;

    // --- Deklarasi Komponen Lain ---
    private RequestQueue requestQueue;
    private SharedPreferences sharedPreferences;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profil_operator, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        inisialisasiView(view);
        setupListeners(view);

        tampilkanDataProfil();
        loadStatistikOperator();
    }

    /**
     * Menginisialisasi semua referensi ke komponen view dan dependensi lainnya.
     */
    private void inisialisasiView(@NonNull View view) {
        textViewNama = view.findViewById(R.id.textViewProfilNamaOperator);
        textViewEmail = view.findViewById(R.id.textViewProfilEmailOperator);
        textViewTelepon = view.findViewById(R.id.textViewProfilTeleponOperator);
        textViewStatDitangani = view.findViewById(R.id.textViewStatMenungguOperator);
        textViewStatDiproses = view.findViewById(R.id.textViewStatDiprosesOperator);
        textViewStatSelesai = view.findViewById(R.id.textViewStatSelesaiOperator);

        requestQueue = Volley.newRequestQueue(requireContext());
        sharedPreferences = requireActivity().getSharedPreferences(LoginActivity.SHARED_PREFS, Context.MODE_PRIVATE);

        Toolbar toolbar = view.findViewById(R.id.toolbarProfilOperator);
        ((AppCompatActivity) requireActivity()).setSupportActionBar(toolbar);
    }

    /**
     * Mengatur semua listener untuk interaksi pengguna pada fragment ini.
     */
    private void setupListeners(@NonNull View view) {
        view.findViewById(R.id.buttonLogoutOperator).setOnClickListener(v -> tampilkanDialogLogout());
        view.findViewById(R.id.menuUbahPasswordOperator).setOnClickListener(v -> startActivity(new Intent(requireContext(), UbahPasswordActivity.class)));
        view.findViewById(R.id.menuTentangAplikasiOperator).setOnClickListener(v -> tampilkanDialogTentang());
    }

    /**
     * Menampilkan data profil operator (nama, email, telepon) yang disimpan
     * di SharedPreferences ke komponen TextView yang sesuai.
     */
    private void tampilkanDataProfil() {
        String namaLengkap = sharedPreferences.getString(LoginActivity.KEY_NAMA_LENGKAP, "Nama Operator");
        String email = sharedPreferences.getString(LoginActivity.KEY_EMAIL, "operator@desabandorasa.id");
        String noTelepon = sharedPreferences.getString(LoginActivity.KEY_NO_TELEPON, "-");

        textViewNama.setText(namaLengkap);
        textViewEmail.setText(email);
        textViewTelepon.setText(noTelepon);
    }

    /**
     * Mengambil dan menampilkan data statistik kinerja (ditangani, diproses, selesai)
     * untuk operator yang sedang login dari server.
     */
    private void loadStatistikOperator() {
        int idOperator = sharedPreferences.getInt(LoginActivity.KEY_ID_USER, -1);
        if (idOperator == -1) {
            Log.e(TAG, "ID Operator tidak ditemukan untuk memuat statistik.");
            return;
        }

        // Tampilkan placeholder "..." saat data sedang dimuat
        textViewStatDitangani.setText("...");
        textViewStatDiproses.setText("...");
        textViewStatSelesai.setText("...");

        String url = ApiConfig.URL_GET_STATISTIK_OPERATOR + "?idOperator=" + idOperator;

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        if ("success".equals(response.getString("status"))) {
                            JSONObject data = response.getJSONObject("data");
                            textViewStatDitangani.setText(String.valueOf(data.getInt("jumlah_ditangani")));
                            textViewStatDiproses.setText(String.valueOf(data.getInt("jumlah_diproses")));
                            textViewStatSelesai.setText(String.valueOf(data.getInt("jumlah_selesai")));
                        } else {
                            Toast.makeText(getContext(), "Gagal memuat statistik: " + response.getString("message"), Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        Log.e(TAG, "Error parsing JSON statistik: ", e);
                    }
                },
                error -> {
                    Log.e(TAG, "Volley Error statistik: ", error);
                    Toast.makeText(getContext(), "Error jaringan saat memuat statistik.", Toast.LENGTH_SHORT).show();
                });

        requestQueue.add(jsonObjectRequest);
    }

    /**
     * Menampilkan dialog "Tentang Aplikasi".
     */
    private void tampilkanDialogTentang() {
        String versiAplikasi = "Versi ";
        try {
            versiAplikasi += requireActivity().getPackageManager()
                    .getPackageInfo(requireActivity().getPackageName(), 0).versionName;
        } catch (Exception e) {
            versiAplikasi += "1.0"; // Fallback
        }

        String pesan = "GAMPIL (Gampang Lapor Infrastruktur) merupakan aplikasi pengaduan kerusakan infrastruktur desa berbasis Android " +
                "dengan menerapkan algoritma Run Length Encoding (RLE) Untuk Kompresi Gambar.\n\n" +
                "Developed by Ridho NF\n\n" +
                versiAplikasi;

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Tentang Aplikasi")
                .setMessage(pesan)
                .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                .show();
    }

    /**
     * Menampilkan dialog konfirmasi sebelum melakukan proses logout.
     */
    private void tampilkanDialogLogout() {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Konfirmasi Logout")
                .setMessage("Apakah Anda yakin ingin keluar dari akun Anda?")
                .setNegativeButton("Batal", null)
                .setPositiveButton("Logout", (dialog, which) -> {
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.clear();
                    editor.apply();

                    Intent intent = new Intent(getActivity(), LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    requireActivity().finish();
                })
                .show();
    }
}