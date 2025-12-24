package com.project.smarthome.api;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.project.smarthome.MainActivity;
import com.project.smarthome.R;
import com.project.smarthome.utils.SharedPrefManager;
import com.project.smarthome.api.ApiClient;
import com.project.smarthome.api.PushTokenRequest;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MyFirebaseMessagingService extends FirebaseMessagingService {
    private static final String CHANNEL_ID = "smart_home_notifications";
    private static final int NOTIFICATION_ID = 1;

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        // Проверяем, есть ли данные в уведомлении
        if (remoteMessage.getData().size() > 0) {
            String title = remoteMessage.getData().get("title");
            String body = remoteMessage.getData().get("body");
            String type = remoteMessage.getData().get("type");

            showNotification(title != null ? title : "Уведомление",
                    body != null ? body : "Новое сообщение",
                    type);
        }

        // Если уведомление пришло с сервером
        if (remoteMessage.getNotification() != null) {
            showNotification(remoteMessage.getNotification().getTitle(),
                    remoteMessage.getNotification().getBody(),
                    "default");
        }
    }

    @Override
    public void onNewToken(String token) {
        // Сохраняем FCM токен в SharedPreferences
        SharedPrefManager.getInstance(this).saveFcmToken(token);

        // Отправляем новый токен на сервер, если пользователь залогинен
        if (SharedPrefManager.getInstance(this).isLoggedIn()) {
            sendTokenToServer(token);
        }
    }

    private void showNotification(String title, String message, String type) {
        createNotificationChannel();

        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("notification_type", type);
        intent.putExtra("notification_title", title);
        intent.putExtra("notification_message", message);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                this, 0, intent,
                PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notifications) // Замените на вашу иконку
                .setContentTitle(title)
                .setContentText(message)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        notificationManager.notify(NOTIFICATION_ID, builder.build());
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Уведомления Smart Home";
            String description = "Уведомления от системы умного дома";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private void sendTokenToServer(String token) {
        // Используем ваш ApiClient
        ApiClient.initialize(this);
        if (ApiClient.isLoggedIn()) {
            com.project.smarthome.api.ApiService apiService = ApiClient.getApiService(this);

            PushTokenRequest request = new PushTokenRequest(
                    token,
                    "android",
                    android.os.Build.MODEL
            );

            Call<com.project.smarthome.api.ApiResponse> call = apiService.registerPushToken(request);
            call.enqueue(new Callback<com.project.smarthome.api.ApiResponse>() {
                @Override
                public void onResponse(Call<com.project.smarthome.api.ApiResponse> call, Response<com.project.smarthome.api.ApiResponse> response) {
                    if (response.isSuccessful()) {
                        System.out.println("FCM token registered successfully on server");
                    } else {
                        System.out.println("Failed to register FCM token on server: " + response.code());
                    }
                }

                @Override
                public void onFailure(Call<com.project.smarthome.api.ApiResponse> call, Throwable t) {
                    System.out.println("Network error while registering FCM token: " + t.getMessage());
                }
            });
        }
    }
}