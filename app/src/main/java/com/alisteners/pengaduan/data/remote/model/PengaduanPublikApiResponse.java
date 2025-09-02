package com.alisteners.pengaduan.data.remote.model;

import com.alisteners.pengaduan.domain.entity.PengaduanPublikItem;
import com.google.gson.annotations.SerializedName;
import java.util.List;

public class PengaduanPublikApiResponse {
    @SerializedName("status") private String status;
    @SerializedName("message") private String message;
    @SerializedName("data") private List<PengaduanPublikItem> data;

    // Getters
    public String getStatus() { return status; }
    public String getMessage() { return message; }
    public List<PengaduanPublikItem> getData() { return data; }
}