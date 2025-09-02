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

public class RegisterActivity extends AppCompatActivity {

    private EditText editTextNamaLengkap, editTextNoHp, editTextAlamat, editTextEmailRegister, editTextPasswordRegister;
    private RequestQueue requestQueue;
    private EditText editTextPasswordConfirmRegister;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        editTextNamaLengkap = findViewById(R.id.editTextNamaLengkap);
        editTextNoHp = findViewById(R.id.editTextNoHp);
        editTextAlamat = findViewById(R.id.editTextAlamat);
        editTextEmailRegister = findViewById(R.id.editTextEmailRegister);
        editTextPasswordRegister = findViewById(R.id.editTextPasswordRegister);
        editTextPasswordConfirmRegister = findViewById(R.id.editTextPasswordConfirmRegister);
        Button buttonRegister = findViewById(R.id.buttonRegister);

        // Inisialisasi RequestQueue
        requestQueue = Volley.newRequestQueue(this);

        buttonRegister.setOnClickListener(v -> {
            // Ambil nilai dari EditText
            final String namaLengkap = editTextNamaLengkap.getText().toString().trim();
            final String noTelepon = editTextNoHp.getText().toString().trim();
            final String alamat = editTextAlamat.getText().toString().trim();
            final String email = editTextEmailRegister.getText().toString().trim();
            final String password = editTextPasswordRegister.getText().toString().trim();
            // ... (ambil nilai field lain) ...
            final String passwordConfirm = editTextPasswordConfirmRegister.getText().toString().trim();

            // Validasi input di sisi klien (sederhana)
            if (namaLengkap.isEmpty() || noTelepon.isEmpty() || alamat.isEmpty() || email.isEmpty() || password.isEmpty()) {
                Toast.makeText(RegisterActivity.this, "Semua field wajib diisi!", Toast.LENGTH_SHORT).show();
                return;
            }
            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()){
                editTextEmailRegister.setError("Format email tidak valid");
                editTextEmailRegister.requestFocus();
                return;
            }
            if (password.length() < 6) { // Validasi panjang password
                editTextPasswordRegister.setError("Password minimal 6 karakter");
                editTextPasswordRegister.requestFocus();
                return;
            }
            if (!password.equals(passwordConfirm)) {
                editTextPasswordConfirmRegister.setError("Konfirmasi password tidak cocok!");
                editTextPasswordConfirmRegister.requestFocus();
                return;
            }

            // Panggil metode untuk melakukan registrasi
            prosesRegistrasi(namaLengkap, email, password, noTelepon, alamat);
        });
    }

    private void prosesRegistrasi(final String namaLengkap, final String email, final String password, final String noTelepon, final String alamat) {
        // Membuat StringRequest baru
        StringRequest stringRequest = new StringRequest(Request.Method.POST, ApiConfig.URL_REGISTER,
                response -> {
                    Log.d("RegisterResponse", "Response: " + response); // Log respons dari server
                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        String status = jsonObject.getString("status");
                        String message = jsonObject.getString("message");

                        if (status.equals("success")) {
                            Toast.makeText(RegisterActivity.this, message, Toast.LENGTH_LONG).show();
                            // Setelah registrasi berhasil, kembali ke LoginActivity
                            finish();
                        } else {
                            Toast.makeText(RegisterActivity.this, message, Toast.LENGTH_LONG).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(RegisterActivity.this, "Error parsing JSON: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        Log.e("RegisterJSONException", e.toString());
                    }
                },
                error -> {
                    Toast.makeText(RegisterActivity.this, "Error koneksi atau server: " + error.toString(), Toast.LENGTH_LONG).show();
                    Log.e("RegisterVolleyError", error.toString());
                    if (error.networkResponse != null) {
                        Log.e("RegisterVolleyErrorData", "Status Code: " + error.networkResponse.statusCode + " Data: " + new String(error.networkResponse.data));
                    }
                }) {
            @NonNull
            @Override
            protected Map<String, String> getParams() {
                // Parameter yang akan dikirim ke server
                Map<String, String> params = new HashMap<>();
                params.put("namaLengkap", namaLengkap);
                params.put("email", email);
                params.put("password", password);
                params.put("noTelepon", noTelepon);
                params.put("alamat", alamat);
                // Role tidak perlu dikirim, sudah di-set default di PHP
                return params;
            }
        };

        // Menambahkan request ke RequestQueue
        requestQueue.add(stringRequest);
    }
}