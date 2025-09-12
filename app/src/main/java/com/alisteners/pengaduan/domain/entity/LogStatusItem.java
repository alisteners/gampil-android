package com.alisteners.pengaduan.domain.entity;

import com.google.gson.annotations.SerializedName;
import java.util.List;

/**
 * Kelas entitas yang merepresentasikan satu item atau "kejadian" dalam riwayat
 * atau timeline tindak lanjut sebuah pengaduan.
 * Setiap objek dari kelas ini mewakili satu perubahan status yang dilakukan oleh operator,
 * beserta informasi pendukungnya.
 */
public class LogStatusItem {

    /**
     * ID unik dari log atau catatan riwayat ini.
     */
    @SerializedName("id_log")
    public int idLog;

    /**
     * Status baru yang ditetapkan pada pengaduan saat log ini dibuat (misal: "Diproses", "Selesai").
     */
    @SerializedName("status_baru")
    public String status;

    /**
     * Catatan atau komentar yang diberikan oleh operator terkait perubahan status ini.
     */
    @SerializedName("catatan")
    public String catatan;

    /**
     * Waktu dan tanggal saat log ini dibuat, dalam format String dari server.
     */
    @SerializedName("timestamp")
    public String timestamp;

    /**
     * Nama operator yang melakukan pembaruan status ini.
     */
    @SerializedName("nama_operator")
    public String namaOperator;

    /**
     * Daftar (List) yang berisi path atau URL relatif dari foto-foto bukti
     * yang dilampirkan oleh operator pada saat pembaruan status ini.
     */
    @SerializedName("foto_bukti")
    public List<String> fotoBukti;

    /**
     * Konstruktor kosong.
     * Dibutuhkan oleh library Gson untuk proses deserialisasi dari JSON ke objek Java.
     */
    public LogStatusItem() {
        // Konstruktor kosong untuk Gson
    }
}