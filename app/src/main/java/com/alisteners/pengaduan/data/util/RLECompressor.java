package com.alisteners.pengaduan.data.util;

import com.alisteners.pengaduan.util.MyPair;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

public class RLECompressor {
    private static final String TAG = "RLECompressor";
    /**
     * Fungsi ini untuk mencetak nilai RGB dari blok 8x8 piksel pertama sebuah Bitmap ke Logcat.
     * @param bitmap Bitmap yang sudah di-resize.
     */
    public static void print8x8RgbValues(Bitmap bitmap) {
        // Tag untuk mempermudah pencarian di Logcat
        String TAG = "PixelExtractor";

        // Pastikan bitmap valid dan ukurannya cukup
        if (bitmap == null || bitmap.getWidth() < 8 || bitmap.getHeight() < 8) {
            Log.e(TAG, "Bitmap tidak valid atau ukurannya kurang dari 8x8.");
            return;
        }

        Log.d(TAG, "--- Memulai Ekstraksi Nilai RGB 8x8 ---");

        // Loop melalui 8 baris (y) dan 8 kolom (x)
        for (int y = 0; y < 8; y++) {
            // StringBuilder untuk menampung satu baris agar rapi
            StringBuilder rowString = new StringBuilder();
            for (int x = 0; x < 8; x++) {
                // Ambil warna dari satu piksel di koordinat (x, y)
                int pixelColor = bitmap.getPixel(x, y);

                // Ekstrak komponen warna Merah, Hijau, Biru
                int r = Color.red(pixelColor);
                int g = Color.green(pixelColor);
                int b = Color.blue(pixelColor);

                // Tambahkan ke string baris (contoh: "(165,41,35) ")
                rowString.append("(").append(r).append(",").append(g).append(",").append(b).append(") ");
            }
            // Cetak satu baris penuh ke Logcat
            Log.d(TAG, "Baris " + y + ": " + rowString);
        }

        Log.d(TAG, "--- Ekstraksi Selesai ---");
    }

    /**
     * Mengonversi Bitmap menjadi array piksel grayscale.
     * @param bitmap Bitmap input.
     * @return Array integer yang merepresentasikan nilai grayscale setiap piksel (0-255).
     */
    public static int[] convertBitmapToGrayscale(Bitmap bitmap) {
        if (bitmap == null) {
            Log.e(TAG, "Bitmap input untuk grayscale adalah null.");
            return new int[0]; // Kembalikan array kosong jika bitmap null
        }
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int[] grayscalePixels = new int[width * height];
        int[] pixels = new int[width * height]; // Array untuk menampung piksel dari bitmap

        try {
            bitmap.getPixels(pixels, 0, width, 0, 0, width, height);
        } catch (Exception e) {
            Log.e(TAG, "Gagal mendapatkan piksel dari bitmap: " + e.getMessage());
            return new int[0]; // Kembalikan array kosong jika ada error
        }

        for (int i = 0; i < pixels.length; i++) {
            int color = pixels[i];
            int r = Color.red(color);
            int g = Color.green(color);
            int b = Color.blue(color);
            // Rumus standar NTSC untuk grayscale: Y = 0.299R + 0.587G + 0.114B
            grayscalePixels[i] = (int) (0.299 * r + 0.587 * g + 0.114 * b);
        }
        return grayscalePixels;
    }

    /**
     * Melakukan quantisasi pada array piksel grayscale.
     * @param grayscalePixels Array piksel grayscale asli (nilai 0-255).
     * @param numLevels Jumlah level keabuan yang diinginkan setelah quantisasi (misalnya, 2, 4, 8, 16).
     * Harus lebih besar dari 0 dan kurang dari atau sama dengan 256.
     * @return Array piksel grayscale yang sudah diquantisasi.
     */
    public static int[] quantizeGrayscale(int[] grayscalePixels, int numLevels) {
        if (grayscalePixels == null || grayscalePixels.length == 0) {
            Log.w(TAG, "Input grayscalePixels untuk quantisasi null atau kosong.");
            return grayscalePixels; // Kembalikan asli jika input tidak valid
        }
        if (numLevels <= 0 || numLevels > 256) {
            Log.e(TAG, "Jumlah level quantisasi tidak valid: " + numLevels + ". Harus antara 1 dan 256.");
            return grayscalePixels; // Kembalikan asli jika numLevels tidak valid
        }
        if (numLevels == 256) { // Tidak perlu quantisasi jika jumlah level sama dengan rentang asli
            Log.d(TAG, "Jumlah level quantisasi adalah 256, tidak ada quantisasi yang dilakukan.");
            return grayscalePixels;
        }

        int[] quantizedPixels = new int[grayscalePixels.length];
        double step = 256.0 / numLevels; // Lebar setiap rentang quantisasi

        for (int i = 0; i < grayscalePixels.length; i++) {
            int originalValue = grayscalePixels[i];
            // Tentukan di level mana piksel ini berada
            int levelIndex = (int) (originalValue / step);
            // Pastikan levelIndex tidak melebihi batas atas karena nilai piksel 255
            if (levelIndex >= numLevels) {
                levelIndex = numLevels - 1;
            }
            // Hitung nilai representatif untuk level tersebut (misalnya, nilai tengah rentang)
            int quantizedValue = (int) (levelIndex * step + (step / 2.0));
            // Pastikan nilai tetap dalam rentang 0-255
            quantizedPixels[i] = Math.max(0, Math.min(255, quantizedValue));
        }
        Log.d(TAG, "Quantisasi selesai. NumLevels: " + numLevels + ", Step: " + step);
        return quantizedPixels;
    }

