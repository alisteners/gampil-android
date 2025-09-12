package com.alisteners.pengaduan.presentation.masyarakat.detailpengaduan;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.alisteners.pengaduan.domain.usecase.GetDetailPengaduanUseCase;

/**
 * Factory class yang bertanggung jawab untuk membuat instance dari {@link DetailPengaduanViewModel}.
 * Kelas ini diperlukan agar ViewModel dapat menerima dependensi (dalam hal ini,
 * {@link GetDetailPengaduanUseCase}) melalui konstruktornya, sesuai dengan
 * prinsip Dependency Injection.
 */
public class DetailPengaduanViewModelFactory implements ViewModelProvider.Factory {
    private final GetDetailPengaduanUseCase getDetailPengaduanUseCase;

    /**
     * Konstruktor untuk DetailPengaduanViewModelFactory.
     *
     * @param getDetailPengaduanUseCase Instance dari use case yang akan disuntikkan
     * ke dalam DetailPengaduanViewModel saat dibuat.
     */
    public DetailPengaduanViewModelFactory(GetDetailPengaduanUseCase getDetailPengaduanUseCase) {
        this.getDetailPengaduanUseCase = getDetailPengaduanUseCase;
    }

    /**
     * Metode yang dipanggil oleh sistem Android untuk membuat instance ViewModel.
     *
     * @param modelClass Kelas dari ViewModel yang diminta untuk dibuat.
     * @return Instance baru dari ViewModel yang diminta.
     * @throws IllegalArgumentException jika modelClass yang diminta bukan {@link DetailPengaduanViewModel}.
     */
    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        // Memeriksa apakah kelas yang diminta adalah DetailPengaduanViewModel atau turunannya.
        if (modelClass.isAssignableFrom(DetailPengaduanViewModel.class)) {
            // Jika ya, buat instance baru dari DetailPengaduanViewModel dengan use case yang sudah ada.
            return (T) new DetailPengaduanViewModel(getDetailPengaduanUseCase);
        }
        // Jika tidak, lemparkan error karena factory ini tidak tahu cara membuat ViewModel lain.
        throw new IllegalArgumentException("Unknown ViewModel class: " + modelClass.getName());
    }
}