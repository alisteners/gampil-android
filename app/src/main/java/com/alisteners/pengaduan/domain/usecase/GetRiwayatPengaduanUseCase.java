package com.alisteners.pengaduan.domain.usecase;

import com.alisteners.pengaduan.domain.repository.PengaduanRepository;

/**
 * Use case untuk menangani logika bisnis pengambilan daftar riwayat pengaduan
 * milik seorang pengguna.
 * Kelas ini menjembatani permintaan dari lapisan presentasi (ViewModel) ke
 * lapisan data (Repository).
 */
public class GetRiwayatPengaduanUseCase {
    private final PengaduanRepository pengaduanRepository;

    /**
     * Konstruktor untuk GetRiwayatPengaduanUseCase.
     *
     * @param pengaduanRepository Implementasi dari {@link PengaduanRepository} yang akan
     * digunakan untuk mengambil data dari data source.
     */
    public GetRiwayatPengaduanUseCase(PengaduanRepository pengaduanRepository) {
        this.pengaduanRepository = pengaduanRepository;
    }

    /**
     * Mengeksekusi use case untuk mengambil daftar riwayat pengaduan.
     *
     * @param idUser ID dari pengguna yang riwayatnya ingin diambil.
     * @param callback Callback yang akan dipanggil untuk mengembalikan hasil operasi
     * ke pemanggil (biasanya ViewModel).
     */
    public void execute(int idUser, PengaduanRepository.GetRiwayatCallback callback) {
        // Contoh logika bisnis: validasi ID pengguna sebelum meneruskan ke repository.
        if (idUser <= 0) {
            callback.onGagal("ID User tidak valid untuk mengambil riwayat.");
            return;
        }
        pengaduanRepository.getRiwayatPengaduan(idUser, callback);
    }
}