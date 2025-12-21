package com.project.smarthome.repositories;

import android.content.Context;

import com.project.smarthome.api.ApiClient;
import com.project.smarthome.api.ApiService;
import com.project.smarthome.models.devices.Device;
import com.project.smarthome.models.homes.Home;
import com.project.smarthome.models.homes.HomeCreateRequest;
import com.project.smarthome.models.homes.HomeResponse;
import com.project.smarthome.models.homes.room.Room;
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
    private final Context context;

    public DeviceRepository(Context context) {
        this.context = context.getApplicationContext();
        ApiClient.initialize(this.context);
        this.apiService = ApiClient.getApiService();
        this.sharedPrefManager = SharedPrefManager.getInstance(this.context);
    }

    // Проверка авторизации
    public boolean isAuthenticated() {
        return sharedPrefManager.isLoggedIn();
    }

    // Получение домов
    public CompletableFuture<List<Home>> getHomes() {
        CompletableFuture<List<Home>> future = new CompletableFuture<>();

        if (!isAuthenticated()) {
            future.completeExceptionally(new Exception("Пользователь не авторизован"));
            return future;
        }

        apiService.getMyHomes().enqueue(new Callback<List<Home>>() {
            @Override
            public void onResponse(Call<List<Home>> call, Response<List<Home>> response) {
                if (response.isSuccessful()) {
                    future.complete(response.body());
                } else {
                    String errorMsg = "Ошибка " + response.code();
                    if (response.code() == 401) {
                        errorMsg = "Требуется повторная авторизация";
                    }
                    future.completeExceptionally(new Exception(errorMsg));
                }
            }

            @Override
            public void onFailure(Call<List<Home>> call, Throwable t) {
                future.completeExceptionally(new Exception("Ошибка сети: " + t.getMessage()));
            }
        });

        return future;
    }

    // Получение комнат для дома
    public CompletableFuture<List<Room>> getRooms(int homeId) {
        CompletableFuture<List<Room>> future = new CompletableFuture<>();

        if (!isAuthenticated()) {
            future.completeExceptionally(new Exception("Пользователь не авторизован"));
            return future;
        }

        apiService.getRooms(homeId).enqueue(new Callback<List<Room>>() {
            @Override
            public void onResponse(Call<List<Room>> call, Response<List<Room>> response) {
                if (response.isSuccessful()) {
                    future.complete(response.body());
                } else {
                    String errorMsg = "Ошибка " + response.code();
                    if (response.code() == 404) {
                        errorMsg = "Дом не найден";
                    }
                    future.completeExceptionally(new Exception(errorMsg));
                }
            }

            @Override
            public void onFailure(Call<List<Room>> call, Throwable t) {
                future.completeExceptionally(new Exception("Ошибка сети: " + t.getMessage()));
            }
        });

        return future;
    }

    // Получение устройств для дома
    public CompletableFuture<List<Device>> getDevices(int homeId) {
        CompletableFuture<List<Device>> future = new CompletableFuture<>();

        if (!isAuthenticated()) {
            future.completeExceptionally(new Exception("Пользователь не авторизован"));
            return future;
        }

        apiService.getDevices(homeId).enqueue(new Callback<List<Device>>() {
            @Override
            public void onResponse(Call<List<Device>> call, Response<List<Device>> response) {
                if (response.isSuccessful()) {
                    future.complete(response.body());
                } else {
                    String errorMsg = "Ошибка " + response.code();
                    if (response.code() == 404) {
                        errorMsg = "Дом не найден";
                    }
                    future.completeExceptionally(new Exception(errorMsg));
                }
            }

            @Override
            public void onFailure(Call<List<Device>> call, Throwable t) {
                future.completeExceptionally(new Exception("Ошибка сети: " + t.getMessage()));
            }
        });

        return future;
    }

    // Управление устройством
    public CompletableFuture<Map<String, Object>> controlDevice(int deviceId, String newState) {
        CompletableFuture<Map<String, Object>> future = new CompletableFuture<>();

        if (!isAuthenticated()) {
            future.completeExceptionally(new Exception("Пользователь не авторизован"));
            return future;
        }

        apiService.controlDevice(deviceId, newState).enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                if (response.isSuccessful()) {
                    future.complete(response.body());
                } else {
                    String errorMsg = "Ошибка " + response.code();
                    switch (response.code()) {
                        case 403:
                            errorMsg = "Нет доступа к устройству";
                            break;
                        case 404:
                            errorMsg = "Устройство не найдено";
                            break;
                    }
                    future.completeExceptionally(new Exception(errorMsg));
                }
            }

            @Override
            public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                future.completeExceptionally(new Exception("Ошибка сети: " + t.getMessage()));
            }
        });

        return future;
    }

    // Получение информации об устройстве
    public CompletableFuture<Device> getDevice(int deviceId) {
        CompletableFuture<Device> future = new CompletableFuture<>();

        if (!isAuthenticated()) {
            future.completeExceptionally(new Exception("Пользователь не авторизован"));
            return future;
        }

        apiService.getDevice(deviceId).enqueue(new Callback<Device>() {
            @Override
            public void onResponse(Call<Device> call, Response<Device> response) {
                if (response.isSuccessful()) {
                    future.complete(response.body());
                } else {
                    future.completeExceptionally(new Exception("Ошибка " + response.code()));
                }
            }

            @Override
            public void onFailure(Call<Device> call, Throwable t) {
                future.completeExceptionally(new Exception("Ошибка сети: " + t.getMessage()));
            }
        });

        return future;
    }

    // Создание комнаты
    public CompletableFuture<Room> createRoom(int homeId, String roomName) {
        CompletableFuture<Room> future = new CompletableFuture<>();

        if (!isAuthenticated()) {
            future.completeExceptionally(new Exception("Пользователь не авторизован"));
            return future;
        }

        Room room = new Room();
        room.setName(roomName);

        apiService.createRoom(homeId, room).enqueue(new Callback<Room>() {
            @Override
            public void onResponse(Call<Room> call, Response<Room> response) {
                if (response.isSuccessful()) {
                    future.complete(response.body());
                } else {
                    future.completeExceptionally(new Exception("Ошибка " + response.code()));
                }
            }

            @Override
            public void onFailure(Call<Room> call, Throwable t) {
                future.completeExceptionally(new Exception("Ошибка сети: " + t.getMessage()));
            }
        });

        return future;
    }

    // Создание дома
    public void createHome(String homeName, RepositoryCallback<HomeResponse> callback) {

        HomeCreateRequest request = new HomeCreateRequest(homeName);

        apiService.createHome(request)
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


    // Проверка соединения с сервером
    public CompletableFuture<Map<String, String>> pingServer() {
        CompletableFuture<Map<String, String>> future = new CompletableFuture<>();

        apiService.ping().enqueue(new Callback<Map<String, String>>() {
            @Override
            public void onResponse(Call<Map<String, String>> call, Response<Map<String, String>> response) {
                if (response.isSuccessful()) {
                    future.complete(response.body());
                } else {
                    future.completeExceptionally(new Exception("Сервер недоступен: " + response.code()));
                }
            }

            @Override
            public void onFailure(Call<Map<String, String>> call, Throwable t) {
                future.completeExceptionally(new Exception("Ошибка сети: " + t.getMessage()));
            }
        });

        return future;
    }
}