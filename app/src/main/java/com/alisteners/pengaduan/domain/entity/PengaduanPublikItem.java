package com.alisteners.pengaduan.domain.entity;

import com.google.gson.annotations.SerializedName;

public class PengaduanPublikItem {
    @SerializedName("idPengaduan") private int idPengaduan;
    @SerializedName("tanggalPengaduan") private String tanggalPengaduan;
    @SerializedName("kategori") private String kategori;
    @SerializedName("status") private String status;
    @SerializedName("namaPelapor") private String namaPelapor;
    @SerializedName("deskripsiSingkat") private String deskripsiSingkat;
    @SerializedName("pathImage") private String pathImage;

    // Getters
    public int getIdPengaduan() { return idPengaduan; }
    public String getTanggalPengaduan() { return tanggalPengaduan; }
    public String getKategori() { return kategori; }
    public String getStatus() { return status; }
    public String getNamaPelapor() { return namaPelapor; }
    public String getDeskripsiSingkat() { return deskripsiSingkat; }
    public String getPathImage() { return pathImage; }
}