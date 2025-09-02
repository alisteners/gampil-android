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

public class ProfilFragment extends Fragment {

    private static final String TAG = "ProfilFragment";

    private TextView textViewNama;
    private TextView textViewEmail;
    private TextView textViewTelepon;

    // Variabel untuk Statistik
    private TextView textViewStatMenunggu, textViewStatDiproses, textViewStatSelesai;
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

        // Inisialisasi komponen UI
        textViewNama = view.findViewById(R.id.textViewProfilNama);
        textViewEmail = view.findViewById(R.id.textViewProfilEmail);
        textViewTelepon = view.findViewById(R.id.textViewProfilTelepon);
        Button buttonLogout = view.findViewById(R.id.buttonLogout);
        TextView menuUbahPassword = view.findViewById(R.id.menuUbahPassword);
        TextView menuTentangAplikasi = view.findViewById(R.id.menuTentangAplikasi);

        // Inisialisasi komponen statistik
        textViewStatMenunggu = view.findViewById(R.id.textViewStatMenunggu);
        textViewStatDiproses = view.findViewById(R.id.textViewStatDiproses);
        textViewStatSelesai = view.findViewById(R.id.textViewStatSelesai);

        // Inisialisasi Volley dan SharedPreferences
        requestQueue = Volley.newRequestQueue(requireContext());
        sharedPreferences = requireActivity().getSharedPreferences(LoginActivity.SHARED_PREFS, Context.MODE_PRIVATE);

        Toolbar toolbar = view.findViewById(R.id.toolbarProfil);
        ((AppCompatActivity) requireActivity()).setSupportActionBar(toolbar);

        // Tampilkan data profil dari SharedPreferences
        tampilkanDataProfil();

        // Muat data statistik dari server
        loadStatistik();

        // Setup listener untuk tombol logout
        buttonLogout.setOnClickListener(v -> tampilkanDialogLogout());
        // Setup listener untuk tombol ubah password
        menuUbahPassword.setOnClickListener(v -> startActivity(new Intent(requireContext(), UbahPasswordActivity.class)));
        // Setup listener untuk tombol tentang aplikasi
        menuTentangAplikasi.setOnClickListener(v -> tampilkanDialogTentang());
    }

    private void tampilkanDataProfil() {
        String namaLengkap = sharedPreferences.getString(LoginActivity.KEY_NAMA_LENGKAP, "Nama Pengguna");
        String email = sharedPreferences.getString(LoginActivity.KEY_EMAIL, "email@pengguna.com");
        String noTelepon = sharedPreferences.getString(LoginActivity.KEY_NO_TELEPON, "-");

        textViewNama.setText(namaLengkap);
        textViewEmail.setText(email);
        textViewTelepon.setText(noTelepon);
    }

    private void loadStatistik() {
        int idUser = sharedPreferences.getInt(LoginActivity.KEY_ID_USER, -1);
        if (idUser == -1) {
            Log.e(TAG, "ID User tidak ditemukan untuk memuat statistik.");
            return;
        }

        // Tampilkan "..." saat loading
        textViewStatMenunggu.setText("...");
        textViewStatDiproses.setText("...");
        textViewStatSelesai.setText("...");

        String url = ApiConfig.URL_GET_STATISTIK_USER + "?idUser=" + idUser;

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        if ("success".equals(response.getString("status"))) {
                            JSONObject data = response.getJSONObject("data");
                            int jumlahMenunggu = data.getInt("jumlah_menunggu");
                            int jumlahDiproses = data.getInt("jumlah_diproses");
                            int jumlahSelesai = data.getInt("jumlah_selesai");

                            // Tampilkan angka ke TextView
                            textViewStatMenunggu.setText(String.valueOf(jumlahMenunggu));
                            textViewStatDiproses.setText(String.valueOf(jumlahDiproses));
                            textViewStatSelesai.setText(String.valueOf(jumlahSelesai));

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

    private void tampilkanDialogTentang() {
        // Ambil informasi versi aplikasi dari Gradle secara dinamis
        String versiAplikasi = "Versi ";
        try {
            versiAplikasi += requireActivity().getPackageManager()
                    .getPackageInfo(requireActivity().getPackageName(), 0).versionName;
        } catch (Exception e) {
            versiAplikasi += "1.0"; // Fallback jika gagal
        }

        // Bangun isi pesan dialog
        String pesan = "GAMPIL (Gampang Lapor Infrastruktur) merupakan aplikasi pengaduan kerusakan infrastruktur desa berbasis Android " +
                "dengan menerapkan algoritma Run Length Encoding (RLE) Untuk Kompresi Gambar.\n\n" +
                "Developed by Ridho NF\n\n" +
                versiAplikasi;

        // Tampilkan dialog menggunakan MaterialAlertDialogBuilder
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Tentang Aplikasi")
                .setMessage(pesan)
                .setPositiveButton("OK", (dialog, which) -> {
                    dialog.dismiss(); // Tutup dialog saat tombol OK diklik
                })
                .show();
    }

    private void tampilkanDialogLogout() {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Konfirmasi Logout")
                .setMessage("Apakah Anda yakin ingin keluar dari akun Anda?")
                .setNegativeButton("Batal", null)
                .setPositiveButton("Logout", (dialog, which) -> {
                    // Hapus sesi dari SharedPreferences
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.clear();
                    editor.apply();

                    // Arahkan ke LoginActivity
                    Intent intent = new Intent(getActivity(), LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    requireActivity().finish();
                })
                .show();
    }
}