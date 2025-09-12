package com.alisteners.pengaduan.domain.operator.usecase;

import com.alisteners.pengaduan.domain.operator.repository.OperatorRepository;

/**
 * Use case untuk menangani logika bisnis pengambilan data rekapitulasi statistik.
 * Kelas ini menjembatani permintaan data rekap dari lapisan presentasi (ViewModel)
 * ke lapisan data (Repository).
 */
public class GetRekapDataUseCase {
    private final OperatorRepository repository;

    /**
     * Konstruktor untuk GetRekapDataUseCase.
     *
     * @param repository Implementasi dari {@link OperatorRepository} yang akan digunakan
     * untuk mengambil data dari data source.
     */
    public GetRekapDataUseCase(OperatorRepository repository) {
        this.repository = repository;
    }

    /**
     * Mengeksekusi use case untuk mengambil data rekapitulasi statistik.
     * Permintaan akan diteruskan langsung ke metode getRekapData di repository.
     *
     * @param callback Callback yang akan dipanggil untuk mengembalikan hasil operasi
     * ke pemanggil (biasanya ViewModel).
     */
    public void execute(OperatorRepository.GetRekapCallback callback) {
        repository.getRekapData(callback);
    }
}