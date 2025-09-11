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
     * Mengonversi sebuah objek Bitmap menjadi array integer yang merepresentasikan nilai grayscale.
     * @param bitmap Bitmap input yang akan dikonversi.
     * @return Array integer berisi nilai grayscale (0-255) untuk setiap piksel.
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
     * @param grayscalePixels Array piksel grayscale asli (0-255).
     * @param numLevels Jumlah level warna yang diinginkan setelah kuantisasi (misal: 2, 4, 8).
     * @return Array piksel grayscale yang sudah terkuantisasi.
     */
    public static int[] quantizeGrayscale(int[] grayscalePixels, int numLevels) {
        if (grayscalePixels == null || grayscalePixels.length == 0) {
            return grayscalePixels;
        }
        if (numLevels <= 0 || numLevels > 256) {
            return grayscalePixels;
        }
        if (numLevels == 256) {
            return grayscalePixels;
        }
        int[] quantizedPixels = new int[grayscalePixels.length];
        double step = 256.0 / numLevels;
        for (int i = 0; i < grayscalePixels.length; i++) {
            int originalValue = grayscalePixels[i];
            int levelIndex = (int) (originalValue / step);
            if (levelIndex >= numLevels) {
                levelIndex = numLevels - 1;
            }
            // Menentukan nilai representatif baru berdasarkan levelnya
            int quantizedValue = (int) (levelIndex * step + (step / 2.0));
            quantizedPixels[i] = Math.max(0, Math.min(255, quantizedValue));
        }
        return quantizedPixels;
    }

    /**
     * Mengompres array piksel grayscale menggunakan algoritma RLE.
     * @param grayscalePixels Array piksel grayscale, biasanya hasil dari kuantisasi.
     * @return Sebuah List yang berisi pasangan (nilai piksel, jumlah kemunculan).
     */
    public static List<MyPair<Integer, Integer>> compressRLE(int[] grayscalePixels) {
        if (grayscalePixels == null || grayscalePixels.length == 0) {
            return new ArrayList<>();
        }
        List<MyPair<Integer, Integer>> compressedData = new ArrayList<>();
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
        // Tambahkan data terakhir yang sedang dihitung
        compressedData.add(new MyPair<>(currentPixel, count));
        return compressedData;
    }

    /**
     * Mengonversi List data RLE menjadi format biner (byte array) yang siap dikirim.
     * Format: [byte_nilai_piksel, byte_jumlah_kemunculan, ...]
     * @param rleList List data RLE yang akan dikonversi.
     * @return Byte array yang merepresentasikan data RLE.
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

            // Menangani kasus jika jumlah kemunculan lebih dari 255 (batas 1 byte)
            // dengan memecahnya menjadi beberapa segmen.
            while (count > 0) {
                int currentRun = Math.min(count, 255);

                // PERBAIKAN: Blok try-catch dihapus karena baos.write() tidak melempar IOException
                baos.write((byte) pixelValue);    // 1. Tulis nilai piksel
                baos.write((byte) currentRun);    // 2. Tulis jumlah kemunculan

                count -= currentRun;
            }
        }
        return baos.toByteArray();
    }
}