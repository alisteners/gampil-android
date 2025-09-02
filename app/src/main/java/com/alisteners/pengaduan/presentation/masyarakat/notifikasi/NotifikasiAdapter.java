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

public class NotifikasiAdapter extends RecyclerView.Adapter<NotifikasiAdapter.ViewHolder> {

    private final List<Notifikasi> notifikasiList;
    private final Context context;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(Notifikasi notifikasi);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public NotifikasiAdapter(Context context, List<Notifikasi> notifikasiList) {
        this.context = context;
        this.notifikasiList = notifikasiList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_notifikasi, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Notifikasi notifikasi = notifikasiList.get(position);
        holder.bind(notifikasi);
    }

    @Override
    public int getItemCount() {
        return notifikasiList.size();
    }

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

        @SuppressLint("SetTextI18n")
        void bind(final Notifikasi notifikasi) {
            // 1. Gunakan getter yang BENAR: getPesan()
            textViewJudulLaporan.setText("Pembaruan Status Pengaduan"); // Judul generik
            textViewStatusNotifikasi.setText(notifikasi.getPesan());

            // 2. Gunakan getter yang BENAR: getTimestamp() dan periksa null
            String tanggal = notifikasi.getTimestamp();
            if (tanggal != null && !tanggal.isEmpty()) {
                textViewTanggalNotifikasi.setText(formatTanggal(tanggal));
            } else {
                textViewTanggalNotifikasi.setText("Tanggal tidak tersedia");
            }

            // 3. Atur tampilan berdasarkan sudah dilihat atau belum (SUDAH DIPERBAIKI)
            if (notifikasi.isSudahDilihat()) {
                textViewJudulLaporan.setTypeface(null, Typeface.NORMAL);
                textViewStatusNotifikasi.setTypeface(null, Typeface.NORMAL);
                cardView.setCardBackgroundColor(ContextCompat.getColor(context, android.R.color.white)); // Gunakan transparan atau warna putih
            } else {
                textViewJudulLaporan.setTypeface(null, Typeface.BOLD);
                textViewStatusNotifikasi.setTypeface(null, Typeface.BOLD);
                cardView.setCardBackgroundColor(ContextCompat.getColor(context, R.color.app_aksen));
            }
        }
    }

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