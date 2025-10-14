package com.project.smarthome;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import android.content.Intent;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;


public class LoginActivity extends AppCompatActivity {

    EditText editServerUrl, editUsername, editPassword;
    Button btnLogin;
    TextView textRegister;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        editServerUrl = findViewById(R.id.editServerUrl);
        editUsername = findViewById(R.id.editUsername);
        editPassword = findViewById(R.id.editPassword);
        btnLogin = findViewById(R.id.btnLogin);
        textRegister = findViewById(R.id.textRegister);

        btnLogin.setOnClickListener(v -> {
            String url = editServerUrl.getText().toString().trim();
            String user = editUsername.getText().toString().trim();
            String pass = editPassword.getText().toString().trim();

            if (url.isEmpty() || user.isEmpty() || pass.isEmpty()) {
                Toast.makeText(this, "Заполните все поля", Toast.LENGTH_SHORT).show();
                return;
            }

            // Пока просто имитация успешного входа:
            Toast.makeText(this, "Подключение к " + url, Toast.LENGTH_SHORT).show();

            // Переход на главный экран
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            startActivity(intent);
        });

        textRegister.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        });
    }
}