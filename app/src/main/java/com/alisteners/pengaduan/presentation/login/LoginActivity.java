package com.alisteners.pengaduan.presentation.login;

import com.alisteners.pengaduan.MainActivity;
import com.alisteners.pengaduan.presentation.operator.dashboard.OperatorDashboardActivity;
import com.alisteners.pengaduan.R;
import com.alisteners.pengaduan.presentation.register.RegisterActivity;
import com.alisteners.pengaduan.data.remote.config.ApiConfig;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {

    private EditText editTextEmail;
    private EditText editTextPassword;
    private RequestQueue requestQueue;

    // --- Kunci untuk SharedPreferences ---
    public static final String SHARED_PREFS = "sharedPrefs";
    public static final String KEY_ID_USER = "idUser";
    public static final String KEY_NAMA_LENGKAP = "namaLengkap";
    public static final String KEY_EMAIL = "email";
    public static final String KEY_NO_TELEPON = "noTelepon"; // KEY UNTUK NOMOR TELEPON
    public static final String KEY_ROLE = "role";
    public static final String KEY_IS_LOGGED_IN = "isLoggedIn";

    public static final String TAG = "LoginActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate: Activity dimulai.");
        // Cek sesi login SEBELUM menampilkan layout
        if (isUserLoggedIn()) {
            navigasiBerdasarkanRole();
        } else {
            Log.d(TAG, "onCreate: Pengguna belum login. Menampilkan halaman login.");
            setContentView(R.layout.activity_login);
            initializeLoginView();
        }
    }

    private void initializeLoginView() {
        Log.d(TAG, "initializeLoginView: Mulai inisialisasi view.");
        editTextEmail = findViewById(R.id.editTextEmail);
        editTextPassword = findViewById(R.id.editTextPassword);
        Button buttonLogin = findViewById(R.id.buttonLogin);
        Button buttonGoToRegister = findViewById(R.id.buttonGoToRegister);
        //TextView textViewForgotPassword = findViewById(R.id.textViewForgotPassword);

        requestQueue = Volley.newRequestQueue(this.getApplicationContext());

        buttonLogin.setOnClickListener(v -> {
            String email = editTextEmail.getText().toString().trim();
            String password = editTextPassword.getText().toString().trim();

            if (!validasiInput(email, password)) {
                return; // Hentikan jika validasi gagal
            }
            prosesLogin(email, password);
        });

        buttonGoToRegister.setOnClickListener(v -> startActivity(new Intent(LoginActivity.this, RegisterActivity.class)));
    }

    private boolean validasiInput(String email, String password) {
        if (email.isEmpty()) {
            editTextEmail.setError("Email tidak boleh kosong!");
            editTextEmail.requestFocus();
            return false;
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            editTextEmail.setError("Format email tidak valid!");
            editTextEmail.requestFocus();
            return false;
        }
        if (password.isEmpty()) {
            editTextPassword.setError("Password tidak boleh kosong!");
            editTextPassword.requestFocus();
            return false;
        }
        return true;
    }

    private void prosesLogin(final String email, final String password) {
        Log.d(TAG, "prosesLogin: Mencoba login untuk email: " + email);
        StringRequest stringRequest = new StringRequest(Request.Method.POST, ApiConfig.URL_LOGIN,
                response -> {
                    Log.d(TAG, "LoginResponse: " + response);
                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        String status = jsonObject.getString("status");
                        String message = jsonObject.getString("message");

                        if (status.equals("success")) {
                            Toast.makeText(LoginActivity.this, message, Toast.LENGTH_LONG).show();

                            JSONObject userData = jsonObject.getJSONObject("user");
                            int idUser = userData.getInt("idUser");
                            String namaLengkap = userData.getString("namaLengkap");
                            String userEmail = userData.getString("email");
                            String role = userData.getString("role");
                            // --- AMBIL DATA TELEPON DARI JSON ---
                            String noTeleponUser = userData.getString("noTelepon");
                            // Simpan sesi pengguna termasuk nomor telepon
                            saveLoginSession(idUser, namaLengkap, userEmail, noTeleponUser, role);

                            navigasiBerdasarkanRole();

                        } else {
                            Toast.makeText(LoginActivity.this, message, Toast.LENGTH_LONG).show();
                            Log.w(TAG, "prosesLogin: Login gagal - " + message);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(LoginActivity.this, "Gagal memproses data login dari server.", Toast.LENGTH_LONG).show();
                        Log.e(TAG, "LoginJSONException: " + e.getMessage() + "\nResponse: " + response);
                    }
                },
                error -> {
                    String errorMessage = "Gagal terhubung ke server. Periksa koneksi internet atau IP server.";
                    if (error.networkResponse != null) {
                        errorMessage += " Kode: " + error.networkResponse.statusCode;
                    }
                    Toast.makeText(LoginActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                    Log.e(TAG, "LoginVolleyError: " + error);
                }) {
            @NonNull
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("email", email);
                params.put("password", password);
                return params;
            }
        };

        requestQueue.add(stringRequest);
    }

    private void saveLoginSession(int idUser, String namaLengkap, String email, String noTelepon, String role) {
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        editor.putInt(KEY_ID_USER, idUser);
        editor.putString(KEY_NAMA_LENGKAP, namaLengkap);
        editor.putString(KEY_EMAIL, email);
        editor.putString(KEY_NO_TELEPON, noTelepon); // Simpan nomor telepon
        editor.putString(KEY_ROLE, role);

        editor.apply();
        Log.d(TAG, "saveLoginSession: Sesi disimpan untuk user: " + email + ", Role: " + role);
    }

    private boolean isUserLoggedIn() {
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        return sharedPreferences.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    private void navigasiBerdasarkanRole() {
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        String role = sharedPreferences.getString(KEY_ROLE, "ROLE_KOSONG");
        Log.d(TAG, "navigasiBerdasarkanRole: Role pengguna adalah '" + role + "'");

        Intent intent;
        if (role.equals("masyarakat")) {
            intent = new Intent(LoginActivity.this, MainActivity.class);
        } else if (role.equals("operator_desa")) {
            intent = new Intent(LoginActivity.this, OperatorDashboardActivity.class);
        } else {
            Log.e(TAG, "Role tidak dikenal, tidak melakukan navigasi.");
            // Jika role tidak dikenal, lebih baik jangan lakukan apa-apa
            // atau tampilkan pesan error dan tetap di halaman login.
            // Ini mencegah crash jika terjadi kesalahan data.
            return;
        }

        startActivity(intent);
        finish(); // Tutup LoginActivity agar tidak bisa kembali dengan tombol back
    }

}
