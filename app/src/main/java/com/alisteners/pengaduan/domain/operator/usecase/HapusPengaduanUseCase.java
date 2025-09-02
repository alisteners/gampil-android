package com.alisteners.pengaduan.domain.operator.usecase;

import com.alisteners.pengaduan.domain.operator.repository.OperatorRepository;

public class HapusPengaduanUseCase {
    private final OperatorRepository repository;
    public HapusPengaduanUseCase(OperatorRepository repository) { this.repository = repository; }
    public void execute(int idPengaduan, String alasan, OperatorRepository.DeletePengaduanCallback callback) {
        repository.hapusPengaduan(idPengaduan, alasan, callback);
    }
}