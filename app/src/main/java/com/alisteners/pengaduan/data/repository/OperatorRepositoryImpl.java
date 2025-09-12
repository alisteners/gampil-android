package com.alisteners.pengaduan.data.repository;

import android.content.Context;
import android.util.Log;
import androidx.annotation.NonNull;

import com.alisteners.pengaduan.data.remote.config.ApiClient;
import com.alisteners.pengaduan.data.remote.model.PengaduanPublikApiResponse;
import com.alisteners.pengaduan.data.remote.model.PengaduanResponse;
import com.alisteners.pengaduan.data.remote.model.RekapApiResponse;
import com.alisteners.pengaduan.data.remote.service.ApiService;
import com.alisteners.pengaduan.domain.operator.repository.OperatorRepository;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Implementasi dari {@link OperatorRepository}.
 * Kelas ini menangani semua operasi data yang berkaitan dengan fungsionalitas operator,
 * seperti mengambil daftar pengaduan, memperbarui status, menghapus, dan lainnya.
 * Bertanggung jawab untuk berkomunikasi dengan server melalui ApiService dan
 * menyampaikan hasilnya kembali ke UseCase melalui callback.
 */
public class OperatorRepositoryImpl implements OperatorRepository {
    private static final String TAG = "OperatorRepositoryImpl";
    private final ApiService apiService;

    /**
     * Konstruktor untuk OperatorRepositoryImpl.
     * @param context Context aplikasi, digunakan untuk inisialisasi ApiClient.
     */
    public OperatorRepositoryImpl(Context context) {
        this.apiService = ApiClient.getApiService();
    }

