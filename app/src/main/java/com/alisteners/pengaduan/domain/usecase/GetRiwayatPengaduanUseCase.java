package com.alisteners.pengaduan.domain.usecase;

import com.alisteners.pengaduan.domain.repository.PengaduanRepository;

public class GetRiwayatPengaduanUseCase {
    private final PengaduanRepository pengaduanRepository;

    public GetRiwayatPengaduanUseCase(PengaduanRepository pengaduanRepository) {
        this.pengaduanRepository = pengaduanRepository;
    }

    public void execute(int idUser, PengaduanRepository.GetRiwayatCallback callback) {
        if (idUser <= 0) {
            callback.onGagal("ID User tidak valid untuk mengambil riwayat.");
            return;
        }
        pengaduanRepository.getRiwayatPengaduan(idUser, callback);
    }
}