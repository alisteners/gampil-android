package com.alisteners.pengaduan.domain.operator.usecase;

import com.alisteners.pengaduan.domain.operator.repository.OperatorRepository;

public class GetPengaduanOperatorUseCase {
    private final OperatorRepository repository;

    public GetPengaduanOperatorUseCase(OperatorRepository repository) {
        this.repository = repository;
    }

    /**
     * Mengeksekusi use case untuk mengambil daftar pengaduan berdasarkan status.
     * @param status Status yang diinginkan ("menunggu", "diproses", "selesai").
     * @param callback Callback untuk mengembalikan hasilnya.
     */
    public void execute(String status, OperatorRepository.GetPengaduanCallback callback) {
        // Di sini bisa ditambahkan logika bisnis,
        // misalnya memeriksa apakah status yang diinput valid.
        // Untuk saat ini langsung teruskan ke repository.
        repository.getPengaduanByStatus(status, callback);
    }
}
