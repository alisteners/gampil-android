package com.alisteners.pengaduan.domain.entity;

/**
 * @param lokasiSingkat Misalnya, beberapa kata awal dari lokasi
 */
public record PengaduanRiwayatItem(int idPengaduan, String tanggalPengaduan, String kategori,
                                   String status, String lokasiSingkat) {

}