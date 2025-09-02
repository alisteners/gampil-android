package com.alisteners.pengaduan.presentation.masyarakat.beranda;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.alisteners.pengaduan.domain.entity.PengaduanPublikItem;
import com.alisteners.pengaduan.domain.repository.PengaduanRepository;
import com.alisteners.pengaduan.domain.usecase.GetPengaduanPublikUseCase;

import java.util.List;

public class BerandaViewModel extends ViewModel {
    private final GetPengaduanPublikUseCase getPengaduanPublikUseCase;

    private final MutableLiveData<List<PengaduanPublikItem>> _pengaduanList = new MutableLiveData<>();
    public LiveData<List<PengaduanPublikItem>> pengaduanList = _pengaduanList;

    private final MutableLiveData<Boolean> _isLoading = new MutableLiveData<>();
    public LiveData<Boolean> isLoading = _isLoading;

    private final MutableLiveData<Boolean> _isEmpty = new MutableLiveData<>();
    public LiveData<Boolean> isEmpty = _isEmpty;

    private final MutableLiveData<String> _errorMessage = new MutableLiveData<>();
    public LiveData<String> errorMessage = _errorMessage;

    public BerandaViewModel(GetPengaduanPublikUseCase getPengaduanPublikUseCase) {
        this.getPengaduanPublikUseCase = getPengaduanPublikUseCase;
    }

    public void loadPengaduanPublik(String status, boolean showLoading) {
        if (showLoading) {
            _isLoading.setValue(true);
        }

        // Panggil use case dengan parameter status
        getPengaduanPublikUseCase.execute(status, new PengaduanRepository.GetPengaduanPublikCallback() {
            @Override
            public void onBerhasil(List<PengaduanPublikItem> pengaduan) {
                _pengaduanList.setValue(pengaduan);
                _isLoading.setValue(false);
            }

            @Override
            public void onGagal(String pesan) {
                _errorMessage.setValue(pesan);
                _isLoading.setValue(false);
            }
        });
    }
}