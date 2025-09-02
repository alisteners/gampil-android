package com.alisteners.pengaduan.domain.operator.usecase;

import com.alisteners.pengaduan.domain.operator.repository.OperatorRepository;
public class GetRekapDataUseCase {
    private final OperatorRepository repository;
    public GetRekapDataUseCase(OperatorRepository repository) { this.repository = repository; }
    public void execute(OperatorRepository.GetRekapCallback callback) {
        repository.getRekapData(callback);
    }
}