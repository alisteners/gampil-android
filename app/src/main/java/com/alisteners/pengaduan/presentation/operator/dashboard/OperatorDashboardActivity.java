package com.alisteners.pengaduan.presentation.operator.dashboard;

import android.os.Bundle;
import androidx.annotation.IdRes;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.alisteners.pengaduan.R;
import com.alisteners.pengaduan.presentation.operator.cari.CariFragment;
import com.alisteners.pengaduan.presentation.operator.profiloperator.ProfilOperatorFragment;
import com.alisteners.pengaduan.presentation.operator.rekap.RekapFragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;

/**
 * Activity utama untuk pengguna dengan peran 'Operator'.
 * Bertanggung jawab sebagai container (wadah) untuk fragment-fragment utama operator
 * dan mengelola navigasi antar fragment tersebut menggunakan BottomNavigationView.
 */
public class OperatorDashboardActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView;

    /**
     * Metode yang dipanggil saat Activity pertama kali dibuat.
     * Mengatur layout, menginisialisasi BottomNavigationView, dan memuat
     * fragment default (Dashboard) saat aplikasi pertama kali dibuka.
     *
     * @param savedInstanceState Jika Activity diinisialisasi ulang, Bundle ini berisi data terakhir.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_operator_dashboard);

        bottomNavigationView = findViewById(R.id.operator_bottom_navigation);
        setupNavigation();

        // Muat fragment default (Dashboard) saat Activity pertama kali dibuat.
        if (savedInstanceState == null) {
            bottomNavigationView.setSelectedItemId(R.id.nav_operator_dashboard);
        }
    }

    /**
     * Mengatur listener untuk BottomNavigationView.
     * Metode ini menangani logika untuk mengganti fragment yang ditampilkan di FrameLayout
     * berdasarkan item menu yang dipilih oleh pengguna.
     */
    private void setupNavigation() {
        bottomNavigationView.setOnItemSelectedListener(item -> {
            // Panggil metode baru untuk mendapatkan fragment yang sesuai
            Fragment selectedFragment = getFragmentForMenuItem(item.getItemId());

            if (selectedFragment != null) {
                // Lakukan transaksi untuk mengganti fragment di container
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.operator_frame_layout, selectedFragment)
                        .commit();
                return true; // Item terpilih
            }

            return false; // Item tidak terpilih jika fragment null
        });
    }

    /**
     * [METODE BARU HASIL EKSTRAKSI]
     * Memilih dan mengembalikan instance Fragment yang sesuai berdasarkan ID item menu.
     *
     * @param itemId ID dari menu item yang di-klik.
     * @return Instance Fragment yang baru, atau null jika tidak ada yang cocok.
     */
    @Nullable
    private Fragment getFragmentForMenuItem(@IdRes int itemId) {
        if (itemId == R.id.nav_operator_dashboard) {
            return new DashboardOperatorFragment();
        } else if (itemId == R.id.nav_operator_search) {
            return new CariFragment();
        } else if (itemId == R.id.nav_operator_rekap) {
            return new RekapFragment();
        } else if (itemId == R.id.nav_operator_profil) {
            return new ProfilOperatorFragment();
        }
        return null; // Kembalikan null jika tidak ada ID yang cocok
    }
}