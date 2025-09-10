package com.alisteners.pengaduan.data.repository;

import android.content.Context;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.alisteners.pengaduan.data.remote.config.ApiClient;
import com.alisteners.pengaduan.data.remote.model.DetailApiResponse;
import com.alisteners.pengaduan.data.remote.model.PengaduanPublikApiResponse;
import com.alisteners.pengaduan.data.remote.model.PengaduanResponse;
import com.alisteners.pengaduan.data.remote.model.RekapApiResponse;
import com.alisteners.pengaduan.data.remote.model.RiwayatApiResponse;
import com.alisteners.pengaduan.data.remote.service.ApiService;
import com.alisteners.pengaduan.domain.entity.PengaduanData;
import com.alisteners.pengaduan.domain.operator.repository.OperatorRepository;
import com.alisteners.pengaduan.domain.repository.PengaduanRepository;
import com.google.gson.Gson;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PengaduanRepositoryImpl implements PengaduanRepository, OperatorRepository {
    private static final String TAG = "PengaduanRepoImpl";
    private final ApiService apiService;

    public PengaduanRepositoryImpl(Context context) {
        this.apiService = ApiClient.getApiService();
    }

    // IMPLEMENTASI DARI PengaduanRepository (Sisi Masyarakat)
    @Override
    public void kirimPengaduanKeServer(PengaduanData dataPengaduan, @Nullable File fileRle, @Nullable File fileThumbnail, KirimPengaduanCallback callback) {
        Log.d(TAG, "kirimPengaduanKeServer: Mengirim data dengan 2 file...");

        // --- PERBAIKAN VALIDASI DI SINI ---
        // Pastikan hanya thumbnail yang wajib ada. fileRle boleh null.
        if (fileThumbnail == null || !fileThumbnail.exists()) {
            callback.onGagal("File thumbnail tidak valid atau tidak ditemukan.");
            return;
        }
        // --- AKHIR PERBAIKAN ---

        Map<String, RequestBody> params = new HashMap<>();
        params.put("idUser", RequestBody.create(MediaType.parse("text/plain"), String.valueOf(dataPengaduan.idUser)));
        params.put("lokasi", RequestBody.create(MediaType.parse("text/plain"), dataPengaduan.lokasi));
        params.put("kategori", RequestBody.create(MediaType.parse("text/plain"), dataPengaduan.kategori));
        params.put("deskripsi", RequestBody.create(MediaType.parse("text/plain"), dataPengaduan.deskripsi));
        params.put("pernyataan", RequestBody.create(MediaType.parse("text/plain"), dataPengaduan.pernyataan ? "1" : "0"));
        params.put("latitude", RequestBody.create(MediaType.parse("text/plain"), String.valueOf(dataPengaduan.latitude)));
        params.put("longitude", RequestBody.create(MediaType.parse("text/plain"), String.valueOf(dataPengaduan.longitude)));
        params.put("lebar", RequestBody.create(MediaType.parse("text/plain"), String.valueOf(dataPengaduan.lebar)));
        params.put("tinggi", RequestBody.create(MediaType.parse("text/plain"), String.valueOf(dataPengaduan.tinggi)));
        params.put("ukuranAsli", RequestBody.create(MediaType.parse("text/plain"), String.valueOf(dataPengaduan.ukuranBitmapAsli)));
        params.put("ukuranSetelahResize", RequestBody.create(MediaType.parse("text/plain"), String.valueOf(dataPengaduan.ukuranBitmapSetelahResize)));
        params.put("ukuranGrayscale", RequestBody.create(MediaType.parse("text/plain"), String.valueOf(dataPengaduan.ukuranGrayscale)));
        params.put("ukuranFileRLE", RequestBody.create(MediaType.parse("text/plain"), String.valueOf(dataPengaduan.ukuranFileRLEByte)));
        params.put("rasioKompresiRLE", RequestBody.create(MediaType.parse("text/plain"), String.format(Locale.US, "%.2f", dataPengaduan.rasioKompresiRLE)));
        params.put("rasioKompresiStandar", RequestBody.create(MediaType.parse("text/plain"), String.format(Locale.US, "%.2f", dataPengaduan.rasioKompresiStandar)));
        params.put("waktuProsesGambarMs", RequestBody.create(MediaType.parse("text/plain"), String.valueOf(dataPengaduan.waktuProsesGambarMs)));

        MultipartBody.Part fileRlePart = null;
        // HANYA buat file part jika fileRle tidak null dan ada
        if (fileRle != null && fileRle.exists()) {
            RequestBody requestFileRle = RequestBody.create(MediaType.parse("application/octet-stream"), fileRle);
            fileRlePart = MultipartBody.Part.createFormData("fileRLE", fileRle.getName(), requestFileRle);
        }

        RequestBody requestFileThumbnail = RequestBody.create(MediaType.parse("image/jpeg"), fileThumbnail);
        MultipartBody.Part fileThumbnailPart = MultipartBody.Part.createFormData("fileThumbnail", fileThumbnail.getName(), requestFileThumbnail);

        apiService.kirimPengaduanDenganFile(params, fileRlePart, fileThumbnailPart).enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<PengaduanResponse> call, @NonNull Response<PengaduanResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    if ("success".equalsIgnoreCase(response.body().getStatus())) {
                        callback.onBerhasil(response.body().getMessage());
                    } else {
                        callback.onGagal(response.body().getMessage());
                    }
                } else {
                    callback.onGagal("Gagal mengirim pengaduan (Error: " + response.code() + ")");
                }
            }

            @Override
            public void onFailure(@NonNull Call<PengaduanResponse> call, @NonNull Throwable t) {
                callback.onGagal("Error jaringan: " + t.getMessage());
            }
        });
    }

    @Override
    public void getRiwayatPengaduan(int idUser, GetRiwayatCallback callback) {
        Log.d(TAG, "getRiwayatPengaduan: Mengambil riwayat untuk idUser: " + idUser);
        apiService.getRiwayatPengaduan(idUser).enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<RiwayatApiResponse> call, @NonNull Response<RiwayatApiResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    RiwayatApiResponse apiResponse = response.body();
                    if ("success".equalsIgnoreCase(apiResponse.getStatus())) {
                        callback.onBerhasil(apiResponse.getData() != null ? apiResponse.getData() : new ArrayList<>());
                    } else {
                        callback.onGagal(apiResponse.getMessage());
                    }
                } else {
                    callback.onGagal("Gagal mengambil riwayat (Server Error: " + response.code() + ")");
                }
            }

            @Override
            public void onFailure(@NonNull Call<RiwayatApiResponse> call, @NonNull Throwable t) {
                callback.onGagal("Error jaringan: " + t.getMessage());
            }
        });
    }

    @Override
    public void getDetailPengaduan(int idPengaduan, GetDetailCallback callback) {
        Log.d(TAG, "getDetailPengaduan: Mengambil detail untuk idPengaduan: " + idPengaduan);
        apiService.getDetailPengaduan(idPengaduan).enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<DetailApiResponse> call, @NonNull Response<DetailApiResponse> response) {
                Log.d("API_RESPONSE_DETAIL", new Gson().toJson(response.body()));
                if (response.isSuccessful() && response.body() != null) {
                    DetailApiResponse apiResponse = response.body();
                    if ("success".equalsIgnoreCase(apiResponse.getStatus()) && apiResponse.getData() != null) {
                        callback.onBerhasil(apiResponse.getData());
                    } else {
                        callback.onGagal(apiResponse.getMessage());
                    }
                } else {
                    callback.onGagal("Gagal mengambil detail (Server Error: " + response.code() + ")");
                }
            }

            @Override
            public void onFailure(@NonNull Call<DetailApiResponse> call, @NonNull Throwable t) {
                callback.onGagal("Error jaringan: " + t.getMessage());
            }
        });
    }

    @Override
    public void getPengaduanPublik(String status, GetPengaduanPublikCallback callback) {
        Log.d(TAG, "getPengaduanPublik: Mengambil pengaduan publik dengan status: " + status);
        apiService.getPengaduanPublik(status).enqueue(new Callback<>() {
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
                    callback.onGagal("Gagal mengambil data publik (Server Error: " + response.code() + ")");
                }
            }

            @Override
            public void onFailure(@NonNull Call<PengaduanPublikApiResponse> call, @NonNull Throwable t) {
                callback.onGagal("Error jaringan: " + t.getMessage());
            }
        });
    }

    @Override
    public void hapusPengaduan(int idPengaduan, DeletePengaduanCallback callback) {
        // Karena menghapus dari sisi operator sekarang butuh alasan, kita panggil versi lain dengan alasan default.
        // Atau, jika sisi masyarakat tidak boleh menghapus, kita bisa langsung callback gagal.
        // Untuk saat ini, kita anggap tidak diimplementasikan dari sisi ini.
        callback.onDeleteGagal("Fungsi hapus tidak tersedia untuk pengguna ini.");
    }


    // IMPLEMENTASI DARI OperatorRepository (Sisi Operator)
    @Override
    public void getPengaduanByStatus(String status, GetPengaduanCallback callback) {
        apiService.getPengaduanOperatorByStatus(status).enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<PengaduanPublikApiResponse> call, @NonNull Response<PengaduanPublikApiResponse> response) {
                if (response.isSuccessful() && response.body() != null && "success".equalsIgnoreCase(response.body().getStatus())) {
                    callback.onBerhasil(response.body().getData() != null ? response.body().getData() : new ArrayList<>());
                } else {
                    callback.onGagal("Gagal mengambil data laporan.");
                }
            }

            @Override
            public void onFailure(@NonNull Call<PengaduanPublikApiResponse> call, @NonNull Throwable t) {
                callback.onGagal("Error jaringan: " + t.getMessage());
            }
        });
    }

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
                    if (response.body() != null && response.body().getMessage() != null) {
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

    @Override
    public void hapusPengaduan(int idPengaduan, String alasan, DeletePengaduanCallback callback) {
        RequestBody idPengaduanBody = RequestBody.create(MediaType.parse("text/plain"), String.valueOf(idPengaduan));
        RequestBody alasanBody = RequestBody.create(MediaType.parse("text/plain"), alasan);

        apiService.hapusPengaduan(idPengaduanBody, alasanBody).enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<PengaduanResponse> call, @NonNull Response<PengaduanResponse> response) {
                if (response.isSuccessful() && response.body() != null && "success".equals(response.body().getStatus())) {
                    callback.onDeleteBerhasil(response.body().getMessage());
                } else {
                    callback.onDeleteGagal("Gagal menghapus di server.");
                }
            }

            @Override
            public void onFailure(@NonNull Call<PengaduanResponse> call, @NonNull Throwable t) {
                callback.onDeleteGagal("Error jaringan: " + t.getMessage());
            }
        });
    }

    @Override
    public void getRekapData(GetRekapCallback callback) {
        apiService.getRekapStatistik().enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<RekapApiResponse> call, @NonNull Response<RekapApiResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().data != null) {
                    callback.onBerhasil(response.body().data);
                } else {
                    callback.onGagal("Gagal mengambil data rekap.");
                }
            }

            @Override
            public void onFailure(@NonNull Call<RekapApiResponse> call, @NonNull Throwable t) {
                callback.onGagal("Error jaringan: " + t.getMessage());
            }
        });
    }

    @Override
    public void searchPengaduan(String keyword, String status, GetPengaduanCallback callback) {
        apiService.searchPengaduan(keyword, status).enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<PengaduanPublikApiResponse> call, @NonNull Response<PengaduanPublikApiResponse> response) {
                if (response.isSuccessful() && response.body() != null && "success".equals(response.body().getStatus())) {
                    callback.onBerhasil(response.body().getData() != null ? response.body().getData() : new ArrayList<>());
                } else {
                    callback.onGagal("Gagal mencari data.");
                }
            }

            @Override
            public void onFailure(@NonNull Call<PengaduanPublikApiResponse> call, @NonNull Throwable t) {
                callback.onGagal("Error Jaringan: " + t.getMessage());
            }
        });
    }
}