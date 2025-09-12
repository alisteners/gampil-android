package com.alisteners.pengaduan.presentation.masyarakat.beranda;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.alisteners.pengaduan.domain.usecase.GetPengaduanPublikUseCase;

/**
 * Factory class yang bertanggung jawab untuk membuat instance dari {@link BerandaViewModel}.
 * Kelas ini diperlukan untuk memungkinkan ViewModel memiliki konstruktor dengan parameter (dependensi),
 * yang dalam kasus ini adalah {@link GetPengaduanPublikUseCase}.
 * Ini adalah implementasi dari pola Dependency Injection manual.
 */
public class BerandaViewModelFactory implements ViewModelProvider.Factory {
    private final GetPengaduanPublikUseCase getPengaduanPublikUseCase;

    /**
     * Konstruktor untuk BerandaViewModelFactory.
     *
     * @param getPengaduanPublikUseCase Instance dari use case yang akan disuntikkan (injected)
     * ke dalam BerandaViewModel saat dibuat.
     */
    public BerandaViewModelFactory(GetPengaduanPublikUseCase getPengaduanPublikUseCase) {
        this.getPengaduanPublikUseCase = getPengaduanPublikUseCase;
    }

    /**
     * Metode yang dipanggil oleh sistem Android untuk membuat instance ViewModel.
     *
     * @param modelClass Kelas dari ViewModel yang diminta.
     * @return Instance baru dari ViewModel yang diminta.
     * @throws IllegalArgumentException jika modelClass yang diminta bukan {@link BerandaViewModel}.
     */
    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        // Memeriksa apakah kelas yang diminta adalah BerandaViewModel
        if (modelClass.isAssignableFrom(BerandaViewModel.class)) {
            // Jika ya, buat instance baru dari BerandaViewModel dengan use case yang sudah ada
            return (T) new BerandaViewModel(getPengaduanPublikUseCase);
        }
        // Jika tidak, lemparkan error karena factory ini tidak tahu cara membuat ViewModel lain.
        throw new IllegalArgumentException("Unknown ViewModel class");
    }
}