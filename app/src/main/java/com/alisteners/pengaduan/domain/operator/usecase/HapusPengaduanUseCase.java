package com.alisteners.pengaduan.domain.operator.usecase;

import com.alisteners.pengaduan.domain.operator.repository.OperatorRepository;

/**
 * Use case untuk menangani logika bisnis penghapusan sebuah pengaduan oleh operator.
 * Kelas ini memastikan bahwa semua parameter yang diperlukan untuk penghapusan
 * (seperti ID dan alasan) diteruskan dengan benar ke lapisan data.
 */
public class HapusPengaduanUseCase {
    private final OperatorRepository repository;

    /**
     * Konstruktor untuk HapusPengaduanUseCase.
     *
     * @param repository Implementasi dari {@link OperatorRepository} yang akan digunakan
     * untuk melakukan operasi penghapusan data.
     */
    public HapusPengaduanUseCase(OperatorRepository repository) {
        this.repository = repository;
    }

    /**
     * Mengeksekusi use case untuk menghapus sebuah pengaduan.
     *
     * @param idPengaduan ID dari pengaduan yang akan dihapus.
     * @param alasan Teks alasan mengapa pengaduan tersebut dihapus.
     * @param callback Callback yang akan dipanggil untuk mengembalikan hasil operasi
     * ke pemanggil (biasanya ViewModel).
     */
    public void execute(int idPengaduan, String alasan, OperatorRepository.DeletePengaduanCallback callback) {
        // Di masa depan, di sini bisa ditambahkan validasi,
        // misalnya memeriksa apakah alasan tidak boleh kosong.
        repository.hapusPengaduan(idPengaduan, alasan, callback);
    }
}