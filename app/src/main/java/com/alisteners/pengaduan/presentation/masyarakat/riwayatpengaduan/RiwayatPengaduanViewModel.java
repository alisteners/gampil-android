package com.alisteners.pengaduan.presentation.masyarakat.riwayatpengaduan;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.alisteners.pengaduan.domain.entity.PengaduanRiwayatItem;
import com.alisteners.pengaduan.domain.repository.PengaduanRepository;
import com.alisteners.pengaduan.domain.usecase.GetRiwayatPengaduanUseCase;

import java.util.List;
import java.util.ArrayList; // Untuk list kosong

public class RiwayatPengaduanViewModel extends ViewModel {
    private final GetRiwayatPengaduanUseCase getRiwayatPengaduanUseCase;

    private final MutableLiveData<List<PengaduanRiwayatItem>> _riwayatList = new MutableLiveData<>();
    public LiveData<List<PengaduanRiwayatItem>> riwayatList = _riwayatList;

    private final MutableLiveData<Boolean> _isLoading = new MutableLiveData<>();
    public LiveData<Boolean> isLoading = _isLoading;

    private final MutableLiveData<String> _errorMessage = new MutableLiveData<>();
    public LiveData<String> errorMessage = _errorMessage;

    private final MutableLiveData<Boolean> _isEmpty = new MutableLiveData<>();
    public LiveData<Boolean> isEmpty = _isEmpty;


    public RiwayatPengaduanViewModel(GetRiwayatPengaduanUseCase getRiwayatPengaduanUseCase) {
        this.getRiwayatPengaduanUseCase = getRiwayatPengaduanUseCase;
    }

    public void loadRiwayat(int idUser) {
        _isLoading.setValue(true);
        _isEmpty.setValue(false); // Reset empty state
        _errorMessage.setValue(null); // Reset error message

        getRiwayatPengaduanUseCase.execute(idUser, new PengaduanRepository.GetRiwayatCallback() {
            @Override
            public void onBerhasil(List<PengaduanRiwayatItem> daftarRiwayat) {
                _isLoading.setValue(false);
                if (daftarRiwayat != null && !daftarRiwayat.isEmpty()) {
                    _riwayatList.setValue(daftarRiwayat);
                    _isEmpty.setValue(false);
                } else {
                    _riwayatList.setValue(new ArrayList<>()); // Set list kosong
                    _isEmpty.setValue(true); // Tandai bahwa data kosong
                }
            }

            @Override
            public void onGagal(String pesanError) {
                _isLoading.setValue(false);
                _errorMessage.setValue(pesanError);
                _riwayatList.setValue(new ArrayList<>()); // Set list kosong saat gagal
                _isEmpty.setValue(true); // Tandai data kosong karena gagal load
            }
        });
    }

}