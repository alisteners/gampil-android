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

/**
 * Implementasi konkret dari {@link PengaduanRepository} dan {@link OperatorRepository}.
 * Kelas ini berfungsi sebagai satu-satunya sumber kebenaran (Single Source of Truth) untuk data pengaduan.
 * Bertanggung jawab untuk mengambil data dari server melalui {@link ApiService} dan menyampaikannya
 * kembali ke lapisan domain (UseCases) melalui callback.
 */
public class PengaduanRepositoryImpl implements PengaduanRepository, OperatorRepository {

    private static final String TAG = "PengaduanRepoImpl";
    private final ApiService apiService;

    /**
     * Konstruktor untuk PengaduanRepositoryImpl.
     * @param context Context aplikasi, digunakan untuk inisialisasi ApiClient.
     */
    public PengaduanRepositoryImpl(Context context) {
        this.apiService = ApiClient.getApiService();
    }

    //region Implementasi PengaduanRepository (Sisi Masyarakat)

    /**
     * Mengirim semua data pengaduan yang telah diproses, termasuk file, ke server.
     * Metode ini mengemas semua parameter teks dan file (data utama dan thumbnail)
     * ke dalam format multipart/form-data untuk dikirim melalui Retrofit.
     *
     * @param dataPengaduan Objek {@link PengaduanData} yang berisi semua informasi dan metrik pengaduan.
     * @param fileData File data utama yang akan diunggah (bisa .rle atau .jpg). Boleh null.
     * @param fileThumbnail File thumbnail gambar (selalu .jpg). Tidak boleh null.
     * @param callback Callback untuk memberitahukan status pengiriman (berhasil/gagal).
     */
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
        // --- TAMBAHAN VALIDASI NASKAH (REVISI SHP) ---
        params.put("ukuran_naskah_sebelum", createPartFromString(String.valueOf(dataPengaduan.ukuranNaskahSebelum)));
        params.put("ukuran_naskah_sesudah", createPartFromString(String.valueOf(dataPengaduan.ukuranNaskahSesudah)));
        // --- AKHIR TAMBAHAN ---

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

        // 4. Melakukan panggilan API secara asynchronous
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

    /**
     * Mengambil daftar riwayat pengaduan untuk seorang pengguna dari server.
     * @param idUser ID pengguna yang riwayatnya akan diambil.
     * @param callback Callback untuk menangani hasil pengambilan data.
     */
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

    /**
     * Mengambil detail lengkap dari satu pengaduan spesifik.
     * @param idPengaduan ID pengaduan yang akan diambil detailnya.
     * @param callback Callback untuk menangani hasil pengambilan data.
     */
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

    /**
     * Mengambil daftar pengaduan publik dari server.
     * @param status Status pengaduan untuk filtering (misal: "Selesai").
     * @param callback Callback untuk menangani hasil pengambilan data.
     */
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

    /**
     * Placeholder untuk fungsi hapus dari sisi masyarakat (tidak diimplementasikan).
     * @param idPengaduan ID pengaduan.
     * @param callback Callback.
     */
    @Override
    public void hapusPengaduan(int idPengaduan, DeletePengaduanCallback callback) {
        callback.onDeleteGagal("Fungsi hapus tidak tersedia untuk pengguna ini.");
    }

    //endregion

    //region Implementasi OperatorRepository (Sisi Operator)

    /**
     * Mengambil daftar pengaduan untuk operator, difilter berdasarkan status.
     * @param status Status pengaduan yang akan diambil.
     * @param callback Callback untuk menangani hasil pengambilan data.
     */
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

    /**
     * Memperbarui status, catatan, dan foto tindak lanjut dari sebuah pengaduan oleh operator.
     * @param idPengaduan ID pengaduan yang akan diperbarui.
     * @param idOperator ID operator yang melakukan aksi.
     * @param status Status baru pengaduan.
     * @param catatan Catatan dari operator.
     * @param files Daftar file foto bukti tindak lanjut.
     * @param requestToken Token untuk validasi.
     * @param callback Callback untuk menangani hasil pembaruan.
     */
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

    /**
     * Menghapus sebuah pengaduan dari server (aksi operator).
     * @param idPengaduan ID pengaduan yang akan dihapus.
     * @param alasan Alasan penghapusan.
     * @param callback Callback untuk menangani hasil penghapusan.
     */
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

    /**
     * Mengambil data rekapitulasi statistik dari server.
     * @param callback Callback untuk menangani hasil pengambilan data.
     */
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

    /**
     * Melakukan pencarian pengaduan di server.
     * @param keyword Kata kunci untuk pencarian.
     * @param status Status untuk memfilter pencarian.
     * @param callback Callback untuk menangani hasil pencarian.
     */
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

    /**
     * Metode helper untuk membuat {@link RequestBody} dari sebuah String.
     * Digunakan untuk mengemas parameter teks dalam request multipart.
     * @param value String yang akan dikonversi.
     * @return Objek RequestBody.
     */
    private RequestBody createPartFromString(String value) {
        return RequestBody.create(MediaType.parse("text/plain"), value);
    }
}