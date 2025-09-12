package com.alisteners.pengaduan.domain.operator.entity;

import com.google.gson.annotations.SerializedName;
import java.util.List;

/**
 * Kelas entitas yang merepresentasikan paket lengkap data rekapitulasi dan statistik
 * untuk dashboard operator.
 * Objek ini menggabungkan ringkasan umum, distribusi per kategori, dan tren bulanan
 * menjadi satu struktur data yang kohesif.
 */
public class RekapData {

    /**
     * Objek yang berisi data ringkasan (summary) jumlah total pengaduan
     * berdasarkan statusnya (menunggu, diproses, selesai).
     */
    @SerializedName("summary")
    public Summary summary;

    /**
     * Daftar (List) yang berisi data distribusi jumlah pengaduan untuk setiap kategori.
     * Digunakan untuk membuat visualisasi seperti diagram lingkaran (pie chart).
     */
    @SerializedName("distribusiKategori")
    public List<KategoriDistribusi> distribusiKategori;

    /**
     * Daftar (List) yang berisi data tren jumlah pengaduan per bulan.
     * Digunakan untuk membuat visualisasi seperti diagram batang (bar chart)
     * untuk menunjukkan tren laporan dari waktu ke waktu.
     */
    @SerializedName("trenBulanan")
    public List<TrenBulanan> trenBulanan;
}