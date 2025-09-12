package com.alisteners.pengaduan.domain.operator.entity;

import com.google.gson.annotations.SerializedName;

/**
 * Kelas entitas yang merepresentasikan data ringkasan (summary) dari
 * jumlah total pengaduan berdasarkan statusnya.
 * Digunakan sebagai bagian dari objek {@link RekapData} untuk dashboard operator.
 */
public class Summary {

    /**
     * Jumlah total semua pengaduan yang ada di sistem.
     */
    @SerializedName("total")
    public int total;

    /**
     * Jumlah pengaduan yang saat ini berstatus "Menunggu".
     */
    @SerializedName("menunggu")
    public int menunggu;

    /**
     * Jumlah pengaduan yang saat ini berstatus "Diproses".
     */
    @SerializedName("diproses")
    public int diproses;

    /**
     * Jumlah pengaduan yang telah berstatus "Selesai".
     */
    @SerializedName("selesai")
    public int selesai;
}