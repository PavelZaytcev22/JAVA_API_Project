package com.project.smarthome.api;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import android.content.Context;
public class ApiClient {

    // 🔹 Базовый URL API (твой сервер)
    private static String BASE_URL = "https://smart-home-x8tm.onrender.com/";

    private static Retrofit retrofit = null;
    public static Retrofit getClient(Context context) {
        if (retrofit == null) {

            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(new OkHttpClient.Builder().build())
                    .build();
        }
        return retrofit;
    }
    // ✅ Метод для получения Retrofit с логированием
    private static Retrofit getClient() {
        if (retrofit == null) {

            // Логирование всех запросов и ответов
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.setLevel(HttpLoggingInterceptor.Level.BODY);

            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(logging)
                    .build();

            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }

    public static ApiService getApiService(Context context) {
        return getClient(context).create(ApiService.class);
    }
    public static ApiService getApiService() {
        return getClient().create(ApiService.class);
    }

    // ✅ Возможность сменить базовый URL динамически
    public static void updateBaseUrl(String newBaseUrl) {
        BASE_URL = newBaseUrl.endsWith("/") ? newBaseUrl : newBaseUrl + "/";
        retrofit = null;
    }

    // ✅ Для отладки
    public static String getBaseUrl() {
        return BASE_URL;
    }
}
