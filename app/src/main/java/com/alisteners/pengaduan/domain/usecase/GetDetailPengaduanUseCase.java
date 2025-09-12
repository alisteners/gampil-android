package com.alisteners.pengaduan.domain.usecase;

import com.alisteners.pengaduan.domain.repository.PengaduanRepository;

/**
 * Use case untuk menangani logika bisnis pengambilan detail lengkap dari satu pengaduan.
 * Kelas ini menjembatani permintaan detail dari lapisan presentasi (ViewModel)
 * ke lapisan data (Repository) dan dapat berisi logika validasi.
 */
public class GetDetailPengaduanUseCase {
    private final PengaduanRepository pengaduanRepository;

    /**
     * Konstruktor untuk GetDetailPengaduanUseCase.
     *
     * @param pengaduanRepository Implementasi dari {@link PengaduanRepository} yang akan
     * digunakan untuk mengambil data dari data source.
     */
    public GetDetailPengaduanUseCase(PengaduanRepository pengaduanRepository) {
        this.pengaduanRepository = pengaduanRepository;
    }

    /**
     * Mengeksekusi use case untuk mengambil detail pengaduan berdasarkan ID-nya.
     *
     * @param idPengaduan ID dari pengaduan yang ingin diambil detailnya.
     * @param callback Callback yang akan dipanggil untuk mengembalikan hasil operasi
     * (berhasil atau gagal) ke pemanggil (biasanya ViewModel).
     */
    public void execute(int idPengaduan, PengaduanRepository.GetDetailCallback callback) {
        // Contoh logika bisnis sederhana: memastikan ID valid sebelum memanggil repository.
        if (idPengaduan <= 0) {
            callback.onGagal("ID Pengaduan tidak valid.");
            return;
        }

        // Meneruskan permintaan ke repository untuk dieksekusi.
        pengaduanRepository.getDetailPengaduan(idPengaduan, callback);
    }
}