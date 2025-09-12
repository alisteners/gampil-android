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

public class ImageProcessingManager {

    private static final String TAG = "ImageProcessingManager";

    private final Context context;

    // Kelas penampung untuk hasil pemrosesan gambar
        public record ProcessingResult(File fileDataUntukUpload, File fileThumbnail,
                                       PengaduanData pengaduanData) {
    }

    public ImageProcessingManager(Context context) {
        this.context = context.getApplicationContext();
    }

    /**
     * Memproses dan mengompres gambar pengaduan di background thread.
     *
     * @param bitmapUntukDiolah Bitmap yang akan diproses.
     * @param ukuranFileAsliBytes Ukuran file asli sebelum di-resize.
     * @param dataAwal Data pengaduan awal dari UI (user, lokasi, dll).
     * @return Objek ProcessingResult yang berisi file hasil olahan dan data lengkap pengaduan.
     * @throws IOException Jika terjadi error saat pemrosesan atau penyimpanan file.
     */
    public ProcessingResult processAndCompressImage(Bitmap bitmapUntukDiolah, long ukuranFileAsliBytes, PengaduanData dataAwal) throws IOException {
        long startTime = System.currentTimeMillis();
        final int bitmapWidth = bitmapUntukDiolah.getWidth();
        final int bitmapHeight = bitmapUntukDiolah.getHeight();
        final int ukuranAsli = (int) ukuranFileAsliBytes;

        // 1. Konversi ke Grayscale (di RAM)
        int[] grayscalePixels = RLECompressor.convertBitmapToGrayscale(bitmapUntukDiolah);
        if (grayscalePixels.length == 0) throw new IOException("Gagal konversi ke grayscale.");
        final int ukuranByteDataGrayscale = grayscalePixels.length;

        // 2. Kuantisasi (di RAM)
        int jumlahLevelQuantisasi = 8;
        int[] quantizedPixels = RLECompressor.quantizeGrayscale(grayscalePixels, jumlahLevelQuantisasi);
        if (quantizedPixels.length == 0) throw new IOException("Gagal melakukan kuantisasi.");

        // 3. Kompresi RLE (di RAM)
        final List<MyPair<Integer, Integer>> compressedDataRLE = RLECompressor.compressRLE(quantizedPixels);

        // 4. Konversi hasil RLE ke format biner (byte[]) (di RAM)
        final byte[] dataRLEBiner = RLECompressor.convertRleListToBinary(compressedDataRLE);
        final int ukuranFileRLEByte = dataRLEBiner.length;

        // 5. Logika Smart-Compression (Perbandingan Cepat di RAM)
        boolean pakaiRle = ukuranFileRLEByte < ukuranByteDataGrayscale;
        Log.d(TAG, "Keputusan Smart-Compression -> " + (pakaiRle ? "Gunakan RLE" : "Gunakan Grayscale (JPG)"));

        File fileDataUntukUpload;
        int ukuranDataTerkirim;

        // --- HANYA SATU KALI OPERASI TULIS FILE (I/O) KE DISK BERDASARKAN KEPUTUSAN ---
        if (pakaiRle) {
            // Skenario A: RLE efisien, simpan data .rle
            fileDataUntukUpload = simpanByteKeFileSementara(dataRLEBiner);
            if (fileDataUntukUpload == null) throw new IOException("Gagal menyimpan data RLE biner ke file.");
            ukuranDataTerkirim = ukuranFileRLEByte;
        } else {
            // Skenario B: RLE tidak efisien, simpan gambar grayscale sebagai fallback .jpg
            Bitmap bitmapGrayscale = Bitmap.createBitmap(bitmapWidth, bitmapHeight, Bitmap.Config.ARGB_8888);
            int[] colorPixelsGrayscale = new int[grayscalePixels.length];
            for (int i = 0; i < grayscalePixels.length; i++) {
                int gray = grayscalePixels[i];
                colorPixelsGrayscale[i] = Color.rgb(gray, gray, gray);
            }
            bitmapGrayscale.setPixels(colorPixelsGrayscale, 0, bitmapWidth, 0, 0, bitmapWidth, bitmapHeight);

            fileDataUntukUpload = simpanBitmapKeFileSementara(bitmapGrayscale, "fallback_grayscale.jpg", 85); // Kualitas 85%
            if (fileDataUntukUpload == null) throw new IOException("Gagal menyimpan fallback grayscale JPG ke file.");
            ukuranDataTerkirim = (int) fileDataUntukUpload.length();
        }

        // Simpan thumbnail untuk ditampilkan di aplikasi (selalu dalam format JPG)
        File fileThumbnail = simpanBitmapKeFileSementara(bitmapUntukDiolah, "thumbnail.jpg", 90);
        if (fileThumbnail == null) throw new IOException("Gagal menyimpan file thumbnail.");

        // Ambil ukuran Bitmap teoritis di RAM untuk tujuan analisis di Detail Pengaduan
        final int ukuranBitmapSetelahResize = bitmapUntukDiolah.getByteCount();

        // Hitung metrik akhir
        final double rasioHematRuang = (ukuranAsli > 0) ? (1.0 - ((double) ukuranDataTerkirim / (double) ukuranAsli)) * 100.0 : 0.0;
        final double rasioKompresiStandar = (ukuranAsli > 0) ? ((double) ukuranDataTerkirim / (double) ukuranAsli) * 100.0 : 0.0;
        long endTime = System.currentTimeMillis();
        long durationMs = endTime - startTime;

        // Siapkan data lengkap untuk dikirim
        PengaduanData pengaduanDataLengkap = new PengaduanData(
                dataAwal.idUser, dataAwal.lokasi, dataAwal.kategori, dataAwal.deskripsi, pakaiRle,
                dataAwal.latitude, dataAwal.longitude,
                bitmapWidth, bitmapHeight, ukuranAsli, ukuranBitmapSetelahResize,
                ukuranByteDataGrayscale, ukuranDataTerkirim, rasioHematRuang,
                rasioKompresiStandar, durationMs, dataAwal.pernyataan);

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