package com.alisteners.pengaduan.presentation.masyarakat.riwayatpengaduan;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.alisteners.pengaduan.R;
import com.alisteners.pengaduan.domain.entity.PengaduanRiwayatItem;
import com.alisteners.pengaduan.util.StringUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Adapter untuk {@link RecyclerView} yang menampilkan daftar riwayat pengaduan pengguna.
 * Kelas ini bertanggung jawab untuk mengambil data dari list {@link PengaduanRiwayatItem}
 * dan menampilkannya ke dalam layout item_riwayat_pengaduan.xml.
 */
public class RiwayatPengaduanAdapter extends RecyclerView.Adapter<RiwayatPengaduanAdapter.RiwayatViewHolder> {

    private final List<PengaduanRiwayatItem> riwayatItems;
    private final Context context;
    private OnItemClickListener listener;

    /**
     * Interface untuk menangani event klik pada item di RecyclerView.
     */
    public interface OnItemClickListener {
        /**
         * Dipanggil ketika sebuah item riwayat di-klik.
         * @param item Objek {@link PengaduanRiwayatItem} yang di-klik.
         */
        void onItemClick(PengaduanRiwayatItem item);
    }

    /**
     * Konstruktor untuk adapter.
     * @param riwayatItems List awal dari data riwayat.
     * @param context Context dari Fragment atau Activity.
     */
    public RiwayatPengaduanAdapter(List<PengaduanRiwayatItem> riwayatItems, Context context) {
        this.riwayatItems = riwayatItems;
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
     */
    @NonNull
    @Override
    public RiwayatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_riwayat_pengaduan, parent, false);
        return new RiwayatViewHolder(view);
    }

    /**
     * Dipanggil oleh RecyclerView untuk menampilkan data pada posisi tertentu.
     */
    @Override
    public void onBindViewHolder(@NonNull RiwayatViewHolder holder, int position) {
        PengaduanRiwayatItem item = riwayatItems.get(position);
        holder.bind(item, listener);
    }

    /**
     * Mengembalikan jumlah total item dalam dataset.
     */
    @Override
    public int getItemCount() {
        return riwayatItems != null ? riwayatItems.size() : 0;
    }

    /**
     * Memperbarui dataset adapter dengan daftar item yang baru dan memberitahu
     * RecyclerView untuk me-render ulang tampilannya.
     * @param newItems List baru dari {@link PengaduanRiwayatItem}.
     */
    @SuppressLint("NotifyDataSetChanged")
    public void updateData(List<PengaduanRiwayatItem> newItems) {
        this.riwayatItems.clear();
        if (newItems != null) {
            this.riwayatItems.addAll(newItems);
        }
        notifyDataSetChanged();
    }

    /**
     * ViewHolder yang merepresentasikan satu item riwayat dalam RecyclerView.
     */
    class RiwayatViewHolder extends RecyclerView.ViewHolder {
        TextView textViewTanggal, textViewStatus, textViewKategori, textViewLokasi;

        RiwayatViewHolder(View itemView) {
            super(itemView);
            textViewTanggal = itemView.findViewById(R.id.textViewRiwayatTanggal);
            textViewStatus = itemView.findViewById(R.id.textViewRiwayatStatus);
            textViewKategori = itemView.findViewById(R.id.textViewRiwayatKategori);
            textViewLokasi = itemView.findViewById(R.id.textViewRiwayatLokasi);
        }

        /**
         * Mengikat data dari objek item ke komponen UI di dalam ViewHolder.
         * @param item Objek {@link PengaduanRiwayatItem} yang berisi data.
         * @param listener Listener untuk menangani klik pada item.
         */
        void bind(final PengaduanRiwayatItem item, final OnItemClickListener listener) {
            textViewTanggal.setText(formatTanggal(item.tanggalPengaduan()));
            textViewKategori.setText(item.kategori());
            textViewLokasi.setText(item.lokasiSingkat());

            setupStatus(item.status());

            // Set listener untuk seluruh item view
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) { // Memastikan posisi valid
                        listener.onItemClick(riwayatItems.get(position));
                    }
                }
            });
        }

        /**
         * Mengatur teks dan warna pada TextView status berdasarkan status pengaduan.
         * @param status String status dari item pengaduan.
         */
        private void setupStatus(String status) {
            if (status != null) {
                textViewStatus.setText(StringUtils.capitalizeFirstLetter(status));

                int colorRes = switch (status.toLowerCase()) {
                    case "menunggu" -> R.color.status_menunggu;
                    case "diproses" -> R.color.status_diproses;
                    case "selesai" -> R.color.status_selesai;
                    default -> R.color.text_sekunder;
                };
                textViewStatus.setTextColor(ContextCompat.getColor(context, colorRes));
            } else {
                textViewStatus.setText("N/A");
            }
        }

        /**
         * Memformat string tanggal dari format "yyyy-MM-dd HH:mm:ss" menjadi "dd MMMM yyyy, HH:mm".
         * @param tanggalMySQL String tanggal dari server.
         * @return String tanggal yang sudah diformat.
         */
        private String formatTanggal(String tanggalMySQL) {
            if (tanggalMySQL == null) return "";
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            SimpleDateFormat outputFormat = new SimpleDateFormat("dd MMMM yyyy, HH:mm", new Locale("id", "ID"));

            try {
                Date date = inputFormat.parse(tanggalMySQL);
                return (date != null) ? outputFormat.format(date) : tanggalMySQL;
            } catch (ParseException e) {
                // Jika parsing gagal, kembalikan tanggal asli untuk debugging
                e.printStackTrace();
                return tanggalMySQL;
            }
        }
    }
}