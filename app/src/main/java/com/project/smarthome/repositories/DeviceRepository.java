package com.project.smarthome.repositories;

import android.content.Context;
import android.util.Log;
import com.project.smarthome.api.ApiService;
import com.project.smarthome.api.RetrofitClient;
import com.project.smarthome.models.Device;
import com.project.smarthome.models.Home;
import com.project.smarthome.models.Room;
import com.project.smarthome.models.Notification;
import com.project.smarthome.utils.SharedPrefManager;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class DeviceRepository {
    private static final String TAG = "DeviceRepository";

    private final ApiService apiService;
    private final SharedPrefManager prefManager;
    private int currentHomeId = -1;
    private int currentRoomId = -1;

    public DeviceRepository(Context context) {
        this.apiService = RetrofitClient.getApiService();
        this.prefManager = SharedPrefManager.getInstance(context);
    }

    private String getAuthToken() {
        String token = prefManager.getToken();
        if (token == null) {
            Log.e(TAG, "No token found in SharedPreferences");
            return "";
        }
        return "Bearer " + token;
    }

    private boolean isTokenValid() {
        return prefManager.getToken() != null && !prefManager.getToken().isEmpty();
    }

    // Homes methods
    public CompletableFuture<List<Home>> getHomes() {
        CompletableFuture<List<Home>> future = new CompletableFuture<>();

        if (!isTokenValid()) {
            future.completeExceptionally(new Exception("User not authenticated"));
            return future;
        }

        Log.d(TAG, "Fetching homes...");
        apiService.getHomes(getAuthToken()).enqueue(new retrofit2.Callback<List<Home>>() {
            @Override
            public void onResponse(retrofit2.Call<List<Home>> call, retrofit2.Response<List<Home>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Home> homes = response.body();
                    Log.d(TAG, "Homes fetched successfully: " + homes.size() + " homes");

                    if (!homes.isEmpty()) {
                        currentHomeId = homes.get(0).getId();
                        Log.d(TAG, "Set current home ID: " + currentHomeId);
                    }
                    future.complete(homes);
                } else {
                    String errorMsg = "Failed to get homes. Code: " + response.code();
                    Log.e(TAG, errorMsg);
                    future.completeExceptionally(new Exception(errorMsg));
                }
            }

            @Override
            public void onFailure(retrofit2.Call<List<Home>> call, Throwable t) {
                Log.e(TAG, "Network error while fetching homes: " + t.getMessage());
                future.completeExceptionally(t);
            }
        });

        return future;
    }

    public CompletableFuture<Home> createHome(String homeName) {
        CompletableFuture<Home> future = new CompletableFuture<>();

        if (!isTokenValid()) {
            future.completeExceptionally(new Exception("User not authenticated"));
            return future;
        }

        Home home = new Home();
        home.setName(homeName);

        apiService.createHome(getAuthToken(), home).enqueue(new retrofit2.Callback<Home>() {
            @Override
            public void onResponse(retrofit2.Call<Home> call, retrofit2.Response<Home> response) {
                if (response.isSuccessful() && response.body() != null) {
                    future.complete(response.body());
                } else {
                    future.completeExceptionally(new Exception("Failed to create home"));
                }
            }

            @Override
            public void onFailure(retrofit2.Call<Home> call, Throwable t) {
                future.completeExceptionally(t);
            }
        });

        return future;
    }

    // Rooms methods
    public CompletableFuture<List<Room>> getRooms(int homeId) {
        CompletableFuture<List<Room>> future = new CompletableFuture<>();

        if (!isTokenValid()) {
            future.completeExceptionally(new Exception("User not authenticated"));
            return future;
        }

        Log.d(TAG, "Fetching rooms for home: " + homeId);
        apiService.getRooms(getAuthToken(), homeId).enqueue(new retrofit2.Callback<List<Room>>() {
            @Override
            public void onResponse(retrofit2.Call<List<Room>> call, retrofit2.Response<List<Room>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Room> rooms = response.body();
                    Log.d(TAG, "Rooms fetched successfully: " + rooms.size() + " rooms");

                    if (!rooms.isEmpty()) {
                        currentRoomId = rooms.get(0).getId();
                        Log.d(TAG, "Set current room ID: " + currentRoomId);
                    }
                    future.complete(rooms);
                } else {
                    String errorMsg = "Failed to get rooms. Code: " + response.code();
                    Log.e(TAG, errorMsg);
                    future.completeExceptionally(new Exception(errorMsg));
                }
            }

            @Override
            public void onFailure(retrofit2.Call<List<Room>> call, Throwable t) {
                Log.e(TAG, "Network error while fetching rooms: " + t.getMessage());
                future.completeExceptionally(t);
            }
        });

        return future;
    }

    public CompletableFuture<Room> createRoom(int homeId, String roomName) {
        CompletableFuture<Room> future = new CompletableFuture<>();

        if (!isTokenValid()) {
            future.completeExceptionally(new Exception("User not authenticated"));
            return future;
        }

        Room room = new Room();
        room.setName(roomName);

        apiService.createRoom(getAuthToken(), homeId, room).enqueue(new retrofit2.Callback<Room>() {
            @Override
            public void onResponse(retrofit2.Call<Room> call, retrofit2.Response<Room> response) {
                if (response.isSuccessful() && response.body() != null) {
                    future.complete(response.body());
                } else {
                    future.completeExceptionally(new Exception("Failed to create room"));
                }
            }

            @Override
            public void onFailure(retrofit2.Call<Room> call, Throwable t) {
                future.completeExceptionally(t);
            }
        });

        return future;
    }

    // Devices methods
    public CompletableFuture<List<Device>> getDevices(int roomId) {
        CompletableFuture<List<Device>> future = new CompletableFuture<>();

        if (!isTokenValid()) {
            future.completeExceptionally(new Exception("User not authenticated"));
            return future;
        }

        Log.d(TAG, "Fetching devices for room: " + roomId);
        apiService.getDevices(getAuthToken(), roomId).enqueue(new retrofit2.Callback<List<Device>>() {
            @Override
            public void onResponse(retrofit2.Call<List<Device>> call, retrofit2.Response<List<Device>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Device> devices = response.body();
                    Log.d(TAG, "Devices fetched successfully: " + devices.size() + " devices");
                    future.complete(devices);
                } else {
                    String errorMsg = "Failed to get devices. Code: " + response.code();
                    Log.e(TAG, errorMsg);
                    future.completeExceptionally(new Exception(errorMsg));
                }
            }

            @Override
            public void onFailure(retrofit2.Call<List<Device>> call, Throwable t) {
                Log.e(TAG, "Network error while fetching devices: " + t.getMessage());
                future.completeExceptionally(t);
            }
        });

        return future;
    }

    public CompletableFuture<Device> toggleDevice(int deviceId, boolean isOn) {
        CompletableFuture<Device> future = new CompletableFuture<>();

        if (!isTokenValid()) {
            future.completeExceptionally(new Exception("User not authenticated"));
            return future;
        }

        Log.d(TAG, "Toggling device: " + deviceId + " to " + (isOn ? "ON" : "OFF"));
        apiService.toggleDevice(getAuthToken(), deviceId).enqueue(new retrofit2.Callback<Device>() {
            @Override
            public void onResponse(retrofit2.Call<Device> call, retrofit2.Response<Device> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Device updatedDevice = response.body();
                    Log.d(TAG, "Device toggled successfully: " + updatedDevice.getName());
                    future.complete(updatedDevice);
                } else {
                    String errorMsg = "Failed to toggle device. Code: " + response.code();
                    Log.e(TAG, errorMsg);
                    future.completeExceptionally(new Exception(errorMsg));
                }
            }

            @Override
            public void onFailure(retrofit2.Call<Device> call, Throwable t) {
                Log.e(TAG, "Network error while toggling device: " + t.getMessage());
                future.completeExceptionally(t);
            }
        });

        return future;
    }

    // Notifications methods
    public CompletableFuture<List<Notification>> getNotifications() {
        CompletableFuture<List<Notification>> future = new CompletableFuture<>();

        if (!isTokenValid()) {
            future.completeExceptionally(new Exception("User not authenticated"));
            return future;
        }

        apiService.getNotifications(getAuthToken()).enqueue(new retrofit2.Callback<List<Notification>>() {
            @Override
            public void onResponse(retrofit2.Call<List<Notification>> call, retrofit2.Response<List<Notification>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    future.complete(response.body());
                } else {
                    future.completeExceptionally(new Exception("Failed to get notifications"));
                }
            }

            @Override
            public void onFailure(retrofit2.Call<List<Notification>> call, Throwable t) {
                future.completeExceptionally(t);
            }
        });

        return future;
    }

    // Utility methods
    public CompletableFuture<Boolean> turnAllDevices(boolean turnOn) {
        CompletableFuture<Boolean> future = new CompletableFuture<>();

        // Since the API doesn't have a "turn all" endpoint, we'll simulate it
        // by toggling each device individually
        getDevices(currentRoomId).thenAccept(devices -> {
            List<CompletableFuture<Device>> futures = devices.stream()
                    .filter(device -> device.getState().isOn() != turnOn)
                    .map(device -> toggleDevice(device.getId(), turnOn))
                    .collect(java.util.stream.Collectors.toList());

            // Wait for all operations to complete
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                    .thenRun(() -> future.complete(true))
                    .exceptionally(throwable -> {
                        future.completeExceptionally(throwable);
                        return null;
                    });
        }).exceptionally(throwable -> {
            future.completeExceptionally(throwable);
            return null;
        });

        return future;
    }

    public CompletableFuture<Boolean> enableSecurityMode() {
        CompletableFuture<Boolean> future = new CompletableFuture<>();

        // Enable all security-related devices (sirens, motion sensors)
        getDevices(currentRoomId).thenAccept(devices -> {
            List<CompletableFuture<Device>> futures = devices.stream()
                    .filter(device -> "siren".equals(device.getType()) || "motion_sensor".equals(device.getType()))
                    .map(device -> toggleDevice(device.getId(), true))
                    .collect(java.util.stream.Collectors.toList());

            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                    .thenRun(() -> future.complete(true))
                    .exceptionally(throwable -> {
                        future.completeExceptionally(throwable);
                        return null;
                    });
        }).exceptionally(throwable -> {
            future.completeExceptionally(throwable);
            return null;
        });

        return future;
    }

    // Getters for current state
    public int getCurrentHomeId() {
        return currentHomeId;
    }

    public int getCurrentRoomId() {
        return currentRoomId;
    }

    public void setCurrentHomeId(int homeId) {
        this.currentHomeId = homeId;
    }

    public void setCurrentRoomId(int roomId) {
        this.currentRoomId = roomId;
    }

    public boolean isAuthenticated() {
        return isTokenValid();
    }
}