package com.project.smarthome;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import com.project.smarthome.api.ApiClient;
import com.project.smarthome.api.ApiService;
import com.project.smarthome.models.RegisterRequest;
import com.project.smarthome.models.RegisterResponse;
import com.project.smarthome.utils.SharedPrefManager;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegisterActivity extends AppCompatActivity {

    private EditText editUsername, editEmail, editPassword, editConfirmPassword, editFullName;
    private Button btnRegister;
    private ProgressBar progressBar;
    private ApiService apiService;
    private SharedPrefManager sharedPrefManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        initViews();
        initDependencies();
        setupClickListeners();

        // Автозаполнение из Intent если есть
        Intent intent = getIntent();
        if (intent.hasExtra("username")) {
            editUsername.setText(intent.getStringExtra("username"));
        }
    }

    private void initViews() {
        editUsername = findViewById(R.id.editUsername);
        editEmail = findViewById(R.id.editEmail);
        editPassword = findViewById(R.id.editPassword);
        editConfirmPassword = findViewById(R.id.editConfirmPassword);
        editFullName = findViewById(R.id.editFullName);
        btnRegister = findViewById(R.id.btnRegister);
        progressBar = findViewById(R.id.progressBar);
    }

    private void initDependencies() {
        ApiClient.initialize(this);
        sharedPrefManager = SharedPrefManager.getInstance(this);
        apiService = ApiClient.getApiService();
    }

    private void setupClickListeners() {
        TextView textLoginLink = findViewById(R.id.textLoginLink);
        textLoginLink.setOnClickListener(v -> {
            startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
            finish();
        });

        btnRegister.setOnClickListener(v -> attemptRegister());
    }

    private void attemptRegister() {
        String username = editUsername.getText().toString().trim();
        String email = editEmail.getText().toString().trim();
        String password = editPassword.getText().toString().trim();
        String confirmPassword = editConfirmPassword.getText().toString().trim();
        String fullName = editFullName.getText().toString().trim();

        if (validateInput(username, email, password, confirmPassword, fullName)) {
            performRegistration(username, email, password, fullName);
        }
    }

    private boolean validateInput(String username, String email, String password, String confirmPassword, String fullName) {
        if (username.isEmpty()) {
            showError(editUsername, "Введите имя пользователя");
            return false;
        }

        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            showError(editEmail, "Введите корректный email");
            return false;
        }

        if (password.isEmpty() || password.length() < 4) {
            showError(editPassword, "Пароль должен содержать минимум 4 символа");
            return false;
        }

        if (!password.equals(confirmPassword)) {
            showError(editConfirmPassword, "Пароли не совпадают");
            return false;
        }

        if (fullName.isEmpty()) {
            showError(editFullName, "Введите полное имя");
            return false;
        }

        return true;
    }

    private void showError(EditText editText, String message) {
        editText.setError(message);
        editText.requestFocus();
    }

    private void performRegistration(String username, String email, String password, String fullName) {
        showLoading(true);

        RegisterRequest request = new RegisterRequest(username, password, email, fullName);

        apiService.register(request).enqueue(new Callback<RegisterResponse>() {
            @Override
            public void onResponse(Call<RegisterResponse> call, Response<RegisterResponse> response) {
                showLoading(false);

                if (response.isSuccessful() && response.body() != null) {
                    handleSuccessfulRegistration(username);
                } else {
                    handleRegistrationError("Ошибка регистрации: " + getErrorMessage(response));
                }
            }

            @Override
            public void onFailure(Call<RegisterResponse> call, Throwable t) {
                showLoading(false);
                handleRegistrationError("Ошибка соединения: " + t.getMessage());
            }
        });
    }

    private void handleSuccessfulRegistration(String username) {
        Toast.makeText(RegisterActivity.this, "Регистрация успешна!", Toast.LENGTH_SHORT).show();

        // Переходим на экран логина с заполненным username
        Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
        intent.putExtra("username", username);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }

    private void handleRegistrationError(String errorMessage) {
        Toast.makeText(RegisterActivity.this, errorMessage, Toast.LENGTH_LONG).show();
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
        btnRegister.setEnabled(!show);
        editUsername.setEnabled(!show);
        editEmail.setEnabled(!show);
        editPassword.setEnabled(!show);
        editConfirmPassword.setEnabled(!show);
        editFullName.setEnabled(!show);
    }
}