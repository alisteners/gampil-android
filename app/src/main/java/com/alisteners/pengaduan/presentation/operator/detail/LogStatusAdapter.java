package com.alisteners.pengaduan.presentation.operator.detail;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.alisteners.pengaduan.R;
import com.alisteners.pengaduan.data.remote.config.ApiConfig;
import com.alisteners.pengaduan.domain.entity.LogStatusItem;
import com.alisteners.pengaduan.presentation.fullscreenimage.FullScreenImageActivity;
import com.alisteners.pengaduan.util.StringUtils;

import com.bumptech.glide.Glide;

import com.github.vipulasri.timelineview.TimelineView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Adapter untuk {@link RecyclerView} yang menampilkan riwayat tindak lanjut pengaduan
 * dalam format timeline (garis waktu) visual.
 * Kelas ini menggunakan library TimelineView untuk membuat garis dan penanda (marker).
 */
public class LogStatusAdapter extends RecyclerView.Adapter<LogStatusAdapter.LogViewHolder> {

    private final List<LogStatusItem> logList;
    private final Context context;

    /**
     * Konstruktor untuk adapter.
     * @param logList List awal dari data log/riwayat.
     * @param context Context dari Activity.
     */
    public LogStatusAdapter(List<LogStatusItem> logList, Context context) {
        this.logList = logList != null ? logList : new ArrayList<>();
        this.context = context;
    }

    /**
     * Menentukan tipe view untuk TimelineView (awal, tengah, atau akhir)
     * berdasarkan posisi item. Ini penting untuk menggambar garis timeline dengan benar.
     */
    @Override
    public int getItemViewType(int position) {
        return TimelineView.getTimeLineViewType(position, getItemCount());
    }

