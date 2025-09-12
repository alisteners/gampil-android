package com.alisteners.pengaduan.presentation.masyarakat.detailpengaduan;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.alisteners.pengaduan.domain.entity.DetailPengaduan;
import com.alisteners.pengaduan.domain.repository.PengaduanRepository;
import com.alisteners.pengaduan.domain.usecase.GetDetailPengaduanUseCase;

/**
 * ViewModel untuk {@link DetailPengaduanActivity}.
 * Bertanggung jawab untuk mengambil dan mengelola data detail dari sebuah pengaduan,
 * serta mengekspos state UI (seperti loading, error, dan data detail itu sendiri)
 * melalui LiveData untuk diamati oleh Activity.
 */
public class DetailPengaduanViewModel extends ViewModel {
    private final GetDetailPengaduanUseCase getDetailPengaduanUseCase;

    // --- LiveData untuk State UI ---

    /** LiveData privat yang menyimpan objek detail pengaduan. */
    private final MutableLiveData<DetailPengaduan> _detailPengaduan = new MutableLiveData<>();
    /** LiveData publik yang terekspos ke Activity untuk diamati (read-only). */
    public LiveData<DetailPengaduan> detailPengaduan = _detailPengaduan;

    /** LiveData privat untuk mengelola status loading. */
    private final MutableLiveData<Boolean> _isLoading = new MutableLiveData<>();
    /** LiveData publik untuk status loading. */
    public LiveData<Boolean> isLoading = _isLoading;

    /** LiveData privat untuk menyimpan pesan error. */
    private final MutableLiveData<String> _errorMessage = new MutableLiveData<>();
    /** LiveData publik untuk pesan error. */
    public LiveData<String> errorMessage = _errorMessage;

    /**
     * Konstruktor untuk DetailPengaduanViewModel.
     * Menerima instance GetDetailPengaduanUseCase melalui dependency injection (dari ViewModelFactory).
     *
     * @param getDetailPengaduanUseCase Use case yang akan digunakan untuk mengambil data detail pengaduan.
     */
    public DetailPengaduanViewModel(GetDetailPengaduanUseCase getDetailPengaduanUseCase) {
        this.getDetailPengaduanUseCase = getDetailPengaduanUseCase;
    }

    /**
     * Memulai proses pengambilan data detail pengaduan dari data source.
     * Metode ini akan mengatur status loading, memanggil use case secara asynchronous,
     * dan memperbarui LiveData yang sesuai berdasarkan hasil dari callback.
     *
     * @param idPengaduan ID dari pengaduan yang detailnya ingin dimuat.
     */
    public void loadDetail(int idPengaduan) {
        _isLoading.setValue(true);
        _errorMessage.setValue(null); // Reset pesan error setiap kali memuat data baru

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