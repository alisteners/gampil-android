package com.alisteners.pengaduan.presentation.operator.dashboard;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.alisteners.pengaduan.domain.entity.PengaduanPublikItem;
import com.alisteners.pengaduan.domain.operator.repository.OperatorRepository;
import com.alisteners.pengaduan.domain.operator.usecase.GetPengaduanOperatorUseCase;

import java.util.List;

/**
 * ViewModel untuk {@link PengaduanListOperatorFragment}.
 * Bertanggung jawab untuk mengambil dan mengelola daftar pengaduan berdasarkan status tertentu
 * (misalnya "menunggu", "diproses", "selesai") untuk ditampilkan kepada operator.
 * Mengekspos state UI melalui LiveData untuk diamati oleh Fragment.
 */
public class OperatorListViewModel extends ViewModel {
    private final GetPengaduanOperatorUseCase getPengaduanOperatorUseCase;

    // --- LiveData untuk State UI ---

    /** LiveData privat yang menyimpan daftar pengaduan. */
    private final MutableLiveData<List<PengaduanPublikItem>> _pengaduanList = new MutableLiveData<>();
    /** LiveData publik untuk daftar pengaduan yang diamati oleh Fragment. */
    public LiveData<List<PengaduanPublikItem>> pengaduanList = _pengaduanList;

    /** LiveData privat untuk mengelola status loading. */
    private final MutableLiveData<Boolean> _isLoading = new MutableLiveData<>();
    /** LiveData publik untuk status loading. */
    public LiveData<Boolean> isLoading = _isLoading;

    /** LiveData privat untuk menyimpan pesan error. */
    private final MutableLiveData<String> _errorMessage = new MutableLiveData<>();
    /** LiveData publik untuk pesan error. */
    public LiveData<String> errorMessage = _errorMessage;

    /**
     * Konstruktor untuk OperatorListViewModel.
     * Menerima instance GetPengaduanOperatorUseCase melalui dependency injection (dari ViewModelFactory).
     *
     * @param useCase Use case yang akan digunakan untuk mengambil data pengaduan.
     */
    public OperatorListViewModel(GetPengaduanOperatorUseCase useCase) {
        this.getPengaduanOperatorUseCase = useCase;
    }

    /**
     * Memulai proses pengambilan daftar pengaduan dari data source berdasarkan status.
     * Metode ini akan mengatur state loading, memanggil use case secara asynchronous,
     * dan memperbarui LiveData yang sesuai berdasarkan hasil dari callback.
     *
     * @param status Status dari pengaduan yang ingin dimuat (misal: "menunggu").
     */
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