package com.alisteners.pengaduan.domain.operator.repository;

import com.alisteners.pengaduan.domain.entity.PengaduanPublikItem;
import com.alisteners.pengaduan.domain.operator.entity.RekapData;

import java.io.File;
import java.util.List;

public interface OperatorRepository {

    interface GetPengaduanCallback {
        void onBerhasil(List<PengaduanPublikItem> list);
        void onGagal(String pesanError);
    }

    interface UpdatePengaduanCallback {
        void onUpdateBerhasil(String pesan);
        void onUpdateGagal(String pesanError);
    }

    void getPengaduanByStatus(String status, GetPengaduanCallback callback);

    void updatePengaduan(
            int idPengaduan,
            int idOperator,
            String status,
            String catatan,
            List<File> files,
            String requestToken,
            UpdatePengaduanCallback callback
    );

    interface DeletePengaduanCallback {
        void onDeleteBerhasil(String pesan);
        void onDeleteGagal(String pesanError);
    }
    void hapusPengaduan(int idPengaduan, String alasan, DeletePengaduanCallback callback);

    interface GetRekapCallback {
        void onBerhasil(RekapData data);
        void onGagal(String pesanError);
    }
    void getRekapData(GetRekapCallback callback);

    // Gunakan callback yang sama dengan get pengaduan
    void searchPengaduan(String keyword, String status, GetPengaduanCallback callback);
}