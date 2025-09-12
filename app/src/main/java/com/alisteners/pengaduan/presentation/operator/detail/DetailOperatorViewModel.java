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

/**
 * ViewModel untuk {@link DetailPengaduanOperatorActivity}.
 * Bertanggung jawab untuk menangani semua logika bisnis yang terkait dengan detail pengaduan
 * dari sisi operator, termasuk memuat detail, memperbarui status, dan menghapus pengaduan.
 * ViewModel ini berinteraksi dengan beberapa UseCase untuk menjalankan tugas-tugas tersebut.
 */
public class DetailOperatorViewModel extends ViewModel {

    // --- Deklarasi Use Case ---
    private final GetDetailPengaduanUseCase getDetailPengaduanUseCase;
    private final UpdatePengaduanUseCase updatePengaduanUseCase;
    private final HapusPengaduanUseCase hapusPengaduanUseCase;

    // --- Deklarasi LiveData untuk State UI ---
    private final MutableLiveData<DetailPengaduan> _detailPengaduan = new MutableLiveData<>();
    public final LiveData<DetailPengaduan> detailPengaduan = _detailPengaduan;

    private final MutableLiveData<Boolean> _isLoading = new MutableLiveData<>();
    public final LiveData<Boolean> isLoading = _isLoading; // Untuk loading awal

    private final MutableLiveData<Boolean> _isUpdating = new MutableLiveData<>();
    public final LiveData<Boolean> isUpdating; // Untuk proses update

    private final MutableLiveData<Boolean> _isDeleting = new MutableLiveData<>();
    public final LiveData<Boolean> isDeleting; // Untuk proses hapus

    private final MutableLiveData<String> _errorMessage = new MutableLiveData<>();
    public final LiveData<String> errorMessage = _errorMessage;

    private final MutableLiveData<String> _updateSuccessMessage = new MutableLiveData<>();
    public final LiveData<String> updateSuccessMessage = _updateSuccessMessage;

    private final MutableLiveData<String> _deleteSuccessMessage = new MutableLiveData<>();
    public final LiveData<String> deleteSuccessMessage = _deleteSuccessMessage;

    /**
     * Konstruktor untuk DetailOperatorViewModel.
     * Menerima semua UseCase yang diperlukan melalui dependency injection.
     *
     * @param getDetail UseCase untuk mengambil detail pengaduan.
     * @param update    UseCase untuk memperbarui pengaduan.
     * @param hapus     UseCase untuk menghapus pengaduan.
     */
    public DetailOperatorViewModel(
            GetDetailPengaduanUseCase getDetail,
            UpdatePengaduanUseCase update,
            HapusPengaduanUseCase hapus
    ) {
        this.getDetailPengaduanUseCase = getDetail;
        this.updatePengaduanUseCase = update;
        this.hapusPengaduanUseCase = hapus;
        // Inisialisasi LiveData publik
        this.isUpdating = _isUpdating;
        this.isDeleting = _isDeleting;
    }

    /**
     * Memulai proses pengambilan data detail pengaduan dari data source.
     * @param idPengaduan ID dari pengaduan yang detailnya ingin dimuat.
     */
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

    /**
     * Memulai proses pembaruan (update) status dan data tindak lanjut pengaduan.
     *
     * @param idPengaduan  ID pengaduan yang akan diperbarui.
     * @param idOperator   ID operator yang melakukan pembaruan.
     * @param status       Status baru untuk pengaduan.
     * @param catatan      Catatan tindak lanjut dari operator.
     * @param files        Daftar file foto bukti tindak lanjut.
     * @param requestToken Token unik untuk validasi request.
     */
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

    /**
     * Memulai proses penghapusan pengaduan.
     *
     * @param idPengaduan ID pengaduan yang akan dihapus.
     * @param alasan      Teks alasan mengapa pengaduan dihapus.
     */
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

    /**
     * Membersihkan (mereset) LiveData pesan error menjadi null.
     * Ini dilakukan untuk mencegah pesan error ditampilkan lagi secara tidak sengaja.
     */
    public void clearErrorMessage() {
        _errorMessage.setValue(null);
    }
}