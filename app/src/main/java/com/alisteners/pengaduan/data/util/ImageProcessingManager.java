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
 * Kelas ini mengisolasi logika berat dari Activity, sesuai dengan Prinsip Tanggung Jawab Tunggal (SRP),
 * membuat kode di Activity menjadi lebih bersih dan fokus pada tugas UI.
 */
public class ImageProcessingManager {

    private static final String TAG = "ImageProcessingManager";

    private final Context context;

    /**
     * Kelas data (record) untuk menampung hasil dari proses kompresi gambar.
     * Digunakan untuk mengembalikan beberapa nilai sekaligus dari metode processAndCompressImage.
     * @param fileDataUntukUpload File utama yang akan diunggah (bisa .rle atau .jpg hasil fallback).
     * @param fileThumbnail File thumbnail gambar (selalu .jpg).
     * @param pengaduanData Objek PengaduanData yang sudah terisi lengkap dengan metrik analisis.
     */
    public record ProcessingResult(File fileDataUntukUpload, File fileThumbnail,
                                   PengaduanData pengaduanData) {
    }

    /**
     * Konstruktor untuk ImageProcessingManager.
     *
     * @param context Context aplikasi, digunakan untuk mengakses direktori cache
     * untuk penyimpanan file sementara.
     */
    public ImageProcessingManager(Context context) {
        this.context = context.getApplicationContext();
    }

