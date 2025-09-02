package com.alisteners.pengaduan.domain.usecase;

import com.alisteners.pengaduan.domain.entity.PengaduanData;
import com.alisteners.pengaduan.domain.repository.PengaduanRepository;
import java.io.File;
import androidx.annotation.Nullable;

public class KirimPengaduanUseCase {
    private final PengaduanRepository pengaduanRepository;

    public KirimPengaduanUseCase(PengaduanRepository pengaduanRepository) {
        this.pengaduanRepository = pengaduanRepository;
    }

    public void execute(PengaduanData dataPengaduan, @Nullable File fileRle, @Nullable File fileThumbnail, PengaduanRepository.KirimPengaduanCallback callback) {
        // Validasi dasar (sesuaikan dengan kebutuhan)
        if (dataPengaduan.deskripsi == null || dataPengaduan.deskripsi.trim().isEmpty()) {
            callback.onGagal("Deskripsi pengaduan tidak boleh kosong.");
            return;
        }
        if (!dataPengaduan.pernyataan) {
            callback.onGagal("Pernyataan harus disetujui.");
            return;
        }
        if (fileRle == null || !fileRle.exists() || fileRle.length() == 0) {
            callback.onGagal("File data RLE tidak valid atau kosong.");
            return;
        }

        pengaduanRepository.kirimPengaduanKeServer(dataPengaduan, fileRle, fileThumbnail, callback);
    }
}