package com.alisteners.pengaduan.presentation.masyarakat.lapor;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.alisteners.pengaduan.domain.usecase.KirimPengaduanUseCase;

/**
 * Factory class yang bertanggung jawab untuk membuat instance dari {@link BuatPengaduanViewModel}.
 * Kelas ini diperlukan agar ViewModel dapat menerima dependensi (dalam hal ini,
 * {@link KirimPengaduanUseCase}) melalui konstruktornya, sesuai dengan
 * prinsip Dependency Injection.
 */
public class BuatPengaduanViewModelFactory implements ViewModelProvider.Factory {
    private final KirimPengaduanUseCase kirimPengaduanUseCase;

    /**
     * Konstruktor untuk BuatPengaduanViewModelFactory.
     *
     * @param kirimPengaduanUseCase Instance dari use case yang akan disuntikkan
     * ke dalam BuatPengaduanViewModel saat dibuat.
     */
    public BuatPengaduanViewModelFactory(KirimPengaduanUseCase kirimPengaduanUseCase) {
        this.kirimPengaduanUseCase = kirimPengaduanUseCase;
    }

    /**
     * Metode yang dipanggil oleh sistem Android untuk membuat instance ViewModel.
     *
     * @param modelClass Kelas dari ViewModel yang diminta untuk dibuat.
     * @return Instance baru dari ViewModel yang diminta.
     * @throws IllegalArgumentException jika modelClass yang diminta bukan {@link BuatPengaduanViewModel}.
     */
    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        // Memeriksa apakah kelas yang diminta adalah BuatPengaduanViewModel atau turunannya.
        if (modelClass.isAssignableFrom(BuatPengaduanViewModel.class)) {
            // Jika ya, buat instance baru dari BuatPengaduanViewModel dengan use case yang sudah ada.
            return (T) new BuatPengaduanViewModel(kirimPengaduanUseCase);
        }
        // Jika tidak, lemparkan error karena factory ini tidak tahu cara membuat ViewModel lain.
        throw new IllegalArgumentException("Unknown ViewModel class: " + modelClass.getName());
    }
}