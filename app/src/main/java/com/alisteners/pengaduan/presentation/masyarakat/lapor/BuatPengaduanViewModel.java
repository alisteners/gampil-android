package com.alisteners.pengaduan.presentation.masyarakat.lapor;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.annotation.Nullable;

import com.alisteners.pengaduan.domain.entity.PengaduanData;
import com.alisteners.pengaduan.domain.repository.PengaduanRepository;
import com.alisteners.pengaduan.domain.usecase.KirimPengaduanUseCase;

import java.io.File;

/**
 * ViewModel untuk {@link BuatPengaduanActivity}.
 * Bertanggung jawab untuk memproses logika pengiriman pengaduan baru,
 * berinteraksi dengan UseCase, dan mengelola state UI (loading, sukses, error)
 * yang akan diamati oleh Activity.
 */
public class BuatPengaduanViewModel extends ViewModel {
    private final KirimPengaduanUseCase kirimPengaduanUseCase;

    // --- LiveData untuk State UI ---

    /** LiveData privat untuk mengelola status loading. */
    private final MutableLiveData<Boolean> _isLoading = new MutableLiveData<>();
    /** LiveData publik untuk status loading yang diamati oleh Activity. */
    public LiveData<Boolean> isLoading = _isLoading;

    /** LiveData privat untuk menyimpan pesan sukses setelah pengiriman berhasil. */
    private final MutableLiveData<String> _pesanSukses = new MutableLiveData<>();
    /** LiveData publik untuk pesan sukses. */
    public LiveData<String> pesanSukses = _pesanSukses;

    /** LiveData privat untuk menyimpan pesan error. */
    private final MutableLiveData<String> _pesanError = new MutableLiveData<>();
    /** LiveData publik untuk pesan error. */
    public LiveData<String> pesanError = _pesanError;

    /**
     * Konstruktor untuk BuatPengaduanViewModel.
     * Menerima instance KirimPengaduanUseCase melalui dependency injection (dari ViewModelFactory).
     *
     * @param kirimPengaduanUseCase Use case yang akan digunakan untuk mengirim data pengaduan.
     */
    public BuatPengaduanViewModel(KirimPengaduanUseCase kirimPengaduanUseCase) {
        this.kirimPengaduanUseCase = kirimPengaduanUseCase;
    }

    /**
     * Memulai proses pengiriman data pengaduan ke data source melalui use case.
     * Metode ini akan mengatur status loading dan memperbarui LiveData yang sesuai
     * berdasarkan hasil dari callback.
     *
     * @param pengaduanData Objek yang berisi semua data pengaduan.
     * @param fileRle File data utama (RLE atau fallback JPG), bisa null.
     * @param fileThumbnail File thumbnail, tidak boleh null.
     */
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

    /**
     * Metode untuk melaporkan error yang terjadi secara lokal di Activity (misalnya, gagal memproses gambar)
     * tanpa melalui server.
     *
     * @param pesan Pesan error yang akan ditampilkan.
     */
    public void laporkanErrorLokal(String pesan) {
        _isLoading.setValue(false);
        _pesanError.setValue(pesan);
    }

    /**
     * Membersihkan (mereset) LiveData pesan sukses menjadi null.
     * Ini dilakukan untuk mencegah pesan sukses ditampilkan lagi, misalnya saat orientasi layar berubah.
     */
    public void clearPesanSukses() {
        _pesanSukses.setValue(null);
    }

    /**
     * Membersihkan (mereset) LiveData pesan error menjadi null.
     * Ini dilakukan untuk mencegah pesan error ditampilkan lagi.
     */
    public void clearPesanError() {
        _pesanError.setValue(null);
    }
}