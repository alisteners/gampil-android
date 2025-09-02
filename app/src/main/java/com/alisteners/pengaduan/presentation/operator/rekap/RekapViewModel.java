package com.alisteners.pengaduan.presentation.operator.rekap;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.alisteners.pengaduan.domain.operator.entity.RekapData;
import com.alisteners.pengaduan.domain.operator.repository.OperatorRepository;
import com.alisteners.pengaduan.domain.operator.usecase.GetRekapDataUseCase;

public class RekapViewModel extends ViewModel {
    private final GetRekapDataUseCase getRekapDataUseCase;

    private final MutableLiveData<RekapData> _rekapData = new MutableLiveData<>();
    public LiveData<RekapData> rekapData = _rekapData;

    private final MutableLiveData<Boolean> _isLoading = new MutableLiveData<>();
    public LiveData<Boolean> isLoading = _isLoading;

    private final MutableLiveData<String> _errorMessage = new MutableLiveData<>();
    public LiveData<String> errorMessage = _errorMessage;

    public RekapViewModel(GetRekapDataUseCase useCase) {
        this.getRekapDataUseCase = useCase;
        loadRekapData(); // Langsung muat data saat ViewModel dibuat
    }

    public void loadRekapData() {
        _isLoading.setValue(true);
        getRekapDataUseCase.execute(new OperatorRepository.GetRekapCallback() {
            @Override
            public void onBerhasil(RekapData data) {
                _isLoading.setValue(false);
                _rekapData.setValue(data);
            }

            @Override
            public void onGagal(String pesanError) {
                _isLoading.setValue(false);
                _errorMessage.setValue(pesanError);
            }
        });
    }
}
