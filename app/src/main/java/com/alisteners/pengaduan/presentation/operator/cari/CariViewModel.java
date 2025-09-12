package com.alisteners.pengaduan.presentation.operator.cari;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.alisteners.pengaduan.domain.entity.PengaduanPublikItem;
import com.alisteners.pengaduan.domain.operator.repository.OperatorRepository;
import com.alisteners.pengaduan.domain.operator.usecase.SearchPengaduanUseCase;

import java.util.List;

/**
 * ViewModel untuk {@link CariFragment}.
 * Bertanggung jawab untuk memproses logika pencarian pengaduan, berinteraksi dengan UseCase,
 * dan mengelola state UI (loading, hasil pencarian, error) yang akan diamati oleh Fragment.
 */
public class CariViewModel extends ViewModel {
    private final SearchPengaduanUseCase searchPengaduanUseCase;

    // --- LiveData untuk State UI ---

    /** LiveData privat yang menyimpan daftar hasil pencarian. */
    private final MutableLiveData<List<PengaduanPublikItem>> _hasilPencarian = new MutableLiveData<>();
    /** LiveData publik untuk hasil pencarian yang diamati oleh Fragment. */
    public LiveData<List<PengaduanPublikItem>> hasilPencarian = _hasilPencarian;

    /** LiveData privat untuk mengelola status loading. */
    private final MutableLiveData<Boolean> _isLoading = new MutableLiveData<>();
    /** LiveData publik untuk status loading. */
    public LiveData<Boolean> isLoading = _isLoading;

    /** LiveData privat untuk menyimpan pesan error. */
    private final MutableLiveData<String> _errorMessage = new MutableLiveData<>();
    /** LiveData publik untuk pesan error. */
    public LiveData<String> errorMessage = _errorMessage;

    /**
     * Konstruktor untuk CariViewModel.
     * Menerima instance SearchPengaduanUseCase melalui dependency injection (dari ViewModelFactory).
     *
     * @param useCase Use case yang akan digunakan untuk melakukan pencarian.
     */
    public CariViewModel(SearchPengaduanUseCase useCase) {
        this.searchPengaduanUseCase = useCase;
    }

    /**
     * Memulai proses pencarian pengaduan di data source.
     * Metode ini akan mengatur status loading, memanggil use case secara asynchronous,
     * dan memperbarui LiveData yang sesuai berdasarkan hasil dari callback.
     *
     * @param keyword Kata kunci yang akan dicari.
     * @param status Status untuk memfilter hasil pencarian.
     */
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