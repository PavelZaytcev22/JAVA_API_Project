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

public class LoginActivity extends AppCompatActivity {

    private EditText editUsername, editPassword;
    private Button btnLogin;
    private ProgressBar progressBar;
    private ApiService apiService;
    private SharedPrefManager sharedPrefManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        initViews();
        initDependencies();
        setupClickListeners();

        // Автозаполнение username если есть
        String savedUsername = sharedPrefManager.getUsername();
        if (savedUsername != null) {
            editUsername.setText(savedUsername);
        }
    }

    private void initViews() {
        editUsername = findViewById(R.id.editUsername);
        editPassword = findViewById(R.id.editPassword);
        btnLogin = findViewById(R.id.btnLogin);
        progressBar = findViewById(R.id.progressBar);
    }

    private void initDependencies() {
        ApiClient.initialize(this);
        sharedPrefManager = SharedPrefManager.getInstance(this);
        apiService = ApiClient.getApiService();
    }

    private void setupClickListeners() {
        TextView textRegister = findViewById(R.id.textRegister);
        textRegister.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
        });

        btnLogin.setOnClickListener(v -> loginUser());
    }

    private void loginUser() {
        String username = editUsername.getText().toString().trim();
        String password = editPassword.getText().toString().trim();

        if (validateInput(username, password)) {
            performLogin(username, password);
        }
    }

    private boolean validateInput(String username, String password) {
        if (username.isEmpty()) {
            editUsername.setError("Введите имя пользователя");
            editUsername.requestFocus();
            return false;
        }

        if (password.isEmpty()) {
            editPassword.setError("Введите пароль");
            editPassword.requestFocus();
            return false;
        }

        if (password.length() < 4) {
            editPassword.setError("Пароль должен содержать минимум 4 символа");
            editPassword.requestFocus();
            return false;
        }

        return true;
    }

    private void performLogin(String username, String password) {
        showLoading(true);

        LoginRequest request = new LoginRequest(username, password);

        apiService.login(request).enqueue(new Callback<TokenResponse>() {
            @Override
            public void onResponse(Call<TokenResponse> call, Response<TokenResponse> response) {
                showLoading(false);

                if (response.isSuccessful() && response.body() != null) {
                    handleSuccessfulLogin(response.body(), username);
                } else {
                    handleLoginError("Ошибка авторизации: " + getErrorMessage(response));
                }
            }

            @Override
            public void onFailure(Call<TokenResponse> call, Throwable t) {
                showLoading(false);
                handleLoginError("Сервер недоступен: " + t.getMessage());
            }
        });
    }

    private void handleSuccessfulLogin(TokenResponse tokenResponse, String username) {
        String token = tokenResponse.getAccessToken();

        if (token != null && !token.isEmpty()) {
            // Сохраняем сессию
            sharedPrefManager.saveSession(token, username);

            Toast.makeText(LoginActivity.this, "Вход выполнен успешно", Toast.LENGTH_SHORT).show();

            // Переходим на главный экран
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        } else {
            handleLoginError("Неверный ответ от сервера");
        }
    }

    private void handleLoginError(String errorMessage) {
        Toast.makeText(LoginActivity.this, errorMessage, Toast.LENGTH_LONG).show();
    }

    private String getErrorMessage(Response<?> response) {
        try {
            if (response.errorBody() != null) {
                return response.errorBody().string();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "Код ошибки: " + response.code();
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        btnLogin.setEnabled(!show);
        editUsername.setEnabled(!show);
        editPassword.setEnabled(!show);
    }
}