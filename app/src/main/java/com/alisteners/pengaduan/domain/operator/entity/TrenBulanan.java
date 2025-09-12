package com.alisteners.pengaduan.domain.operator.entity;

import com.google.gson.annotations.SerializedName;

/**
 * Kelas entitas yang merepresentasikan data tren jumlah pengaduan untuk satu bulan tertentu.
 * Digunakan sebagai bagian dari objek {@link RekapData} untuk menampilkan
 * statistik tren pengaduan dari waktu ke waktu, misalnya dalam diagram batang (bar chart).
 */
public class TrenBulanan {

    /**
     * Nama bulan (misal: "Januari", "Februari").
     */
    @SerializedName("bulan")
    public String bulan;

    /**
     * Jumlah total pengaduan yang masuk pada bulan tersebut.
     */
    @SerializedName("jumlah")
    public int jumlah;
}