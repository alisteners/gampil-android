package com.alisteners.pengaduan.data.remote.model;

import com.google.gson.annotations.SerializedName;

/**
 * Kelas ini merepresentasikan struktur respons JSON generik dari berbagai endpoint API
 * yang tidak mengembalikan data spesifik, melainkan hanya status keberhasilan dan pesan.
 * Contohnya adalah endpoint untuk membuat pengaduan, update, atau hapus, di mana
 * server hanya perlu mengonfirmasi bahwa aksi tersebut berhasil atau gagal.
 */
public class PengaduanResponse {

    /**
     * Menunjukkan status dari respons API, misalnya "success" atau "error".
     * Dipetakan dari field "status" dalam JSON.
     */
    @SerializedName("status")
    private String status;

    /**
     * Berisi pesan deskriptif dari server terkait hasil request.
     * Misalnya "Pengaduan berhasil dikirim" atau "Gagal menghapus data".
     * Dipetakan dari field "message" dalam JSON.
     */
    @SerializedName("message")
    private String message;


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
     * @return String pesan, contoh: "Pengaduan berhasil dikirim".
     */
    public String getMessage() {
        return message;
    }
}