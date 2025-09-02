package com.alisteners.pengaduan.domain.entity;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class DetailPengaduan {
    // Info dari tabel pengaduan dan users
    @SerializedName("idPengaduan")
    public int idPengaduan;
    @SerializedName("tanggalPengaduan")
    public String tanggalPengaduan;
    @SerializedName("kategori")
    public String kategori;
    @SerializedName("status")
    public String status;
    @SerializedName("lokasi")
    public String lokasi;
    @SerializedName("deskripsi")
    public String deskripsi;
    @SerializedName("latitude")
    public double latitude;
    @SerializedName("longitude")
    public double longitude;
    @SerializedName("namaLengkap")
    public String namaPelapor;

    // Info dari tabel foto
    @SerializedName("pathImage")
    public String pathImage;
    @SerializedName("lebar")
    public int lebar;
    @SerializedName("tinggi")
    public int tinggi;
    @SerializedName("ukuranAsli")
    public int ukuranAsli;
    @SerializedName("ukuranSetelahResize")
    public int ukuranSetelahResize;
    @SerializedName("ukuranGrayscale")
    public int ukuranGrayscale;
    @SerializedName("ukuranFileRLEByte")
    public int ukuranFileRLEByte;
    @SerializedName("rasioKompresiRLE")
    public double rasioKompresiRLE;
    @SerializedName("rasioKompresiStandar")
    public double rasioKompresiStandar;
    @SerializedName("waktuProsesGambarMs")
    public long waktuProsesGambarMs;

    // Untuk menerima data timeline
    @SerializedName("riwayat_tindak_lanjut")
    public List<LogStatusItem> riwayatTindakLanjut;

    public DetailPengaduan() {
        // Konstruktor kosong untuk Gson
    }

    // Getters
    public String getNamaPelapor() { return namaPelapor; }
    public double getLatitude() { return latitude; }
    public double getLongitude() { return longitude; }
    public List<LogStatusItem> getRiwayatTindakLanjut() {
        return riwayatTindakLanjut;
    }
}