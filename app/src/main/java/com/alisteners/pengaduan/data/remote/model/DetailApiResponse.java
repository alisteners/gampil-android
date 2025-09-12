package com.alisteners.pengaduan.data.remote.model;

import com.alisteners.pengaduan.domain.entity.DetailPengaduan;

import com.google.gson.annotations.SerializedName;

/**
 * Kelas ini merepresentasikan struktur respons JSON lengkap dari endpoint API get_detail_pengaduan.php.
 * Berfungsi sebagai Data Transfer Object (DTO) yang digunakan oleh Retrofit dan Gson untuk mem-parsing
 * data dari server ke dalam objek Java yang terstruktur.
 */
public class DetailApiResponse {

    /**
     * Menunjukkan status dari respons API, misalnya "success" atau "error".
     * Dipetakan dari field "status" dalam JSON.
     */
    @SerializedName("status")
    private String status;

    /**
     * Berisi pesan deskriptif dari server terkait hasil request.
     * Misalnya "Data berhasil diambil" atau pesan error spesifik.
     * Dipetakan dari field "message" dalam JSON.
     */
    @SerializedName("message")
    private String message;

    /**
     * Objek utama yang berisi semua detail dari pengaduan.
     * Gson akan secara otomatis mem-parsing objek JSON "data" menjadi kelas DetailPengaduan.
     * Dipetakan dari field "data" dalam JSON.
     */
    @SerializedName("data")
    private DetailPengaduan data;


    // --- Getters ---
    // Metode getter memungkinkan layer lain (seperti Repository) untuk mengakses
    // properti privat dari kelas ini dengan aman.

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
     * Mengembalikan objek data detail pengaduan.
     * @return Objek {@link DetailPengaduan} yang berisi semua informasi detail.
     */
    public DetailPengaduan getData() {
        return data;
    }
}