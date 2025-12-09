package com.project.smarthome.ui;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.project.smarthome.R;
import com.project.smarthome.api.ApiClient;
import com.project.smarthome.api.ApiService;
import com.project.smarthome.models.homes.Home;
import com.project.smarthome.models.homes.HomeCreateRequest;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class CreateHomeFragment extends Fragment {

    private EditText homeNameEditText;
    private Button createButton;
    private ApiService apiService;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_create_home, container, false);

        homeNameEditText = view.findViewById(R.id.editTextHomeName);
        createButton = view.findViewById(R.id.buttonCreateHome);

        apiService = ApiClient.getApiService();

        createButton.setOnClickListener(v -> createHome());

        return view;
    }

    private void createHome() {
        String homeName = homeNameEditText.getText().toString().trim();
        if (TextUtils.isEmpty(homeName)) {
            Toast.makeText(getContext(), "Введите название дома", Toast.LENGTH_SHORT).show();
            return;
        }

        // Создаём DTO запроса
        HomeCreateRequest request = new HomeCreateRequest(homeName);

        // Токен берем из SharedPreferences или через синглтон UserSession
        String token = "Bearer " + "YOUR_JWT_TOKEN_HERE";

        apiService.createHome(token, request).enqueue(new Callback<Home>() {
            @Override
            public void onResponse(Call<Home> call, Response<Home> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Home createdHome = response.body();
                    Toast.makeText(getContext(),
                            "Дом создан: " + createdHome.getName(),
                            Toast.LENGTH_SHORT).show();
                    // Можно перейти к следующему экрану — добавление комнат
                } else {
                    Toast.makeText(getContext(),
                            "Ошибка при создании дома: " + response.code(),
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Home> call, Throwable t) {
                Toast.makeText(getContext(),
                        "Сетевая ошибка: " + t.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }
}