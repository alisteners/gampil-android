package com.alisteners.pengaduan.domain.repository;

import com.alisteners.pengaduan.domain.entity.DetailPengaduan;
import com.alisteners.pengaduan.domain.entity.PengaduanData;
import com.alisteners.pengaduan.domain.entity.PengaduanPublikItem;
import com.alisteners.pengaduan.domain.entity.PengaduanRiwayatItem;
import com.alisteners.pengaduan.domain.operator.repository.OperatorRepository;

import androidx.annotation.Nullable;

import java.io.File;
import java.util.List;

/**
 * Interface yang mendefinisikan kontrak (aturan) untuk semua operasi data
 * yang berkaitan dengan fungsionalitas pengguna (Masyarakat).
 * Ini adalah bagian dari lapisan domain yang memisahkan logika bisnis (UseCases)
 * dari implementasi pengambilan data (RepositoryImpl).
 */
public interface PengaduanRepository {

    // Catatan: Metode hapus, rekap, dan search di bawah ini tampaknya lebih cocok
    // berada di OperatorRepository, namun didokumentasikan sesuai file yang diberikan.

    void hapusPengaduan(int idPengaduan, OperatorRepository.DeletePengaduanCallback callback);

    void getRekapData(OperatorRepository.GetRekapCallback callback);

    void searchPengaduan(String keyword, String status, OperatorRepository.GetPengaduanCallback callback);

    /**
     * Callback interface untuk menangani hasil dari operasi pengiriman pengaduan baru.
     */
    interface KirimPengaduanCallback {
        /** Dipanggil saat pengaduan berhasil dikirim ke server. */
        void onBerhasil(String pesan);
        /** Dipanggil saat terjadi kegagalan. */
        void onGagal(String pesanError);
    }

    /**
     * Callback interface untuk menangani hasil pengambilan daftar riwayat pengaduan pengguna.
     */
    interface GetRiwayatCallback {
        /** Dipanggil saat daftar riwayat berhasil diambil. */
        void onBerhasil(List<PengaduanRiwayatItem> riwayatList);
        /** Dipanggil saat terjadi kegagalan. */
        void onGagal(String pesanError);
    }

    /**
     * Callback interface untuk menangani hasil pengambilan detail satu pengaduan.
     */
    interface GetDetailCallback {
        /** Dipanggil saat detail pengaduan berhasil diambil. */
        void onBerhasil(DetailPengaduan detailPengaduan);
        /** Dipanggil saat terjadi kegagalan. */
        void onGagal(String pesanError);
    }

    /**
     * Callback interface untuk menangani hasil pengambilan daftar pengaduan publik.
     */
    interface GetPengaduanPublikCallback {
        /** Dipanggil saat daftar pengaduan publik berhasil diambil. */
        void onBerhasil(List<PengaduanPublikItem> list);
        /** Dipanggil saat terjadi kegagalan. */
        void onGagal(String pesanError);
    }

    /**
     * Mengirim data pengaduan baru yang lengkap ke data source (server).
     *
     * @param dataPengaduan Objek yang berisi semua data teks dan metrik analisis pengaduan.
     * @param fileData Objek File yang menunjuk ke data utama yang akan diunggah (RLE atau fallback JPG).
     * @param fileThumbnail Objek File yang menunjuk ke gambar thumbnail yang akan diunggah.
     * @param callback Callback untuk menerima hasil operasi.
     */
    void kirimPengaduanKeServer(PengaduanData dataPengaduan, @Nullable File fileData, @Nullable File fileThumbnail, KirimPengaduanCallback callback);

    /**
     * Mengambil daftar riwayat pengaduan untuk seorang pengguna.
     *
     * @param idUser ID dari pengguna yang riwayatnya ingin diambil.
     * @param callback Callback untuk menerima hasil operasi.
     */
    void getRiwayatPengaduan(int idUser, GetRiwayatCallback callback);

    /**
     * Mengambil informasi detail lengkap dari satu pengaduan spesifik.
     *
     * @param idPengaduan ID dari pengaduan yang detailnya ingin diambil.
     * @param callback Callback untuk menerima hasil operasi.
     */
    void getDetailPengaduan(int idPengaduan, GetDetailCallback callback);

    /**
     * Mengambil daftar pengaduan yang bersifat publik untuk ditampilkan di Beranda.
     *
     * @param status Status untuk memfilter pengaduan publik (misal: "Selesai").
     * @param callback Callback untuk menerima hasil operasi.
     */
    void getPengaduanPublik(String status, GetPengaduanPublikCallback callback);
}