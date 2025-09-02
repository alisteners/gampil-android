package com.alisteners.pengaduan.domain.usecase;

import com.alisteners.pengaduan.domain.repository.PengaduanRepository;

public class GetPengaduanPublikUseCase {
    private final PengaduanRepository pengaduanRepository;

    public GetPengaduanPublikUseCase(PengaduanRepository pengaduanRepository) {
        this.pengaduanRepository = pengaduanRepository;
    }

    public void execute(String status, PengaduanRepository.GetPengaduanPublikCallback callback) {
        pengaduanRepository.getPengaduanPublik(status, callback);
    }
}