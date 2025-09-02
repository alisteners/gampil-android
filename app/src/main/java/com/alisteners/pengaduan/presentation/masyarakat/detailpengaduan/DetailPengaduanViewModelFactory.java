package com.alisteners.pengaduan.presentation.masyarakat.detailpengaduan;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.alisteners.pengaduan.domain.usecase.GetDetailPengaduanUseCase;

public class DetailPengaduanViewModelFactory implements ViewModelProvider.Factory {
    private final GetDetailPengaduanUseCase getDetailPengaduanUseCase;

    public DetailPengaduanViewModelFactory(GetDetailPengaduanUseCase getDetailPengaduanUseCase) {
        this.getDetailPengaduanUseCase = getDetailPengaduanUseCase;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(DetailPengaduanViewModel.class)) {
            // Jika ViewModel yang diminta adalah DetailPengaduanViewModel, buat instance-nya
            return (T) new DetailPengaduanViewModel(getDetailPengaduanUseCase);
        }
        // Jika bukan, lempar exception
        throw new IllegalArgumentException("Unknown ViewModel class: " + modelClass.getName());
    }
}