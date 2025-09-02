package com.alisteners.pengaduan.domain.operator.entity;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class RekapData {
    @SerializedName("summary")
    public Summary summary;

    // Pastikan nama ini cocok dengan PHP (camelCase)
    @SerializedName("distribusiKategori")
    public List<KategoriDistribusi> distribusiKategori;

    // Pastikan nama ini cocok dengan PHP (camelCase)
    @SerializedName("trenBulanan")
    public List<TrenBulanan> trenBulanan;
}