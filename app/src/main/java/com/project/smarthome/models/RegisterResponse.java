package com.project.smarthome.models;

public class RegisterResponse {
    private String access_token;
    private String message;
    private boolean success;

    // Getters and setters
    public String getAccessToken() { return access_token; }
    public void setAccessToken(String access_token) { this.access_token = access_token; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }
}