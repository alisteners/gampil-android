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

/**
 * Activity yang berfungsi sebagai layar login untuk autentikasi pengguna.
 * Bertanggung jawab untuk menangani input email dan password, memvalidasinya,
 * mengirimkannya ke server, dan mengelola sesi login pengguna menggunakan SharedPreferences.
 */
public class LoginActivity extends AppCompatActivity {

    private EditText editTextEmail;
    private EditText editTextPassword;
    private RequestQueue requestQueue;

    // --- Kunci untuk Manajemen Sesi via SharedPreferences ---
    /** Nama file SharedPreferences yang digunakan untuk menyimpan data sesi. */
    public static final String SHARED_PREFS = "sharedPrefs";
    /** Kunci untuk menyimpan ID pengguna yang sedang login. */
    public static final String KEY_ID_USER = "idUser";
    /** Kunci untuk menyimpan nama lengkap pengguna. */
    public static final String KEY_NAMA_LENGKAP = "namaLengkap";
    /** Kunci untuk menyimpan email pengguna. */
    public static final String KEY_EMAIL = "email";
    /** Kunci untuk menyimpan nomor telepon pengguna. */
    public static final String KEY_NO_TELEPON = "noTelepon";
    /** Kunci untuk menyimpan peran (role) pengguna (misal: "masyarakat", "operator_desa"). */
    public static final String KEY_ROLE = "role";
    /** Kunci untuk menyimpan status login (boolean). */
    public static final String KEY_IS_LOGGED_IN = "isLoggedIn";

    public static final String TAG = "LoginActivity";

    /**
     * Metode yang dipanggil saat Activity pertama kali dibuat.
     * Logika utamanya adalah memeriksa apakah pengguna sudah memiliki sesi login yang aktif.
     * Jika ya, pengguna akan langsung diarahkan ke halaman yang sesuai. Jika tidak,
     * layout login akan ditampilkan.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate: Activity dimulai.");
        // Cek sesi login sebelum menampilkan layout untuk efisiensi.
        if (isUserLoggedIn()) {
            navigasiBerdasarkanRole();
        } else {
            Log.d(TAG, "onCreate: Pengguna belum login. Menampilkan halaman login.");
            setContentView(R.layout.activity_login);
            initializeLoginView();
        }
    }

    /**
     * Menginisialisasi semua komponen view pada layout login dan mengatur listener untuk tombol.
     */
    private void initializeLoginView() {
        Log.d(TAG, "initializeLoginView: Mulai inisialisasi view.");
        editTextEmail = findViewById(R.id.editTextEmail);
        editTextPassword = findViewById(R.id.editTextPassword);
        Button buttonLogin = findViewById(R.id.buttonLogin);
        Button buttonGoToRegister = findViewById(R.id.buttonGoToRegister);

        requestQueue = Volley.newRequestQueue(this.getApplicationContext());

        buttonLogin.setOnClickListener(v -> {
            String email = editTextEmail.getText().toString().trim();
            String password = editTextPassword.getText().toString().trim();

            if (validasiInput(email, password)) {
                prosesLogin(email, password);
            }
        });

        buttonGoToRegister.setOnClickListener(v -> startActivity(new Intent(LoginActivity.this, RegisterActivity.class)));
    }

    /**
     * Memvalidasi input email dan password dari pengguna.
     * @param email Email yang dimasukkan.
     * @param password Password yang dimasukkan.
     * @return true jika input valid, false jika tidak.
     */
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

    /**
     * Memproses permintaan login ke server menggunakan Volley.
     * @param email Email pengguna yang sudah divalidasi.
     * @param password Password pengguna yang sudah divalidasi.
     */
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
                            String noTeleponUser = userData.getString("noTelepon");

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

    /**
     * Menyimpan data sesi pengguna ke SharedPreferences setelah login berhasil.
     * @param idUser ID pengguna.
     * @param namaLengkap Nama lengkap pengguna.
     * @param email Email pengguna.
     * @param noTelepon Nomor telepon pengguna.
     * @param role Peran pengguna (role).
     */
    private void saveLoginSession(int idUser, String namaLengkap, String email, String noTelepon, String role) {
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        editor.putInt(KEY_ID_USER, idUser);
        editor.putString(KEY_NAMA_LENGKAP, namaLengkap);
        editor.putString(KEY_EMAIL, email);
        editor.putString(KEY_NO_TELEPON, noTelepon);
        editor.putString(KEY_ROLE, role);

        editor.apply();
        Log.d(TAG, "saveLoginSession: Sesi disimpan untuk user: " + email + ", Role: " + role);
    }

    /**
     * Memeriksa apakah ada sesi login yang aktif di SharedPreferences.
     * @return true jika pengguna sudah login, false jika belum.
     */
    private boolean isUserLoggedIn() {
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        return sharedPreferences.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    /**
     * Mengarahkan (navigasi) pengguna ke Activity yang sesuai berdasarkan peran (role) mereka.
     * Activity ini kemudian akan ditutup (finish) untuk mencegah pengguna kembali ke halaman login.
     */
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
            return;
        }

        startActivity(intent);
        finish(); // Tutup LoginActivity agar tidak bisa kembali dengan tombol back.
    }

}