package com.project.smarthome.api;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {


    private static final String BASE_URL = "https://smart-home-x8tm.onrender.com/";

    private static Retrofit retrofit = null;

    // Возвращаем Retrofit с базовым URL
    public static Retrofit getClient() {
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }

    // Если захотим динамический URL (например, с EditText)
    public static Retrofit getClient(String baseUrl) {
        return new Retrofit.Builder()
                .baseUrl(baseUrl.endsWith("/") ? baseUrl : baseUrl + "/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }

    // Для получения текущего базового URL
    public static String getBaseUrl() {
        return BASE_URL;
    }
}
