package com.project.smarthome.models;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.project.smarthome.LoginActivity;
import com.project.smarthome.R;
import com.project.smarthome.api.ApiService;
import com.project.smarthome.utils.SharedPrefManager;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RegisterActivity extends AppCompatActivity {

    private EditText editServerUrl, editUsername, editPassword, editConfirmPassword;
    private Button btnRegister;
    private TextView textLoginLink;

    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // --- Привязка элементов интерфейса ---
        editServerUrl = findViewById(R.id.editServerUrl);
        editUsername = findViewById(R.id.editUsername);
        editPassword = findViewById(R.id.editPassword);
        editConfirmPassword = findViewById(R.id.editConfirmPassword);
        btnRegister = findViewById(R.id.btnRegister);
        textLoginLink = findViewById(R.id.textLoginLink);

        // --- Переход на экран логина ---
        textLoginLink.setOnClickListener(v -> {
            startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
            finish();
        });

        // --- Обработка кнопки регистрации ---
        btnRegister.setOnClickListener(v -> {
            String serverUrl = editServerUrl.getText().toString().trim();
            String username = editUsername.getText().toString().trim();
            String password = editPassword.getText().toString().trim();
            String confirmPassword = editConfirmPassword.getText().toString().trim();

            if (serverUrl.isEmpty() || username.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Заполните все поля", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!password.equals(confirmPassword)) {
                Toast.makeText(this, "Пароли не совпадают", Toast.LENGTH_SHORT).show();
                return;
            }

            // --- Инициализация Retrofit ---
            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(serverUrl + "/") // "/" обязательно
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();

            apiService = retrofit.create(ApiService.class);

            // --- Отправка данных ---
            RegisterRequest request = new RegisterRequest(username, password);

            apiService.register(request).enqueue(new Callback<RegisterResponse>() {
                @Override
                public void onResponse(Call<RegisterResponse> call, Response<RegisterResponse> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        RegisterResponse registerResponse = response.body();

                        // Проверяем разные возможные форматы успешного ответа
                        boolean isSuccess = false;

                        if (registerResponse.getAccessToken() != null && !registerResponse.getAccessToken().isEmpty()) {
                            // Формат 1: есть access_token
                            SharedPrefManager.getInstance(RegisterActivity.this).saveToken(registerResponse.getAccessToken());
                            isSuccess = true;
                        } else if (registerResponse.isSuccess()) {
                            // Формат 2: есть флаг success
                            isSuccess = true;
                        } else if (registerResponse.getMessage() != null && registerResponse.getMessage().contains("success")) {
                            // Формат 3: сообщение об успехе
                            isSuccess = true;
                        }

                        if (isSuccess) {
                            Toast.makeText(RegisterActivity.this, "Регистрация успешна", Toast.LENGTH_SHORT).show();

                            // Переходим на логин или сразу в приложение
                            Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                            intent.putExtra("username", request.getUsername());
                            intent.putExtra("server_url", serverUrl);
                            startActivity(intent);
                            finish();
                        } else {
                            Toast.makeText(RegisterActivity.this, "Регистрация не удалась", Toast.LENGTH_SHORT).show();
                        }

                    } else {
                        // Обработка ошибок HTTP
                        String errorMessage = "Ошибка регистрации";
                        if (response.errorBody() != null) {
                            try {
                                errorMessage += ": " + response.errorBody().string();
                            } catch (Exception e) {
                                errorMessage += " (код: " + response.code() + ")";
                            }
                        }
                        Toast.makeText(RegisterActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                    }
                }

                @Override
                public void onFailure(Call<RegisterResponse> call, Throwable t) {
                    Toast.makeText(RegisterActivity.this, "Ошибка соединения: " + t.getMessage(), Toast.LENGTH_LONG).show();
                    t.printStackTrace(); // Для отладки
                }
            });
        });
    }
}
