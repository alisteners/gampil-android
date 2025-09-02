package com.alisteners.pengaduan.util;

public class StringUtils {

    /**
     * Mengubah string menjadi format dengan huruf awal kapital.
     * Contoh: "diproses" -> "Diproses"
     * Contoh: "SELESAI" -> "Selesai"
     * @param text Teks yang akan diubah.
     * @return Teks yang sudah diformat.
     */
    public static String capitalizeFirstLetter(String text) {
        if (text == null || text.isEmpty()) {
            return ""; // Kembalikan string kosong jika input null atau kosong
        }
        if (text.length() == 1) {
            return text.toUpperCase(); // Jika hanya satu huruf, jadikan kapital
        }
        // Ambil huruf pertama, jadikan kapital, lalu gabungkan dengan sisa string yang dijadikan huruf kecil
        return text.substring(0, 1).toUpperCase() + text.substring(1).toLowerCase();
    }
}