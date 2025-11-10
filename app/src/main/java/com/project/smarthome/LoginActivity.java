package com.project.smarthome;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;

import com.project.smarthome.api.ApiClient;
import com.project.smarthome.api.ApiService;
import com.project.smarthome.models.LoginRequest;
import com.project.smarthome.models.TokenResponse;
import com.project.smarthome.utils.SharedPrefManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class LoginActivity extends AppCompatActivity {

    private EditText editUsername, editPassword;
    private Button btnLogin;
    private TextView textRegister;
    private ProgressBar progressBar;

    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // ✅ Инициализация UI
        editUsername = findViewById(R.id.editUsername);
        editPassword = findViewById(R.id.editPassword);
        btnLogin = findViewById(R.id.btnLogin);
        textRegister = findViewById(R.id.textRegister);
        progressBar = findViewById(R.id.progressBar); // добавим в XML

        // ✅ Retrofit
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(ApiClient.getBaseUrl())
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        apiService = ApiClient.getClient().create(ApiService.class);


        // Переход на регистрацию
        textRegister.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
            finish();
        });

        // Вход
        btnLogin.setOnClickListener(v -> loginUser());
    }

    private void loginUser() {
        String username = editUsername.getText().toString().trim();
        String password = editPassword.getText().toString().trim();

        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Введите логин и пароль", Toast.LENGTH_SHORT).show();
            return;
        }

        // 🔹 UI: показываем прогресс, блокируем кнопку
        progressBar.setVisibility(View.VISIBLE);
        btnLogin.setEnabled(false);

        LoginRequest request = new LoginRequest(username, password, "user@mail.ru");

        // 🔹 Асинхронный вызов Retrofit
        apiService.login(request).enqueue(new Callback<TokenResponse>() {
            @Override
            public void onResponse(Call<TokenResponse> call, Response<TokenResponse> response) {
                progressBar.setVisibility(View.GONE);
                btnLogin.setEnabled(true);

                if (response.isSuccessful() && response.body() != null) {
                    String token = response.body().getAccessToken();

                    SharedPrefManager.getInstance(LoginActivity.this).saveToken(token);
                    SharedPrefManager.getInstance(LoginActivity.this).saveServerUrl(ApiClient.getBaseUrl());
                    SharedPrefManager.getInstance(LoginActivity.this).saveUsername(username);

                    Toast.makeText(LoginActivity.this, "Вход выполнен", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(LoginActivity.this, MainActivity.class));
                    finish();
                } else {
                    Toast.makeText(LoginActivity.this, "Ошибка авторизации", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<TokenResponse> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                btnLogin.setEnabled(true);
                Toast.makeText(LoginActivity.this, "Сервер не готов. Подождите...", Toast.LENGTH_LONG).show();
            }
        });
    }
}
