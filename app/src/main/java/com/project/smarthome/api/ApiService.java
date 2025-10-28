package com.project.smarthome.api;

import com.project.smarthome.models.RegisterRequest;
import com.project.smarthome.models.RegisterResponse;
import com.project.smarthome.models.*;
import retrofit2.Call;
import retrofit2.http.*;

import java.util.List;

public interface ApiService {

    // Auth endpoints
    @POST("/api/auth/register")
    Call<RegisterResponse> register(@Body RegisterRequest request); // Изменили имя метода

    @POST("api/auth/token")
    Call<TokenResponse> login(@Body LoginRequest request);

    // Homes endpoints
    @GET("homes/")
    Call<List<Home>> getHomes(@Header("Authorization") String token);

    @POST("homes/")
    Call<Home> createHome(@Header("Authorization") String token, @Body Home home);

    // Rooms endpoints
    @GET("homes/{home_id}/rooms")
    Call<List<Room>> getRooms(@Header("Authorization") String token, @Path("home_id") int homeId);

    @POST("homes/{home_id}/rooms")
    Call<Room> createRoom(@Header("Authorization") String token, @Path("home_id") int homeId, @Body Room room);

    // Devices endpoints
    @GET("rooms/{room_id}/devices")
    Call<List<Device>> getDevices(@Header("Authorization") String token, @Path("room_id") int roomId);

    @POST("devices/{device_id}/toggle")
    Call<Device> toggleDevice(@Header("Authorization") String token, @Path("device_id") int deviceId);

    // Notifications endpoints
    @GET("notifications/")
    Call<List<Notification>> getNotifications(@Header("Authorization") String token);
}