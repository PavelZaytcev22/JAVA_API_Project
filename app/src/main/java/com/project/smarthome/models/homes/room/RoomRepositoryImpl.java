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
    private final SharedPrefManager prefManager;

    public RoomRepositoryImpl(
            @NonNull Context context,
            @NonNull ApiService apiService
    ) {
        this.apiService = apiService;
        this.prefManager = SharedPrefManager.getInstance(context);
    }

    @Override
    public void loadRooms(@NonNull LoadRoomsCallback callback) {
        long homeId = prefManager.getActiveHomeId();
        String token = prefManager.getToken();

        if (homeId == -1 || token == null) {
            callback.onError(
                    new IllegalStateException("Active home or token not found")
            );
            return;
        }

        apiService.getRooms(
                "Bearer " + token,
                (int) homeId
        ).enqueue(new Callback<List<RoomResponse>>() {
            @Override
            public void onResponse(
                    @NonNull Call<List<RoomResponse>> call,
                    @NonNull Response<List<RoomResponse>> response
            ) {
                if (!response.isSuccessful() || response.body() == null) {
                    callback.onError(
                            new RuntimeException("Failed to load rooms: " + response.code())
                    );
                    return;
                }

                List<Room> result = new ArrayList<>();
                for (RoomResponse roomResponse : response.body()) {
                    result.add(Room.fromResponse(roomResponse));
                }

                callback.onSuccess(result);
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

    @Override
    public void createRoom(
            @NonNull String name,
            @NonNull CreateRoomCallback callback
    ) {
        long homeId = prefManager.getActiveHomeId();
        String token = prefManager.getToken();

        if (homeId == -1 || token == null) {
            callback.onError(
                    new IllegalStateException("Active home or token not found")
            );
            return;
        }

        RoomCreateRequest request = new RoomCreateRequest(name);

        apiService.createRoom(
                "Bearer " + token,
                (int) homeId,
                request
        ).enqueue(new Callback<RoomResponse>() {
            @Override
            public void onResponse(
                    @NonNull Call<RoomResponse> call,
                    @NonNull Response<RoomResponse> response
            ) {
                if (!response.isSuccessful() || response.body() == null) {
                    callback.onError(
                            new RuntimeException("Failed to create room: " + response.code())
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

