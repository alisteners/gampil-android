package com.alisteners.pengaduan.data.remote.model;

import com.google.gson.annotations.SerializedName;

public class PengaduanResponse {
    @SerializedName("status")
    private String status;

    @SerializedName("message")
    private String message;

    // Getter
    public String getStatus() {
        return status;
    }
    public String getMessage() {
        return message;
    }
}