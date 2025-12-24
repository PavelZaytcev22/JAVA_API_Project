package com.project.smarthome.api;

public class PushTokenRequest {
    private String token;
    private String device_type;
    private String device_name;

    public PushTokenRequest(String token, String deviceType, String deviceName) {
        this.token = token;
        this.device_type = deviceType;
        this.device_name = deviceName;
    }

    // getters
    public String getToken() { return token; }
    public String getDevice_type() { return device_type; }
    public String getDevice_name() { return device_name; }
}



