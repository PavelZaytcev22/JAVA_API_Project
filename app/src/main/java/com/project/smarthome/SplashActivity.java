package com.project.smarthome;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.project.smarthome.api.ApiClient;
import com.project.smarthome.api.ApiService;
import com.project.smarthome.models.auth.LoginActivity;
import com.project.smarthome.utils.SharedPrefManager;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import java.util.Map;

public class SplashActivity extends AppCompatActivity {

    private static final int SPLASH_DELAY = 2000; // Увеличим задержку для проверки сервера
    private SharedPrefManager sharedPrefManager;
    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // Инициализируем ApiClient и SharedPrefManager
        ApiClient.initialize(this);
        sharedPrefManager = SharedPrefManager.getInstance(this);
        apiService = ApiClient.getApiService();

        new Handler().postDelayed(this::checkServerAndNavigate, SPLASH_DELAY);
    }

    private void checkServerAndNavigate() {
        // Используем правильный endpoint для проверки сервера
        apiService.ping().enqueue(new Callback<Map<String, String>>() {
            @Override
            public void onResponse(Call<Map<String, String>> call, Response<Map<String, String>> response) {
                if (response.isSuccessful()) {
                    // Сервер доступен, проверяем авторизацию
                    navigateBasedOnAuth();
                } else {
                    // Сервер отвечает но с ошибкой
                    showErrorAndNavigate("Ошибка сервера: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<Map<String, String>> call, Throwable t) {
                showErrorAndNavigate("Сервер недоступен: " + t.getMessage());
            }
        });
    }

    private void navigateBasedOnAuth() {
        if (sharedPrefManager.isLoggedIn()) {
            // Пользователь авторизован
            startActivity(new Intent(SplashActivity.this, MainActivity.class));
        } else {
            // Пользователь не авторизован
            startActivity(new Intent(SplashActivity.this, LoginActivity.class));
        }
        finish();
    }

    private void showErrorAndNavigate(String message) {
        Toast.makeText(SplashActivity.this, message, Toast.LENGTH_LONG).show();
        // Все равно переходим к логину, но показываем ошибку
        startActivity(new Intent(SplashActivity.this, LoginActivity.class));
        finish();
    }
}