    @NonNull
    @Override
    public LogViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_log_status, parent, false);
        return new LogViewHolder(view, viewType);
    }

    @Override
    public void onBindViewHolder(@NonNull LogViewHolder holder, int position) {
        LogStatusItem logItem = logList.get(position);
        holder.bind(logItem);
    }

    @Override
    public int getItemCount() {
        return logList.size();
    }

    /**
     * Memperbarui dataset adapter dengan daftar log yang baru.
     * @param newLogList List baru dari {@link LogStatusItem}.
     */
    @SuppressLint("NotifyDataSetChanged")
    public void updateData(List<LogStatusItem> newLogList) {
        this.logList.clear();
        if (newLogList != null) {
            this.logList.addAll(newLogList);
        }
        notifyDataSetChanged();
    }

    /**
     * Mengembalikan list data log saat ini.
     * @return List dari {@link LogStatusItem}.
     */
    public List<LogStatusItem> getLogList() {
        return logList;
    }

    /**
     * ViewHolder yang merepresentasikan satu "kejadian" atau titik pada timeline.
     */
    class LogViewHolder extends RecyclerView.ViewHolder {
        TimelineView mTimelineView;
        TextView textViewStatusLog, textViewOperatorTimestamp, textViewCatatanLog;
        HorizontalScrollView horizontalScrollViewLogFoto;
        LinearLayout containerLogFoto;

        LogViewHolder(View itemView, int viewType) {
            super(itemView);
            mTimelineView = itemView.findViewById(R.id.timeline);
            mTimelineView.initLine(viewType); // Inisialisasi garis timeline sesuai tipenya

            textViewStatusLog = itemView.findViewById(R.id.textViewStatusLog);
            textViewOperatorTimestamp = itemView.findViewById(R.id.textViewOperatorTimestamp);
            textViewCatatanLog = itemView.findViewById(R.id.textViewCatatanLog);
            horizontalScrollViewLogFoto = itemView.findViewById(R.id.horizontalScrollViewLogFoto);
            containerLogFoto = itemView.findViewById(R.id.containerLogFoto);
        }

        /**
         * Mengikat data dari objek {@link LogStatusItem} ke komponen UI di dalam ViewHolder.
         * @param logItem Objek yang berisi data untuk satu log.
         */
        @SuppressLint("SetTextI18n")
        void bind(final LogStatusItem logItem) {
            textViewStatusLog.setText(StringUtils.capitalizeFirstLetter(logItem.status));
            String operatorInfo = "Diperbarui oleh " + (logItem.namaOperator != null ? logItem.namaOperator : "Sistem");
            textViewOperatorTimestamp.setText(operatorInfo + " pada " + formatTanggal(logItem.timestamp));

            // Atur ikon marker pada timeline berdasarkan status
            if (logItem.status.equalsIgnoreCase("selesai")) {
                mTimelineView.setMarker(ContextCompat.getDrawable(context, R.drawable.ic_status_selesai_circle));
            } else if (logItem.status.equalsIgnoreCase("diproses")) {
                mTimelineView.setMarker(ContextCompat.getDrawable(context, R.drawable.ic_status_diproses_circle));
            } else { // Menunggu
                mTimelineView.setMarker(ContextCompat.getDrawable(context, R.drawable.ic_status_menunggu_circle));
            }

            // Tampilkan atau sembunyikan catatan
            if (logItem.catatan != null && !logItem.catatan.trim().isEmpty()) {
                textViewCatatanLog.setVisibility(View.VISIBLE);
                textViewCatatanLog.setText(logItem.catatan);
            } else {
                textViewCatatanLog.setVisibility(View.GONE);
            }

            // Tampilkan atau sembunyikan galeri foto bukti
            if (logItem.fotoBukti != null && !logItem.fotoBukti.isEmpty()) {
                horizontalScrollViewLogFoto.setVisibility(View.VISIBLE);
                tampilkanFoto(logItem.fotoBukti);
            } else {
                horizontalScrollViewLogFoto.setVisibility(View.GONE);
            }
        }

        /**
         * Secara dinamis membuat dan menampilkan pratinjau gambar bukti di dalam
         * HorizontalScrollView.
         * @param listPathFoto Daftar path/URL relatif dari gambar-gambar.
         */
        private void tampilkanFoto(List<String> listPathFoto) {
            containerLogFoto.removeAllViews(); // Selalu bersihkan view lama sebelum menambahkan yang baru
            for (String pathFoto : listPathFoto) {
                ImageView imageView = new ImageView(context);
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(dpToPx(80), dpToPx(80));
                params.setMarginEnd(dpToPx(8));
                imageView.setLayoutParams(params);
                imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);

                final String imageUrl = ApiConfig.BASE_URL + pathFoto;
                Glide.with(context).load(imageUrl).centerCrop().into(imageView);

                imageView.setOnClickListener(v -> {
                    Intent intent = new Intent(context, FullScreenImageActivity.class);
                    intent.putExtra(FullScreenImageActivity.EXTRA_IMAGE_URL, imageUrl);
                    context.startActivity(intent);
                });
                containerLogFoto.addView(imageView);
            }
        }

        /**
         * Memformat string tanggal dari format server ke format yang lebih mudah dibaca.
         * @param tanggalMySQL String tanggal dari server.
         * @return String tanggal yang sudah diformat.
         */
        private String formatTanggal(String tanggalMySQL) {
            if (tanggalMySQL == null || tanggalMySQL.isEmpty()) return "N/A";
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            SimpleDateFormat outputFormat = new SimpleDateFormat("dd MMM yy, HH:mm", new Locale("id", "ID"));
            try {
                Date date = inputFormat.parse(tanggalMySQL);
                return date != null ? outputFormat.format(date) : "N/A";
            } catch (ParseException e) {
                return tanggalMySQL;
            }
        }

        /**
         * Mengonversi nilai Dimen (dp) ke Piksel (px).
         * @param dp Nilai dalam dp.
         * @return Nilai ekuivalen dalam px.
         */
        private int dpToPx(int dp) {
            return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, context.getResources().getDisplayMetrics());
        }
    }
}