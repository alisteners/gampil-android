package com.alisteners.pengaduan.presentation.masyarakat.beranda;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.alisteners.pengaduan.domain.entity.PengaduanPublikItem;
import com.alisteners.pengaduan.domain.repository.PengaduanRepository;
import com.alisteners.pengaduan.domain.usecase.GetPengaduanPublikUseCase;

import java.util.List;

/**
 * ViewModel untuk {@link BerandaFragment}.
 * Bertanggung jawab untuk mengambil dan mengelola data yang akan ditampilkan di Beranda,
 * serta mengekspos state UI (seperti loading, error, dan daftar data) melalui LiveData
 * untuk diamati oleh Fragment.
 */
public class BerandaViewModel extends ViewModel {
    private final GetPengaduanPublikUseCase getPengaduanPublikUseCase;

    // --- LiveData untuk State UI ---

    /** LiveData privat yang menyimpan daftar pengaduan. Hanya bisa diubah dari dalam ViewModel. */
    private final MutableLiveData<List<PengaduanPublikItem>> _pengaduanList = new MutableLiveData<>();
    /** LiveData publik yang terekspos ke Fragment untuk diamati (read-only). */
    public LiveData<List<PengaduanPublikItem>> pengaduanList = _pengaduanList;

    /** LiveData privat untuk mengelola status loading. */
    private final MutableLiveData<Boolean> _isLoading = new MutableLiveData<>();
    /** LiveData publik untuk status loading. */
    public LiveData<Boolean> isLoading = _isLoading;

    /** LiveData privat untuk mengelola state jika data kosong. */
    private final MutableLiveData<Boolean> _isEmpty = new MutableLiveData<>();
    /** LiveData publik untuk state data kosong. */
    public LiveData<Boolean> isEmpty = _isEmpty;

    /** LiveData privat untuk menyimpan pesan error. */
    private final MutableLiveData<String> _errorMessage = new MutableLiveData<>();
    /** LiveData publik untuk pesan error. */
    public LiveData<String> errorMessage = _errorMessage;

    /**
     * Konstruktor untuk BerandaViewModel.
     * Menerima instance GetPengaduanPublikUseCase melalui dependency injection (dari ViewModelFactory).
     *
     * @param getPengaduanPublikUseCase Use case yang akan digunakan untuk mengambil data pengaduan publik.
     */
    public BerandaViewModel(GetPengaduanPublikUseCase getPengaduanPublikUseCase) {
        this.getPengaduanPublikUseCase = getPengaduanPublikUseCase;
    }

    /**
     * Memulai proses pengambilan data daftar pengaduan publik dari data source.
     * Metode ini memanggil use case secara asynchronous dan memperbarui LiveData
     * sesuai dengan hasil yang diterima (berhasil atau gagal).
     *
     * @param status Status pengaduan yang akan digunakan untuk memfilter data.
     * @param showLoading boolean flag untuk menentukan apakah indikator loading awal perlu ditampilkan.
     */
    public void loadPengaduanPublik(String status, boolean showLoading) {
        if (showLoading) {
            _isLoading.setValue(true);
        }

        // Mengeksekusi use case
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