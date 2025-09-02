package com.alisteners.pengaduan.domain.operator.usecase;

import com.alisteners.pengaduan.domain.operator.repository.OperatorRepository;

public class SearchPengaduanUseCase {
    private final OperatorRepository repository;
    public SearchPengaduanUseCase(OperatorRepository repository) { this.repository = repository; }
    public void execute(String keyword, String status, OperatorRepository.GetPengaduanCallback callback) {
        repository.searchPengaduan(keyword, status, callback);
    }
}
