package com.alisteners.pengaduan.data.remote.config;

/**
 * Kelas konfigurasi yang berisi semua konstanta terkait endpoint API server.
 * Kelas ini berfungsi sebagai satu tempat untuk mengelola semua URL API,
 * memudahkan perubahan jika ada pembaruan pada alamat server atau endpoint.
 */
public class ApiConfig {

    /**
     * Alamat dasar (base URL) dari server API.
     * Pastikan untuk mengganti alamat IP ini sesuai dengan alamat IP lokal server
     * saat menjalankan aplikasi di perangkat fisik yang terhubung ke jaringan yang sama.
     */
    public static final String BASE_URL = "http://10.237.52.82/gampil-api/";

    // --- Endpoint untuk Fungsionalitas Pengguna (Masyarakat) ---

    /** Endpoint untuk proses login pengguna. */
    public static final String URL_LOGIN = BASE_URL + "login.php";
    /** Endpoint untuk proses registrasi pengguna baru. */
    public static final String URL_REGISTER = BASE_URL + "register.php";
    /** Endpoint untuk mengirim data pengaduan baru ke server. */
    public static final String URL_BUAT_PENGADUAN = BASE_URL + "buat_pengaduan.php";
    /** Endpoint untuk mengambil daftar riwayat pengaduan milik pengguna yang sedang login. */
    public static final String URL_GET_RIWAYAT = BASE_URL + "get_riwayat_pengaduan.php";
    /** Endpoint untuk mengambil detail dari satu pengaduan spesifik. */
    public static final String URL_GET_DETAIL_PENGADUAN = BASE_URL + "get_detail_pengaduan.php";
    /** Endpoint untuk mengambil daftar pengaduan yang bersifat publik. */
    public static final String URL_GET_PENGADUAN_PUBLIK = BASE_URL + "get_pengaduan_publik.php";

    // --- Endpoint untuk Fungsionalitas Notifikasi ---

    /** Endpoint untuk memeriksa apakah ada notifikasi baru yang belum dibaca. */
    public static final String URL_CEK_NOTIFIKASI = BASE_URL + "cek_notifikasi.php";
    /** Endpoint untuk mengambil semua notifikasi milik pengguna. */
    public static final String URL_GET_NOTIFIKASI = BASE_URL + "get_notifikasi.php";
    /** Endpoint untuk menandai semua notifikasi sebagai "telah dilihat". */
    public static final String URL_TANDAI_SEMUA_DILIHAT = BASE_URL + "tandai_semua_dilihat.php";
    /** Endpoint untuk menandai satu notifikasi spesifik sebagai "telah dilihat". */
    public static final String URL_TANDAI_SATU_DILIHAT = BASE_URL + "tandai_satu_dilihat.php";

    // --- Endpoint untuk Fungsionalitas Operator ---

    /** Endpoint untuk mengambil daftar pengaduan berdasarkan status (untuk operator). */
    public static final String URL_GET_PENGADUAN_OPERATOR = BASE_URL + "get_pengaduan_operator";
    /** Endpoint untuk menghapus pengaduan oleh operator. */
    public static final String URL_HAPUS_PENGADUAN = BASE_URL + "hapus_pengaduan.php";
    /** Endpoint untuk mengambil data rekapitulasi dan statistik pengaduan. */
    public static final String URL_GET_REKAP_STATISTIK = BASE_URL + "get_rekap_statistik.php";
    /** Endpoint untuk memperbarui status dan menambahkan tindak lanjut pada pengaduan. */
    public static final String URL_UPDATE_PENGADUAN = BASE_URL + "update_pengaduan.php";
    /** Endpoint untuk melakukan pencarian pengaduan berdasarkan kata kunci. */
    public static final String URL_SEARCH_PENGADUAN = BASE_URL +"search_pengaduan.php";

    // --- Endpoint untuk Statistik (Dashboard) ---

    /** Endpoint untuk mengambil data statistik ringkas untuk dashboard pengguna. */
    public static final String URL_GET_STATISTIK_USER = BASE_URL + "get_statistik_user.php";
    /** Endpoint untuk mengambil data statistik ringkas untuk dashboard operator. */
    public static final String URL_GET_STATISTIK_OPERATOR = BASE_URL + "get_statistik_operator.php";

    // --- Endpoint Lain-lain ---

    /** Endpoint untuk mengubah password pengguna. */
    public static final String URL_UBAH_PASSWORD = BASE_URL + "ubah_password.php";
    /** Endpoint untuk mencetak laporan pengaduan (format PDF/Cetak). */
    public static final String URL_CETAK_LAPORAN = BASE_URL + "cetak_laporan.php";

}