package com.alisteners.pengaduan.data.remote.service;

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

public interface ApiService {
    @Multipart
    @POST("buat_pengaduan.php")
    Call<PengaduanResponse> kirimPengaduanDenganFile(
            @PartMap Map<String, RequestBody> params,
            @Part MultipartBody.Part fileRle,        // File pertama: data RLE
            @Part MultipartBody.Part fileThumbnail   // File kedua: Gambar Thumbnail
    );

    @FormUrlEncoded
    @POST("get_riwayat_pengaduan.php")
    Call<RiwayatApiResponse> getRiwayatPengaduan(@Field("idUser") int idUser);

    @GET("get_detail_pengaduan.php")
    Call<DetailApiResponse> getDetailPengaduan(@Query("id") int idPengaduan);

    @GET("get_pengaduan_publik.php")
    Call<PengaduanPublikApiResponse> getPengaduanPublik(@Query("status") String status);

    @GET("get_pengaduan_operator.php")
    Call<PengaduanPublikApiResponse> getPengaduanOperatorByStatus(@Query("status") String status);

    @Multipart
    @POST("update_pengaduan.php")
    Call<PengaduanResponse> updatePengaduan(
            @PartMap Map<String, RequestBody> params,
            // Menggunakan List<MultipartBody.Part> untuk mengirim banyak file
            @Part List<MultipartBody.Part> fotoTindakLanjut
    );

    @Multipart
    @POST("hapus_pengaduan.php")
    Call<PengaduanResponse> hapusPengaduan(
            @Part("idPengaduan") RequestBody idPengaduan,
            @Part("alasan") RequestBody alasan
    );

    @GET("get_rekap_statistik.php")
    Call<RekapApiResponse> getRekapStatistik();

    @GET("search_pengaduan.php")
    Call<PengaduanPublikApiResponse> searchPengaduan(
            @Query("keyword") String keyword,
            @Query("status") String status
    );
}