package com.alisteners.pengaduan.domain.entity;

import com.google.gson.annotations.SerializedName;

/**
 * Kelas entitas yang merepresentasikan satu item pengaduan dalam bentuk ringkas.
 * Objek dari kelas ini biasanya digunakan untuk menampilkan daftar pengaduan
 * di halaman publik, riwayat, atau hasil pencarian, di mana tidak semua detail
 * lengkap diperlukan.
 */
public class PengaduanPublikItem {

    /** ID unik dari pengaduan. */
    @SerializedName("idPengaduan")
    private int idPengaduan;

    /** Waktu dan tanggal saat pengaduan dibuat. */
    @SerializedName("tanggalPengaduan")
    private String tanggalPengaduan;

    /** Kategori kerusakan yang dilaporkan. */
    @SerializedName("kategori")
    private String kategori;

    /** Status terkini dari pengaduan. */
    @SerializedName("status")
    private String status;

    /** Nama lengkap dari pengguna yang membuat laporan. */
    @SerializedName("namaPelapor")
    private String namaPelapor;

    /** Versi singkat dari deskripsi pengaduan untuk pratinjau. */
    @SerializedName("deskripsiSingkat")
    private String deskripsiSingkat;

    /** Path atau URL relatif dari gambar thumbnail yang tersimpan di server. */
    @SerializedName("pathImage")
    private String pathImage;

    // --- Getters ---

    /**
     * Mengembalikan ID unik dari pengaduan.
     * @return ID pengaduan.
     */
    public int getIdPengaduan() { return idPengaduan; }

    /**
     * Mengembalikan tanggal pengaduan.
     * @return String tanggal.
     */
    public String getTanggalPengaduan() { return tanggalPengaduan; }

    /**
     * Mengembalikan kategori pengaduan.
     * @return String kategori.
     */
    public String getKategori() { return kategori; }

    /**
     * Mengembalikan status pengaduan.
     * @return String status.
     */
    public String getStatus() { return status; }

    /**
     * Mengembalikan nama pelapor.
     * @return String nama pelapor.
     */
    public String getNamaPelapor() { return namaPelapor; }

    /**
     * Mengembalikan deskripsi singkat.
     * @return String deskripsi singkat.
     */
    public String getDeskripsiSingkat() { return deskripsiSingkat; }

    /**
     * Mengembalikan path gambar thumbnail.
     * @return String path gambar.
     */
    public String getPathImage() { return pathImage; }
}