package com.alisteners.pengaduan.presentation.masyarakat.lapor;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.alisteners.pengaduan.domain.usecase.KirimPengaduanUseCase;

public class BuatPengaduanViewModelFactory implements ViewModelProvider.Factory {
    private final KirimPengaduanUseCase kirimPengaduanUseCase;

    public BuatPengaduanViewModelFactory(KirimPengaduanUseCase kirimPengaduanUseCase) {
        this.kirimPengaduanUseCase = kirimPengaduanUseCase;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(BuatPengaduanViewModel.class)) {
            return (T) new BuatPengaduanViewModel(kirimPengaduanUseCase);
        }
        throw new IllegalArgumentException("Unknown ViewModel class: " + modelClass.getName());
    }
}