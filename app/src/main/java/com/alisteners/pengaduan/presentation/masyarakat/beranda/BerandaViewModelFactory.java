package com.alisteners.pengaduan.presentation.masyarakat.beranda;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.alisteners.pengaduan.domain.usecase.GetPengaduanPublikUseCase;

public class BerandaViewModelFactory implements ViewModelProvider.Factory {
    private final GetPengaduanPublikUseCase getPengaduanPublikUseCase;

    public BerandaViewModelFactory(GetPengaduanPublikUseCase getPengaduanPublikUseCase) {
        this.getPengaduanPublikUseCase = getPengaduanPublikUseCase;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(BerandaViewModel.class)) {
            return (T) new BerandaViewModel(getPengaduanPublikUseCase);
        }
        throw new IllegalArgumentException("Unknown ViewModel class");
    }
}