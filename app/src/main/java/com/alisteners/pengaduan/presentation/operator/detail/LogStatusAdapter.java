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

public class LogStatusAdapter extends RecyclerView.Adapter<LogStatusAdapter.LogViewHolder> {

    private final List<LogStatusItem> logList;
    private final Context context;

    public LogStatusAdapter(List<LogStatusItem> logList, Context context) {
        this.logList = logList != null ? logList : new ArrayList<>();
        this.context = context;
    }

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

    @SuppressLint("NotifyDataSetChanged")
    public void updateData(List<LogStatusItem> newLogList) {
        this.logList.clear();
        if (newLogList != null) {
            this.logList.addAll(newLogList);
        }
        notifyDataSetChanged();
    }

    public List<LogStatusItem> getLogList() {
        return logList;
    }

    class LogViewHolder extends RecyclerView.ViewHolder {
        TimelineView mTimelineView;
        TextView textViewStatusLog, textViewOperatorTimestamp, textViewCatatanLog;
        HorizontalScrollView horizontalScrollViewLogFoto;
        LinearLayout containerLogFoto;

        LogViewHolder(View itemView, int viewType) {
            super(itemView);
            mTimelineView = itemView.findViewById(R.id.timeline);
            mTimelineView.initLine(viewType);

            textViewStatusLog = itemView.findViewById(R.id.textViewStatusLog);
            textViewOperatorTimestamp = itemView.findViewById(R.id.textViewOperatorTimestamp);
            textViewCatatanLog = itemView.findViewById(R.id.textViewCatatanLog);
            horizontalScrollViewLogFoto = itemView.findViewById(R.id.horizontalScrollViewLogFoto);
            containerLogFoto = itemView.findViewById(R.id.containerLogFoto);
        }

        @SuppressLint("SetTextI18n")
        void bind(final LogStatusItem logItem) {
            // 1. Selalu atur Status dan Info Waktu
            textViewStatusLog.setText(StringUtils.capitalizeFirstLetter(logItem.status));
            String operatorInfo = "Diperbarui oleh " + (logItem.namaOperator != null ? logItem.namaOperator : "Sistem");
            textViewOperatorTimestamp.setText(operatorInfo + " pada " + formatTanggal(logItem.timestamp));

            // 2. Atur ikon marker berdasarkan status
            if (logItem.status.equalsIgnoreCase("selesai")) {
                mTimelineView.setMarker(ContextCompat.getDrawable(context, R.drawable.ic_status_selesai_circle));
            } else if (logItem.status.equalsIgnoreCase("diproses")) {
                mTimelineView.setMarker(ContextCompat.getDrawable(context, R.drawable.ic_status_diproses_circle));
            } else { // Menunggu
                mTimelineView.setMarker(ContextCompat.getDrawable(context, R.drawable.ic_status_menunggu_circle));
            }

            // 3. Atur Catatan dengan blok if-else yang aman
            if (logItem.catatan != null && !logItem.catatan.trim().isEmpty()) {
                textViewCatatanLog.setVisibility(View.VISIBLE);
                textViewCatatanLog.setText(logItem.catatan);
            } else {
                // WAJIB: Sembunyikan dan bersihkan teks jika tidak ada catatan
                textViewCatatanLog.setVisibility(View.GONE);
                textViewCatatanLog.setText("");
            }

            // 4. Atur Galeri Foto dengan blok if-else yang aman
            if (logItem.fotoBukti != null && !logItem.fotoBukti.isEmpty()) {
                horizontalScrollViewLogFoto.setVisibility(View.VISIBLE);
                tampilkanFoto(logItem.fotoBukti);
            } else {
                // WAJIB: Sembunyikan dan bersihkan kontainer jika tidak ada foto
                horizontalScrollViewLogFoto.setVisibility(View.GONE);
                containerLogFoto.removeAllViews();
            }
        }

        private void tampilkanFoto(List<String> listPathFoto) {
            containerLogFoto.removeAllViews(); // Selalu bersihkan view lama
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

        private int dpToPx(int dp) {
            return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, context.getResources().getDisplayMetrics());
        }
    }
}