package com.alisteners.pengaduan.presentation.operator.detail;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.alisteners.pengaduan.domain.entity.DetailPengaduan;
import com.alisteners.pengaduan.domain.operator.repository.OperatorRepository;
import com.alisteners.pengaduan.domain.operator.usecase.HapusPengaduanUseCase;
import com.alisteners.pengaduan.domain.operator.usecase.UpdatePengaduanUseCase;
import com.alisteners.pengaduan.domain.repository.PengaduanRepository;
import com.alisteners.pengaduan.domain.usecase.GetDetailPengaduanUseCase;

import java.io.File;
import java.util.List;

public class DetailOperatorViewModel extends ViewModel {

    // DEKLARASI SEMUA USE CASE YANG DIPERLUKAN
    private final GetDetailPengaduanUseCase getDetailPengaduanUseCase;
    private final UpdatePengaduanUseCase updatePengaduanUseCase;
    private final HapusPengaduanUseCase hapusPengaduanUseCase;

    // Deklarasi LiveData
    private final MutableLiveData<DetailPengaduan> _detailPengaduan = new MutableLiveData<>();
    public final LiveData<DetailPengaduan> detailPengaduan = _detailPengaduan;

    private final MutableLiveData<Boolean> _isLoading = new MutableLiveData<>();
    public final LiveData<Boolean> isLoading = _isLoading;

    private final MutableLiveData<Boolean> _isUpdating = new MutableLiveData<>();
    public final LiveData<Boolean> isUpdating = _isUpdating;

    private final MutableLiveData<Boolean> _isDeleting = new MutableLiveData<>();
    public final LiveData<Boolean> isDeleting = _isDeleting;

    private final MutableLiveData<String> _errorMessage = new MutableLiveData<>();
    public final LiveData<String> errorMessage = _errorMessage;

    private final MutableLiveData<String> _updateSuccessMessage = new MutableLiveData<>();
    public final LiveData<String> updateSuccessMessage = _updateSuccessMessage;

    private final MutableLiveData<String> _deleteSuccessMessage = new MutableLiveData<>();
    public final LiveData<String> deleteSuccessMessage = _deleteSuccessMessage;

    // KONSTRUKTOR UNTUK MENERIMA SEMUA USE CASE
    public DetailOperatorViewModel(
            GetDetailPengaduanUseCase getDetail,
            UpdatePengaduanUseCase update,
            HapusPengaduanUseCase hapus
    ) {
        this.getDetailPengaduanUseCase = getDetail;
        this.updatePengaduanUseCase = update;
        this.hapusPengaduanUseCase = hapus;
    }

    public void loadDetail(int idPengaduan) {
        _isLoading.setValue(true);
        getDetailPengaduanUseCase.execute(idPengaduan, new PengaduanRepository.GetDetailCallback() {
            @Override
            public void onBerhasil(DetailPengaduan detail) {
                _detailPengaduan.setValue(detail);
                _isLoading.setValue(false);
            }
            @Override
            public void onGagal(String pesan) {
                _errorMessage.setValue(pesan);
                _isLoading.setValue(false);
            }
        });
    }

    public void simpanPerubahan(
            int idPengaduan,
            int idOperator,
            String status,
            String catatan,
            List<File> files,
            String requestToken
    ) {
        _isUpdating.setValue(true);
        updatePengaduanUseCase.execute(idPengaduan, idOperator, status, catatan, files, requestToken, new OperatorRepository.UpdatePengaduanCallback() {
            @Override
            public void onUpdateBerhasil(String pesan) {
                _updateSuccessMessage.setValue(pesan);
                _isUpdating.setValue(false);
            }
            @Override
            public void onUpdateGagal(String pesan) {
                _errorMessage.setValue(pesan);
                _isUpdating.setValue(false);
            }
        });
    }

    public void hapusPengaduan(int idPengaduan, String alasan) {
        _isDeleting.setValue(true);
        hapusPengaduanUseCase.execute(idPengaduan, alasan, new OperatorRepository.DeletePengaduanCallback() {
            @Override
            public void onDeleteBerhasil(String message) {
                _deleteSuccessMessage.setValue(message);
                _isDeleting.setValue(false);
            }
            @Override
            public void onDeleteGagal(String message) {
                _errorMessage.setValue(message);
                _isDeleting.setValue(false);
            }
        });
    }

    public void clearErrorMessage() {
        _errorMessage.setValue(null);
    }
}