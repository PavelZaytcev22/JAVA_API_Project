package com.project.smarthome;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;


public class RegisterRequest {
    private String username;
    private String password;

    public RegisterRequest(String username, String password) {
        this.username = username;
        this.password = password;
    }
}
