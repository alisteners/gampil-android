package com.alisteners.pengaduan.presentation.register;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.alisteners.pengaduan.R;
import com.alisteners.pengaduan.data.remote.config.ApiConfig;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Activity yang menangani fungsionalitas registrasi pengguna baru.
 * Bertanggung jawab untuk menampilkan form pendaftaran, memvalidasi input,
 * mengirimkan data ke server, dan memberikan umpan balik kepada pengguna.
 */
public class RegisterActivity extends AppCompatActivity {

    // --- Deklarasi View ---
    private EditText editTextNamaLengkap, editTextNoHp, editTextAlamat, editTextEmailRegister, editTextPasswordRegister, editTextPasswordConfirmRegister;
    private RequestQueue requestQueue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Inisialisasi komponen UI
        editTextNamaLengkap = findViewById(R.id.editTextNamaLengkap);
        editTextNoHp = findViewById(R.id.editTextNoHp);
        editTextAlamat = findViewById(R.id.editTextAlamat);
        editTextEmailRegister = findViewById(R.id.editTextEmailRegister);
        editTextPasswordRegister = findViewById(R.id.editTextPasswordRegister);
        editTextPasswordConfirmRegister = findViewById(R.id.editTextPasswordConfirmRegister);
        Button buttonRegister = findViewById(R.id.buttonRegister);

        requestQueue = Volley.newRequestQueue(this);

        buttonRegister.setOnClickListener(v -> attemptRegistration());
    }

    /**
     * Memulai alur registrasi saat tombol "Daftar" di-klik.
     * Mengambil data dari form, memvalidasinya, dan jika valid, memanggil proses registrasi.
     */
    private void attemptRegistration() {
        // Ambil nilai dari semua EditText
        final String namaLengkap = editTextNamaLengkap.getText().toString().trim();
        final String noTelepon = editTextNoHp.getText().toString().trim();
        final String alamat = editTextAlamat.getText().toString().trim();
        final String email = editTextEmailRegister.getText().toString().trim();
        final String password = editTextPasswordRegister.getText().toString().trim();
        final String passwordConfirm = editTextPasswordConfirmRegister.getText().toString().trim();

        // Lakukan validasi input
        if (isInputValid(namaLengkap, noTelepon, alamat, email, password, passwordConfirm)) {
            // Jika valid, lanjutkan ke proses pengiriman data ke server
            prosesRegistrasi(namaLengkap, email, password, noTelepon, alamat);
        }
    }

    /**
     * Memvalidasi semua input dari form registrasi.
     * @return true jika semua input valid, false jika ada yang tidak valid.
     */
    private boolean isInputValid(String namaLengkap, String noTelepon, String alamat, String email, String password, String passwordConfirm) {
        if (namaLengkap.isEmpty() || noTelepon.isEmpty() || alamat.isEmpty() || email.isEmpty() || password.isEmpty()) {
            Toast.makeText(RegisterActivity.this, "Semua field wajib diisi!", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            editTextEmailRegister.setError("Format email tidak valid");
            editTextEmailRegister.requestFocus();
            return false;
        }
        if (password.length() < 6) {
            editTextPasswordRegister.setError("Password minimal 6 karakter");
            editTextPasswordRegister.requestFocus();
            return false;
        }
        if (!password.equals(passwordConfirm)) {
            editTextPasswordConfirmRegister.setError("Konfirmasi password tidak cocok!");
            editTextPasswordConfirmRegister.requestFocus();
            return false;
        }
        return true;
    }

    /**
     * Mengirim data registrasi ke server menggunakan Volley.
     *
     * @param namaLengkap Nama lengkap pengguna.
     * @param email Email pengguna.
     * @param password Password pengguna.
     * @param noTelepon Nomor telepon pengguna.
     * @param alamat Alamat pengguna.
     */
    private void prosesRegistrasi(final String namaLengkap, final String email, final String password, final String noTelepon, final String alamat) {
        StringRequest stringRequest = new StringRequest(Request.Method.POST, ApiConfig.URL_REGISTER,
                response -> {
                    Log.d("RegisterResponse", "Response: " + response);
                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        String status = jsonObject.getString("status");
                        String message = jsonObject.getString("message");

                        Toast.makeText(RegisterActivity.this, message, Toast.LENGTH_LONG).show();
                        if (status.equals("success")) {
                            // Jika registrasi berhasil, tutup activity ini untuk kembali ke LoginActivity.
                            finish();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(RegisterActivity.this, "Error parsing JSON: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                },
                error -> {
                    Toast.makeText(RegisterActivity.this, "Error koneksi atau server: " + error.toString(), Toast.LENGTH_LONG).show();
                    Log.e("RegisterVolleyError", error.toString());
                }) {
            @NonNull
            @Override
            protected Map<String, String> getParams() {
                // Parameter yang akan dikirim dalam body request
                Map<String, String> params = new HashMap<>();
                params.put("namaLengkap", namaLengkap);
                params.put("email", email);
                params.put("password", password);
                params.put("noTelepon", noTelepon);
                params.put("alamat", alamat);
                return params;
            }
        };

        requestQueue.add(stringRequest);
    }
}