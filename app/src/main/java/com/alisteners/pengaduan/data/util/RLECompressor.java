package com.alisteners.pengaduan.data.util;

import com.alisteners.pengaduan.util.MyPair;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Kelas utilitas yang menyediakan metode statis untuk melakukan kompresi gambar
 * menggunakan algoritma Run-Length Encoding (RLE).
 * Alur kerja utama meliputi konversi ke grayscale, kuantisasi warna,
 * kompresi RLE, dan konversi ke format biner.
 */
public class RLECompressor {
    private static final String TAG = "RLECompressor";

    /**
     * Mengonversi sebuah objek Bitmap berwarna menjadi array integer yang merepresentasikan nilai grayscale.
     * Metode ini merupakan langkah pertama dalam pra-pemrosesan gambar untuk menyederhanakan data warna
     * sebelum kompresi. Menggunakan formula standar NTSC untuk akurasi konversi.
     *
     * @param bitmap Bitmap input yang akan dikonversi. Tidak boleh null.
     * @return Array integer yang berisi nilai grayscale (0-255) untuk setiap piksel.
     * Mengembalikan array kosong jika input bitmap null atau terjadi error.
     */
    public static int[] convertBitmapToGrayscale(Bitmap bitmap) {
        if (bitmap == null) {
            Log.e(TAG, "Bitmap input untuk grayscale adalah null.");
            return new int[0];
        }
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int[] grayscalePixels = new int[width * height];
        int[] pixels = new int[width * height];
        try {
            // Ekstrak semua data piksel dari bitmap
            bitmap.getPixels(pixels, 0, width, 0, 0, width, height);
        } catch (Exception e) {
            Log.e(TAG, "Gagal mendapatkan piksel dari bitmap: " + e.getMessage());
            return new int[0];
        }
        for (int i = 0; i < pixels.length; i++) {
            int color = pixels[i];
            int r = Color.red(color);
            int g = Color.green(color);
            int b = Color.blue(color);
            // Menggunakan formula NTSC standard untuk konversi ke grayscale
            grayscalePixels[i] = (int) (0.299 * r + 0.587 * g + 0.114 * b);
        }
        return grayscalePixels;
    }

    /**
     * Melakukan kuantisasi pada data piksel grayscale untuk mengurangi jumlah level warna.
     * Proses ini bertujuan untuk meningkatkan jumlah piksel berurutan dengan nilai yang sama,
     * sehingga membuat kompresi RLE menjadi lebih efektif.
     *
     * @param grayscalePixels Array piksel grayscale asli (rentang 0-255).
     * @param numLevels Jumlah level warna yang diinginkan setelah kuantisasi (misal: 2, 4, 8).
     * Jika nilai di luar rentang (1-255), array asli akan dikembalikan.
     * @return Array piksel grayscale yang sudah terkuantisasi sesuai dengan jumlah level yang ditentukan.
     */
    public static int[] quantizeGrayscale(int[] grayscalePixels, int numLevels) {
        if (grayscalePixels == null || grayscalePixels.length == 0) {
            return grayscalePixels;
        }
        // Validasi untuk memastikan numLevels berada dalam rentang yang wajar
        if (numLevels <= 1 || numLevels >= 256) {
            return grayscalePixels;
        }

        int[] quantizedPixels = new int[grayscalePixels.length];
        double step = 256.0 / numLevels;
        for (int i = 0; i < grayscalePixels.length; i++) {
            int originalValue = grayscalePixels[i];
            // Tentukan di "ember" (level) mana piksel ini berada
            int levelIndex = (int) (originalValue / step);
            // Menentukan nilai representatif baru berdasarkan levelnya (nilai tengah dari rentang level)
            int quantizedValue = (int) (levelIndex * step + (step / 2.0));
            // Pastikan nilai tetap dalam rentang 0-255
            quantizedPixels[i] = Math.max(0, Math.min(255, quantizedValue));
        }
        return quantizedPixels;
    }

