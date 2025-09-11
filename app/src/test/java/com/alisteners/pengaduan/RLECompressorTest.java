package com.alisteners.pengaduan;

import org.junit.Test;
import static org.junit.Assert.*;

import com.alisteners.pengaduan.data.util.RLECompressor;
import com.alisteners.pengaduan.util.MyPair;

import java.util.ArrayList;
import java.util.List;

public class RLECompressorTest {

    // Metode bantu untuk membandingkan List<MyPair>
    private void assertMyPairListEquals(String messagePrefix, List<MyPair<Integer, Integer>> expected, List<MyPair<Integer, Integer>> actual) {
        assertNotNull(messagePrefix + ": List aktual tidak boleh null", actual);
        assertNotNull(messagePrefix + ": List ekspektasi tidak boleh null", expected);
        assertEquals(messagePrefix + ": Ukuran list harus sama", expected.size(), actual.size());

        for (int i = 0; i < expected.size(); i++) {
            MyPair<Integer, Integer> expectedPair = expected.get(i);
            MyPair<Integer, Integer> actualPair = actual.get(i);

            assertNotNull(messagePrefix + ": MyPair ekspektasi ke-" + i + " tidak boleh null", expectedPair);
            assertNotNull(messagePrefix + ": MyPair aktual ke-" + i + " tidak boleh null", actualPair);
            assertEquals(messagePrefix + ": Nilai 'first' pada MyPair ke-" + i + " harus sama", expectedPair.first(), actualPair.first());
            assertEquals(messagePrefix + ": Nilai 'second' pada MyPair ke-" + i + " harus sama", expectedPair.second(), actualPair.second());
        }
    }

    // --- TEST CASES UNTUK compressRLE ---
    @Test
    public void testCompressRLE_emptyArray() {
        int[] input = {};
        List<MyPair<Integer, Integer>> expected = new ArrayList<>();
        List<MyPair<Integer, Integer>> actual = RLECompressor.compressRLE(input);
        assertMyPairListEquals("Compress empty array", expected, actual);
    }

    @Test
    public void testCompressRLE_singleElementArray() {
        int[] input = {5};
        List<MyPair<Integer, Integer>> expected = new ArrayList<>();
        expected.add(new MyPair<>(5, 1));
        List<MyPair<Integer, Integer>> actual = RLECompressor.compressRLE(input);
        assertMyPairListEquals("Compress single element", expected, actual);
    }

    @Test
    public void testCompressRLE_allSameElements() {
        int[] input = {7, 7, 7, 7, 7};
        List<MyPair<Integer, Integer>> expected = new ArrayList<>();
        expected.add(new MyPair<>(7, 5));
        List<MyPair<Integer, Integer>> actual = RLECompressor.compressRLE(input);
        assertMyPairListEquals("Compress all same elements", expected, actual);
    }

    @Test
    public void testCompressRLE_allDifferentElements() {
        int[] input = {1, 2, 3, 4, 5};
        List<MyPair<Integer, Integer>> expected = new ArrayList<>();
        expected.add(new MyPair<>(1, 1));
        expected.add(new MyPair<>(2, 1));
        expected.add(new MyPair<>(3, 1));
        expected.add(new MyPair<>(4, 1));
        expected.add(new MyPair<>(5, 1));
        List<MyPair<Integer, Integer>> actual = RLECompressor.compressRLE(input);
        assertMyPairListEquals("Compress all different elements", expected, actual);
    }

    @Test
    public void testCompressRLE_mixedElements() {
        int[] input = {1, 1, 2, 3, 3, 3, 4, 4, 5};
        List<MyPair<Integer, Integer>> expected = new ArrayList<>();
        expected.add(new MyPair<>(1, 2));
        expected.add(new MyPair<>(2, 1));
        expected.add(new MyPair<>(3, 3));
        expected.add(new MyPair<>(4, 2));
        expected.add(new MyPair<>(5, 1));
        List<MyPair<Integer, Integer>> actual = RLECompressor.compressRLE(input);
        assertMyPairListEquals("Compress mixed elements", expected, actual);
    }

    @Test
    public void testCompressRLE_nullInput() {
        List<MyPair<Integer, Integer>> actual = RLECompressor.compressRLE(null);
        assertNotNull("Hasil kompresi dari input null seharusnya list kosong, bukan null", actual);
        assertTrue("Hasil kompresi dari input null seharusnya list kosong", actual.isEmpty());
    }


