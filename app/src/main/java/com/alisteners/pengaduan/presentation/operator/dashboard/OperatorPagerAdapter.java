package com.alisteners.pengaduan.presentation.operator.dashboard;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import java.util.ArrayList;
import java.util.List;

public class OperatorPagerAdapter extends FragmentStateAdapter {

    private final List<String> tabTitles = new ArrayList<>();

    public OperatorPagerAdapter(@NonNull Fragment fragment) {
        super(fragment);
    }

    public OperatorPagerAdapter(FragmentActivity fragmentActivity) {
        super(fragmentActivity);
        // Inisialisasi judul tab awal
        tabTitles.add("Baru");
        tabTitles.add("Diproses");
        tabTitles.add("Selesai");
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        // Mengirimkan status yang sesuai ke setiap fragment
        return switch (position) {
            case 0 -> PengaduanListOperatorFragment.newInstance("menunggu");
            case 1 -> PengaduanListOperatorFragment.newInstance("diproses");
            case 2 -> PengaduanListOperatorFragment.newInstance("selesai");
            default ->
                // Seharusnya tidak pernah terjadi
                    PengaduanListOperatorFragment.newInstance("menunggu");
        };
    }

    public String getPageTitle(int position) {
        return tabTitles.get(position);
    }

    public void updateTabTitle(int position, String title) {
        if (position >= 0 && position < tabTitles.size()) {
            tabTitles.set(position, title);
            // Perubahan ini akan diambil saat TabLayout di-refresh
        }
    }

    @Override
    public int getItemCount() {
        return 3; // Jumlah tab : Baru, Diproses, Selesai
    }
}
