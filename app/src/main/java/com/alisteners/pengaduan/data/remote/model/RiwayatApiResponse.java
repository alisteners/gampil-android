package com.alisteners.pengaduan.data.remote.model;

import com.alisteners.pengaduan.domain.entity.PengaduanRiwayatItem;
import com.google.gson.annotations.SerializedName;
import java.util.List;

public class RiwayatApiResponse {
    @SerializedName("status")
    private String status;

    @SerializedName("message")
    private String message;

    @SerializedName("data") // Asumsi PHP mengembalikan array riwayat di bawah key "data"
    private List<PengaduanRiwayatItem> data; // List dari entitas domain

    // Getters
    public String getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    public List<PengaduanRiwayatItem> getData() {
        return data;
    }

    // Tambahkan setter jika diperlukan, atau buat field final dengan konstruktor
}