package com.alisteners.pengaduan.domain.usecase;

import com.alisteners.pengaduan.domain.repository.PengaduanRepository;

public class GetDetailPengaduanUseCase {
    private final PengaduanRepository pengaduanRepository;

    public GetDetailPengaduanUseCase(PengaduanRepository pengaduanRepository) {
        this.pengaduanRepository = pengaduanRepository;
    }

    /**
     * Mengeksekusi use case untuk mengambil detail pengaduan.
     * @param idPengaduan ID dari pengaduan yang ingin diambil detailnya.
     * @param callback Callback untuk mengembalikan hasil (berhasil atau gagal).
     */
    public void execute(int idPengaduan, PengaduanRepository.GetDetailCallback callback) {
        // Pastikan ID valid sebelum memanggil repository.
        if (idPengaduan <= 0) {
            callback.onGagal("ID Pengaduan tidak valid.");
            return;
        }

        // Meneruskan permintaan ke repository
        pengaduanRepository.getDetailPengaduan(idPengaduan, callback);
    }
}
