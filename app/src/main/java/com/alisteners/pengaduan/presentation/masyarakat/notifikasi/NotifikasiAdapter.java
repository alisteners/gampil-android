package com.alisteners.pengaduan.presentation.masyarakat.notifikasi;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.alisteners.pengaduan.R;
import com.alisteners.pengaduan.domain.entity.Notifikasi;

import com.google.android.material.card.MaterialCardView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Adapter untuk {@link RecyclerView} yang menampilkan daftar notifikasi pengguna.
 * Kelas ini bertanggung jawab untuk mengambil data dari list {@link Notifikasi}
 * dan menampilkannya ke dalam layout item_notifikasi.xml.
 */
public class NotifikasiAdapter extends RecyclerView.Adapter<NotifikasiAdapter.ViewHolder> {

    private final List<Notifikasi> notifikasiList;
    private final Context context;
    private OnItemClickListener listener;

    /**
     * Interface untuk menangani event klik pada item di RecyclerView.
     */
    public interface OnItemClickListener {
        /**
         * Dipanggil ketika sebuah item notifikasi di-klik.
         * @param notifikasi Objek {@link Notifikasi} yang di-klik.
         */
        void onItemClick(Notifikasi notifikasi);
    }

    /**
     * Mendaftarkan listener untuk event klik item.
     * @param listener Implementasi dari {@link OnItemClickListener}.
     */
    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    /**
     * Konstruktor untuk adapter.
     * @param context Context dari Fragment atau Activity.
     * @param notifikasiList List awal dari data notifikasi.
     */
    public NotifikasiAdapter(Context context, List<Notifikasi> notifikasiList) {
        this.context = context;
        this.notifikasiList = notifikasiList;
    }

    /**
     * Dipanggil oleh RecyclerView untuk membuat ViewHolder baru.
     */
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_notifikasi, parent, false);
        return new ViewHolder(view);
    }

    /**
     * Dipanggil oleh RecyclerView untuk menampilkan data pada posisi tertentu.
     */
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Notifikasi notifikasi = notifikasiList.get(position);
        holder.bind(notifikasi);
    }

    /**
     * Mengembalikan jumlah total item dalam dataset.
     */
    @Override
    public int getItemCount() {
        return notifikasiList.size();
    }

    /**
     * ViewHolder yang merepresentasikan satu item notifikasi dalam RecyclerView.
     * Kelas ini menyimpan referensi ke view-view di dalam layout item dan
     * berisi logika untuk mengikat data ke view tersebut.
     */
    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView textViewJudulLaporan, textViewStatusNotifikasi, textViewTanggalNotifikasi;
        MaterialCardView cardView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = (MaterialCardView) itemView;
            textViewJudulLaporan = itemView.findViewById(R.id.textViewJudulLaporan);
            textViewStatusNotifikasi = itemView.findViewById(R.id.textViewStatusNotifikasi);
            textViewTanggalNotifikasi = itemView.findViewById(R.id.textViewTanggalNotifikasi);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (listener != null && position != RecyclerView.NO_POSITION) {
                    listener.onItemClick(notifikasiList.get(position));
                }
            });
        }

        /**
         * Mengikat data dari objek notifikasi ke komponen UI di dalam ViewHolder.
         * Juga mengatur style tampilan (tebal/normal) berdasarkan status 'sudahDilihat'.
         * @param notifikasi Objek {@link Notifikasi} yang berisi data.
         */
        @SuppressLint("SetTextI18n")
        void bind(final Notifikasi notifikasi) {
            textViewJudulLaporan.setText("Pembaruan Status Pengaduan"); // Judul generik
            textViewStatusNotifikasi.setText(notifikasi.getPesan());

            String tanggal = notifikasi.getTimestamp();
            if (tanggal != null && !tanggal.isEmpty()) {
                textViewTanggalNotifikasi.setText(formatTanggal(tanggal));
            } else {
                textViewTanggalNotifikasi.setText("Tanggal tidak tersedia");
            }

            // Mengatur tampilan (style teks dan warna latar) berdasarkan status 'sudahDilihat'.
            if (notifikasi.isSudahDilihat()) {
                textViewJudulLaporan.setTypeface(null, Typeface.NORMAL);
                textViewStatusNotifikasi.setTypeface(null, Typeface.NORMAL);
                cardView.setCardBackgroundColor(ContextCompat.getColor(context, android.R.color.white));
            } else {
                textViewJudulLaporan.setTypeface(null, Typeface.BOLD);
                textViewStatusNotifikasi.setTypeface(null, Typeface.BOLD);
                cardView.setCardBackgroundColor(ContextCompat.getColor(context, R.color.app_aksen));
            }
        }
    }

    /**
     * Memformat string tanggal dari format "yyyy-MM-dd HH:mm:ss" menjadi "dd MMM yyyy, HH:mm".
     * @param tanggalMySQL String tanggal dari server.
     * @return String tanggal yang sudah diformat.
     */
    private String formatTanggal(String tanggalMySQL) {
        if (tanggalMySQL == null) return "";
        SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        SimpleDateFormat outputFormat = new SimpleDateFormat("dd MMM yyyy, HH:mm", new Locale("id", "ID"));
        try {
            Date date = inputFormat.parse(tanggalMySQL);
            return date != null ? outputFormat.format(date) : "";
        } catch (ParseException e) {
            return tanggalMySQL;
        }
    }
}