package com.project.smarthome.api;

import com.project.smarthome.models.*;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Path;

import java.util.List;

public interface ApiService {

    // ----------------------------------------
    // AUTH
    // ----------------------------------------

    // Регистрация
    @POST("api/auth/register")
    Call<RegisterResponse> register(@Body RegisterRequest request);

    // Логин (JWT)
    @POST("api/auth/token")
    Call<TokenResponse> login(@Body LoginRequest request);


    // ----------------------------------------
    // HOMES
    // ----------------------------------------

    @GET("homes/")
    Call<List<Home>> getHomes(
            @Header("Authorization") String token
    );

    @POST("homes/")
    Call<Home> createHome(
            @Header("Authorization") String token,
            @Body Home home
    );


    // ----------------------------------------
    // ROOMS
    // ----------------------------------------

    @GET("homes/{home_id}/rooms")
    Call<List<RoomCreateRequest>> getRooms(
            @Header("Authorization") String token,
            @Path("home_id") int homeId
    );

    @POST("homes/{home_id}/rooms")
    Call<RoomCreateRequest> createRoom(
            @Header("Authorization") String token,
            @Path("home_id") int homeId,
            @Body RoomCreateRequest room
    );


    // ----------------------------------------
    // DEVICES
    // ----------------------------------------

    @GET("rooms/{room_id}/devices")
    Call<List<Device>> getDevices(
            @Header("Authorization") String token,
            @Path("room_id") int roomId
    );

    @POST("devices/{device_id}/toggle")
    Call<Device> toggleDevice(
            @Header("Authorization") String token,
            @Path("device_id") int deviceId
    );


    // ----------------------------------------
    // SERVER HEALTH
    // ----------------------------------------

    @GET("health")
    Call<Void> ping();


    // ----------------------------------------
    // NOTIFICATIONS
    // ----------------------------------------

    @GET("notifications/")
    Call<List<Notification>> getNotifications(
            @Header("Authorization") String token
    );
}
