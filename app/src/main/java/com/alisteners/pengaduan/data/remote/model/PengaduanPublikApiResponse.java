package com.alisteners.pengaduan.data.remote.model;

import com.alisteners.pengaduan.domain.entity.PengaduanPublikItem;
import com.google.gson.annotations.SerializedName;
import java.util.List;

/**
 * Kelas ini merepresentasikan struktur respons JSON dari endpoint API yang mengembalikan daftar
 * pengaduan publik (get_pengaduan_publik.php).
 * Berfungsi sebagai Data Transfer Object (DTO) untuk Retrofit dan Gson.
 */
public class PengaduanPublikApiResponse {

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
     * Berisi daftar (List) dari objek PengaduanPublikItem.
     * Gson akan secara otomatis mem-parsing array JSON "data" menjadi sebuah List dari objek ini.
     * Dipetakan dari field "data" dalam JSON.
     */
    @SerializedName("data")
    private List<PengaduanPublikItem> data;


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
     * @return String pesan, contoh: "Data berhasil diambil".
     */
    public String getMessage() {
        return message;
    }

    /**
     * Mengembalikan daftar pengaduan publik.
     * @return Sebuah {@link List} dari {@link PengaduanPublikItem}.
     */
    public List<PengaduanPublikItem> getData() {
        return data;
    }
}