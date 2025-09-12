package com.alisteners.pengaduan.presentation.operator.rekap;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.alisteners.pengaduan.domain.operator.usecase.GetRekapDataUseCase;

/**
 * Factory class yang bertanggung jawab untuk membuat instance dari {@link RekapViewModel}.
 * Kelas ini diperlukan agar ViewModel dapat menerima dependensi (dalam hal ini,
 * {@link GetRekapDataUseCase}) melalui konstruktornya, sesuai dengan
 * prinsip Dependency Injection.
 */
public class RekapViewModelFactory implements ViewModelProvider.Factory {
    private final GetRekapDataUseCase getRekapDataUseCase;

    /**
     * Konstruktor untuk RekapViewModelFactory.
     *
     * @param useCase Instance dari use case yang akan disuntikkan
     * ke dalam RekapViewModel saat dibuat.
     */
    public RekapViewModelFactory(GetRekapDataUseCase useCase) {
        this.getRekapDataUseCase = useCase;
    }

    /**
     * Metode yang dipanggil oleh sistem Android untuk membuat instance ViewModel.
     *
     * @param modelClass Kelas dari ViewModel yang diminta untuk dibuat.
     * @return Instance baru dari ViewModel yang diminta.
     * @throws IllegalArgumentException jika modelClass yang diminta bukan {@link RekapViewModel}.
     */
    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        // Memeriksa apakah kelas yang diminta adalah RekapViewModel atau turunannya.
        if (modelClass.isAssignableFrom(RekapViewModel.class)) {
            // Jika ya, buat instance baru dari RekapViewModel dengan use case yang sudah ada.
            return (T) new RekapViewModel(getRekapDataUseCase);
        }
        // Jika tidak, lemparkan error karena factory ini tidak tahu cara membuat ViewModel lain.
        throw new IllegalArgumentException("Unknown ViewModel class");
    }
}