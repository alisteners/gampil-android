package com.alisteners.pengaduan.data.remote.service;

import androidx.annotation.Nullable;

import com.alisteners.pengaduan.data.remote.model.PengaduanPublikApiResponse;
import com.alisteners.pengaduan.data.remote.model.PengaduanResponse;
import com.alisteners.pengaduan.data.remote.model.RekapApiResponse;
import com.alisteners.pengaduan.data.remote.model.RiwayatApiResponse;
import com.alisteners.pengaduan.data.remote.model.DetailApiResponse;

import java.util.List;
import java.util.Map;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.PartMap;
import retrofit2.http.Query;

/**
 * Interface yang mendefinisikan semua endpoint API menggunakan anotasi Retrofit.
 * Retrofit akan mengimplementasikan interface ini secara otomatis untuk melakukan
 * panggilan HTTP ke server.
 */
public interface ApiService {

    /**
     * Mengirim data pengaduan baru beserta file-file terkait (data RLE/fallback dan thumbnail) ke server.
     * Menggunakan metode POST dengan format multipart/form-data karena ada pengiriman file.
     *
     * @param params Peta (Map) yang berisi semua parameter teks (deskripsi, lokasi, metrik, dll).
     * @param fileRle MultipartBody.Part untuk file data utama. Bisa berupa file .rle atau .jpg (jika RLE tidak efisien).
     * Dapat bernilai null jika tidak ada file utama yang diunggah.
     * @param fileThumbnail MultipartBody.Part untuk file thumbnail gambar (selalu .jpg).
     * @return Objek Call yang akan dieksekusi untuk mendapatkan {@link PengaduanResponse}.
     */
    @Multipart
    @POST("buat_pengaduan.php")
    Call<PengaduanResponse> kirimPengaduanDenganFile(
            @PartMap Map<String, RequestBody> params,
            @Part @Nullable MultipartBody.Part fileRle,
            @Part MultipartBody.Part fileThumbnail
    );

    /**
     * Mengambil daftar riwayat pengaduan milik seorang pengguna.
     * Menggunakan metode POST dengan format form-urlencoded.
     *
     * @param idUser ID dari pengguna yang riwayatnya ingin diambil.
     * @return Objek Call yang akan dieksekusi untuk mendapatkan {@link RiwayatApiResponse}.
     */
    @FormUrlEncoded
    @POST("get_riwayat_pengaduan.php")
    Call<RiwayatApiResponse> getRiwayatPengaduan(@Field("idUser") int idUser);

    /**
     * Mengambil detail lengkap dari satu pengaduan spesifik.
     * Menggunakan metode GET dengan parameter query.
     *
     * @param idPengaduan ID dari pengaduan yang ingin dilihat detailnya.
     * @return Objek Call yang akan dieksekusi untuk mendapatkan {@link DetailApiResponse}.
     */
    @GET("get_detail_pengaduan.php")
    Call<DetailApiResponse> getDetailPengaduan(@Query("id") int idPengaduan);

    /**
     * Mengambil daftar pengaduan publik yang bisa dilihat oleh semua pengguna.
     *
     * @param status String status untuk memfilter pengaduan (misal: "Selesai").
     * @return Objek Call yang akan dieksekusi untuk mendapatkan {@link PengaduanPublikApiResponse}.
     */
    @GET("get_pengaduan_publik.php")
    Call<PengaduanPublikApiResponse> getPengaduanPublik(@Query("status") String status);

    /**
     * Mengambil daftar pengaduan untuk dashboard operator, difilter berdasarkan status.
     *
     * @param status String status untuk memfilter pengaduan (misal: "Menunggu", "Diproses").
     * @return Objek Call yang akan dieksekusi untuk mendapatkan {@link PengaduanPublikApiResponse}.
     */
    @GET("get_pengaduan_operator.php")
    Call<PengaduanPublikApiResponse> getPengaduanOperatorByStatus(@Query("status") String status);

    /**
     * Memperbarui status dan data tindak lanjut dari sebuah pengaduan oleh operator.
     * Menggunakan metode POST dengan format multipart/form-data karena ada kemungkinan
     * pengiriman file foto bukti tindak lanjut.
     *
     * @param params Peta (Map) yang berisi parameter teks (idPengaduan, status, catatan, dll).
     * @param fotoTindakLanjut Sebuah List dari MultipartBody.Part untuk mengirim satu atau lebih file foto.
     * @return Objek Call yang akan dieksekusi untuk mendapatkan {@link PengaduanResponse}.
     */
    @Multipart
    @POST("update_pengaduan.php")
    Call<PengaduanResponse> updatePengaduan(
            @PartMap Map<String, RequestBody> params,
            @Part List<MultipartBody.Part> fotoTindakLanjut
    );

    /**
     * Menghapus sebuah pengaduan dari sistem (hanya bisa dilakukan oleh operator).
     *
     * @param idPengaduan ID dari pengaduan yang akan dihapus.
     * @param alasan Alasan mengapa pengaduan tersebut dihapus.
     * @return Objek Call yang akan dieksekusi untuk mendapatkan {@link PengaduanResponse}.
     */
    @Multipart
    @POST("hapus_pengaduan.php")
    Call<PengaduanResponse> hapusPengaduan(
            @Part("idPengaduan") RequestBody idPengaduan,
            @Part("alasan") RequestBody alasan
    );

    /**
     * Mengambil data rekapitulasi statistik untuk ditampilkan di dashboard operator.
     *
     * @return Objek Call yang akan dieksekusi untuk mendapatkan {@link RekapApiResponse}.
     */
    @GET("get_rekap_statistik.php")
    Call<RekapApiResponse> getRekapStatistik();

    /**
     * Melakukan pencarian pengaduan berdasarkan kata kunci dan status.
     *
     * @param keyword Kata kunci yang akan dicari dalam data pengaduan.
     * @param status Status pengaduan untuk memfilter hasil pencarian.
     * @return Objek Call yang akan dieksekusi untuk mendapatkan {@link PengaduanPublikApiResponse}.
     */
    @GET("search_pengaduan.php")
    Call<PengaduanPublikApiResponse> searchPengaduan(
            @Query("keyword") String keyword,
            @Query("status") String status
    );
}