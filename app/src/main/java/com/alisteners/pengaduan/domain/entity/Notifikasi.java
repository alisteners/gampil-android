package com.alisteners.pengaduan.domain.entity;

import com.google.gson.annotations.SerializedName;

public class Notifikasi {

    @SerializedName("idNotifikasi")
    private int idNotifikasi;

    @SerializedName("idPengaduan")
    private int idPengaduan;

    @SerializedName("pesan")
    private String pesan;

    @SerializedName("timestamp")
    private String timestamp;

    @SerializedName("sudahDilihat")
    private boolean sudahDilihat;

    public int getIdNotifikasi() {
        return idNotifikasi;
    }

    public int getIdPengaduan() {
        return idPengaduan;
    }

    public String getPesan() {
        return pesan;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public boolean isSudahDilihat() {
        return sudahDilihat;
    }
}