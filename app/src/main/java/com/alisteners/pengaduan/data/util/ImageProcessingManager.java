package com.alisteners.pengaduan.data.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.util.Log;

import com.alisteners.pengaduan.domain.entity.PengaduanData;
import com.alisteners.pengaduan.util.MyPair;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

/**
 * Kelas yang bertanggung jawab untuk mengelola seluruh alur kerja pemrosesan dan kompresi gambar.
 * (Javadoc lainnya tetap sama)
 */
public class ImageProcessingManager {

    private static final String TAG = "ImageProcessingManager";

    private final Context context;

    public record ProcessingResult(File fileDataUntukUpload, File fileThumbnail,
                                   PengaduanData pengaduanData) {
    }

    public ImageProcessingManager(Context context) {
        this.context = context.getApplicationContext();
    }

    public ProcessingResult processAndCompressImage(Bitmap bitmapUntukDiolah, long ukuranFileAsliBytes, PengaduanData dataAwal) throws IOException {
        long startTime = System.currentTimeMillis();
        final int bitmapWidth = bitmapUntukDiolah.getWidth();
        final int bitmapHeight = bitmapUntukDiolah.getHeight();
        final int ukuranAsli = (int) ukuranFileAsliBytes;

        // Langkah 1: Ubah gambar menjadi grayscale
        int[] grayscalePixels = RLECompressor.convertBitmapToGrayscale(bitmapUntukDiolah);
        if (grayscalePixels.length == 0) throw new IOException("Gagal konversi ke grayscale.");
        final int ukuranByteDataGrayscale = grayscalePixels.length;

        // Langkah 2: Kurangi jumlah warna (kuantisasi) untuk meningkatkan efektivitas RLE
        // UBAH NILAI INI MENJADI 0 UNTUK EKSPERIMEN ANDA
        int jumlahLevelQuantisasi = 0;
        int[] quantizedPixels = RLECompressor.quantizeGrayscale(grayscalePixels, jumlahLevelQuantisasi);
        if (quantizedPixels.length == 0) throw new IOException("Gagal melakukan kuantisasi.");

        // Langkah 3: Lakukan kompresi RLE pada data yang sudah dikuantisasi
        final List<MyPair<Integer, Integer>> compressedDataRLE = RLECompressor.compressRLE(quantizedPixels);

        // Langkah 4: Ubah hasil RLE menjadi format biner (byte array)
        final byte[] dataRLEBiner = RLECompressor.convertRleListToBinary(compressedDataRLE);
        final int ukuranFileRLEByte = dataRLEBiner.length;
        
        // Logika Smart-Compression dinonaktifkan dengan memaksa 'pakaiRle' menjadi 'true'.
        // Aplikasi akan selalu menggunakan RLE, bahkan jika hasilnya lebih besar.
        boolean pakaiRle = true;
        Log.d(TAG, "Keputusan Smart-Compression -> " + (pakaiRle ? "Gunakan RLE (DIPAKSA)" : "Gunakan Grayscale (JPG)"));

        File fileDataUntukUpload;
        int ukuranDataTerkirim;

        // Karena 'pakaiRle' selalu true, blok 'else' tidak akan pernah dijalankan
        if (pakaiRle) {
            // Skenario A: RLE efisien (atau dipaksa). Simpan data biner .rle ke file sementara.
            fileDataUntukUpload = simpanByteKeFileSementara(dataRLEBiner);
            if (fileDataUntukUpload == null) throw new IOException("Gagal menyimpan data RLE biner ke file.");
            ukuranDataTerkirim = ukuranFileRLEByte;
        } else {
            // Skenario B: Blok ini tidak akan dieksekusi dalam mode eksperimen ini.
            Bitmap bitmapGrayscale = Bitmap.createBitmap(bitmapWidth, bitmapHeight, Bitmap.Config.ARGB_8888);
            int[] colorPixelsGrayscale = new int[grayscalePixels.length];
            for (int i = 0; i < grayscalePixels.length; i++) {
                int gray = grayscalePixels[i];
                colorPixelsGrayscale[i] = Color.rgb(gray, gray, gray);
            }
            bitmapGrayscale.setPixels(colorPixelsGrayscale, 0, bitmapWidth, 0, 0, bitmapWidth, bitmapHeight);

            fileDataUntukUpload = simpanBitmapKeFileSementara(bitmapGrayscale, "fallback_grayscale.jpg", 85);
            if (fileDataUntukUpload == null) throw new IOException("Gagal menyimpan fallback grayscale JPG ke file.");
            ukuranDataTerkirim = (int) fileDataUntukUpload.length();
        }

        File fileThumbnail = simpanBitmapKeFileSementara(bitmapUntukDiolah, "thumbnail.jpg", 90);
        if (fileThumbnail == null) throw new IOException("Gagal menyimpan file thumbnail.");

        final int ukuranBitmapSetelahResize = bitmapUntukDiolah.getByteCount();
        final double rasioHematRuang = (ukuranAsli > 0) ? (1.0 - ((double) ukuranDataTerkirim / (double) ukuranAsli)) * 100.0 : 0.0;
        final double rasioKompresiStandar = (ukuranAsli > 0) ? ((double) ukuranDataTerkirim / (double) ukuranAsli) * 100.0 : 0.0;
        long endTime = System.currentTimeMillis();
        long durationMs = endTime - startTime;

        PengaduanData pengaduanDataLengkap = new PengaduanData(
                dataAwal.idUser, dataAwal.lokasi, dataAwal.kategori, dataAwal.deskripsi,
                dataAwal.pernyataan,
                dataAwal.latitude, dataAwal.longitude,
                bitmapWidth, bitmapHeight, ukuranAsli, ukuranBitmapSetelahResize,
                ukuranByteDataGrayscale, ukuranDataTerkirim, rasioHematRuang,
                rasioKompresiStandar, durationMs,
                pakaiRle);

        return new ProcessingResult(fileDataUntukUpload, fileThumbnail, pengaduanDataLengkap);
    }

    private File simpanBitmapKeFileSementara(Bitmap bitmap, String namaFile, int quality) {
        File file = new File(context.getCacheDir(), namaFile);
        try (FileOutputStream out = new FileOutputStream(file)) {
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, out);
            return file;
        } catch (IOException e) {
            Log.e(TAG, "Gagal menyimpan bitmap ke file sementara", e);
            return null;
        }
    }

    private File simpanByteKeFileSementara(byte[] data) {
        File fileOutput = new File(context.getCacheDir(), "hasil_kompresi.rle");
        try (FileOutputStream fos = new FileOutputStream(fileOutput)) {
            fos.write(data);
            return fileOutput;
        } catch (IOException e) {
            Log.e(TAG, "Gagal menyimpan byte array ke file sementara", e);
            return null;
        }
    }
}