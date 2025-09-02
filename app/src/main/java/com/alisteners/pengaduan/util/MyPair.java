package com.alisteners.pengaduan.util;

import androidx.annotation.NonNull;

import java.util.Objects;

// Tidak ada dependensi Android, ini adalah kelas Java murni
public record MyPair<F, S>(F first, S second) {

    // Implementasi equals dan hashCode penting agar List<MyPair> bisa dibandingkan
    // dengan benar oleh assertEquals di tes, dan agar berfungsi baik di koleksi.
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MyPair<?, ?> myPair = (MyPair<?, ?>) o;
        if (!Objects.equals(first, myPair.first)) return false;
        return Objects.equals(second, myPair.second);
    }

    // toString() untuk debugging yang lebih mudah
    @NonNull
    @Override
    public String toString() {
        return "MyPair{" +
                "first=" + first +
                ", second=" + second +
                '}';
    }
}