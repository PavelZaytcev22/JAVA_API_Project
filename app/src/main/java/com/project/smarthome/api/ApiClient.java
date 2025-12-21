package com.project.smarthome.api;

import android.content.Context;
import com.project.smarthome.utils.SharedPrefManager;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {
    private static Retrofit retrofit = null;
    private static String currentBaseUrl = "https://smart-home-x8tm.onrender.com/";
    private static SharedPrefManager sharedPrefManager;

    // Инициализация должна быть вызвана в Application классе или первой Activity
    public static void initialize(Context context) {
        if (sharedPrefManager == null) {
            sharedPrefManager = SharedPrefManager.getInstance(context);

            // Восстанавливаем сохраненный URL если есть
            String savedUrl = sharedPrefManager.getServerUrl();
            if (savedUrl != null && !savedUrl.isEmpty()) {
                currentBaseUrl = savedUrl;
            }

            // Восстанавливаем токен
            String savedToken = sharedPrefManager.getToken();
            if (savedToken != null && !savedToken.isEmpty()) {
                // Токен уже в sharedPrefManager, клиент будет его использовать через interceptor
            }
        }
    }

    public static Retrofit getClient() {
        if (retrofit == null) {
            if (sharedPrefManager == null) {
                throw new IllegalStateException("ApiClient not initialized. Call ApiClient.initialize(context) first.");
            }

            OkHttpClient.Builder httpClient = new OkHttpClient.Builder();

            // Interceptor для добавления токена авторизации
            httpClient.addInterceptor(chain -> {
                Request original = chain.request();
                Request.Builder requestBuilder = original.newBuilder();

                // Добавляем токен если есть
                String token = sharedPrefManager.getToken();
                if (token != null && !token.isEmpty()) {
                    requestBuilder.header("Authorization", "Bearer " + token);
                }

                // Стандартные заголовки
                requestBuilder.header("Content-Type", "application/json");
                requestBuilder.header("Accept", "application/json");

                return chain.proceed(requestBuilder.build());
            });

            // Логирование для отладки
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.setLevel(HttpLoggingInterceptor.Level.BODY);
            httpClient.addInterceptor(logging);

            retrofit = new Retrofit.Builder()
                    .baseUrl(currentBaseUrl)
                    .client(httpClient.build())
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }

    public static ApiService getApiService(Context context) {
        initialize(context);
        return getClient().create(ApiService.class);
    }

    public static ApiService getApiService() {
        if (sharedPrefManager == null) {
            throw new IllegalStateException("ApiClient not initialized. Call ApiClient.initialize(context) first.");
        }
        return getClient().create(ApiService.class);
    }

    public static void updateServerUrl(String newUrl) {
        String formattedUrl = newUrl.endsWith("/") ? newUrl : newUrl + "/";
        currentBaseUrl = formattedUrl;
        if (sharedPrefManager != null) {
            sharedPrefManager.saveServerUrl(formattedUrl);
        }
        retrofit = null; // Пересоздаем клиент с новым URL
    }

    public static boolean isLoggedIn() {
        return sharedPrefManager != null && sharedPrefManager.isLoggedIn();
    }

    public static void logout() {
        if (sharedPrefManager != null) {
            sharedPrefManager.clearAll();
        }
        retrofit = null;
    }
}