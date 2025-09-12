package com.alisteners.pengaduan.presentation.operator.detail;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.alisteners.pengaduan.domain.operator.usecase.HapusPengaduanUseCase;
import com.alisteners.pengaduan.domain.operator.usecase.UpdatePengaduanUseCase;
import com.alisteners.pengaduan.domain.usecase.GetDetailPengaduanUseCase;

/**
 * Factory class yang bertanggung jawab untuk membuat instance dari {@link DetailOperatorViewModel}.
 * Kelas ini diperlukan agar ViewModel dapat menerima beberapa dependensi (UseCases)
 * melalui konstruktornya, sesuai dengan prinsip Dependency Injection.
 */
public class DetailOperatorViewModelFactory implements ViewModelProvider.Factory {

    // --- Deklarasi Use Case sebagai dependensi ---
    private final GetDetailPengaduanUseCase getDetailPengaduanUseCase;
    private final UpdatePengaduanUseCase updatePengaduanUseCase;
    private final HapusPengaduanUseCase hapusPengaduanUseCase;

    /**
     * Konstruktor untuk DetailOperatorViewModelFactory.
     *
     * @param getDetailPengaduanUseCase Use case untuk mengambil detail pengaduan.
     * @param updatePengaduanUseCase    Use case untuk memperbarui pengaduan.
     * @param hapusPengaduanUseCase     Use case untuk menghapus pengaduan.
     */
    public DetailOperatorViewModelFactory(
            GetDetailPengaduanUseCase getDetailPengaduanUseCase,
            UpdatePengaduanUseCase updatePengaduanUseCase,
            HapusPengaduanUseCase hapusPengaduanUseCase) {
        this.getDetailPengaduanUseCase = getDetailPengaduanUseCase;
        this.updatePengaduanUseCase = updatePengaduanUseCase;
        this.hapusPengaduanUseCase = hapusPengaduanUseCase;
    }

    /**
     * Metode yang dipanggil oleh sistem Android untuk membuat instance ViewModel.
     *
     * @param modelClass Kelas dari ViewModel yang diminta untuk dibuat.
     * @return Instance baru dari ViewModel yang diminta.
     * @throws IllegalArgumentException jika modelClass yang diminta bukan {@link DetailOperatorViewModel}.
     */
    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        // Memeriksa apakah kelas yang diminta adalah DetailOperatorViewModel atau turunannya.
        if (modelClass.isAssignableFrom(DetailOperatorViewModel.class)) {
            // Jika ya, buat instance baru dari DetailOperatorViewModel dengan semua use case yang dibutuhkan.
            return (T) new DetailOperatorViewModel(
                    getDetailPengaduanUseCase,
                    updatePengaduanUseCase,
                    hapusPengaduanUseCase
            );
        }
        // Jika tidak, lemparkan error karena factory ini tidak tahu cara membuat ViewModel lain.
        throw new IllegalArgumentException("Unknown ViewModel class: " + modelClass.getName());
    }
}