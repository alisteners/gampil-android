package com.alisteners.pengaduan.domain.operator.entity;

import com.google.gson.annotations.SerializedName;

/**
 * Kelas entitas yang merepresentasikan data distribusi pengaduan berdasarkan kategori.
 * Setiap objek dari kelas ini menampung satu nama kategori beserta jumlah total
 * pengaduan dalam kategori tersebut.
 * Digunakan untuk menampilkan data statistik, misalnya dalam diagram lingkaran (pie chart).
 */
public class KategoriDistribusi {

    /**
     * Nama dari kategori pengaduan (misal: "Jalan Rusak", "Lampu Mati").
     */
    @SerializedName("kategori")
    public String kategori;

    /**
     * Jumlah total pengaduan yang masuk untuk kategori ini.
     */
    @SerializedName("jumlah")
    public int jumlah;
}