package com.alisteners.pengaduan.util;

import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

/**
 * Kelas utilitas (helper) untuk mendapatkan path file absolut dari sebuah content URI.
 * Penanganan URI di Android bisa menjadi kompleks karena berbagai macam provider (Galeri,
 * Google Drive, Downloads, dll.). Kelas ini menyediakan metode untuk menangani
 * berbagai skenario tersebut dan mengembalikan path file yang sebenarnya di penyimpanan.
 */
public class URIPathHelper {

    /**
     * Metode utama untuk mengonversi sebuah Uri menjadi path file absolut.
     * Metode ini menangani berbagai jenis skema dan authority URI yang umum di Android.
     *
     * @param context Context aplikasi.
     * @param uri URI dari file yang akan dicari path-nya.
     * @return String path file absolut, atau null jika path tidak dapat ditemukan.
     */
    public static String getPath(final Context context, final Uri uri) {
        // Cek apakah ini Document Uri (cara modern untuk mengakses file)
        if (DocumentsContract.isDocumentUri(context, uri)) {
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                }
            } else if (isDownloadsDocument(uri)) {
                final String id = DocumentsContract.getDocumentId(uri);

                if (!TextUtils.isEmpty(id)) {
                    if (id.startsWith("raw:")) {
                        return id.substring(4);
                    }
                    try {
                        final Uri contentUri = ContentUris.withAppendedId(
                                Uri.parse("content://downloads/public_downloads"), Long.parseLong(id));
                        String path = getDataColumn(context, contentUri, null, null);
                        if (path != null) return path;
                    } catch (NumberFormatException e) {
                        Log.e("URIPathHelper", "Downloads Uri id is not a number: " + e.getMessage());
                        // Fallback ke penyalinan file jika ID tidak valid
                    }
                }
                // Jika cara di atas gagal atau ID kosong, salin file sebagai fallback.
                return copyFileToInternal(context, uri);

            } else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }

                final String selection = "_id=?";
                final String[] selectionArgs = new String[]{split[1]};

                return getDataColumn(context, contentUri, selection, selectionArgs);
            }
        } else if ("content".equalsIgnoreCase(uri.getScheme())) {
            // Penanganan untuk content URI biasa (non-document)
            return getDataColumn(context, uri, null, null);
        } else if ("file".equalsIgnoreCase(uri.getScheme())) {
            // Penanganan untuk file URI
            return uri.getPath();
        }

        return null;
    }

    /**
     * Mengambil nilai dari kolom '_data' di ContentResolver untuk sebuah URI.
     * Kolom ini biasanya berisi path file absolut.
     * Jika gagal (misalnya kolom tidak ada), metode ini akan memanggil fallback.
     *
     * @param context       Context aplikasi.
     * @param uri           URI yang akan di-query.
     * @param selection     Filter query (bisa null).
     * @param selectionArgs Argumen untuk filter query (bisa null).
     * @return String path file, atau null jika tidak ditemukan.
     */
    private static String getDataColumn(Context context, Uri uri, String selection, String[] selectionArgs) {
        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {column};

        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs, null);
            if (cursor != null && cursor.moveToFirst()) {
                final int columnIndex = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(columnIndex);
            }
        } catch (IllegalArgumentException e) {
            // Menangani error seperti "column '_data' does not exist" dengan menyalin file
            Log.w("URIPathHelper", "getDataColumn gagal, fallback ke copyFileToInternal. " + e.getMessage());
            return copyFileToInternal(context, uri);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return null;
    }

    /**
     * Metode fallback yang akan menyalin file dari URI ke direktori cache internal aplikasi.
     * Ini digunakan ketika path file absolut tidak bisa didapatkan secara langsung,
     * yang sering terjadi pada Android versi baru atau dari provider tertentu (misal: Google Photos).
     *
     * @param context Context aplikasi.
     * @param uri     URI dari file sumber.
     * @return Path absolut dari file yang telah disalin di cache, atau null jika gagal.
     */
    private static String copyFileToInternal(Context context, Uri uri) {
        try {
            InputStream inputStream = context.getContentResolver().openInputStream(uri);
            if (inputStream == null) return null;

            String fileName = "temp_file_" + System.currentTimeMillis();
            File file = new File(context.getCacheDir(), fileName);

            try (FileOutputStream outputStream = new FileOutputStream(file)) {
                byte[] buffer = new byte[1024];
                int length;
                while ((length = inputStream.read(buffer)) > 0) {
                    outputStream.write(buffer, 0, length);
                }
            }
            inputStream.close();
            return file.getAbsolutePath();
        } catch (Exception e) {
            Log.e("URIPathHelper", "Gagal menyalin file", e);
            return null;
        }
    }

    /**
     * Memeriksa apakah URI berasal dari ExternalStorageProvider.
     */
    private static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    /**
     * Memeriksa apakah URI berasal dari DownloadsProvider.
     */
    private static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    /**
     * Memeriksa apakah URI berasal dari MediaProvider.
     */
    private static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }
}