    /**
     * Mengambil daftar pengaduan dari server, difilter berdasarkan status.
     *
     * @param status Status pengaduan yang ingin diambil (misal: "Menunggu", "Diproses").
     * @param callback Callback yang akan dipanggil dengan hasil (berhasil atau gagal).
     */
    @Override
    public void getPengaduanByStatus(String status, GetPengaduanCallback callback) {
        Log.d(TAG, "getPengaduanByStatus: Mengambil laporan untuk status: " + status);
        apiService.getPengaduanOperatorByStatus(status).enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<PengaduanPublikApiResponse> call, @NonNull Response<PengaduanPublikApiResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    PengaduanPublikApiResponse apiResponse = response.body();
                    if ("success".equalsIgnoreCase(apiResponse.getStatus()) && apiResponse.getData() != null) {
                        callback.onBerhasil(apiResponse.getData());
                    } else {
                        callback.onGagal(apiResponse.getMessage());
                    }
                } else {
                    callback.onGagal("Gagal mengambil data (Server Error: " + response.code() + ")");
                }
            }

            @Override
            public void onFailure(@NonNull Call<PengaduanPublikApiResponse> call, @NonNull Throwable t) {
                callback.onGagal("Error jaringan: " + t.getMessage());
            }
        });
    }

    /**
     * Memperbarui status, catatan, dan foto tindak lanjut dari sebuah pengaduan.
     *
     * @param idPengaduan ID pengaduan yang akan diperbarui.
     * @param idOperator ID operator yang melakukan pembaruan.
     * @param status Status baru untuk pengaduan.
     * @param catatan Catatan tindak lanjut dari operator.
     * @param files Daftar file foto bukti tindak lanjut (bisa kosong).
     * @param requestToken Token unik untuk validasi request.
     * @param callback Callback yang akan dipanggil dengan hasil pembaruan.
     */
    @Override
    public void updatePengaduan(int idPengaduan, int idOperator, String status, String catatan, List<File> files, String requestToken, UpdatePengaduanCallback callback) {
        Map<String, RequestBody> params = new HashMap<>();
        params.put("idPengaduan", RequestBody.create(MediaType.parse("text/plain"), String.valueOf(idPengaduan)));
        params.put("idOperator", RequestBody.create(MediaType.parse("text/plain"), String.valueOf(idOperator)));
        params.put("status", RequestBody.create(MediaType.parse("text/plain"), status));
        params.put("catatan", RequestBody.create(MediaType.parse("text/plain"), catatan));
        params.put("request_token", RequestBody.create(MediaType.parse("text/plain"), requestToken));

        List<MultipartBody.Part> fotoParts = new ArrayList<>();
        if (files != null && !files.isEmpty()) {
            for (File file : files) {
                RequestBody requestFile = RequestBody.create(MediaType.parse("image/jpeg"), file);
                MultipartBody.Part body = MultipartBody.Part.createFormData("fotoTindakLanjut[]", file.getName(), requestFile);
                fotoParts.add(body);
            }
        }

        apiService.updatePengaduan(params, fotoParts).enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<PengaduanResponse> call, @NonNull Response<PengaduanResponse> response) {
                if (response.isSuccessful() && response.body() != null && "success".equals(response.body().getStatus())) {
                    callback.onUpdateBerhasil(response.body().getMessage());
                } else {
                    String errorMessage = "Gagal memperbarui status.";
                    if(response.body() != null && response.body().getMessage() != null) {
                        errorMessage = response.body().getMessage();
                    } else if (!response.isSuccessful()) {
                        errorMessage += " (Error: " + response.code() + ")";
                    }
                    callback.onUpdateGagal(errorMessage);
                }
            }

            @Override
            public void onFailure(@NonNull Call<PengaduanResponse> call, @NonNull Throwable t) {
                callback.onUpdateGagal("Error jaringan: " + t.getMessage());
            }
        });
    }

    /**
     * Metode helper untuk membaca semua byte dari sebuah InputStream.
     * @param inputStream InputStream yang akan dibaca.
     * @return Array byte dari konten InputStream.
     * @throws IOException Jika terjadi error saat membaca stream.
     */
    private byte[] getBytesFromInputStream(InputStream inputStream) throws IOException {
        ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
        int bufferSize = 1024;
        byte[] buffer = new byte[bufferSize];
        int len;
        while ((len = inputStream.read(buffer)) != -1) {
            byteBuffer.write(buffer, 0, len);
        }
        return byteBuffer.toByteArray();
    }

    /**
     * Menghapus sebuah pengaduan dari server.
     *
     * @param idPengaduan ID pengaduan yang akan dihapus.
     * @param alasan Teks alasan mengapa pengaduan dihapus.
     * @param callback Callback yang akan dipanggil dengan hasil penghapusan.
     */
    @Override
    public void hapusPengaduan(int idPengaduan, String alasan, DeletePengaduanCallback callback) {
        Log.d(TAG, "hapusPengaduan: Menghapus pengaduan ID: " + idPengaduan + " dengan alasan: " + alasan);

        RequestBody idPengaduanBody = RequestBody.create(MediaType.parse("text/plain"), String.valueOf(idPengaduan));
        RequestBody alasanBody = RequestBody.create(MediaType.parse("text/plain"), alasan);

        apiService.hapusPengaduan(idPengaduanBody, alasanBody).enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<PengaduanResponse> call, @NonNull Response<PengaduanResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    if ("success".equals(response.body().getStatus())) {
                        callback.onDeleteBerhasil(response.body().getMessage());
                    } else {
                        callback.onDeleteGagal(response.body().getMessage());
                    }
                } else {
                    callback.onDeleteGagal("Gagal menghapus di server (Error: " + response.code() + ")");
                }
            }

            @Override
            public void onFailure(@NonNull Call<PengaduanResponse> call, @NonNull Throwable t) {
                Log.e(TAG, "onFailure: hapusPengaduan", t);
                callback.onDeleteGagal("Error jaringan: " + t.getMessage());
            }
        });
    }

    /**
     * Mengambil data rekapitulasi statistik dari server.
     *
     * @param callback Callback yang akan dipanggil dengan data rekap atau pesan error.
     */
    @Override
    public void getRekapData(GetRekapCallback callback) {
        Log.d(TAG, "getRekapData: Mengambil data rekap statistik...");
        apiService.getRekapStatistik().enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<RekapApiResponse> call, @NonNull Response<RekapApiResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    RekapApiResponse apiResponse = response.body();
                    if ("success".equalsIgnoreCase(apiResponse.status) && apiResponse.data != null) {
                        callback.onBerhasil(apiResponse.data);
                    } else {
                        callback.onGagal("Gagal mengambil data rekap (respons tidak sukses).");
                    }
                } else {
                    callback.onGagal("Gagal mengambil data rekap (Server Error: " + response.code() + ")");
                }
            }

            @Override
            public void onFailure(@NonNull Call<RekapApiResponse> call, @NonNull Throwable t) {
                Log.e(TAG, "onFailure: getRekapData", t);
                callback.onGagal("Error jaringan: " + t.getMessage());
            }
        });
    }

    /**
     * Melakukan pencarian pengaduan di server berdasarkan kata kunci dan status.
     *
     * @param keyword Kata kunci untuk pencarian.
     * @param status Status untuk memfilter hasil pencarian.
     * @param callback Callback yang akan dipanggil dengan hasil pencarian.
     */
    @Override
    public void searchPengaduan (String keyword, String status, GetPengaduanCallback callback){
        Log.d(TAG, "searchPengaduan: Mencari dengan keyword='" + keyword + "', status='" + status + "'");

        apiService.searchPengaduan(keyword, status).enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<PengaduanPublikApiResponse> call, @NonNull Response<PengaduanPublikApiResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    PengaduanPublikApiResponse apiResponse = response.body();
                    if ("success".equalsIgnoreCase(apiResponse.getStatus())) {
                        callback.onBerhasil(apiResponse.getData() != null ? apiResponse.getData() : new ArrayList<>());
                    } else {
                        callback.onGagal(apiResponse.getMessage());
                    }
                } else {
                    callback.onGagal("Gagal mencari data (Error: " + response.code() + ")");
                }
            }

            @Override
            public void onFailure(@NonNull Call<PengaduanPublikApiResponse> call, @NonNull Throwable t) {
                Log.e(TAG, "onFailure: searchPengaduan", t);
                callback.onGagal("Error Jaringan: " + t.getMessage());
            }
        });
    }
}