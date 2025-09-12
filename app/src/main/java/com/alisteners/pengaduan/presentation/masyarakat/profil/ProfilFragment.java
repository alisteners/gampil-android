package com.alisteners.pengaduan.presentation.masyarakat.profil;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Fragment yang bertanggung jawab untuk menampilkan halaman profil pengguna.
 * Halaman ini menampilkan data diri pengguna (nama, email, telepon), statistik singkat
 * tentang laporan mereka, serta menyediakan menu untuk mengubah password, melihat
 * info aplikasi, dan logout.
 */
public class ProfilFragment extends Fragment {

    private static final String TAG = "ProfilFragment";

    // --- Deklarasi View ---
    private TextView textViewNama;
    private TextView textViewEmail;
    private TextView textViewTelepon;
    private TextView textViewStatMenunggu, textViewStatDiproses, textViewStatSelesai;

    // --- Deklarasi Komponen Lain ---
    private RequestQueue requestQueue;
    private SharedPreferences sharedPreferences;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profil, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        inisialisasiView(view);
        setupListeners(view);

        // Tampilkan data profil dari SharedPreferences
        tampilkanDataProfil();
        // Muat data statistik dari server
        loadStatistik();
    }

    /**
     * Menginisialisasi semua referensi ke komponen view dan dependensi lainnya.
     */
    private void inisialisasiView(@NonNull View view) {
        textViewNama = view.findViewById(R.id.textViewProfilNama);
        textViewEmail = view.findViewById(R.id.textViewProfilEmail);
        textViewTelepon = view.findViewById(R.id.textViewProfilTelepon);
        textViewStatMenunggu = view.findViewById(R.id.textViewStatMenunggu);
        textViewStatDiproses = view.findViewById(R.id.textViewStatDiproses);
        textViewStatSelesai = view.findViewById(R.id.textViewStatSelesai);

        requestQueue = Volley.newRequestQueue(requireContext());
        sharedPreferences = requireActivity().getSharedPreferences(LoginActivity.SHARED_PREFS, Context.MODE_PRIVATE);

        Toolbar toolbar = view.findViewById(R.id.toolbarProfil);
        ((AppCompatActivity) requireActivity()).setSupportActionBar(toolbar);
    }

    /**
     * Mengatur semua listener untuk interaksi pengguna pada fragment ini.
     */
    private void setupListeners(@NonNull View view) {
        view.findViewById(R.id.buttonLogout).setOnClickListener(v -> tampilkanDialogLogout());
        view.findViewById(R.id.menuUbahPassword).setOnClickListener(v -> startActivity(new Intent(requireContext(), UbahPasswordActivity.class)));
        view.findViewById(R.id.menuTentangAplikasi).setOnClickListener(v -> tampilkanDialogTentang());
    }

    /**
     * Menampilkan data profil pengguna (nama, email, telepon) yang disimpan
     * di SharedPreferences ke komponen TextView yang sesuai.
     */
    private void tampilkanDataProfil() {
        String namaLengkap = sharedPreferences.getString(LoginActivity.KEY_NAMA_LENGKAP, "Nama Pengguna");
        String email = sharedPreferences.getString(LoginActivity.KEY_EMAIL, "email@pengguna.com");
        String noTelepon = sharedPreferences.getString(LoginActivity.KEY_NO_TELEPON, "-");

        textViewNama.setText(namaLengkap);
        textViewEmail.setText(email);
        textViewTelepon.setText(noTelepon);
    }

    /**
     * Mengambil dan menampilkan data statistik pengaduan (menunggu, diproses, selesai)
     * untuk pengguna yang sedang login dari server.
     */
    private void loadStatistik() {
        int idUser = sharedPreferences.getInt(LoginActivity.KEY_ID_USER, -1);
        if (idUser == -1) {
            Log.e(TAG, "ID User tidak ditemukan untuk memuat statistik.");
            return;
        }

        // Tampilkan placeholder "..." saat data sedang dimuat
        textViewStatMenunggu.setText("...");
        textViewStatDiproses.setText("...");
        textViewStatSelesai.setText("...");

        String url = ApiConfig.URL_GET_STATISTIK_USER + "?idUser=" + idUser;

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        if ("success".equals(response.getString("status"))) {
                            JSONObject data = response.getJSONObject("data");
                            textViewStatMenunggu.setText(String.valueOf(data.getInt("jumlah_menunggu")));
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
     * Menampilkan dialog "Tentang Aplikasi" yang berisi informasi nama aplikasi,
     * deskripsi singkat, pengembang, dan versi aplikasi.
     */
    private void tampilkanDialogTentang() {
        String versiAplikasi = "Versi ";
        try {
            versiAplikasi += requireActivity().getPackageManager()
                    .getPackageInfo(requireActivity().getPackageName(), 0).versionName;
        } catch (Exception e) {
            versiAplikasi += "1.0"; // Fallback jika gagal mendapatkan versi
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
     * Jika pengguna menekan "Logout", sesi akan dihapus dan pengguna akan diarahkan
     * kembali ke halaman Login.
     */
    private void tampilkanDialogLogout() {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Konfirmasi Logout")
                .setMessage("Apakah Anda yakin ingin keluar dari akun Anda?")
                .setNegativeButton("Batal", null)
                .setPositiveButton("Logout", (dialog, which) -> {
                    // Hapus semua data sesi dari SharedPreferences
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.clear();
                    editor.apply();

                    // Arahkan ke LoginActivity dan hapus semua activity sebelumnya dari back stack
                    Intent intent = new Intent(getActivity(), LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    requireActivity().finish();
                })
                .show();
    }
}