package com.alisteners.pengaduan.domain.entity;

public class PengaduanData {
    public final int idUser;
    public final String lokasi;
    public final String kategori;
    public final String deskripsi;
    public final boolean pernyataan;
    public final double latitude;
    public final double longitude;
    public final int lebar;
    public final int tinggi;
    public final int ukuranBitmapAsli;
    public final int ukuranBitmapSetelahResize;
    public int ukuranGrayscale;
    public final int ukuranFileRLEByte;
    public final double rasioKompresiRLE;
    public final double rasioKompresiStandar;
    public final long waktuProsesGambarMs;
    public final boolean isRleCompressed;

    public PengaduanData(int idUser, String lokasi, String kategori, String deskripsi,
                         boolean pernyataan, double latitude, double longitude,
                         int lebar, int tinggi, int ukuranBitmapAsli, int ukuranBitmapSetelahResize, int ukuranGrayscale,
                         int ukuranFileRLEByte, double rasioKompresiRLE, double rasioKompresiStandar,
                         long waktuProsesGambarMs, boolean isRleCompressed) {
        this.idUser = idUser;
        this.lokasi = lokasi;
        this.kategori = kategori;
        this.deskripsi = deskripsi;
        this.pernyataan = pernyataan;
        this.latitude = latitude;
        this.longitude = longitude;
        this.lebar = lebar;
        this.tinggi = tinggi;
        this.ukuranBitmapAsli = ukuranBitmapAsli;
        this.ukuranBitmapSetelahResize = ukuranBitmapSetelahResize;
        this.ukuranGrayscale = ukuranGrayscale;
        this.ukuranFileRLEByte = ukuranFileRLEByte;
        this.rasioKompresiRLE = rasioKompresiRLE;
        this.rasioKompresiStandar = rasioKompresiStandar;
        this.waktuProsesGambarMs = waktuProsesGambarMs;
        this.isRleCompressed = isRleCompressed;
    }

    public PengaduanData(int idUser, String lokasi, String kategori, String deskripsi, boolean isRleCompressed,
                         double latitude, double longitude, boolean pernyataan) {
        this.idUser = idUser;
        this.lokasi = lokasi;
        this.kategori = kategori;
        this.deskripsi = deskripsi;
        this.pernyataan = pernyataan;
        this.latitude = latitude;
        this.longitude = longitude;
        this.isRleCompressed = isRleCompressed;
        // Inisialisasi nilai default untuk properti lainnya
        this.lebar = 0;
        this.tinggi = 0;
        this.ukuranBitmapAsli = 0;
        this.ukuranBitmapSetelahResize = 0;
        this.ukuranGrayscale = 0;
        this.ukuranFileRLEByte = 0;
        this.rasioKompresiRLE = 0.0;
        this.rasioKompresiStandar = 0.0;
        this.waktuProsesGambarMs = 0;
    }
}