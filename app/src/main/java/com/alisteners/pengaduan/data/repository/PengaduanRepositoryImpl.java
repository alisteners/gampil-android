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

    //region Implementasi PengaduanRepository (Sisi Masyarakat)

    @Override
    public void kirimPengaduanKeServer(PengaduanData dataPengaduan, @Nullable File fileData, @Nullable File fileThumbnail, KirimPengaduanCallback callback) {
        Log.d(TAG, "Mempersiapkan pengiriman pengaduan ke server...");

        // 1. Membuat RequestBody untuk semua parameter teks
        Map<String, RequestBody> params = new HashMap<>();
        params.put("idUser", createPartFromString(String.valueOf(dataPengaduan.idUser)));
        params.put("lokasi", createPartFromString(dataPengaduan.lokasi));
        params.put("kategori", createPartFromString(dataPengaduan.kategori));
        params.put("deskripsi", createPartFromString(dataPengaduan.deskripsi));
        params.put("pernyataan", createPartFromString(dataPengaduan.pernyataan ? "1" : "0"));
        params.put("latitude", createPartFromString(String.valueOf(dataPengaduan.latitude)));
        params.put("longitude", createPartFromString(String.valueOf(dataPengaduan.longitude)));
        params.put("lebar", createPartFromString(String.valueOf(dataPengaduan.lebar)));
        params.put("tinggi", createPartFromString(String.valueOf(dataPengaduan.tinggi)));
        params.put("ukuranAsli", createPartFromString(String.valueOf(dataPengaduan.ukuranBitmapAsli)));
        params.put("ukuranSetelahResize", createPartFromString(String.valueOf(dataPengaduan.ukuranBitmapSetelahResize)));
        params.put("ukuranGrayscale", createPartFromString(String.valueOf(dataPengaduan.ukuranGrayscale)));
        params.put("ukuranFileRLE", createPartFromString(String.valueOf(dataPengaduan.ukuranFileRLEByte)));
        params.put("rasioKompresiRLE", createPartFromString(String.format(Locale.US, "%.2f", dataPengaduan.rasioKompresiRLE)));
        params.put("rasioKompresiStandar", createPartFromString(String.format(Locale.US, "%.2f", dataPengaduan.rasioKompresiStandar)));
        params.put("waktuProsesGambarMs", createPartFromString(String.valueOf(dataPengaduan.waktuProsesGambarMs)));
        params.put("isRleCompressed", createPartFromString(dataPengaduan.isRleCompressed ? "1" : "0"));

        // 2. Membuat MultipartBody.Part untuk file data utama (bisa .rle atau .jpg)
        MultipartBody.Part fileDataPart = null;
        if (fileData != null && fileData.exists()) {
            String mimeType = dataPengaduan.isRleCompressed ? "application/octet-stream" : "image/jpeg";
            RequestBody requestFileData = RequestBody.create(MediaType.parse(mimeType), fileData);
            fileDataPart = MultipartBody.Part.createFormData("fileRLE", fileData.getName(), requestFileData);
        }

        // 3. Membuat MultipartBody.Part untuk file thumbnail (selalu .jpg)
        MultipartBody.Part fileThumbnailPart;
        if (fileThumbnail != null && fileThumbnail.exists()) {
            RequestBody requestFileThumbnail = RequestBody.create(MediaType.parse("image/jpeg"), fileThumbnail);
            fileThumbnailPart = MultipartBody.Part.createFormData("fileThumbnail", fileThumbnail.getName(), requestFileThumbnail);
        } else {
            callback.onGagal("File thumbnail tidak valid atau tidak ditemukan.");
            return;
        }

        // 4. Melakukan panggilan API
        apiService.kirimPengaduanDenganFile(params, fileDataPart, fileThumbnailPart).enqueue(new Callback<>() {
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
        apiService.getRiwayatPengaduan(idUser).enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<RiwayatApiResponse> call, @NonNull Response<RiwayatApiResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    if ("success".equalsIgnoreCase(response.body().getStatus())) {
                        callback.onBerhasil(response.body().getData() != null ? response.body().getData() : new ArrayList<>());
                    } else {
                        callback.onGagal(response.body().getMessage());
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
        apiService.getDetailPengaduan(idPengaduan).enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<DetailApiResponse> call, @NonNull Response<DetailApiResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    if ("success".equalsIgnoreCase(response.body().getStatus()) && response.body().getData() != null) {
                        callback.onBerhasil(response.body().getData());
                    } else {
                        callback.onGagal(response.body().getMessage());
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
        apiService.getPengaduanPublik(status).enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<PengaduanPublikApiResponse> call, @NonNull Response<PengaduanPublikApiResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    if ("success".equalsIgnoreCase(response.body().getStatus()) && response.body().getData() != null) {
                        callback.onBerhasil(response.body().getData());
                    } else {
                        callback.onGagal(response.body().getMessage());
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
        callback.onDeleteGagal("Fungsi hapus tidak tersedia untuk pengguna ini.");
    }

    //endregion

    //region Implementasi OperatorRepository (Sisi Operator)

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
        params.put("idPengaduan", createPartFromString(String.valueOf(idPengaduan)));
        params.put("idOperator", createPartFromString(String.valueOf(idOperator)));
        params.put("status", createPartFromString(status));
        params.put("catatan", createPartFromString(catatan));
        params.put("request_token", createPartFromString(requestToken));

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
        RequestBody idPengaduanBody = createPartFromString(String.valueOf(idPengaduan));
        RequestBody alasanBody = createPartFromString(alasan);

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

    //endregion

    // Metode helper untuk membuat RequestBody dari String
    private RequestBody createPartFromString(String value) {
        return RequestBody.create(MediaType.parse("text/plain"), value);
    }
}