package com.project.smarthome.api;

import com.project.smarthome.models.*;
import com.project.smarthome.models.auth.*;
import com.project.smarthome.models.devices.*;
import com.project.smarthome.models.families.*;
import com.project.smarthome.models.homes.*;


import retrofit2.Call;
import retrofit2.http.*;

import java.util.List;
import java.util.Map;
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

    @GET("api/family/my-homes")
    Call<List<Home>> getMyHomes();

    @POST("api/homes/")
    Call<Home> createHome(@Body Home home);

    // ----------------------------------------
    // FAMILY MEMBERS
    //
    @POST("families")
    Call<FamilyResponse> createFamily(
            @Header("Authorization") String token,
            @Body FamilyCreateRequest request
    );

    @GET("families/{id}")
    Call<FamilyResponse> getFamily(
            @Header("Authorization") String token,
            @Path("id") int id
    );

    @PUT("families/{id}")
    Call<FamilyResponse> updateFamily(
            @Header("Authorization") String token,
            @Path("id") int id,
            @Body FamilyUpdateRequest request
    );

    @GET("api/family/homes/{home_id}/members")
    Call<List<FamilyMember>> getFamilyMembers(@Path("home_id") int homeId);

    @POST("api/family/homes/{home_id}/members")
    Call<Map<String, Object>> addFamilyMember(@Path("home_id") int homeId, @Body FamilyMemberAdd request);

//    @DELETE("api/family/homes/{home_id}/members/{user_id}")
//    Call<Map<String, String>> removeFamilyMember(@Path("home_id") int homeId, @Path("user_id") int userId);


    // ----------------------------------------
    // ROOMS
    // ----------------------------------------

    @GET("api/rooms/homes/{home_id}")
    Call<List<Room>> getRooms(@Path("home_id") int homeId);

    @POST("api/rooms/homes/{home_id}")
    Call<Room> createRoom(@Path("home_id") int homeId, @Body Room room);


    // ----------------------------------------
    // DEVICES
    // ----------------------------------------

    @GET("api/devices/homes/{home_id}")
    Call<List<Device>> getDevices(@Path("home_id") int homeId);

    @POST("api/devices/homes/{home_id}")
    Call<Device> createDevice(@Path("home_id") int homeId, @Body DeviceCreateRequest device);

    @POST("api/devices/{device_id}/action")
    Call<Map<String, Object>> controlDevice(
            @Path("device_id") int deviceId,
            @Query("new_state") String newState
    );

    @GET("api/devices/{device_id}")
    Call<Device> getDevice(@Path("device_id") int deviceId);


    // ----------------------------------------
    // SERVER SYS
    // ----------------------------------------


    @GET("api/system/ping")
    Call<Map<String, String>> ping();

    @GET("api/system/me")
    Call<Map<String, Object>> getCurrentUser();

    // ----------------------------------------
    // NOTIFICATIONS
    // ----------------------------------------

    @GET("notifications/")
    Call<List<Notification>> getNotifications(
            @Header("Authorization") String token
    );
}
