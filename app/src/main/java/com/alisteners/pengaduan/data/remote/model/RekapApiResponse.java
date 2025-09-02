package com.alisteners.pengaduan.data.remote.model;

import com.alisteners.pengaduan.domain.operator.entity.RekapData;
import com.google.gson.annotations.SerializedName;
public class RekapApiResponse {
    @SerializedName("status") public String status;
    @SerializedName("data") public RekapData data;
}