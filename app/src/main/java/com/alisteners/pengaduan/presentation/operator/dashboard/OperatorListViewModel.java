package com.alisteners.pengaduan.presentation.operator.dashboard;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.alisteners.pengaduan.domain.entity.PengaduanPublikItem;
import com.alisteners.pengaduan.domain.operator.repository.OperatorRepository;
import com.alisteners.pengaduan.domain.operator.usecase.GetPengaduanOperatorUseCase;

import java.util.List;

public class OperatorListViewModel extends ViewModel {
    private final GetPengaduanOperatorUseCase getPengaduanOperatorUseCase;
    private final MutableLiveData<List<PengaduanPublikItem>> _pengaduanList = new MutableLiveData<>();
    public LiveData<List<PengaduanPublikItem>> pengaduanList = _pengaduanList;

    private final MutableLiveData<Boolean> _isLoading = new MutableLiveData<>();
    public LiveData<Boolean> isLoading = _isLoading;

    private final MutableLiveData<String> _errorMessage = new MutableLiveData<>();
    public LiveData<String> errorMessage = _errorMessage;

    public OperatorListViewModel(GetPengaduanOperatorUseCase useCase) {
        this.getPengaduanOperatorUseCase = useCase;
    }

    public void loadPengaduanByStatus(String status) {
        _isLoading.setValue(true);
        _errorMessage.setValue(null); // Bersihkan error lama saat memulai request baru

        getPengaduanOperatorUseCase.execute(status, new OperatorRepository.GetPengaduanCallback() {
            @Override
            public void onBerhasil(List<PengaduanPublikItem> list) {
                _isLoading.setValue(false);
                _pengaduanList.setValue(list);
            }

            @Override
            public void onGagal(String pesanError) {
                _isLoading.setValue(false);
                _errorMessage.setValue(pesanError);
            }
        });
    }
}
