package com.alisteners.pengaduan.presentation.operator.dashboard;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapter yang menghubungkan ViewPager2 dengan fragment-fragment daftar pengaduan
 * untuk dashboard operator.
 * Kelas ini bertanggung jawab untuk membuat dan menyediakan instance fragment yang tepat
 * untuk setiap posisi (tab).
 */
public class OperatorPagerAdapter extends FragmentStateAdapter {

    private final List<String> tabTitles = new ArrayList<>();

    /**
     * Konstruktor yang digunakan saat adapter dibuat dari dalam sebuah Fragment.
     * @param fragment Fragment induk yang menampung ViewPager2.
     */
    public OperatorPagerAdapter(@NonNull Fragment fragment) {
        super(fragment);
    }

    /**
     * Konstruktor yang digunakan saat adapter dibuat dari dalam sebuah Activity.
     * @param fragmentActivity Activity induk yang menampung ViewPager2.
     */
    public OperatorPagerAdapter(FragmentActivity fragmentActivity) {
        super(fragmentActivity);
        // Inisialisasi judul tab awal
        tabTitles.add("Baru");
        tabTitles.add("Diproses");
        tabTitles.add("Selesai");
    }

    /**
     * Dipanggil oleh ViewPager2 untuk membuat Fragment baru untuk posisi tertentu.
     * Setiap fragment {@link PengaduanListOperatorFragment} dibuat dengan status
     * yang sesuai untuk memfilter data yang akan ditampilkan.
     *
     * @param position Posisi tab yang akan dibuat fragment-nya.
     * @return Instance Fragment yang baru.
     */
    @NonNull
    @Override
    public Fragment createFragment(int position) {
        // Mengirimkan status yang sesuai ke setiap fragment
        return switch (position) {
            case 0 -> PengaduanListOperatorFragment.newInstance("menunggu");
            case 1 -> PengaduanListOperatorFragment.newInstance("diproses");
            case 2 -> PengaduanListOperatorFragment.newInstance("selesai");
            default -> PengaduanListOperatorFragment.newInstance("menunggu"); // Fallback
        };
    }

    /**
     * Mengembalikan judul tab untuk posisi tertentu.
     * @param position Posisi tab.
     * @return String judul tab.
     */
    public String getPageTitle(int position) {
        return tabTitles.get(position);
    }

    /**
     * Memperbarui judul tab pada posisi tertentu.
     * @param position Posisi tab yang akan diubah.
     * @param title Judul baru untuk tab.
     */
    public void updateTabTitle(int position, String title) {
        if (position >= 0 && position < tabTitles.size()) {
            tabTitles.set(position, title);
        }
    }

    /**
     * Mengembalikan jumlah total tab/fragment.
     * @return Jumlah tab.
     */
    @Override
    public int getItemCount() {
        return 3; // Jumlah tab: Baru, Diproses, Selesai
    }
}