package com.alisteners.pengaduan.domain.operator.entity;

import com.google.gson.annotations.SerializedName;

public class Summary {
    @SerializedName("total") public int total;
    @SerializedName("menunggu") public int menunggu;
    @SerializedName("diproses") public int diproses;
    @SerializedName("selesai") public int selesai;
}