package com.alisteners.pengaduan.util;

import androidx.annotation.NonNull;

import java.util.Objects;

/**
 * Sebuah kelas record generik yang berfungsi sebagai pengganti sederhana untuk
 * kelas Pair bawaan Android. Dibuat sebagai Java Record untuk imutabilitas
 * (nilai tidak dapat diubah setelah dibuat) dan keringkasan kode.
 *
 * @param <F> Tipe data untuk elemen pertama.
 * @param <S> Tipe data untuk elemen kedua.
 * @param first Elemen pertama dari pasangan.
 * @param second Elemen kedua dari pasangan.
 */
public record MyPair<F, S>(F first, S second) {

    /**
     * Memeriksa kesamaan antara dua objek MyPair.
     * Dua objek MyPair dianggap sama jika elemen 'first' dan 'second' keduanya sama.
     *
     * @param o Objek lain yang akan dibandingkan.
     * @return true jika objek sama, false jika berbeda.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MyPair<?, ?> myPair = (MyPair<?, ?>) o;
        return Objects.equals(first, myPair.first) && Objects.equals(second, myPair.second);
    }

    /**
     * Menghasilkan representasi String dari objek MyPair.
     * Berguna untuk keperluan logging dan debugging.
     *
     * @return String yang merepresentasikan objek.
     */
    @NonNull
    @Override
    public String toString() {
        return "MyPair{" +
                "first=" + first +
                ", second=" + second +
                '}';
    }
}