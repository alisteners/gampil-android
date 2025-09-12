package com.alisteners.pengaduan.data.remote.model;

import com.alisteners.pengaduan.domain.operator.entity.RekapData;
import com.google.gson.annotations.SerializedName;

/**
 * Kelas ini merepresentasikan struktur respons JSON dari endpoint API yang mengembalikan
 * data rekapitulasi statistik (get_rekap_statistik.php).
 * Berfungsi sebagai Data Transfer Object (DTO) untuk Retrofit dan Gson.
 */
public class RekapApiResponse {

    /**
     * Menunjukkan status dari respons API, misalnya "success" atau "error".
     * Dipetakan dari field "status" dalam JSON.
     */
    @SerializedName("status")
    public String status;

    /**
     * Objek utama yang berisi semua data rekapitulasi dan statistik.
     * Gson akan secara otomatis mem-parsing objek JSON "data" menjadi kelas {@link RekapData}.
     * Dipetakan dari field "data" dalam JSON.
     */
    @SerializedName("data")
    public RekapData data;
}