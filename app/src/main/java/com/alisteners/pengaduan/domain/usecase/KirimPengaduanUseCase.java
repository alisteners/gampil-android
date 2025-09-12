package com.alisteners.pengaduan.domain.usecase;

import com.alisteners.pengaduan.domain.entity.PengaduanData;
import com.alisteners.pengaduan.domain.repository.PengaduanRepository;
import java.io.File;
import androidx.annotation.Nullable;

/**
 * Use case untuk menangani logika bisnis pengiriman pengaduan baru.
 * Kelas ini bertanggung jawab untuk melakukan validasi data input sebelum
 * meneruskannya ke lapisan data (Repository) untuk dikirim ke server.
 */
public class KirimPengaduanUseCase {
    private final PengaduanRepository pengaduanRepository;

    /**
     * Konstruktor untuk KirimPengaduanUseCase.
     *
     * @param pengaduanRepository Implementasi dari {@link PengaduanRepository} yang akan
     * digunakan untuk mengirim data ke data source.
     */
    public KirimPengaduanUseCase(PengaduanRepository pengaduanRepository) {
        this.pengaduanRepository = pengaduanRepository;
    }

    /**
     * Mengeksekusi use case untuk mengirim data pengaduan baru.
     * Metode ini melakukan validasi awal terhadap data penting seperti deskripsi,
     * persetujuan pernyataan, dan keberadaan file thumbnail.
     *
     * @param dataPengaduan Objek {@link PengaduanData} yang berisi semua informasi pengaduan.
     * @param fileRle Objek File yang menunjuk ke data utama yang akan diunggah (RLE atau fallback JPG). Boleh null.
     * @param fileThumbnail Objek File yang menunjuk ke gambar thumbnail. Wajib ada.
     * @param callback Callback yang akan dipanggil untuk mengembalikan hasil operasi
     * ke pemanggil (biasanya ViewModel).
     */
    public void execute(PengaduanData dataPengaduan, @Nullable File fileRle, @Nullable File fileThumbnail, PengaduanRepository.KirimPengaduanCallback callback) {
        // Validasi dasar untuk memastikan data krusial tidak kosong.
        if (dataPengaduan.deskripsi == null || dataPengaduan.deskripsi.trim().isEmpty()) {
            callback.onGagal("Deskripsi pengaduan tidak boleh kosong.");
            return;
        }
        if (!dataPengaduan.pernyataan) {
            callback.onGagal("Pernyataan harus disetujui.");
            return;
        }
        // Validasi keberadaan file thumbnail, karena ini wajib untuk ditampilkan di daftar.
        if (fileThumbnail == null || !fileThumbnail.exists() || fileThumbnail.length() == 0) {
            callback.onGagal("File thumbnail tidak valid atau kosong.");
            return;
        }

        // Jika semua validasi lolos, teruskan data ke repository untuk dikirim.
        pengaduanRepository.kirimPengaduanKeServer(dataPengaduan, fileRle, fileThumbnail, callback);
    }
}