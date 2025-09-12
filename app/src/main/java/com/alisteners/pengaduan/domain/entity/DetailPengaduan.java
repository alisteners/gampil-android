package com.alisteners.pengaduan.domain.entity;

import com.google.gson.annotations.SerializedName;
import java.util.List;

/**
 * Kelas entitas yang merepresentasikan model data lengkap untuk satu pengaduan.
 * Objek dari kelas ini berisi semua informasi yang relevan, mulai dari data dasar
 * pengaduan, informasi pelapor, metrik analisis kompresi gambar, hingga riwayat
 * tindak lanjut. Kelas ini digunakan di seluruh aplikasi sebagai representasi
 * detail sebuah pengaduan.
 */
public class DetailPengaduan {

    // --- Informasi Dasar Pengaduan & Pelapor ---

    /** ID unik dari pengaduan. */
    @SerializedName("idPengaduan")
    public int idPengaduan;
    /** Waktu dan tanggal saat pengaduan dibuat, dalam format String dari server. */
    @SerializedName("tanggalPengaduan")
    public String tanggalPengaduan;
    /** Kategori kerusakan yang dilaporkan. */
    @SerializedName("kategori")
    public String kategori;
    /** Status terkini dari pengaduan (misal: "Menunggu", "Diproses", "Selesai"). */
    @SerializedName("status")
    public String status;
    /** Deskripsi lokasi kerusakan. */
    @SerializedName("lokasi")
    public String lokasi;
    /** Deskripsi detail mengenai kerusakan yang dilaporkan. */
    @SerializedName("deskripsi")
    public String deskripsi;
    /** Koordinat lintang (latitude) dari lokasi kerusakan. */
    @SerializedName("latitude")
    public double latitude;
    /** Koordinat bujur (longitude) dari lokasi kerusakan. */
    @SerializedName("longitude")
    public double longitude;
    /** Nama lengkap dari pengguna yang membuat laporan. */
    @SerializedName("namaLengkap")
    public String namaPelapor;

    // --- Informasi & Metrik Analisis Gambar ---

    /** Path atau URL relatif dari gambar thumbnail yang tersimpan di server. */
    @SerializedName("pathImage")
    public String pathImage;
    /** Lebar gambar dalam piksel setelah di-resize oleh aplikasi. */
    @SerializedName("lebar")
    public int lebar;
    /** Tinggi gambar dalam piksel setelah di-resize oleh aplikasi. */
    @SerializedName("tinggi")
    public int tinggi;
    /** Ukuran file asli (dalam bytes) yang diterima aplikasi dari galeri/kamera. */
    @SerializedName("ukuranAsli")
    public int ukuranAsli;
    /** Ukuran gambar (dalam bytes) setelah di-decode menjadi Bitmap di memori. */
    @SerializedName("ukuranSetelahResize")
    public int ukuranSetelahResize;
    /** Ukuran data gambar (dalam bytes) setelah dikonversi menjadi grayscale (lebar x tinggi). */
    @SerializedName("ukuranGrayscale")
    public int ukuranGrayscale;
    /** Ukuran file akhir (dalam bytes) yang dikirim ke server setelah kompresi (bisa RLE atau fallback JPG). */
    @SerializedName("ukuranFileRLEByte")
    public int ukuranFileRLEByte;
    /** Persentase hemat ruang (space saving) yang dicapai, dihitung dari (ukuranFileRLEByte / ukuranAsli). */
    @SerializedName("rasioKompresiRLE")
    public double rasioKompresiRLE;
    /** Persentase rasio ukuran file akhir terhadap ukuran asli (compression ratio). */
    @SerializedName("rasioKompresiStandar")
    public double rasioKompresiStandar;
    /** Waktu yang dibutuhkan untuk memproses gambar (dalam milidetik). */
    @SerializedName("waktuProsesGambarMs")
    public long waktuProsesGambarMs;

    // --- Informasi Riwayat Tindak Lanjut ---

    /** Daftar (List) yang berisi riwayat status dan tindak lanjut dari pengaduan. */
    @SerializedName("riwayat_tindak_lanjut")
    public List<LogStatusItem> riwayatTindakLanjut;

    /**
     * Konstruktor kosong.
     * Dibutuhkan oleh library Gson untuk proses deserialisasi dari JSON ke objek Java.
     */
    public DetailPengaduan() {
        // Konstruktor kosong untuk Gson
    }


    // --- Getters ---

    /**
     * Mengembalikan nama lengkap pelapor.
     * @return Nama pelapor.
     */
    public String getNamaPelapor() { return namaPelapor; }

    /**
     * Mengembalikan koordinat latitude.
     * @return Nilai latitude.
     */
    public double getLatitude() { return latitude; }

    /**
     * Mengembalikan koordinat longitude.
     * @return Nilai longitude.
     */
    public double getLongitude() { return longitude; }

    /**
     * Mengembalikan daftar riwayat tindak lanjut.
     * @return Sebuah {@link List} dari {@link LogStatusItem}.
     */
    public List<LogStatusItem> getRiwayatTindakLanjut() {
        return riwayatTindakLanjut;
    }
}