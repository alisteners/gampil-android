package com.alisteners.pengaduan.presentation.masyarakat.riwayatpengaduan;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.alisteners.pengaduan.domain.usecase.GetRiwayatPengaduanUseCase;

/**
 * Factory class yang bertanggung jawab untuk membuat instance dari {@link RiwayatPengaduanViewModel}.
 * Kelas ini diperlukan agar ViewModel dapat menerima dependensi (dalam hal ini,
 * {@link GetRiwayatPengaduanUseCase}) melalui konstruktornya, sesuai dengan
 * prinsip Dependency Injection.
 */
public class RiwayatPengaduanViewModelFactory implements ViewModelProvider.Factory {
    private final GetRiwayatPengaduanUseCase getRiwayatPengaduanUseCase;

    /**
     * Konstruktor untuk RiwayatPengaduanViewModelFactory.
     *
     * @param getRiwayatPengaduanUseCase Instance dari use case yang akan disuntikkan
     * ke dalam RiwayatPengaduanViewModel saat dibuat.
     */
    public RiwayatPengaduanViewModelFactory(GetRiwayatPengaduanUseCase getRiwayatPengaduanUseCase) {
        this.getRiwayatPengaduanUseCase = getRiwayatPengaduanUseCase;
    }

    /**
     * Metode yang dipanggil oleh sistem Android untuk membuat instance ViewModel.
     *
     * @param modelClass Kelas dari ViewModel yang diminta untuk dibuat.
     * @return Instance baru dari ViewModel yang diminta.
     * @throws IllegalArgumentException jika modelClass yang diminta bukan {@link RiwayatPengaduanViewModel}.
     */
    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        // Memeriksa apakah kelas yang diminta adalah RiwayatPengaduanViewModel atau turunannya.
        if (modelClass.isAssignableFrom(RiwayatPengaduanViewModel.class)) {
            // Jika ya, buat instance baru dari RiwayatPengaduanViewModel dengan use case yang sudah ada.
            return (T) new RiwayatPengaduanViewModel(getRiwayatPengaduanUseCase);
        }
        // Jika tidak, lemparkan error karena factory ini tidak tahu cara membuat ViewModel lain.
        throw new IllegalArgumentException("Unknown ViewModel class: " + modelClass.getName());
    }
}