    /**
     * Mengompres array piksel grayscale menggunakan algoritma Run-Length Encoding (RLE).
     * Metode ini menghitung jumlah kemunculan berurutan dari nilai piksel yang sama
     * dan menyimpannya sebagai pasangan (nilai piksel, jumlah kemunculan).
     *
     * @param grayscalePixels Array piksel grayscale yang akan dikompres, biasanya hasil dari kuantisasi.
     * @return Sebuah List yang berisi pasangan (nilai piksel, jumlah kemunculan) sebagai representasi data terkompresi.
     * Mengembalikan list kosong jika input array null atau kosong.
     */
    public static List<MyPair<Integer, Integer>> compressRLE(int[] grayscalePixels) {
        if (grayscalePixels == null || grayscalePixels.length == 0) {
            return new ArrayList<>();
        }
        List<MyPair<Integer, Integer>> compressedData = new ArrayList<>();
        int count = 1;
        int currentPixel = grayscalePixels[0];
        // Iterasi dimulai dari elemen kedua untuk dibandingkan dengan yang pertama
        for (int i = 1; i < grayscalePixels.length; i++) {
            if (grayscalePixels[i] == currentPixel) {
                // Jika piksel sama, tambah hitungan
                count++;
            } else {
                // Jika piksel berbeda, simpan pasangan (nilai, hitungan) sebelumnya
                compressedData.add(new MyPair<>(currentPixel, count));
                // Reset untuk piksel baru
                currentPixel = grayscalePixels[i];
                count = 1;
            }
        }
        // Jangan lupa tambahkan data terakhir yang sedang dihitung setelah loop selesai
        compressedData.add(new MyPair<>(currentPixel, count));
        return compressedData;
    }

    /**
     * Mengonversi List data RLE menjadi format biner (byte array) yang siap untuk transmisi data.
     * Format output adalah [byte_nilai_piksel_1, byte_jumlah_kemunculan_1, byte_nilai_piksel_2, ...].
     * Metode ini juga menangani kasus di mana jumlah kemunculan (run count) melebihi 255
     * dengan memecahnya menjadi beberapa segmen.
     *
     * @param rleList List data RLE yang akan dikonversi.
     * @return Byte array yang merepresentasikan data RLE dalam format biner.
     * Mengembalikan byte array kosong jika input list null.
     */
    public static byte[] convertRleListToBinary(List<MyPair<Integer, Integer>> rleList) {
        if (rleList == null) {
            return new byte[0];
        }
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        for (MyPair<Integer, Integer> pair : rleList) {
            if (pair == null || pair.first() == null || pair.second() == null) {
                continue; // Lewati jika ada data yang tidak valid
            }
            int pixelValue = pair.first();
            int count = pair.second();

            // Logika ini penting jika ada urutan piksel yang sama lebih dari 255 kali.
            // Contoh: jika ada 300 piksel hitam, akan dipecah menjadi (hitam, 255) dan (hitam, 45).
            while (count > 0) {
                int currentRun = Math.min(count, 255);

                // Tulis nilai piksel (1 byte)
                baos.write((byte) pixelValue);
                // Tulis jumlah kemunculan (1 byte)
                baos.write((byte) currentRun);

                count -= currentRun;
            }
        }
        return baos.toByteArray();
    }

    /**
     * Menghitung ukuran terkompresi RLE dalam byte.
     * Setiap pasangan (nilai, jumlah) dianggap 2 byte.
     * Sesuai dengan perhitungan naskah (Tabel 3.6).
     *
     * @param compressedList Daftar pasangan RLE.
     * @return Ukuran total dalam byte.
     */
    public static long calculateCompressedSize(List<MyPair<Integer, Integer>> compressedList) {
        // Setiap pasangan RLE (nilai, hitungan) disimpan sebagai 2 byte
        // Ini sesuai dengan kesimpulan di naskah (misal 46 pasangan -> 92 byte)
        return (long) compressedList.size() * 2;
    }

}