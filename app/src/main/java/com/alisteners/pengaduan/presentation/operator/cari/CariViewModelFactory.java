package com.alisteners.pengaduan.presentation.operator.cari;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.alisteners.pengaduan.domain.operator.usecase.SearchPengaduanUseCase;

public class CariViewModelFactory implements ViewModelProvider.Factory {
    private final SearchPengaduanUseCase useCase;

    public CariViewModelFactory(SearchPengaduanUseCase useCase) {
        this.useCase = useCase;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(CariViewModel.class)) {
            return (T) new CariViewModel(useCase);
        }
        throw new IllegalArgumentException("Unknown ViewModel class");
    }
}