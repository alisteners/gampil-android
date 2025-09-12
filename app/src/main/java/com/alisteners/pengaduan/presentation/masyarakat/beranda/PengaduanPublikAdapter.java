package com.alisteners.pengaduan.presentation.masyarakat.beranda;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.alisteners.pengaduan.R;
import com.alisteners.pengaduan.data.remote.config.ApiConfig;
import com.alisteners.pengaduan.domain.entity.PengaduanPublikItem;
import com.alisteners.pengaduan.util.StringUtils;
import com.bumptech.glide.Glide;
import com.google.android.material.chip.Chip;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Adapter untuk {@link RecyclerView} yang menampilkan daftar pengaduan publik.
 * Kelas ini bertanggung jawab untuk mengambil data dari list {@link PengaduanPublikItem}
 * dan menampilkannya ke dalam layout item_pengaduan_publik.xml.
 */
public class PengaduanPublikAdapter extends RecyclerView.Adapter<PengaduanPublikAdapter.PengaduanPublikViewHolder> {

    private final List<PengaduanPublikItem> pengaduanItems;
    private final Context context;
    private OnItemClickListener listener;

    /**
     * Interface untuk menangani event klik pada item di RecyclerView.
     */
    public interface OnItemClickListener {
        /**
         * Dipanggil ketika sebuah item di-klik.
         * @param item Objek {@link PengaduanPublikItem} yang di-klik.
         */
        void onItemClick(PengaduanPublikItem item);
    }

    /**
     * Konstruktor untuk adapter.
     * @param pengaduanItems List awal dari data pengaduan.
     * @param context Context dari Fragment atau Activity.
     */
    public PengaduanPublikAdapter(List<PengaduanPublikItem> pengaduanItems, Context context) {
        this.pengaduanItems = pengaduanItems;
        this.context = context;
    }

