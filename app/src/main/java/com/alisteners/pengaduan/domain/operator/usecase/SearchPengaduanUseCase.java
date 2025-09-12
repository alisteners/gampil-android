package com.alisteners.pengaduan.domain.operator.usecase;

import com.alisteners.pengaduan.domain.operator.repository.OperatorRepository;

/**
 * Use case untuk menangani logika bisnis pencarian pengaduan.
 * Kelas ini bertanggung jawab untuk meneruskan kriteria pencarian dari
 * lapisan presentasi (ViewModel) ke lapisan data (Repository).
 */
public class SearchPengaduanUseCase {
    private final OperatorRepository repository;

    /**
     * Konstruktor untuk SearchPengaduanUseCase.
     *
     * @param repository Implementasi dari {@link OperatorRepository} yang akan digunakan
     * untuk melakukan operasi pencarian data.
     */
    public SearchPengaduanUseCase(OperatorRepository repository) {
        this.repository = repository;
    }

    /**
     * Mengeksekusi use case untuk melakukan pencarian pengaduan.
     *
     * @param keyword Kata kunci yang akan digunakan untuk mencari pengaduan.
     * @param status Status pengaduan untuk memfilter hasil pencarian.
     * @param callback Callback yang akan dipanggil untuk mengembalikan hasil operasi
     * ke pemanggil (biasanya ViewModel).
     */
    public void execute(String keyword, String status, OperatorRepository.GetPengaduanCallback callback) {
        // Logika bisnis tambahan, seperti sanitasi input 'keyword' untuk keamanan,
        // bisa ditambahkan di sini sebelum meneruskan ke repository.
        repository.searchPengaduan(keyword, status, callback);
    }
}