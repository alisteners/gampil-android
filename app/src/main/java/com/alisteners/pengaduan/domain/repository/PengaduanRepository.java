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
 * Interface yang mendefinisikan semua operasi data yang bisa diakses
 * dari sisi pengguna (Masyarakat).
 */
public interface PengaduanRepository {

    void hapusPengaduan(int idPengaduan, OperatorRepository.DeletePengaduanCallback callback);

    void getRekapData(OperatorRepository.GetRekapCallback callback);

    void searchPengaduan(String keyword, String status, OperatorRepository.GetPengaduanCallback callback);

    /**
     * Callback untuk operasi pengiriman pengaduan baru.
     */
    interface KirimPengaduanCallback {
        void onBerhasil(String pesan);
        void onGagal(String pesanError);
    }

    /**
     * Callback untuk operasi pengambilan daftar riwayat PENGGUNA.
     */
    interface GetRiwayatCallback {
        void onBerhasil(List<PengaduanRiwayatItem> riwayatList);
        void onGagal(String pesanError);
    }

    /**
     * Callback untuk operasi pengambilan detail satu pengaduan.
     */
    interface GetDetailCallback {
        void onBerhasil(DetailPengaduan detailPengaduan);
        void onGagal(String pesanError);
    }

    /**
     * Callback untuk operasi pengambilan daftar pengaduan PUBLIK.
     */
    interface GetPengaduanPublikCallback {
        void onBerhasil(List<PengaduanPublikItem> list);
        void onGagal(String pesanError);
    }

    /**
     * Mengirim data pengaduan baru ke server.
     * @param dataPengaduan Objek yang berisi semua data teks dan metrik pengaduan.
     * @param fileRle Objek File yang menunjuk ke data RLE yang disimpan sementara.
     * @param fileThumbnail Objek File yang menunjuk ke gambar thumbnail yang disimpan sementara.
     * @param callback Callback untuk memberitahu hasil operasi.
     */
    void kirimPengaduanKeServer(PengaduanData dataPengaduan, @Nullable File fileRle, @Nullable File fileThumbnail, KirimPengaduanCallback callback);

    /**
     * Mengambil daftar riwayat pengaduan untuk seorang pengguna.
     * @param idUser ID dari pengguna yang riwayatnya ingin diambil.
     * @param callback Callback untuk memberitahu hasil operasi.
     */
    void getRiwayatPengaduan(int idUser, GetRiwayatCallback callback);

    /**
     * Mengambil informasi lengkap dari satu pengaduan spesifik.
     * @param idPengaduan ID dari pengaduan yang detailnya ingin diambil.
     * @param callback Callback untuk memberitahu hasil operasi.
     */
    void getDetailPengaduan(int idPengaduan, GetDetailCallback callback);

    /**
     * Mengambil semua pengaduan yang bersifat publik untuk ditampilkan di Beranda.
     * @param callback Callback untuk memberitahu hasil operasi.
     */
    void getPengaduanPublik(String status, GetPengaduanPublikCallback callback);
}
