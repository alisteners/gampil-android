package com.alisteners.pengaduan.domain.operator.usecase;

import com.alisteners.pengaduan.domain.operator.repository.OperatorRepository;

import java.io.File;
import java.util.List;

public class UpdatePengaduanUseCase {
    private final OperatorRepository repository;

    public UpdatePengaduanUseCase(OperatorRepository repository) {
        this.repository = repository;
    }

    public void execute(
            int idPengaduan,
            int idOperator,
            String status,
            String catatan,
            List<File> files,
            String requestToken,
            OperatorRepository.UpdatePengaduanCallback callback
    ) {
        repository.updatePengaduan(idPengaduan, idOperator, status, catatan, files, requestToken, callback);
    }
}