    /**
     * Mendaftarkan listener untuk event klik item.
     * @param listener Implementasi dari {@link OnItemClickListener}.
     */
    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    /**
     * Dipanggil oleh RecyclerView untuk membuat ViewHolder baru.
     * Metode ini meng-inflate layout item dan menginisialisasi ViewHolder.
     */
    @NonNull
    @Override
    public PengaduanPublikViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_pengaduan_publik, parent, false);
        return new PengaduanPublikViewHolder(view);
    }

    /**
     * Dipanggil oleh RecyclerView untuk menampilkan data pada posisi tertentu.
     * Metode ini mengikat (bind) data dari objek {@link PengaduanPublikItem} ke view
     * di dalam ViewHolder.
     */
    @Override
    public void onBindViewHolder(@NonNull PengaduanPublikViewHolder holder, int position) {
        PengaduanPublikItem item = pengaduanItems.get(position);
        holder.bind(item, listener);
    }

    /**
     * Mengembalikan jumlah total item dalam dataset.
     */
    @Override
    public int getItemCount() {
        return pengaduanItems != null ? pengaduanItems.size() : 0;
    }

    /**
     * Memperbarui dataset adapter dengan daftar item yang baru dan memberitahu
     * RecyclerView untuk me-render ulang tampilannya.
     * @param newItems List baru dari {@link PengaduanPublikItem}.
     */
    @SuppressLint("NotifyDataSetChanged")
    public void updateData(List<PengaduanPublikItem> newItems) {
        pengaduanItems.clear();
        if (newItems != null) {
            pengaduanItems.addAll(newItems);
        }
        // notifyDataSetChanged digunakan untuk kesederhanaan. Untuk performa yang lebih baik
        // pada daftar yang sangat besar, pertimbangkan menggunakan DiffUtil.
        notifyDataSetChanged();
    }

    /**
     * ViewHolder yang merepresentasikan satu item dalam RecyclerView.
     * Kelas ini menyimpan referensi ke view-view di dalam layout item dan
     * berisi logika untuk mengikat data ke view tersebut.
     */
    class PengaduanPublikViewHolder extends RecyclerView.ViewHolder {
        ImageView imageViewThumbnail;
        TextView textViewKategori, textViewTanggal, textViewNamaPelapor, textViewDeskripsi;
        Chip chipStatus;

        public PengaduanPublikViewHolder(@NonNull View itemView) {
            super(itemView);
            imageViewThumbnail = itemView.findViewById(R.id.imageViewPublikThumbnail);
            textViewKategori = itemView.findViewById(R.id.textViewPublikKategori);
            textViewTanggal = itemView.findViewById(R.id.textViewPublikTanggal);
            textViewNamaPelapor = itemView.findViewById(R.id.textViewPublikNamaPelapor);
            textViewDeskripsi = itemView.findViewById(R.id.textViewPublikDeskripsi);
            chipStatus = itemView.findViewById(R.id.chipPublikStatus);
        }

        /**
         * Mengikat data dari objek item ke komponen UI di dalam ViewHolder.
         * @param item Objek {@link PengaduanPublikItem} yang berisi data.
         * @param listener Listener untuk menangani klik pada item.
         */
        void bind(final PengaduanPublikItem item, final OnItemClickListener listener) {

            textViewKategori.setText(item.getKategori());
            textViewTanggal.setText(formatTanggal(item.getTanggalPengaduan()));
            textViewNamaPelapor.setText(item.getNamaPelapor());
            textViewDeskripsi.setText(item.getDeskripsiSingkat());

            setupChipStatus(item.getStatus());

            String path = item.getPathImage();

            if (path != null && !path.isEmpty() && !path.equalsIgnoreCase("null")) {
                String imageUrl = ApiConfig.BASE_URL + path;
                Log.d("PengaduanPublikAdapter", "Memuat gambar dari URL: " + imageUrl);
                Glide.with(context)
                        .load(imageUrl)
                        .placeholder(R.drawable.ic_image_placeholder)
                        .error(R.drawable.ic_image_error)
                        .into(imageViewThumbnail);
            } else {
                Log.w("PengaduanPublikAdapter", "Path gambar null atau kosong untuk item: " + item.getKategori());
                imageViewThumbnail.setImageResource(R.drawable.ic_image_placeholder);
            }

            itemView.setOnClickListener(v -> {
                if (listener != null && getAdapterPosition() != RecyclerView.NO_POSITION) {
                    listener.onItemClick(pengaduanItems.get(getAdapterPosition()));
                }
            });
        }

        /**
         * Memformat string tanggal dari format "yyyy-MM-dd HH:mm:ss" menjadi "dd MMMM yyyy".
         * @param tanggalMySQL String tanggal dari server.
         * @return String tanggal yang sudah diformat.
         */
        private String formatTanggal(String tanggalMySQL) {
            if (tanggalMySQL == null) return "";
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            SimpleDateFormat outputFormat = new SimpleDateFormat("dd MMMM yyyy", new Locale("id", "ID"));
            try {
                Date date = inputFormat.parse(tanggalMySQL);
                return (date != null) ? outputFormat.format(date) : tanggalMySQL;
            } catch (ParseException e) {
                return tanggalMySQL;
            }
        }

        /**
         * Mengatur tampilan Chip (teks, warna, ikon) berdasarkan status pengaduan.
         * @param status String status dari item pengaduan.
         */
        private void setupChipStatus(String status) {
            if (status == null) {
                chipStatus.setVisibility(View.GONE);
                return;
            }
            chipStatus.setVisibility(View.VISIBLE);
            chipStatus.setText(StringUtils.capitalizeFirstLetter(status));

            int backgroundColorRes;
            int iconRes = switch (status.toLowerCase()) {
                case "diproses" -> {
                    backgroundColorRes = R.color.status_diproses_bg;
                    yield R.drawable.ic_status_diproses;
                }
                case "selesai" -> {
                    backgroundColorRes = R.color.status_selesai_bg;
                    yield R.drawable.ic_status_selesai;
                }
                default -> {
                    backgroundColorRes = R.color.status_menunggu_bg;
                    yield R.drawable.ic_status_menunggu;
                }
            };

            chipStatus.setChipBackgroundColorResource(backgroundColorRes);
            if (iconRes != 0) {
                chipStatus.setChipIconResource(iconRes);
            }
        }
    }
}