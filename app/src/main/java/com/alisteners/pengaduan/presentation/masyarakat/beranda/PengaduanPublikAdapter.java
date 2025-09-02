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

public class PengaduanPublikAdapter extends RecyclerView.Adapter<PengaduanPublikAdapter.PengaduanPublikViewHolder> {

    private final List<PengaduanPublikItem> pengaduanItems;
    private final Context context;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(PengaduanPublikItem item);
    }

    public PengaduanPublikAdapter(List<PengaduanPublikItem> pengaduanItems, Context context) {
        this.pengaduanItems = pengaduanItems;
        this.context = context;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public PengaduanPublikViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_pengaduan_publik, parent, false);
        return new PengaduanPublikViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PengaduanPublikViewHolder holder, int position) {
        PengaduanPublikItem item = pengaduanItems.get(position);
        holder.bind(item, listener);
    }

    @Override
    public int getItemCount() {
        return pengaduanItems != null ? pengaduanItems.size() : 0;
    }

    @SuppressLint("NotifyDataSetChanged")
    public void updateData(List<PengaduanPublikItem> newItems) {
        pengaduanItems.clear();
        if (newItems != null) {
            pengaduanItems.addAll(newItems);
        }
        notifyDataSetChanged();
    }

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

        void bind(final PengaduanPublikItem item, final OnItemClickListener listener) {

            textViewKategori.setText(item.getKategori());
            textViewTanggal.setText(formatTanggal(item.getTanggalPengaduan()));
            // LOG INI UNTUK MEMERIKSA DATA ASLI
            Log.d("AdapterDebug", "Isi dari item.getNamaPelapor(): '" + item.getNamaPelapor() + "'");
            textViewNamaPelapor.setText(item.getNamaPelapor());
            textViewDeskripsi.setText(item.getDeskripsiSingkat());

            setupChipStatus(item.getStatus());

            String path = item.getPathImage();

            // Cek jika path valid
            if (path != null && !path.isEmpty() && !path.equalsIgnoreCase("null")) {
                String imageUrl = ApiConfig.BASE_URL + path;

                // LOGGING YANG SANGAT PENTING
                Log.d("PengaduanPublikAdapter", "Memuat gambar dari URL: " + imageUrl);

                Glide.with(context)
                        .load(imageUrl)
                        .placeholder(R.drawable.ic_image_placeholder)
                        .error(R.drawable.ic_image_error) // Tampilkan ikon error jika gagal
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
