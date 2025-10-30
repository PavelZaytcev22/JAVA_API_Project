package com.project.smarthome;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.project.smarthome.api.ApiService;
import com.project.smarthome.models.LoginRequest;
import com.project.smarthome.models.TokenResponse;
import com.project.smarthome.utils.SharedPrefManager;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class LoginActivity extends AppCompatActivity {

    private EditText editServerUrl, editUsername, editPassword;
    private Button btnLogin;
    private TextView textRegister;
    private ApiService apiService;

    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        editServerUrl = findViewById(R.id.editServerUrl);
        editUsername = findViewById(R.id.editUsername);
        editPassword = findViewById(R.id.editPassword);
        btnLogin = findViewById(R.id.btnLogin);
        textRegister = findViewById(R.id.textRegister);

        textRegister.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
            finish();
        });

        btnLogin.setOnClickListener(v -> loginUser());
    }

    private void loginUser() {
        String serverUrl = editServerUrl.getText().toString().trim();
        String username = editUsername.getText().toString().trim();
        String password = editPassword.getText().toString().trim();

        if (serverUrl.isEmpty() || username.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Заполните все обязательные поля", Toast.LENGTH_SHORT).show();
            return;
        }

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(serverUrl + "/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        apiService = retrofit.create(ApiService.class);

        LoginRequest request = new LoginRequest(username, password,"user@mail.ru");

        // Асинхронный вызов через Executor
        executor.execute(() -> {
            try {
                Response<TokenResponse> response = apiService.login(request).execute(); // синхронный вызов в отдельном потоке
                runOnUiThread(() -> handleLoginResponse(response, serverUrl, username));
            } catch (Exception e) {
                runOnUiThread(() -> {
                    Toast.makeText(LoginActivity.this, "Ошибка соединения: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    e.printStackTrace();
                });
            }
        });
    }

    private void handleLoginResponse(Response<TokenResponse> response, String serverUrl, String username) {
        if (response.isSuccessful() && response.body() != null) {
            String token = response.body().getAccessToken();

            SharedPrefManager.getInstance(LoginActivity.this).saveToken(token);
            SharedPrefManager.getInstance(LoginActivity.this).saveServerUrl(serverUrl);
            SharedPrefManager.getInstance(LoginActivity.this).saveUsername(username);

            Toast.makeText(LoginActivity.this, "Вход успешен!", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(LoginActivity.this, MainActivity.class));
            finish();
        } else {
            if (!response.isSuccessful())
                Toast.makeText(LoginActivity.this, "провал" + response.isSuccessful(), Toast.LENGTH_SHORT).show();

        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.shutdown(); // корректное завершение Executor
    }
}
