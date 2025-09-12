package com.alisteners.pengaduan.domain.entity;

import com.google.gson.annotations.SerializedName;

/**
 * Kelas entitas yang merepresentasikan satu item notifikasi.
 * Setiap objek dari kelas ini berisi informasi yang akan ditampilkan kepada pengguna
 * terkait pembaruan status pada pengaduan mereka.
 */
public class Notifikasi {

    /**
     * ID unik dari notifikasi.
     */
    @SerializedName("idNotifikasi")
    private int idNotifikasi;

    /**
     * ID pengaduan yang terkait dengan notifikasi ini, digunakan untuk navigasi
     * saat notifikasi di-klik.
     */
    @SerializedName("idPengaduan")
    private int idPengaduan;

    /**
     * Isi pesan atau teks dari notifikasi yang akan ditampilkan.
     */
    @SerializedName("pesan")
    private String pesan;

    /**
     * Waktu dan tanggal saat notifikasi ini dibuat, dalam format String dari server.
     */
    @SerializedName("timestamp")
    private String timestamp;

    /**
     * Status yang menunjukkan apakah notifikasi ini sudah dilihat oleh pengguna atau belum.
     * Bernilai true jika sudah dilihat, false jika belum.
     */
    @SerializedName("sudahDilihat")
    private boolean sudahDilihat;

    /**
     * Mengembalikan ID unik dari notifikasi.
     * @return ID notifikasi.
     */
    public int getIdNotifikasi() {
        return idNotifikasi;
    }

    /**
     * Mengembalikan ID pengaduan yang terkait.
     * @return ID pengaduan.
     */
    public int getIdPengaduan() {
        return idPengaduan;
    }

    /**
     * Mengembalikan isi pesan notifikasi.
     * @return Pesan notifikasi.
     */
    public String getPesan() {
        return pesan;
    }

    /**
     * Mengembalikan timestamp notifikasi.
     * @return String timestamp.
     */
    public String getTimestamp() {
        return timestamp;
    }

    /**
     * Mengembalikan status "sudah dilihat" dari notifikasi.
     * @return true jika sudah dilihat, false jika belum.
     */
    public boolean isSudahDilihat() {
        return sudahDilihat;
    }
}