package com.alisteners.pengaduan.domain.operator.usecase;

import com.alisteners.pengaduan.domain.operator.repository.OperatorRepository;

/**
 * Use case untuk menangani logika bisnis pengambilan daftar pengaduan untuk operator.
 * Kelas ini bertindak sebagai perantara antara lapisan presentasi (ViewModel) dan
 * lapisan data (Repository), memastikan bahwa logika bisnis terpusat dan terpisah
 * dari detail implementasi.
 */
public class GetPengaduanOperatorUseCase {
    private final OperatorRepository repository;

    /**
     * Konstruktor untuk GetPengaduanOperatorUseCase.
     *
     * @param repository Implementasi dari {@link OperatorRepository} yang akan digunakan
     * untuk mengambil data dari data source (misalnya, server API).
     */
    public GetPengaduanOperatorUseCase(OperatorRepository repository) {
        this.repository = repository;
    }

    /**
     * Mengeksekusi use case untuk mengambil daftar pengaduan berdasarkan status.
     * Metode ini dapat diperluas di masa depan untuk menambahkan logika bisnis tambahan,
     * seperti validasi input status atau caching data.
     *
     * @param status Status pengaduan yang ingin diambil ("menunggu", "diproses", "selesai").
     * @param callback Callback yang akan dipanggil untuk mengembalikan hasil operasi
     * ke pemanggil (biasanya ViewModel).
     */
    public void execute(String status, OperatorRepository.GetPengaduanCallback callback) {
        // Logika bisnis bisa ditambahkan di sini.
        // Misalnya, memeriksa apakah nilai 'status' valid sebelum meneruskannya.
        // Untuk saat ini, permintaan langsung diteruskan ke repository.
        repository.getPengaduanByStatus(status, callback);
    }
}