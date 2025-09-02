package com.alisteners.pengaduan.presentation.operator.dashboard;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.alisteners.pengaduan.domain.operator.usecase.GetPengaduanOperatorUseCase;

public class OperatorListViewModelFactory implements ViewModelProvider.Factory {
    private final GetPengaduanOperatorUseCase getPengaduanOperatorUseCase;

    public OperatorListViewModelFactory(GetPengaduanOperatorUseCase useCase) {
        this.getPengaduanOperatorUseCase = useCase;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(OperatorListViewModel.class)) {
            return (T) new OperatorListViewModel(getPengaduanOperatorUseCase);
        }
        throw new IllegalArgumentException("Unknown ViewModel class");
    }
}