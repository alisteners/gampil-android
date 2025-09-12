package com.alisteners.pengaduan.data.remote.model;

import com.alisteners.pengaduan.domain.entity.PengaduanRiwayatItem;
import com.google.gson.annotations.SerializedName;
import java.util.List;

/**
 * Kelas ini merepresentasikan struktur respons JSON dari endpoint API yang mengembalikan
 * daftar riwayat pengaduan milik seorang pengguna (get_riwayat_pengaduan.php).
 * Berfungsi sebagai Data Transfer Object (DTO) untuk Retrofit dan Gson.
 */
public class RiwayatApiResponse {

    /**
     * Menunjukkan status dari respons API, misalnya "success" atau "error".
     * Dipetakan dari field "status" dalam JSON.
     */
    @SerializedName("status")
    private String status;

    /**
     * Berisi pesan deskriptif dari server terkait hasil request.
     * Dipetakan dari field "message" dalam JSON.
     */
    @SerializedName("message")
    private String message;

    /**
     * Berisi daftar (List) dari objek PengaduanRiwayatItem.
     * Gson akan secara otomatis mem-parsing array JSON "data" menjadi sebuah List dari objek ini.
     * Dipetakan dari field "data" dalam JSON.
     */
    @SerializedName("data")
    private List<PengaduanRiwayatItem> data;


    // --- Getters ---

    /**
     * Mengembalikan status dari respons API.
     * @return String status, contoh: "success".
     */
    public String getStatus() {
        return status;
    }

    /**
     * Mengembalikan pesan dari respons API.
     * @return String pesan, contoh: "Data riwayat berhasil diambil".
     */
    public String getMessage() {
        return message;
    }

    /**
     * Mengembalikan daftar riwayat pengaduan.
     * @return Sebuah {@link List} dari {@link PengaduanRiwayatItem}.
     */
    public List<PengaduanRiwayatItem> getData() {
        return data;
    }
}