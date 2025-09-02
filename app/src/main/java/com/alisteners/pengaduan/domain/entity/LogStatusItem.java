package com.alisteners.pengaduan.domain.entity;

import com.google.gson.annotations.SerializedName;
import java.util.List;

// Class ini merepresentasikan satu "kejadian" di dalam timeline
public class LogStatusItem {

    @SerializedName("id_log")
    public int idLog;

    @SerializedName("status_baru")
    public String status;

    @SerializedName("catatan")
    public String catatan;

    @SerializedName("timestamp")
    public String timestamp;

    @SerializedName("nama_operator")
    public String namaOperator;

    // List untuk menampung path foto-foto yang dilampirkan pada log ini
    @SerializedName("foto_bukti")
    public List<String> fotoBukti;

    public LogStatusItem() {
        // Konstruktor kosong untuk Gson
    }
}