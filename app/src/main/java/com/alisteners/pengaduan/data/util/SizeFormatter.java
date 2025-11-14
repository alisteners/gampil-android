package com.alisteners.pengaduan.data.util;

import java.text.DecimalFormat;

/**
 * Utility class untuk memformat ukuran byte secara konsisten di seluruh aplikasi.
 * Menggunakan standar Biner (Basis 1024) agar konsisten dengan perhitungan teknis
 * dan naskah penelitian (BAB 4).
 * 1 KB = 1024 Bytes
 * 1 MB = 1024 KB
 */
public class SizeFormatter {

    /**
     * Memformat ukuran byte menjadi string yang mudah dibaca (KB, MB, GB)
     * menggunakan standar biner (Basis 1024).
     *
     * @param bytes Ukuran dalam bytes
     * @return String yang sudah diformat (mis. "1.96 MB")
     */
    public static String formatUkuranBiner(long bytes) {
        if (bytes <= 0) return "0 B";

        // Menggunakan Basis 1024
        final String[] units = new String[]{"B", "KB", "MB", "GB", "TB"};
        int digitGroups = (int) (Math.log10(bytes) / Math.log10(1024.0));

        return new DecimalFormat("#,##0.00")
                .format(bytes / Math.pow(1024.0, digitGroups))
                + " " + units[digitGroups];
    }
}