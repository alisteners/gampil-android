package com.alisteners.pengaduan.presentation.masyarakat.riwayatpengaduan;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.alisteners.pengaduan.domain.entity.PengaduanRiwayatItem;
import com.alisteners.pengaduan.domain.repository.PengaduanRepository;
import com.alisteners.pengaduan.domain.usecase.GetRiwayatPengaduanUseCase;

import java.util.List;
import java.util.ArrayList;

/**
 * ViewModel untuk {@link RiwayatFragment}.
 * Bertanggung jawab untuk mengambil dan mengelola data riwayat pengaduan pengguna,
 * serta mengekspos state UI (seperti loading, error, dan daftar data) melalui LiveData
 * untuk diamati oleh Fragment.
 */
public class RiwayatPengaduanViewModel extends ViewModel {
    private final GetRiwayatPengaduanUseCase getRiwayatPengaduanUseCase;

    // --- LiveData untuk State UI ---

    /** LiveData privat yang menyimpan daftar riwayat pengaduan. */
    private final MutableLiveData<List<PengaduanRiwayatItem>> _riwayatList = new MutableLiveData<>();
    /** LiveData publik yang terekspos ke Fragment untuk diamati (read-only). */
    public LiveData<List<PengaduanRiwayatItem>> riwayatList = _riwayatList;

    /** LiveData privat untuk mengelola status loading. */
    private final MutableLiveData<Boolean> _isLoading = new MutableLiveData<>();
    /** LiveData publik untuk status loading. */
    public LiveData<Boolean> isLoading = _isLoading;

    /** LiveData privat untuk menyimpan pesan error. */
    private final MutableLiveData<String> _errorMessage = new MutableLiveData<>();
    /** LiveData publik untuk pesan error. */
    public LiveData<String> errorMessage = _errorMessage;

    /** LiveData privat untuk mengelola state jika data kosong. (Sudah di-handle di dalam riwayatList) */
    private final MutableLiveData<Boolean> _isEmpty = new MutableLiveData<>();
    /** LiveData publik untuk state data kosong. */
    public LiveData<Boolean> isEmpty = _isEmpty;


    /**
     * Konstruktor untuk RiwayatPengaduanViewModel.
     * Menerima instance GetRiwayatPengaduanUseCase melalui dependency injection (dari ViewModelFactory).
     *
     * @param getRiwayatPengaduanUseCase Use case yang akan digunakan untuk mengambil data riwayat.
     */
    public RiwayatPengaduanViewModel(GetRiwayatPengaduanUseCase getRiwayatPengaduanUseCase) {
        this.getRiwayatPengaduanUseCase = getRiwayatPengaduanUseCase;
    }

    /**
     * Memulai proses pengambilan data riwayat pengaduan dari data source.
     * Metode ini akan mengatur state loading, memanggil use case secara asynchronous,
     * dan memperbarui LiveData yang sesuai berdasarkan hasil dari callback.
     *
     * @param idUser ID dari pengguna yang riwayatnya ingin dimuat.
     */
    public void loadRiwayat(int idUser) {
        _isLoading.setValue(true);
        _errorMessage.setValue(null); // Reset pesan error setiap kali memuat data baru

        getRiwayatPengaduanUseCase.execute(idUser, new PengaduanRepository.GetRiwayatCallback() {
            @Override
            public void onBerhasil(List<PengaduanRiwayatItem> daftarRiwayat) {
                _isLoading.setValue(false);
                // Cukup perbarui list. Logika untuk menampilkan pesan "kosong"
                // akan ditangani di Fragment berdasarkan ukuran list ini.
                if (daftarRiwayat != null) {
                    _riwayatList.setValue(daftarRiwayat);
                } else {
                    _riwayatList.setValue(new ArrayList<>()); // Kirim list kosong jika null
                }
            }

            @Override
            public void onGagal(String pesanError) {
                _isLoading.setValue(false);
                _errorMessage.setValue(pesanError);
                _riwayatList.setValue(new ArrayList<>()); // Kirim list kosong saat gagal
            }
        });
    }
}