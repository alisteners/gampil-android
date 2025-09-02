package com.alisteners.pengaduan.presentation.operator.cari;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.alisteners.pengaduan.domain.entity.PengaduanPublikItem;
import com.alisteners.pengaduan.domain.operator.repository.OperatorRepository;
import com.alisteners.pengaduan.domain.operator.usecase.SearchPengaduanUseCase;

import java.util.List;

public class CariViewModel extends ViewModel {
    private final SearchPengaduanUseCase searchPengaduanUseCase;

    private final MutableLiveData<List<PengaduanPublikItem>> _hasilPencarian = new MutableLiveData<>();
    public LiveData<List<PengaduanPublikItem>> hasilPencarian = _hasilPencarian;

    private final MutableLiveData<Boolean> _isLoading = new MutableLiveData<>();
    public LiveData<Boolean> isLoading = _isLoading;

    private final MutableLiveData<String> _errorMessage = new MutableLiveData<>();
    public LiveData<String> errorMessage = _errorMessage;

    public CariViewModel(SearchPengaduanUseCase useCase) {
        this.searchPengaduanUseCase = useCase;
    }

    public void cariPengaduan(String keyword, String status) {
        _isLoading.setValue(true);
        searchPengaduanUseCase.execute(keyword, status, new OperatorRepository.GetPengaduanCallback() {
            @Override
            public void onBerhasil(List<PengaduanPublikItem> list) {
                _isLoading.setValue(false);
                _hasilPencarian.setValue(list);
            }

            @Override
            public void onGagal(String pesanError) {
                _isLoading.setValue(false);
                _errorMessage.setValue(pesanError);
            }
        });
    }
}
