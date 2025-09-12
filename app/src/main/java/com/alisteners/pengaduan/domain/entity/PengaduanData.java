package com.alisteners.pengaduan.domain.entity;

/**
 * Kelas entitas yang berfungsi sebagai objek transfer data (Data Transfer Object)
 * untuk membawa semua informasi terkait satu pengaduan yang akan dibuat.
 * Objek ini dibuat di lapisan presentasi (UI), diperkaya dengan data analisis
 * di lapisan data (utilitas), dan akhirnya dikirim ke server melalui repository.
 */
public class PengaduanData {

    // --- Informasi Dasar ---
    public final int idUser;
    public final String lokasi;
    public final String kategori;
    public final String deskripsi;
    public final boolean pernyataan;
    public final double latitude;
    public final double longitude;

    // --- Metrik Gambar & Kompresi ---
    public final int lebar;
    public final int tinggi;
    /** Ukuran file asli (dalam bytes) yang diterima aplikasi. */
    public final int ukuranBitmapAsli;
    /** Ukuran gambar (dalam bytes) setelah di-decode menjadi Bitmap di memori. */
    public final int ukuranBitmapSetelahResize;
    /** Ukuran data gambar (dalam bytes) setelah dikonversi menjadi grayscale. */
    public int ukuranGrayscale;
    /** Ukuran file akhir (dalam bytes) yang dikirim ke server setelah kompresi. */
    public final int ukuranFileRLEByte;
    /** Persentase hemat ruang (space saving) yang dicapai. */
    public final double rasioKompresiRLE;
    /** Persentase rasio ukuran file akhir terhadap ukuran asli (compression ratio). */
    public final double rasioKompresiStandar;
    /** Waktu yang dibutuhkan untuk memproses gambar (dalam milidetik). */
    public final long waktuProsesGambarMs;
    /** Flag yang menandakan apakah file akhir dikompresi menggunakan RLE (true) atau fallback JPG (false). */
    public final boolean isRleCompressed;

    /**
     * Konstruktor lengkap untuk membuat objek PengaduanData dengan semua informasi,
     * termasuk metrik analisis gambar yang sudah dihitung.
     * Biasanya digunakan oleh {@link com.alisteners.pengaduan.data.util.ImageProcessingManager}
     * setelah semua proses kompresi selesai.
     *
     * @param idUser ID pengguna yang membuat laporan.
     * @param lokasi Deskripsi lokasi.
     * @param kategori Kategori kerusakan.
     * @param deskripsi Deskripsi detail kerusakan.
     * @param pernyataan Status persetujuan pernyataan.
     * @param latitude Koordinat lintang.
     * @param longitude Koordinat bujur.
     * @param lebar Lebar gambar dalam piksel.
     * @param tinggi Tinggi gambar dalam piksel.
     * @param ukuranBitmapAsli Ukuran file asli dalam bytes.
     * @param ukuranBitmapSetelahResize Ukuran bitmap di memori dalam bytes.
     * @param ukuranGrayscale Ukuran data grayscale dalam bytes.
     * @param ukuranFileRLEByte Ukuran file terkompresi dalam bytes.
     * @param rasioKompresiRLE Rasio hemat ruang (space saving).
     * @param rasioKompresiStandar Rasio kompresi (compression ratio).
     * @param waktuProsesGambarMs Waktu proses gambar dalam milidetik.
     * @param isRleCompressed Menandakan apakah kompresi RLE digunakan.
     */
    public PengaduanData(int idUser, String lokasi, String kategori, String deskripsi,
                         boolean pernyataan, double latitude, double longitude,
                         int lebar, int tinggi, int ukuranBitmapAsli, int ukuranBitmapSetelahResize, int ukuranGrayscale,
                         int ukuranFileRLEByte, double rasioKompresiRLE, double rasioKompresiStandar,
                         long waktuProsesGambarMs, boolean isRleCompressed) {
        this.idUser = idUser;
        this.lokasi = lokasi;
        this.kategori = kategori;
        this.deskripsi = deskripsi;
        this.pernyataan = pernyataan;
        this.latitude = latitude;
        this.longitude = longitude;
        this.lebar = lebar;
        this.tinggi = tinggi;
        this.ukuranBitmapAsli = ukuranBitmapAsli;
        this.ukuranBitmapSetelahResize = ukuranBitmapSetelahResize;
        this.ukuranGrayscale = ukuranGrayscale;
        this.ukuranFileRLEByte = ukuranFileRLEByte;
        this.rasioKompresiRLE = rasioKompresiRLE;
        this.rasioKompresiStandar = rasioKompresiStandar;
        this.waktuProsesGambarMs = waktuProsesGambarMs;
        this.isRleCompressed = isRleCompressed;
    }

    /**
     * Konstruktor untuk data awal yang dikumpulkan dari UI, sebelum proses kompresi gambar.
     * Objek yang dibuat dengan konstruktor ini akan diteruskan ke
     * {@link com.alisteners.pengaduan.data.util.ImageProcessingManager} sebagai data input.
     *
     * @param idUser ID pengguna yang membuat laporan.
     * @param lokasi Deskripsi lokasi.
     * @param kategori Kategori kerusakan.
     * @param deskripsi Deskripsi detail kerusakan.
     * @param isRleCompressed Nilai awal untuk flag kompresi (biasanya false).
     * @param latitude Koordinat lintang.
     * @param longitude Koordinat bujur.
     * @param pernyataan Status persetujuan pernyataan.
     */
    public PengaduanData(int idUser, String lokasi, String kategori, String deskripsi, boolean isRleCompressed,
                         double latitude, double longitude, boolean pernyataan) {
        this.idUser = idUser;
        this.lokasi = lokasi;
        this.kategori = kategori;
        this.deskripsi = deskripsi;
        this.pernyataan = pernyataan;
        this.latitude = latitude;
        this.longitude = longitude;
        this.isRleCompressed = isRleCompressed;
        // Inisialisasi nilai default untuk properti lainnya yang akan diisi nanti
        this.lebar = 0;
        this.tinggi = 0;
        this.ukuranBitmapAsli = 0;
        this.ukuranBitmapSetelahResize = 0;
        this.ukuranGrayscale = 0;
        this.ukuranFileRLEByte = 0;
        this.rasioKompresiRLE = 0.0;
        this.rasioKompresiStandar = 0.0;
        this.waktuProsesGambarMs = 0;
    }
}