package com.alisteners.pengaduan.presentation.masyarakat.lapor;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.annotation.Nullable;

import com.alisteners.pengaduan.domain.entity.PengaduanData;
import com.alisteners.pengaduan.domain.repository.PengaduanRepository;
import com.alisteners.pengaduan.domain.usecase.KirimPengaduanUseCase;

import java.io.File;

public class BuatPengaduanViewModel extends ViewModel {
    private final KirimPengaduanUseCase kirimPengaduanUseCase;

    private final MutableLiveData<Boolean> _isLoading = new MutableLiveData<>();
    public LiveData<Boolean> isLoading = _isLoading;

    private final MutableLiveData<String> _pesanSukses = new MutableLiveData<>();
    public LiveData<String> pesanSukses = _pesanSukses;

    private final MutableLiveData<String> _pesanError = new MutableLiveData<>();
    public LiveData<String> pesanError = _pesanError;

    public BuatPengaduanViewModel(KirimPengaduanUseCase kirimPengaduanUseCase) {
        this.kirimPengaduanUseCase = kirimPengaduanUseCase;
    }

    public void prosesKirimPengaduan(PengaduanData pengaduanData, @Nullable File fileRle, @Nullable File fileThumbnail) {
        _isLoading.setValue(true);
        kirimPengaduanUseCase.execute(pengaduanData, fileRle, fileThumbnail, new PengaduanRepository.KirimPengaduanCallback() {
            @Override
            public void onBerhasil(String pesan) {
                _isLoading.setValue(false);
                _pesanSukses.setValue(pesan);
            }

            @Override
            public void onGagal(String pesanError) {
                _isLoading.setValue(false);
                _pesanError.setValue(pesanError);
            }
        });
    }

    public void laporkanErrorLokal(String pesan) {
        _isLoading.setValue(false);
        _pesanError.setValue(pesan);
    }

    public void clearPesanSukses() {
        _pesanSukses.setValue(null);
    }

    public void clearPesanError() {
        _pesanError.setValue(null);
    }
}