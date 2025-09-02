package com.alisteners.pengaduan.data.remote.config;

public class ApiConfig {

    // GANTI DENGAN IP ADDRESS LOKAL
    public static final String BASE_URL = "http://192.168.246.82/api_pengaduan/";

    // Endpoint untuk User
    public static final String URL_LOGIN = BASE_URL + "login.php";
    public static final String URL_REGISTER = BASE_URL + "register.php";
    public static final String URL_BUAT_PENGADUAN = BASE_URL + "buat_pengaduan.php";
    public static final String URL_GET_RIWAYAT = BASE_URL + "get_riwayat_pengaduan.php";
    public static final String URL_GET_DETAIL_PENGADUAN = BASE_URL + "get_detail_pengaduan.php";
    public static final String URL_GET_PENGADUAN_PUBLIK = BASE_URL + "get_pengaduan_publik.php";

    // Endpoint untuk Notifikasi
    public static final String URL_CEK_NOTIFIKASI = BASE_URL + "cek_notifikasi.php";
    public static final String URL_GET_NOTIFIKASI = BASE_URL + "get_notifikasi.php";
    public static final String URL_TANDAI_SEMUA_DILIHAT = BASE_URL + "tandai_semua_dilihat.php";
    public static final String URL_TANDAI_SATU_DILIHAT = BASE_URL + "tandai_satu_dilihat.php";

    // Endpoint untuk Operator
    public static final String URL_GET_PENGADUAN_OPERATOR = BASE_URL + "get_pengaduan_operator";
    public static final String URL_HAPUS_PENGADUAN = BASE_URL + "hapus_pengaduan.php";
    public static final String URL_GET_REKAP_STATISTIK = BASE_URL + "get_rekap_statistik.php";
    public static final String URL_UPDATE_PENGADUAN = BASE_URL + "update_pengaduan.php";
    public static final String URL_SEARCH_PENGADUAN = BASE_URL +"search_pengaduan.php";

    // Endpoint untuk Statistik User
    public static final String URL_GET_STATISTIK_USER = BASE_URL + "get_statistik_user.php";
    public static final String URL_GET_STATISTIK_OPERATOR = BASE_URL + "get_statistik_operator.php";

    // Endpoint Ubah Password
    public static final String URL_UBAH_PASSWORD = BASE_URL + "ubah_password.php";
    // Endpoint Cetak Laporan Pengaduan
    public static final String URL_CETAK_LAPORAN = BASE_URL + "cetak_laporan.php";

}