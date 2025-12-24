package com.project.smarthome.repositories;

import android.content.Context;
import android.util.Log;

import com.project.smarthome.api.ApiClient;
import com.project.smarthome.api.ApiService;
import com.project.smarthome.models.devices.Device;
import com.project.smarthome.models.devices.DeviceCreateRequest;
import com.project.smarthome.utils.SharedPrefManager;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DeviceRepository {

    private static final String TAG = "DeviceRepository";

    private final ApiService apiService;
    private final SharedPrefManager sharedPrefManager;

    public DeviceRepository(Context context) {
        this.apiService = ApiClient.getApiService();
        this.sharedPrefManager = SharedPrefManager.getInstance(context);
    }

    /* ==========================
       Авторизация
       ========================== */

    public boolean isAuthenticated() {
        String token = sharedPrefManager.getToken();
        return token != null && !token.isEmpty();
    }

    private String authHeader() {
        return "Bearer " + sharedPrefManager.getToken();
    }

    /* ==========================
       Устройства
       ========================== */

    /**
     * Получить список устройств дома
     * GET /api/devices/homes/{home_id}
     */
    public CompletableFuture<List<Device>> getDevices(int homeId) {
        CompletableFuture<List<Device>> future = new CompletableFuture<>();

        if (!isAuthenticated()) {
            future.completeExceptionally(
                    new IllegalStateException("Пользователь не авторизован")
            );
            return future;
        }

        apiService.getDevices(homeId)
                .enqueue(new Callback<List<Device>>() {
                    @Override
                    public void onResponse(
                            Call<List<Device>> call,
                            Response<List<Device>> response
                    ) {
                        if (response.isSuccessful() && response.body() != null) {
                            future.complete(response.body());
                        } else {
                            future.completeExceptionally(
                                    new RuntimeException(
                                            "Ошибка получения устройств: " + response.code()
                                    )
                            );
                        }
                    }

                    @Override
                    public void onFailure(Call<List<Device>> call, Throwable t) {
                        Log.e(TAG, "getDevices failed", t);
                        future.completeExceptionally(t);
                    }
                });

        return future;
    }

    /**
     * Получить одно устройство
     * GET /api/devices/{device_id}
     */
    public CompletableFuture<Device> getDevice(int deviceId) {
        CompletableFuture<Device> future = new CompletableFuture<>();

        if (!isAuthenticated()) {
            future.completeExceptionally(
                    new IllegalStateException("Пользователь не авторизован")
            );
            return future;
        }

        apiService.getDevice(deviceId)
                .enqueue(new Callback<Device>() {
                    @Override
                    public void onResponse(
                            Call<Device> call,
                            Response<Device> response
                    ) {
                        if (response.isSuccessful() && response.body() != null) {
                            future.complete(response.body());
                        } else {
                            future.completeExceptionally(
                                    new RuntimeException(
                                            "Устройство не найдено: " + response.code()
                                    )
                            );
                        }
                    }

                    @Override
                    public void onFailure(Call<Device> call, Throwable t) {
                        Log.e(TAG, "getDevice failed", t);
                        future.completeExceptionally(t);
                    }
                });

        return future;
    }

    /**
     * Создание устройства
     * POST /api/devices/homes/{home_id}
     */
    public CompletableFuture<Device> createDevice(
            int homeId,
            DeviceCreateRequest request
    ) {
        CompletableFuture<Device> future = new CompletableFuture<>();

        if (!isAuthenticated()) {
            future.completeExceptionally(
                    new IllegalStateException("Пользователь не авторизован")
            );
            return future;
        }

        apiService.createDevice(homeId, request)
                .enqueue(new Callback<Device>() {
                    @Override
                    public void onResponse(
                            Call<Device> call,
                            Response<Device> response
                    ) {
                        if (response.isSuccessful() && response.body() != null) {
                            future.complete(response.body());
                        } else {
                            future.completeExceptionally(
                                    new RuntimeException(
                                            "Ошибка создания устройства: " + response.code()
                                    )
                            );
                        }
                    }

                    @Override
                    public void onFailure(Call<Device> call, Throwable t) {
                        Log.e(TAG, "createDevice failed", t);
                        future.completeExceptionally(t);
                    }
                });

        return future;
    }

    /**
     * Управление устройством
     * POST /api/devices/{device_id}/action?new_state=...
     */
    public CompletableFuture<Map<String, Object>> controlDevice(
            int deviceId,
            String newState
    ) {
        CompletableFuture<Map<String, Object>> future = new CompletableFuture<>();

        if (!isAuthenticated()) {
            future.completeExceptionally(
                    new IllegalStateException("Пользователь не авторизован")
            );
            return future;
        }

        apiService.controlDevice(deviceId, newState)
                .enqueue(new Callback<Map<String, Object>>() {
                    @Override
                    public void onResponse(
                            Call<Map<String, Object>> call,
                            Response<Map<String, Object>> response
                    ) {
                        if (response.isSuccessful() && response.body() != null) {
                            future.complete(response.body());
                        } else {
                            future.completeExceptionally(
                                    new RuntimeException(
                                            "Ошибка управления устройством: " + response.code()
                                    )
                            );
                        }
                    }

                    @Override
                    public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                        Log.e(TAG, "controlDevice failed", t);
                        future.completeExceptionally(t);
                    }
                });

        return future;
    }
}
