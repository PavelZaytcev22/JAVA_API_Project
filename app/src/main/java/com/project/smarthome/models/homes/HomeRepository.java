package com.project.smarthome.models.homes;

import android.util.Log;

import com.project.smarthome.api.ApiService;
import com.project.smarthome.models.homes.HomeCreateRequest;
import com.project.smarthome.models.homes.HomeResponse;
import com.project.smarthome.utils.SharedPrefManager;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeRepository {

    private static final String TAG = "HomeRepository";

    private final ApiService apiService;
    private final SharedPrefManager sharedPrefManager;

    public HomeRepository(ApiService apiService,
                          SharedPrefManager sharedPrefManager) {
        this.apiService = apiService;
        this.sharedPrefManager = sharedPrefManager;
    }

    /**
     * Получение списка домов текущего пользователя
     */
    public void getHomes(RepositoryCallback<List<HomeResponse>> callback) {

        String token = sharedPrefManager.getToken();
        if (token == null || token.isEmpty()) {
            callback.onError("Пользователь не авторизован");
            return;
        }

        apiService.getMyHomes("Bearer " + token)
                .enqueue(new Callback<List<HomeResponse>>() {

                    @Override
                    public void onResponse(
                            Call<List<HomeResponse>> call,
                            Response<List<HomeResponse>> response
                    ) {
                        if (response.isSuccessful() && response.body() != null) {
                            callback.onSuccess(response.body());
                        } else {
                            callback.onError(
                                    "Ошибка получения домов: " + response.code()
                            );
                        }
                    }

                    @Override
                    public void onFailure(
                            Call<List<HomeResponse>> call,
                            Throwable t
                    ) {
                        Log.e(TAG, "getHomes failed", t);
                        callback.onError("Ошибка сети: " + t.getMessage());
                    }
                });
    }

    /**
     * Создание нового дома
     */
    public void createHome(String homeName,
                           RepositoryCallback<HomeResponse> callback) {

        if (homeName == null || homeName.trim().isEmpty()) {
            callback.onError("Имя дома не может быть пустым");
            return;
        }

        String token = sharedPrefManager.getToken();
        if (token == null || token.isEmpty()) {
            callback.onError("Пользователь не авторизован");
            return;
        }

        HomeCreateRequest request =
                new HomeCreateRequest(homeName.trim());

        apiService.createHome("Bearer " + token, request)
                .enqueue(new Callback<HomeResponse>() {

                    @Override
                    public void onResponse(
                            Call<HomeResponse> call,
                            Response<HomeResponse> response
                    ) {
                        if (response.isSuccessful() && response.body() != null) {
                            callback.onSuccess(response.body());
                        } else {
                            callback.onError(
                                    "Ошибка создания дома: " + response.code()
                            );
                        }
                    }

                    @Override
                    public void onFailure(
                            Call<HomeResponse> call,
                            Throwable t
                    ) {
                        Log.e(TAG, "createHome failed", t);
                        callback.onError("Ошибка сети: " + t.getMessage());
                    }
                });
    }

    /**
     * Универсальный callback для Repository
     */
    public interface RepositoryCallback<T> {
        void onSuccess(T data);
        void onError(String errorMessage);
    }
}
