package com.project.smarthome.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedPrefManager {
    private static final String PREF_NAME = "SmartHomePref";
    private static final String KEY_TOKEN = "token";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_BASE_URL = "base_url";

    private static SharedPrefManager instance;
    private SharedPreferences pref;

    private SharedPrefManager(Context context) {
        pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public static synchronized SharedPrefManager getInstance(Context context) {
        if (instance == null) {
            instance = new SharedPrefManager(context);
        }
        return instance;
    }

    public void saveToken(String token) {
        pref.edit().putString(KEY_TOKEN, token).apply();
    }

    public String getToken() {
        return pref.getString(KEY_TOKEN, null);
    }

    public void saveUsername(String username) {
        pref.edit().putString(KEY_USERNAME, username).apply();
    }

    public String getUsername() {
        return pref.getString(KEY_USERNAME, null);
    }

    public void saveBaseUrl(String baseUrl) {
        pref.edit().putString(KEY_BASE_URL, baseUrl).apply();
    }

    public String getBaseUrl() {
        return pref.getString(KEY_BASE_URL, "http://192.168.1.100:8000/");
    }

    public boolean isLoggedIn() {
        return getToken() != null;
    }

    public void clear() {
        pref.edit().clear().apply();
    }
}