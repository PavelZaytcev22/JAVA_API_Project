package com.project.smarthome;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Handler;
import android.widget.ImageView;

public class SplashActivity extends AppCompatActivity {

    private static final int SPLASH_DELAY = 1500; // 1.5 секунды на экран заставки

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // Можно добавить анимацию логотипа, если хочешь — потом покажу пример
        ImageView logo = findViewById(R.id.logoImage);

        // "Фоновая" проверка — просто запускаем через Handler, чтобы не блокировать UI
        new Handler().postDelayed(this::checkAuthAndProceed, SPLASH_DELAY);
    }

    private void checkAuthAndProceed() {
        boolean isLoggedIn = checkUserLoggedIn();

        Intent intent;
        if (isLoggedIn) {
            intent = new Intent(this, MainActivity.class);
        } else {
            intent = new Intent(this, LoginActivity.class);
        }

        startActivity(intent);
        finish();
    }

    private boolean checkUserLoggedIn() {
        // 🔹 Пример: проверяем флаг в SharedPreferences (временно вместо Firebase)
        return getSharedPreferences("auth_prefs", MODE_PRIVATE)
                .getBoolean("is_logged_in", false);
    }
}