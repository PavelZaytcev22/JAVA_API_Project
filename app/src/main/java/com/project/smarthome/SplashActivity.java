package com.project.smarthome;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.project.smarthome.api.ApiClient;
import com.project.smarthome.api.ApiService;
import com.project.smarthome.utils.SessionManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SplashActivity extends AppCompatActivity {

    private static final int SPLASH_DELAY = 1000;
    private SessionManager sessionManager;
    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        sessionManager = new SessionManager(this);
        apiService = ApiClient.getApiService(); // ✅ Исправлено — теперь корректно создается сервис

        new Handler().postDelayed(this::checkServerAndNavigate, SPLASH_DELAY);
    }

    private void checkServerAndNavigate() {
        // 🔹 Проверяем, доступен ли сервер (эндпоинт ping)
        apiService.ping().enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    if (sessionManager.isLoggedIn()) {
                        // ✅ Пользователь авторизован — идем в MainActivity
                        startActivity(new Intent(SplashActivity.this, MainActivity.class));
                    } else {
                        // 🔹 Нет токена — идем на LoginActivity
                        startActivity(new Intent(SplashActivity.this, LoginActivity.class));
                    }
                } else {
                    Toast.makeText(SplashActivity.this, "Ошибка соединения с сервером", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(SplashActivity.this, LoginActivity.class));
                }
                finish();
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(SplashActivity.this, "Сервер недоступен", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(SplashActivity.this, LoginActivity.class));
                finish();
            }
        });
    }
}
