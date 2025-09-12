package com.alisteners.pengaduan.presentation.operator.rekap;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.alisteners.pengaduan.domain.operator.entity.RekapData;
import com.alisteners.pengaduan.domain.operator.repository.OperatorRepository;
import com.alisteners.pengaduan.domain.operator.usecase.GetRekapDataUseCase;

/**
 * ViewModel untuk {@link RekapFragment}.
 * Bertanggung jawab untuk mengambil dan mengelola data rekapitulasi statistik,
 * serta mengekspos state UI (seperti loading, error, dan data rekap itu sendiri)
 * melalui LiveData untuk diamati oleh Fragment.
 */
public class RekapViewModel extends ViewModel {
    private final GetRekapDataUseCase getRekapDataUseCase;

    // --- LiveData untuk State UI ---

    /** LiveData privat yang menyimpan objek data rekapitulasi. */
    private final MutableLiveData<RekapData> _rekapData = new MutableLiveData<>();
    /** LiveData publik untuk data rekapitulasi yang diamati oleh Fragment. */
    public LiveData<RekapData> rekapData = _rekapData;

    /** LiveData privat untuk mengelola status loading. */
    private final MutableLiveData<Boolean> _isLoading = new MutableLiveData<>();
    /** LiveData publik untuk status loading. */
    public LiveData<Boolean> isLoading = _isLoading;

    /** LiveData privat untuk menyimpan pesan error. */
    private final MutableLiveData<String> _errorMessage = new MutableLiveData<>();
    /** LiveData publik untuk pesan error. */
    public LiveData<String> errorMessage = _errorMessage;

    /**
     * Konstruktor untuk RekapViewModel.
     * Menerima instance GetRekapDataUseCase melalui dependency injection (dari ViewModelFactory).
     *
     * @param useCase Use case yang akan digunakan untuk mengambil data rekap.
     */
    public RekapViewModel(GetRekapDataUseCase useCase) {
        this.getRekapDataUseCase = useCase;
    }

    /**
     * Memulai proses pengambilan data rekapitulasi statistik dari data source.
     * Metode ini akan mengatur state loading, memanggil use case secara asynchronous,
     * dan memperbarui LiveData yang sesuai berdasarkan hasil dari callback.
     */
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