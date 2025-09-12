package com.alisteners.pengaduan.util;

/**
 * Kelas utilitas (helper) yang berisi metode-metode statis untuk memanipulasi String.
 * Kelas ini dibuat untuk menampung fungsi-fungsi umum terkait String agar dapat
 * digunakan kembali di seluruh aplikasi dan menjaga kode tetap bersih (DRY - Don't Repeat Yourself).
 */
public class StringUtils {

    /**
     * Mengubah huruf pertama dari sebuah String menjadi huruf kapital (kapitalisasi judul)
     * dan sisa hurufnya menjadi huruf kecil.
     * Metode ini aman untuk input null atau kosong.
     * <p>
     * Contoh:
     * <ul>
     * <li>"diproses" -> "Diproses"</li>
     * <li>"SELESAI" -> "Selesai"</li>
     * <li>"m" -> "M"</li>
     * <li>"" -> ""</li>
     * <li>null -> ""</li>
     * </ul>
     *
     * @param text Teks yang akan diubah formatnya.
     * @return Teks yang sudah diformat dengan huruf awal kapital, atau string kosong jika input tidak valid.
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