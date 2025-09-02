package com.alisteners.pengaduan.presentation.operator.detail;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.alisteners.pengaduan.domain.operator.usecase.HapusPengaduanUseCase;
import com.alisteners.pengaduan.domain.operator.usecase.UpdatePengaduanUseCase;
import com.alisteners.pengaduan.domain.usecase.GetDetailPengaduanUseCase;

public class DetailOperatorViewModelFactory implements ViewModelProvider.Factory {

    private final GetDetailPengaduanUseCase getDetailPengaduanUseCase;
    private final UpdatePengaduanUseCase updatePengaduanUseCase;
    private final HapusPengaduanUseCase hapusPengaduanUseCase;

    public DetailOperatorViewModelFactory(
            GetDetailPengaduanUseCase getDetailPengaduanUseCase,
            UpdatePengaduanUseCase updatePengaduanUseCase,
            HapusPengaduanUseCase hapusPengaduanUseCase) {
        this.getDetailPengaduanUseCase = getDetailPengaduanUseCase;
        this.updatePengaduanUseCase = updatePengaduanUseCase;
        this.hapusPengaduanUseCase = hapusPengaduanUseCase;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(DetailOperatorViewModel.class)) {
            // Kirim SEMUA use case saat membuat ViewModel baru
            return (T) new DetailOperatorViewModel(
                    getDetailPengaduanUseCase,
                    updatePengaduanUseCase,
                    hapusPengaduanUseCase
            );
        }
        throw new IllegalArgumentException("Unknown ViewModel class: " + modelClass.getName());
    }
}