package com.alisteners.pengaduan.domain.entity;

/**
 * Kelas entitas (dalam bentuk Record) yang merepresentasikan satu item ringkas
 * dalam daftar riwayat pengaduan pengguna.
 * Menjadi model data yang lebih ringan dibandingkan {@link DetailPengaduan} untuk
 * keperluan penampilan daftar.
 *
 * @param idPengaduan ID unik dari pengaduan.
 * @param tanggalPengaduan Waktu dan tanggal saat pengaduan dibuat.
 * @param kategori Kategori kerusakan yang dilaporkan.
 * @param status Status terkini dari pengaduan.
 * @param lokasiSingkat Beberapa kata awal dari lokasi untuk pratinjau.
 */
public record PengaduanRiwayatItem(int idPengaduan, String tanggalPengaduan, String kategori,
                                   String status, String lokasiSingkat) {

}