package com.alisteners.pengaduan.presentation.operator.rekap;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.alisteners.pengaduan.domain.operator.usecase.GetRekapDataUseCase;
public class RekapViewModelFactory implements ViewModelProvider.Factory {
    private final GetRekapDataUseCase getRekapDataUseCase;
    public RekapViewModelFactory(GetRekapDataUseCase useCase) { this.getRekapDataUseCase = useCase; }
    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(RekapViewModel.class)) {
            return (T) new RekapViewModel(getRekapDataUseCase);
        }
        throw new IllegalArgumentException("Unknown ViewModel class");
    }
}