    /**
     * Mengeksekusi seluruh alur kerja pemrosesan dan kompresi gambar.
     * Proses ini mencakup: konversi grayscale, kuantisasi, kompresi RLE, logika smart-compression
     * untuk memilih format file terbaik, pembuatan thumbnail, dan kalkulasi semua metrik analisis.
     *
     * @param bitmapUntukDiolah Bitmap yang akan diproses.
     * @param ukuranFileAsliBytes Ukuran file asli sebelum di-resize (dari ImagePicker), digunakan sebagai basis perbandingan.
     * @param dataAwal Objek PengaduanData yang berisi informasi awal dari input UI (user, lokasi, deskripsi, dll).
     * @return Objek ProcessingResult yang berisi file-file yang siap diunggah dan objek PengaduanData yang sudah terisi lengkap dengan metrik analisis.
     * @throws IOException Jika terjadi error saat pemrosesan atau penyimpanan file ke cache.
     */
    public ProcessingResult processAndCompressImage(Bitmap bitmapUntukDiolah, long ukuranFileAsliBytes, PengaduanData dataAwal) throws IOException {
        // Catat waktu mulai untuk menghitung durasi proses
        long startTime = System.currentTimeMillis();
        final int bitmapWidth = bitmapUntukDiolah.getWidth();
        final int bitmapHeight = bitmapUntukDiolah.getHeight();
        final int ukuranAsli = (int) ukuranFileAsliBytes;

        // Langkah 1: Ubah gambar menjadi grayscale
        int[] grayscalePixels = RLECompressor.convertBitmapToGrayscale(bitmapUntukDiolah);
        if (grayscalePixels.length == 0) throw new IOException("Gagal konversi ke grayscale.");
        final int ukuranByteDataGrayscale = grayscalePixels.length; // Ukuran data mentah grayscale (W x H x 1 byte)

        // Langkah 2: Kurangi jumlah warna (kuantisasi) untuk meningkatkan efektivitas RLE
        int jumlahLevelQuantisasi = 8;
        int[] quantizedPixels = RLECompressor.quantizeGrayscale(grayscalePixels, jumlahLevelQuantisasi);
        if (quantizedPixels.length == 0) throw new IOException("Gagal melakukan kuantisasi.");

        // Langkah 3: Lakukan kompresi RLE pada data yang sudah dikuantisasi
        final List<MyPair<Integer, Integer>> compressedDataRLE = RLECompressor.compressRLE(quantizedPixels);

        // Langkah 4: Ubah hasil RLE menjadi format biner (byte array)
        final byte[] dataRLEBiner = RLECompressor.convertRleListToBinary(compressedDataRLE);
        final int ukuranFileRLEByte = dataRLEBiner.length;

        // Langkah 5: Logika Smart-Compression. Bandingkan hasil RLE dengan data grayscale mentah.
        // Jika RLE lebih besar, maka lebih baik mengirim grayscale dalam format JPG.
        boolean pakaiRle = ukuranFileRLEByte < ukuranByteDataGrayscale;
        Log.d(TAG, "Keputusan Smart-Compression -> " + (pakaiRle ? "Gunakan RLE" : "Gunakan Grayscale (JPG)"));

        File fileDataUntukUpload;
        int ukuranDataTerkirim;

        // Hanya lakukan satu kali operasi penulisan file ke disk berdasarkan keputusan
        if (pakaiRle) {
            // Skenario A: RLE efisien. Simpan data biner .rle ke file sementara.
            fileDataUntukUpload = simpanByteKeFileSementara(dataRLEBiner);
            if (fileDataUntukUpload == null) throw new IOException("Gagal menyimpan data RLE biner ke file.");
            ukuranDataTerkirim = ukuranFileRLEByte;
        } else {
            // Skenario B: RLE tidak efisien. Buat Bitmap dari data grayscale dan simpan sebagai JPG (fallback).
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

        // Selalu buat dan simpan thumbnail berkualitas baik untuk ditampilkan di daftar laporan.
        File fileThumbnail = simpanBitmapKeFileSementara(bitmapUntukDiolah, "thumbnail.jpg", 90);
        if (fileThumbnail == null) throw new IOException("Gagal menyimpan file thumbnail.");

        // Ambil ukuran Bitmap di memori untuk data analisis
        final int ukuranBitmapSetelahResize = bitmapUntukDiolah.getByteCount();

        // Hitung semua metrik akhir untuk analisis
        final double rasioHematRuang = (ukuranAsli > 0) ? (1.0 - ((double) ukuranDataTerkirim / (double) ukuranAsli)) * 100.0 : 0.0;
        final double rasioKompresiStandar = (ukuranAsli > 0) ? ((double) ukuranDataTerkirim / (double) ukuranAsli) * 100.0 : 0.0;
        long endTime = System.currentTimeMillis();
        long durationMs = endTime - startTime;

        // Panggil method validasi naskah 8x8
        int[] hasilNaskah = hitungValidasiNaskah(bitmapUntukDiolah);

        // Siapkan objek PengaduanData yang sudah lengkap dengan semua metrik
        PengaduanData pengaduanDataLengkap = new PengaduanData(
                dataAwal.idUser, dataAwal.lokasi, dataAwal.kategori, dataAwal.deskripsi,
                dataAwal.pernyataan, 
                dataAwal.latitude, dataAwal.longitude,
                bitmapWidth, bitmapHeight, ukuranAsli, ukuranBitmapSetelahResize,
                ukuranByteDataGrayscale, ukuranDataTerkirim, rasioHematRuang,
                rasioKompresiStandar, durationMs,
                pakaiRle, hasilNaskah[0],
                hasilNaskah[1]);

        return new ProcessingResult(fileDataUntukUpload, fileThumbnail, pengaduanDataLengkap);
    }

    /**
     * Metode helper privat untuk menyimpan objek Bitmap ke file sementara di direktori cache.
     *
     * @param bitmap   Bitmap yang akan disimpan.
     * @param namaFile Nama file tujuan.
     * @param quality  Kualitas kompresi JPEG (0-100).
     * @return Objek File jika berhasil, atau null jika gagal.
     */
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

    /**
     * Metode helper privat untuk menyimpan byte array ke file sementara di direktori cache.
     *
     * @param data Byte array yang akan disimpan.
     * @return Objek File jika berhasil, atau null jika gagal.
     */
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

    /**
     * Menjalankan perhitungan validasi naskah (studi kasus 8x8) secara terpisah.
     * Metode ini secara spesifik meniru alur di BAB 3:
     * 1. Mengambil sampel 8x8 piksel (64 piksel) dari pojok kiri atas.
     * 2. Mengonversi 64 piksel RGB (192 byte) menjadi 64 piksel Grayscale (64 byte)
     * menggunakan rumus RATA-RATA ( (R+G+B)/3 ), BUKAN rumus NTSC.
     * 3. Mengompres 64 byte grayscale tersebut dengan RLE.
     * 4. Mengembalikan ukuran sebelum (192 byte) dan ukuran sesudah (hasil RLE).
     *
     * @param bitmap Bitmap yang sudah di-resize (misal: 1600x1200).
     * @return Array int[2] berisi {ukuranNaskahSebelum, ukuranNaskahSesudah}.
     */
    private int[] hitungValidasiNaskah(Bitmap bitmap) {
        final int SAMPEL_SIZE = 8; // Matriks 8x8
        // Sesuai naskah BAB 3, "Ukuran sebelum dikompresi = 192 byte" (data RGB 8x8x3)
        final int UKURAN_NASKAH_SEBELUM = 192;

        // Input untuk RLE adalah 64 byte grayscale (8x8x1)
        int[] grayscaleSampel = new int[SAMPEL_SIZE * SAMPEL_SIZE]; // 64 piksel

        try {
            // 1. Ambil sampel 8x8 piksel
            for (int y = 0; y < SAMPEL_SIZE; y++) {
                for (int x = 0; x < SAMPEL_SIZE; x++) {
                    int pixel = bitmap.getPixel(x, y);
                    int r = Color.red(pixel);
                    int g = Color.green(pixel);
                    int b = Color.blue(pixel);

                    // 2. Konversi ke Grayscale pakai RUMUS NASKAH (Average), BUKAN NTSC.
                    int gray = (r + g + b) / 3;

                    grayscaleSampel[(y * SAMPEL_SIZE) + x] = gray;
                }
            }

            // 3. Kompres data sampel 64 piksel grayscale
            List<MyPair<Integer, Integer>> rleSampel = RLECompressor.compressRLE(grayscaleSampel);

            // 4. Hitung ukuran sesudah (sesuai naskah, 2 byte per pair)
            // Panggil helper yang sudah dibuat di Fase 1
            int ukuranNaskahSesudah = (int) RLECompressor.calculateCompressedSize(rleSampel);

            return new int[]{UKURAN_NASKAH_SEBELUM, ukuranNaskahSesudah};

        } catch (Exception e) {
            Log.e(TAG, "Gagal menghitung validasi naskah 8x8", e);
            // Return 0 jika gagal (misal gambar lebih kecil dari 8x8)
            return new int[]{0, 0};
        }
    }

}