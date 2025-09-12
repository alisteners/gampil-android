package com.alisteners.pengaduan.domain.operator.usecase;

import com.alisteners.pengaduan.domain.operator.repository.OperatorRepository;

import java.io.File;
import java.util.List;

/**
 * Use case untuk menangani logika bisnis pembaruan (update) data pengaduan oleh operator.
 * Kelas ini bertanggung jawab untuk mengambil semua data yang diperlukan dari lapisan presentasi
 * dan meneruskannya ke lapisan data untuk diproses.
 */
public class UpdatePengaduanUseCase {
    private final OperatorRepository repository;

    /**
     * Konstruktor for UpdatePengaduanUseCase.
     *
     * @param repository Implementasi dari {@link OperatorRepository} yang akan digunakan
     * untuk melakukan operasi pembaruan data.
     */
    public UpdatePengaduanUseCase(OperatorRepository repository) {
        this.repository = repository;
    }

    /**
     * Mengeksekusi use case untuk memperbarui sebuah pengaduan.
     * Metode ini dapat divalidasi di masa depan, misalnya memastikan status yang dimasukkan valid
     * atau catatan tidak boleh kosong saat statusnya "Selesai".
     *
     * @param idPengaduan ID dari pengaduan yang akan diperbarui.
     * @param idOperator ID dari operator yang melakukan pembaruan.
     * @param status Status baru untuk pengaduan (misal: "Diproses", "Selesai").
     * @param catatan Catatan tindak lanjut yang diberikan oleh operator.
     * @param files Daftar (List) dari file foto bukti tindak lanjut. Bisa kosong.
     * @param requestToken Token unik untuk validasi permintaan di server.
     * @param callback Callback yang akan dipanggil untuk mengembalikan hasil operasi
     * ke pemanggil (biasanya ViewModel).
     */
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