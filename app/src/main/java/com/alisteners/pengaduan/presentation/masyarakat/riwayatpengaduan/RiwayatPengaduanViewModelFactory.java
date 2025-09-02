package com.alisteners.pengaduan.presentation.masyarakat.riwayatpengaduan;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.alisteners.pengaduan.domain.usecase.GetRiwayatPengaduanUseCase;

public class RiwayatPengaduanViewModelFactory implements ViewModelProvider.Factory {
    private final GetRiwayatPengaduanUseCase getRiwayatPengaduanUseCase;

    public RiwayatPengaduanViewModelFactory(GetRiwayatPengaduanUseCase getRiwayatPengaduanUseCase) {
        this.getRiwayatPengaduanUseCase = getRiwayatPengaduanUseCase;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(RiwayatPengaduanViewModel.class)) {
            // Jika ViewModel yang diminta adalah RiwayatPengaduanViewModel, buat instance-nya
            return (T) new RiwayatPengaduanViewModel(getRiwayatPengaduanUseCase);
        }
        // Jika bukan, lempar exception
        throw new IllegalArgumentException("Unknown ViewModel class: " + modelClass.getName());
    }
}