package com.alisteners.pengaduan.domain.usecase;

import com.alisteners.pengaduan.domain.repository.PengaduanRepository;

/**
 * Use case untuk menangani logika bisnis pengambilan daftar pengaduan publik.
 * Kelas ini menjembatani permintaan data dari lapisan presentasi (ViewModel)
 * ke lapisan data (Repository) untuk fungsionalitas di Beranda.
 */
public class GetPengaduanPublikUseCase {
    private final PengaduanRepository pengaduanRepository;

    /**
     * Konstruktor untuk GetPengaduanPublikUseCase.
     *
     * @param pengaduanRepository Implementasi dari {@link PengaduanRepository} yang akan
     * digunakan untuk mengambil data dari data source.
     */
    public GetPengaduanPublikUseCase(PengaduanRepository pengaduanRepository) {
        this.pengaduanRepository = pengaduanRepository;
    }

    /**
     * Mengeksekusi use case untuk mengambil daftar pengaduan publik.
     *
     * @param status Status pengaduan untuk memfilter daftar (misal: "Selesai").
     * @param callback Callback yang akan dipanggil untuk mengembalikan hasil operasi
     * ke pemanggil (biasanya ViewModel).
     */
    public void execute(String status, PengaduanRepository.GetPengaduanPublikCallback callback) {
        // Meneruskan permintaan langsung ke repository.
        // Logika bisnis tambahan dapat ditambahkan di sini jika diperlukan di masa depan.
        pengaduanRepository.getPengaduanPublik(status, callback);
    }
}