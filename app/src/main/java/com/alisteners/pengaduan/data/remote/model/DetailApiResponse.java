package com.alisteners.pengaduan.data.remote.model;

import com.alisteners.pengaduan.domain.entity.DetailPengaduan;

import com.google.gson.annotations.SerializedName;

/**
 * Kelas ini merepresentasikan struktur respons JSON lengkap dari API get_detail_pengaduan.php.
 * Ini berfungsi sebagai Data Transfer Object (DTO) untuk Retrofit.
 */
public class DetailApiResponse {

    @SerializedName("status")
    private String status;

    @SerializedName("message")
    private String message;

    // Key "data" di JSON akan dipetakan ke field ini.
    // Tipe datanya adalah DetailPengaduan, yang akan di-parsing oleh Gson secara rekursif.
    @SerializedName("data")
    private DetailPengaduan data;

    // Getters untuk memungkinkan layer lain (seperti Repository) mengakses data ini.
    public String getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    public DetailPengaduan getData() {
        return data;
    }
}
