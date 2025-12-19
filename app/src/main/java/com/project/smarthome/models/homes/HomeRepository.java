package com.project.smarthome.repositories;

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

    public HomeRepository(ApiService apiService, SharedPrefManager sharedPrefManager) {
        this.apiService = apiService;
        this.sharedPrefManager = sharedPrefManager;
    }

    /**
     * Получение домов пользователя
     */
    public void getHomes(RepositoryCallback<List<HomeResponse>> callback) {

        apiService.getMyHomes()
                .enqueue(new Callback<List<HomeResponse>>() {

                    @Override
                    public void onResponse(
                            Call<List<HomeResponse>> call,
                            Response<List<HomeResponse>> response
                    ) {
                        if (response.isSuccessful() && response.body() != null) {
                            callback.onSuccess(response.body());
                        } else {
                            callback.onError("Ошибка сервера: " + response.code());
                        }
                    }

                    @Override
                    public void onFailure(Call<List<HomeResponse>> call, Throwable t) {
                        Log.e(TAG, "getHomes failed", t);
                        callback.onError("Ошибка сети: " + t.getMessage());
                    }
                });
    }


    /**
     * Создание дома
     */
    public void createHome(String homeName, RepositoryCallback<HomeResponse> callback) {

        if (homeName == null || homeName.trim().isEmpty()) {
            callback.onError("Имя дома не может быть пустым");
            return;
        }

        String token = sharedPrefManager.getToken();
        if (token == null) {
            callback.onError("Пользователь не авторизован");
            return;
        }

        HomeCreateRequest request = new HomeCreateRequest(homeName);

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
                            callback.onError("Ошибка создания дома: " + response.code());
                        }
                    }

                    @Override
                    public void onFailure(Call<HomeResponse> call, Throwable t) {
                        callback.onError("Ошибка сети: " + t.getMessage());
                    }
                });
    }

    /**
     * Callback интерфейс
     */
    public interface RepositoryCallback<T> {
        void onSuccess(T data);
        void onError(String errorMessage);
    }
}
