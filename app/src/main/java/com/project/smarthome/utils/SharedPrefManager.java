package com.project.smarthome.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedPrefManager {
    private static final String PREF_NAME = "smarthome_prefs";
    private static final String KEY_TOKEN = "key_token";
    private static final String KEY_SERVER_URL = "key_server_url";
    private static final String KEY_USERNAME = "key_username";

    private static SharedPrefManager instance;
    private final SharedPreferences prefs;

    private SharedPrefManager(Context context) {
        prefs = context.getApplicationContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public static synchronized SharedPrefManager getInstance(Context context) {
        if (instance == null) {
            instance = new SharedPrefManager(context);
        }
        return instance;
    }

    // Token methods
    public void saveToken(String token) {
        prefs.edit().putString(KEY_TOKEN, token).apply();
    }

    public String getToken() {
        return prefs.getString(KEY_TOKEN, null);
    }

    public void clearToken() {
        prefs.edit().remove(KEY_TOKEN).apply();
    }

    // Server URL
    public void saveServerUrl(String serverUrl) {
        prefs.edit().putString(KEY_SERVER_URL, serverUrl).apply();
    }

    public String getServerUrl() {
        return prefs.getString(KEY_SERVER_URL, null);
    }

    // Username
    public void saveUsername(String username) {
        prefs.edit().putString(KEY_USERNAME, username).apply();
    }

    public String getUsername() {
        return prefs.getString(KEY_USERNAME, null);
    }

    // Session management (для обратной совместимости)
    public void saveSession(String token, String username) {
        saveToken(token);
        saveUsername(username);
    }

    public boolean isLoggedIn() {
        return getToken() != null && !getToken().isEmpty();
    }

    // Clear all (logout)
    public void clearAll() {
        prefs.edit().clear().apply();
    }
}