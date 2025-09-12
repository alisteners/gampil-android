package com.alisteners.pengaduan.domain.operator.repository;

import com.alisteners.pengaduan.domain.entity.PengaduanPublikItem;
import com.alisteners.pengaduan.domain.operator.entity.RekapData;

import java.io.File;
import java.util.List;

/**
 * Interface yang mendefinisikan kontrak (aturan) untuk semua operasi data
 * yang berkaitan dengan fungsionalitas operator.
 * Ini adalah bagian dari lapisan domain yang memisahkan logika bisnis (UseCases)
 * dari implementasi pengambilan data (RepositoryImpl).
 */
public interface OperatorRepository {

    /**
     * Callback interface untuk menangani hasil pengambilan daftar pengaduan.
     */
    interface GetPengaduanCallback {
        /** Dipanggil saat daftar pengaduan berhasil diambil. */
        void onBerhasil(List<PengaduanPublikItem> list);
        /** Dipanggil saat terjadi kegagalan. */
        void onGagal(String pesanError);
    }

    /**
     * Callback interface untuk menangani hasil dari operasi pembaruan pengaduan.
     */
    interface UpdatePengaduanCallback {
        /** Dipanggil saat pengaduan berhasil diperbarui. */
        void onUpdateBerhasil(String pesan);
        /** Dipanggil saat terjadi kegagalan. */
        void onUpdateGagal(String pesanError);
    }

    /**
     * Callback interface untuk menangani hasil dari operasi penghapusan pengaduan.
     */
    interface DeletePengaduanCallback {
        /** Dipanggil saat pengaduan berhasil dihapus. */
        void onDeleteBerhasil(String pesan);
        /** Dipanggil saat terjadi kegagalan. */
        void onDeleteGagal(String pesanError);
    }

    /**
     * Callback interface untuk menangani hasil pengambilan data rekapitulasi statistik.
     */
    interface GetRekapCallback {
        /** Dipanggil saat data rekap berhasil diambil. */
        void onBerhasil(RekapData data);
        /** Dipanggil saat terjadi kegagalan. */
        void onGagal(String pesanError);
    }

    /**
     * Mengambil daftar pengaduan dari data source, difilter berdasarkan status.
     *
     * @param status Status pengaduan yang ingin diambil (misal: "Menunggu", "Diproses").
     * @param callback Callback untuk menerima hasil operasi.
     */
    void getPengaduanByStatus(String status, GetPengaduanCallback callback);

    /**
     * Memperbarui status, catatan, dan foto tindak lanjut dari sebuah pengaduan.
     *
     * @param idPengaduan ID pengaduan yang akan diperbarui.
     * @param idOperator ID operator yang melakukan pembaruan.
     * @param status Status baru untuk pengaduan.
     * @param catatan Catatan tindak lanjut dari operator.
     * @param files Daftar file foto bukti tindak lanjut (bisa kosong).
     * @param requestToken Token unik untuk validasi request di server.
     * @param callback Callback untuk menerima hasil operasi.
     */
    void updatePengaduan(
            int idPengaduan,
            int idOperator,
            String status,
            String catatan,
            List<File> files,
            String requestToken,
            UpdatePengaduanCallback callback
    );

    /**
     * Menghapus sebuah pengaduan dari data source.
     *
     * @param idPengaduan ID pengaduan yang akan dihapus.
     * @param alasan Teks alasan mengapa pengaduan dihapus.
     * @param callback Callback untuk menerima hasil operasi.
     */
    void hapusPengaduan(int idPengaduan, String alasan, DeletePengaduanCallback callback);

    /**
     * Mengambil data rekapitulasi statistik dari data source.
     *
     * @param callback Callback untuk menerima hasil operasi.
     */
    void getRekapData(GetRekapCallback callback);

    /**
     * Melakukan pencarian pengaduan di data source berdasarkan kata kunci dan status.
     *
     * @param keyword Kata kunci untuk pencarian.
     * @param status Status untuk memfilter hasil pencarian.
     * @param callback Callback untuk menerima hasil operasi (menggunakan GetPengaduanCallback yang sama).
     */
    void searchPengaduan(String keyword, String status, GetPengaduanCallback callback);
}