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

public class RiwayatPengaduanAdapter extends RecyclerView.Adapter<RiwayatPengaduanAdapter.RiwayatViewHolder> {

    private final List<PengaduanRiwayatItem> riwayatItems;
    private final Context context;
    private OnItemClickListener listener;

    // Interface untuk menangani klik pada item RecyclerView
    public interface OnItemClickListener {
        void onItemClick(PengaduanRiwayatItem item);
    }

    public RiwayatPengaduanAdapter(List<PengaduanRiwayatItem> riwayatItems, Context context) {
        this.riwayatItems = riwayatItems;
        this.context = context;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public RiwayatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_riwayat_pengaduan, parent, false);
        return new RiwayatViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RiwayatViewHolder holder, int position) {
        PengaduanRiwayatItem item = riwayatItems.get(position);
        holder.bind(item, listener);
    }

    @Override
    public int getItemCount() {
        return riwayatItems != null ? riwayatItems.size() : 0;
    }

    // Metode untuk memperbarui data di adapter setelah data baru diambil dari server
    @SuppressLint("NotifyDataSetChanged")
    public void updateData(List<PengaduanRiwayatItem> newItems) {
        this.riwayatItems.clear();
        if (newItems != null) {
            this.riwayatItems.addAll(newItems);
        }
        notifyDataSetChanged(); // Memberi tahu RecyclerView untuk me-refresh tampilannya
    }

    class RiwayatViewHolder extends RecyclerView.ViewHolder {
        TextView textViewTanggal, textViewStatus, textViewKategori, textViewLokasi;

        RiwayatViewHolder(View itemView) {
            super(itemView);
            textViewTanggal = itemView.findViewById(R.id.textViewRiwayatTanggal);
            textViewStatus = itemView.findViewById(R.id.textViewRiwayatStatus);
            textViewKategori = itemView.findViewById(R.id.textViewRiwayatKategori);
            textViewLokasi = itemView.findViewById(R.id.textViewRiwayatLokasi);
        }

        void bind(final PengaduanRiwayatItem item, final OnItemClickListener listener) {
            // Format tanggal dari "YYYY-MM-DD HH:MM:SS" (default MySQL) ke format yang lebih ramah
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            SimpleDateFormat outputFormat = new SimpleDateFormat("dd MMMM yyyy, HH:mm", new Locale("id", "ID"));

            try {
                Date date = inputFormat.parse(item.tanggalPengaduan());
                if (date != null) {
                    textViewTanggal.setText(outputFormat.format(date));
                }
            } catch (ParseException e) {
                textViewTanggal.setText(item.tanggalPengaduan()); // Tampilkan apa adanya jika parsing gagal
                e.printStackTrace();
            }

            textViewKategori.setText(item.kategori());
            textViewLokasi.setText(item.lokasiSingkat());

            String statusDariDb = item.status();
            if (statusDariDb != null) {
                // Panggil helper untuk memformat teks
                textViewStatus.setText(StringUtils.capitalizeFirstLetter(statusDariDb));

                // 1. Ambil status dan ubah ke huruf kecil untuk perbandingan yang aman
                String statusLowerCase = statusDariDb.toLowerCase();

                // 2. Buat string untuk ditampilkan dengan huruf awal kapital
                String statusTampil = statusLowerCase.substring(0, 1).toUpperCase() + statusLowerCase.substring(1);
                textViewStatus.setText(statusTampil);

                // 3. Atur warna berdasarkan perbandingan huruf kecil yang konsisten
                int colorRes = switch (statusDariDb.toLowerCase()) {
                    case "menunggu" -> R.color.status_menunggu;
                    case "diproses" -> R.color.status_diproses;
                    case "selesai" -> R.color.status_selesai;
                    default -> R.color.text_sekunder;
                };
                textViewStatus.setTextColor(ContextCompat.getColor(context, colorRes));
            } else {
                textViewStatus.setText("N/A");
            }

            // Set listener untuk seluruh item view
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) { // Pastikan posisi valid
                        listener.onItemClick(riwayatItems.get(position));
                    }
                }
            });
        }
    }
}