    /**
     * Mengompres array piksel grayscale menggunakan algoritma RLE.
     * @param grayscalePixels Array piksel grayscale (bisa hasil quantisasi).
     * @return List dari MyPair<Integer, Integer> yang merepresentasikan (nilaiPiksel, jumlahKemunculan).
     */
    public static List<MyPair<Integer, Integer>> compressRLE(int[] grayscalePixels) {
        if (grayscalePixels == null || grayscalePixels.length == 0) {
            Log.w(TAG, "Input grayscalePixels untuk RLE null atau kosong.");
            return new ArrayList<>(); // Kembalikan list kosong jika input tidak valid
        }

        List<MyPair<Integer, Integer>> compressedData = new ArrayList<>();
        // Pengecekan grayscalePixels.length == 0 sebenarnya sudah dicakup di atas, tapi tidak masalah untuk robustisitas
        int count = 1;
        int currentPixel = grayscalePixels[0];

        for (int i = 1; i < grayscalePixels.length; i++) {
            if (grayscalePixels[i] == currentPixel) {
                count++;
            } else {
                compressedData.add(new MyPair<>(currentPixel, count));
                currentPixel = grayscalePixels[i];
                count = 1;
            }
        }
        // Tambahkan run terakhir yang belum tersimpan
        compressedData.add(new MyPair<>(currentPixel, count));
        return compressedData;
    }

    /**
     * Mengonversi List data RLE (MyPair) menjadi format string efisien: "nilai1,jumlah1;nilai2,jumlah2;..."
     * @param rleList List data RLE.
     * @return String representasi RLE yang efisien.
     */
    public static String convertRleListToEfficientString(List<MyPair<Integer, Integer>> rleList) {
        if (rleList == null || rleList.isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < rleList.size(); i++) {
            MyPair<Integer, Integer> pair = rleList.get(i);
            if (pair != null && pair.first() != null && pair.second() != null) {
                sb.append(pair.first()).append(",").append(pair.second());
                if (i < rleList.size() - 1) {
                    sb.append(";"); // Pemisah antar pasangan
                }
            } else {
                Log.w(TAG, "Menemukan pair atau elemennya null dalam RLE list saat konversi ke string efisien.");
                // Pertimbangkan apakah ingin melanjutkan atau menghentikan/melempar error
            }
        }
        return sb.toString();
    }

    /**
     * Mengonversi List data RLE (MyPair) menjadi byte array murni.
     * Setiap pasangan (nilaiPiksel, jumlah) disimpan sebagai 2 byte:
     * Byte 1: nilaiPiksel (0-255)
     * Byte 2: jumlah (0-255). Jika jumlah > 255, run akan dipecah.
     * @param rleList List data RLE.
     * @return Byte array representasi RLE, atau array kosong jika terjadi error atau input tidak valid.
     */
    public static byte[] convertRleListToByteArray(List<MyPair<Integer, Integer>> rleList) {
        if (rleList == null) {
            Log.w(TAG, "Input rleList untuk konversi ke byte array adalah null.");
            return new byte[0];
        }

        ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        try {
            for (MyPair<Integer, Integer> pair : rleList) {
                if (pair == null || pair.first() == null || pair.second() == null) {
                    Log.w(TAG, "Menemukan pair atau elemennya null dalam RLE list saat konversi ke byte array, dilewati.");
                    continue; // Lewati pair yang tidak valid
                }

                int pixelValue = pair.first();
                int count = pair.second();

                // Pastikan pixelValue dalam rentang byte (0-255)
                if (pixelValue < 0 || pixelValue > 255) {
                    Log.w(TAG, "Nilai pixel di luar rentang byte: " + pixelValue + ", akan di-clamp menjadi 0-255.");
                    pixelValue = Math.max(0, Math.min(255, pixelValue));
                }

                // Bagi run yang panjangnya lebih dari 255 agar count muat dalam 1 byte
                while (count > 0) {
                    int currentRunCount = Math.min(count, 255);
                    byteStream.write((byte) pixelValue);    // Tulis nilai piksel (1 byte)
                    byteStream.write((byte) currentRunCount); // Tulis jumlah kemunculan (1 byte)
                    count -= currentRunCount;
                }
            }
        } catch (Exception e) { // Menangkap Exception umum jika ada masalah dengan ByteArrayOutputStream atau lainnya
            Log.e(TAG, "Error saat menulis ke ByteArrayOutputStream di convertRleListToByteArray: " + e.getMessage(), e);
            return new byte[0]; // Kembalikan array kosong jika error
        }
        return byteStream.toByteArray();
    }
}