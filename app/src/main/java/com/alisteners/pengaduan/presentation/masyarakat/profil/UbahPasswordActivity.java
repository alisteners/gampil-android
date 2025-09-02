package com.alisteners.pengaduan.presentation.masyarakat.profil;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.alisteners.pengaduan.presentation.login.LoginActivity;
import com.alisteners.pengaduan.R;
import com.alisteners.pengaduan.data.remote.config.ApiConfig;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class UbahPasswordActivity extends AppCompatActivity {

    private TextInputLayout inputLayoutPasswordLama, inputLayoutPasswordBaru, inputLayoutKonfirmasiPassword;
    private TextInputEditText editTextPasswordLama, editTextPasswordBaru, editTextKonfirmasiPassword;
    private Button buttonSimpanPassword;
    private ProgressBar progressBar;
    private RequestQueue requestQueue;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ubah_password);

        inisialisasiView();
        setupToolbar();

        requestQueue = Volley.newRequestQueue(this);
        sharedPreferences = getSharedPreferences(LoginActivity.SHARED_PREFS, MODE_PRIVATE);

        buttonSimpanPassword.setOnClickListener(v -> validasiDanSimpan());
    }

    private void inisialisasiView() {
        inputLayoutPasswordLama = findViewById(R.id.inputLayoutPasswordLama);
        editTextPasswordLama = findViewById(R.id.editTextPasswordLama);
        inputLayoutPasswordBaru = findViewById(R.id.inputLayoutPasswordBaru);
        editTextPasswordBaru = findViewById(R.id.editTextPasswordBaru);
        inputLayoutKonfirmasiPassword = findViewById(R.id.inputLayoutKonfirmasiPassword);
        editTextKonfirmasiPassword = findViewById(R.id.editTextKonfirmasiPassword);
        buttonSimpanPassword = findViewById(R.id.buttonSimpanPassword);
        progressBar = findViewById(R.id.progressBarUbahPassword);
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbarUbahPassword);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);

            Objects.requireNonNull(toolbar.getNavigationIcon()).setTint(getResources().getColor(android.R.color.black));
        }
    }

    private boolean validasiInput(String passwordLama, String passwordBaru, String konfirmasiPassword) {
        // Reset error sebelumnya
        inputLayoutPasswordLama.setError(null);
        inputLayoutPasswordBaru.setError(null);
        inputLayoutKonfirmasiPassword.setError(null);

        if (passwordLama.isEmpty()) {
            inputLayoutPasswordLama.setError("Password lama wajib diisi");
            return false;
        }
        if (passwordBaru.isEmpty()) {
            inputLayoutPasswordBaru.setError("Password baru wajib diisi");
            return false;
        }
        if (passwordBaru.length() < 6) {
            inputLayoutPasswordBaru.setError("Password baru minimal 6 karakter");
            return false;
        }
        if (konfirmasiPassword.isEmpty()) {
            inputLayoutKonfirmasiPassword.setError("Konfirmasi password wajib diisi");
            return false;
        }
        if (!passwordBaru.equals(konfirmasiPassword)) {
            inputLayoutKonfirmasiPassword.setError("Konfirmasi password tidak cocok");
            return false;
        }
        if (passwordBaru.equals(passwordLama)) {
            inputLayoutPasswordBaru.setError("Password baru tidak boleh sama dengan password lama");
            return false;
        }
        return true;
    }

    private void validasiDanSimpan() {
        String passwordLama = Objects.requireNonNull(editTextPasswordLama.getText()).toString().trim();
        String passwordBaru = Objects.requireNonNull(editTextPasswordBaru.getText()).toString().trim();
        String konfirmasiPassword = Objects.requireNonNull(editTextKonfirmasiPassword.getText()).toString().trim();

        if (validasiInput(passwordLama, passwordBaru, konfirmasiPassword)) {
            kirimKeServer(passwordLama, passwordBaru);
        }
    }

    private void kirimKeServer(final String passwordLama, final String passwordBaru) {
        setLoading(true);
        int idUser = sharedPreferences.getInt(LoginActivity.KEY_ID_USER, -1);
        if (idUser == -1) {
            Toast.makeText(this, "Sesi tidak valid, silakan login ulang.", Toast.LENGTH_SHORT).show();
            setLoading(false);
            return;
        }

        StringRequest stringRequest = new StringRequest(Request.Method.POST, ApiConfig.URL_UBAH_PASSWORD,
                response -> {
                    setLoading(false);
                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        Toast.makeText(this, jsonObject.getString("message"), Toast.LENGTH_LONG).show();
                        if ("success".equals(jsonObject.getString("status"))) {
                            finish(); // Kembali ke halaman profil jika berhasil
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(this, "Gagal memproses respons server.", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    setLoading(false);
                    Toast.makeText(this, "Error jaringan: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                }) {
            @NonNull
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("idUser", String.valueOf(idUser));
                params.put("passwordLama", passwordLama);
                params.put("passwordBaru", passwordBaru);
                return params;
            }
        };
        requestQueue.add(stringRequest);
    }

    private void setLoading(boolean isLoading) {
        if (isLoading) {
            progressBar.setVisibility(View.VISIBLE);
            buttonSimpanPassword.setEnabled(false);
        } else {
            progressBar.setVisibility(View.GONE);
            buttonSimpanPassword.setEnabled(true);
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}