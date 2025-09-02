package com.alisteners.pengaduan.presentation.masyarakat.detailpengaduan;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.alisteners.pengaduan.domain.entity.DetailPengaduan;
import com.alisteners.pengaduan.domain.repository.PengaduanRepository;
import com.alisteners.pengaduan.domain.usecase.GetDetailPengaduanUseCase;

public class DetailPengaduanViewModel extends ViewModel {
    private final GetDetailPengaduanUseCase getDetailPengaduanUseCase;

    private final MutableLiveData<DetailPengaduan> _detailPengaduan = new MutableLiveData<>();
    public LiveData<DetailPengaduan> detailPengaduan = _detailPengaduan;

    private final MutableLiveData<Boolean> _isLoading = new MutableLiveData<>();
    public LiveData<Boolean> isLoading = _isLoading;

    private final MutableLiveData<String> _errorMessage = new MutableLiveData<>();
    public LiveData<String> errorMessage = _errorMessage;

    public DetailPengaduanViewModel(GetDetailPengaduanUseCase getDetailPengaduanUseCase) {
        this.getDetailPengaduanUseCase = getDetailPengaduanUseCase;
    }

    public void loadDetail(int idPengaduan) {
        _isLoading.setValue(true);
        _errorMessage.setValue(null);

        getDetailPengaduanUseCase.execute(idPengaduan, new PengaduanRepository.GetDetailCallback() {
            @Override
            public void onBerhasil(DetailPengaduan detail) {
                _isLoading.setValue(false);
                _detailPengaduan.setValue(detail);
            }

            @Override
            public void onGagal(String pesanError) {
                _isLoading.setValue(false);
                _errorMessage.setValue(pesanError);
            }
        });
    }
}