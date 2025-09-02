package com.alisteners.pengaduan.presentation.operator.dashboard;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.alisteners.pengaduan.R;
import com.alisteners.pengaduan.presentation.operator.cari.CariFragment;
import com.alisteners.pengaduan.presentation.operator.profiloperator.ProfilOperatorFragment;
import com.alisteners.pengaduan.presentation.operator.rekap.RekapFragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class OperatorDashboardActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_operator_dashboard);

        bottomNavigationView = findViewById(R.id.operator_bottom_navigation);
        setupNavigation();

        // Muat fragment default (Dashboard) saat pertama kali dibuka
        if (savedInstanceState == null) {
            bottomNavigationView.setSelectedItemId(R.id.nav_operator_dashboard);
        }
    }

    private void setupNavigation() {
        bottomNavigationView.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            int itemId = item.getItemId();

            if (itemId == R.id.nav_operator_dashboard) {
                selectedFragment = new DashboardOperatorFragment(); // Fragment untuk Dashboard
            } else if (itemId == R.id.nav_operator_search) {
                selectedFragment = new CariFragment();
            } else if (itemId == R.id.nav_operator_rekap) {
                selectedFragment = new RekapFragment();
            } else if (itemId == R.id.nav_operator_profil) {
                selectedFragment = new ProfilOperatorFragment(); // Gunakan ulang ProfilFragment
            }

            if (selectedFragment != null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.operator_frame_layout, selectedFragment)
                        .commit();
                return true;
            }

            return false; // Return false agar item tidak terpilih jika fragment null
        });
    }
}
