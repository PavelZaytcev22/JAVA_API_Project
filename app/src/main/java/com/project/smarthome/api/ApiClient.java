package com.project.smarthome.api;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {

    // 🔹 Базовый URL API (твой сервер)
    private static String BASE_URL = "https://smart-home-x8tm.onrender.com/";

    private static Retrofit retrofit = null;

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

    // ✅ Упрощённый способ получения API-интерфейса
    public static ApiService getApiService() {
        return getClient().create(ApiService.class);
    }

    // ✅ Возможность сменить базовый URL динамически
    public static void updateBaseUrl(String newBaseUrl) {
        BASE_URL = newBaseUrl.endsWith("/") ? newBaseUrl : newBaseUrl + "/";
        retrofit = null; // сбрасываем, чтобы пересоздать с новым адресом
    }

    // ✅ Для отладки
    public static String getBaseUrl() {
        return BASE_URL;
    }
}
