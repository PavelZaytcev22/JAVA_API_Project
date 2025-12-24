package com.project.smarthome.api;

import java.util.Map;

public class Notification {
    private int id;
    private String title;
    private String message;
    private String type;
    private boolean isRead;
    private String createdAt;
    private Map<String, Object> data;

    // Конструктор для создания уведомления из FCM данных
    public static Notification fromFcmData(Map<String, String> fcmData) {
        Notification notification = new Notification();
        notification.setTitle(fcmData.get("title"));
        notification.setMessage(fcmData.get("body"));
        notification.setType(fcmData.get("type"));
        notification.setCreatedAt(new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
                .format(new java.util.Date()));
        notification.setRead(false);
        return notification;
    }

    // Геттеры и сеттеры...
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public boolean isRead() { return isRead; }
    public void setRead(boolean read) { isRead = read; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public Map<String, Object> getData() { return data; }
    public void setData(Map<String, Object> data) { this.data = data; }
}