package com.project.smarthome.models.homes.room;

import android.content.Context;

import androidx.annotation.NonNull;

import com.project.smarthome.api.ApiService;
import com.project.smarthome.utils.SharedPrefManager;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RoomRepositoryImpl implements RoomRepository {

    private final ApiService apiService;
    private final SharedPrefManager sharedPrefManager;

    public RoomRepositoryImpl(
            @NonNull Context context,
            @NonNull ApiService apiService
    ) {
        this.apiService = apiService;
        this.sharedPrefManager = SharedPrefManager.getInstance(context);
    }

    public RoomRepositoryImpl() {

    }

    /* ===== Загрузка комнат ===== */

    @Override
    public void loadRooms(
            int homeId,
            @NonNull LoadRoomsCallback callback
    ) {
        String token = sharedPrefManager.getToken();

        if (token == null || token.isEmpty()) {
            callback.onError(
                    new IllegalStateException("Пользователь не авторизован")
            );
            return;
        }

        apiService.getRooms("Bearer " + token, homeId)
                .enqueue(new Callback<List<RoomResponse>>() {

                    @Override
                    public void onResponse(
                            @NonNull Call<List<RoomResponse>> call,
                            @NonNull Response<List<RoomResponse>> response
                    ) {
                        if (!response.isSuccessful() || response.body() == null) {
                            callback.onError(
                                    new RuntimeException(
                                            "Ошибка загрузки комнат: " + response.code()
                                    )
                            );
                            return;
                        }

                        List<Room> rooms = new ArrayList<>();
                        for (RoomResponse roomResponse : response.body()) {
                            rooms.add(Room.fromResponse(roomResponse));
                        }

                        callback.onSuccess(rooms);
                    }

                    @Override
                    public void onFailure(
                            @NonNull Call<List<RoomResponse>> call,
                            @NonNull Throwable t
                    ) {
                        callback.onError(t);
                    }
                });
    }

    /* ===== Создание комнаты ===== */

    @Override
    public void createRoom(
            int homeId,
            @NonNull String name,
            @NonNull CreateRoomCallback callback
    ) {
        String token = sharedPrefManager.getToken();

        if (token == null || token.isEmpty()) {
            callback.onError(
                    new IllegalStateException("Пользователь не авторизован")
            );
            return;
        }

        RoomCreateRequest request = new RoomCreateRequest(name);

        apiService.createRoom(
                "Bearer " + token,
                homeId,
                request
        ).enqueue(new Callback<RoomResponse>() {

            @Override
            public void onResponse(
                    @NonNull Call<RoomResponse> call,
                    @NonNull Response<RoomResponse> response
            ) {
                if (!response.isSuccessful() || response.body() == null) {
                    callback.onError(
                            new RuntimeException(
                                    "Ошибка создания комнаты: " + response.code()
                            )
                    );
                    return;
                }

                callback.onSuccess(
                        Room.fromResponse(response.body())
                );
            }

            @Override
            public void onFailure(
                    @NonNull Call<RoomResponse> call,
                    @NonNull Throwable t
            ) {
                callback.onError(t);
            }
        });
    }
}