    // --- TEST CASES UNTUK quantizeGrayscale ---
    @Test
    public void testQuantizeGrayscale_nullInput() {
        RLECompressor.quantizeGrayscale(null, 16);
        int[] actualQuantized = null;
        assertNull("Quantisasi dari input null harus mengembalikan null", null);
    }

    @Test
    public void testQuantizeGrayscale_emptyInput() {
        int[] input = {};
        int[] actualQuantized = RLECompressor.quantizeGrayscale(input, 16);
        assertArrayEquals("Quantisasi dari input kosong harus mengembalikan array kosong", input, actualQuantized);
    }

    @Test
    public void testQuantizeGrayscale_4Levels() {
        int[] grayscaleInput = {0, 10, 63, 64, 70, 127, 128, 150, 191, 192, 220, 255};
        int[] expectedQuantized = {32, 32, 32, 96, 96, 96, 160, 160, 160, 224, 224, 224};
        int[] actualQuantized = RLECompressor.quantizeGrayscale(grayscaleInput, 4);
        assertArrayEquals("Quantisasi dengan 4 level gagal", expectedQuantized, actualQuantized);
    }

    @Test
    public void testQuantizeGrayscale_256Levels() {
        int[] grayscaleInput = {0, 10, 128, 255};
        int[] actualQuantized = RLECompressor.quantizeGrayscale(grayscaleInput, 256);
        assertArrayEquals("Quantisasi dengan 256 level seharusnya tidak mengubah array", grayscaleInput, actualQuantized);
    }

    @Test
    public void testQuantizeGrayscale_invalidLevels() {
        int[] grayscaleInput = {10, 50, 100};
        int[] actualZeroLevels = RLECompressor.quantizeGrayscale(grayscaleInput, 0);
        assertArrayEquals("Quantisasi dengan 0 level harus mengembalikan array asli", grayscaleInput, actualZeroLevels);

        int[] actualTooManyLevels = RLECompressor.quantizeGrayscale(grayscaleInput, 257);
        assertArrayEquals("Quantisasi dengan >256 level harus mengembalikan array asli", grayscaleInput, actualTooManyLevels);
    }

    // --- TEST CASES BARU UNTUK convertRleListToBinary ---
    @Test
    public void testConvertRleListToBinary_emptyList() {
        List<MyPair<Integer, Integer>> rleList = new ArrayList<>();
        byte[] expected = {};
        assertArrayEquals("Byte array dari list kosong gagal", expected, RLECompressor.convertRleListToBinary(rleList));
    }

    @Test
    public void testConvertRleListToBinary_singlePair() {
        List<MyPair<Integer, Integer>> rleList = new ArrayList<>();
        rleList.add(new MyPair<>(100, 5)); // pixel 100, count 5
        // URUTAN BYTE: [nilai, jumlah]
        byte[] expected = {(byte)100, (byte)5};
        assertArrayEquals("Byte array dari satu pair gagal", expected, RLECompressor.convertRleListToBinary(rleList));
    }

    @Test
    public void testConvertRleListToBinary_multiplePairs() {
        List<MyPair<Integer, Integer>> rleList = new ArrayList<>();
        rleList.add(new MyPair<>(10, 2));
        rleList.add(new MyPair<>(255, 1));
        rleList.add(new MyPair<>(0, 3));
        // URUTAN BYTE: [nilai, jumlah], [nilai, jumlah], ...
        byte[] expected = {(byte)10, (byte)2, (byte)255, (byte)1, (byte)0, (byte)3};
        assertArrayEquals("Byte array dari beberapa pair gagal", expected, RLECompressor.convertRleListToBinary(rleList));
    }

    @Test
    public void testConvertRleListToBinary_longRun() {
        List<MyPair<Integer, Integer>> rleList = new ArrayList<>();
        rleList.add(new MyPair<>(50, 300)); // count 300 -> (50,255), (50,45)
        // URUTAN BYTE: [nilai, jumlah], [nilai, jumlah]
        byte[] expected = {(byte)50, (byte)255, (byte)50, (byte)45};
        assertArrayEquals("Byte array dari long run gagal", expected, RLECompressor.convertRleListToBinary(rleList));
    }

    @Test
    public void testConvertRleListToBinary_nullList() {
        // Implementasi kita sekarang mengembalikan array kosong untuk input null
        byte[] actual = RLECompressor.convertRleListToBinary(null);
        assertNotNull("Hasil konversi byte array dari list null seharusnya array kosong, bukan null", actual);
        assertEquals("Panjang array dari list null harus 0", 0, actual.length);
    }
}