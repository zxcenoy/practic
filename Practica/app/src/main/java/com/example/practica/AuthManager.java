package com.example.practica;

import android.content.Context;
import android.content.SharedPreferences;

public class AuthManager {

    private static final String PREFS_NAME = "auth_prefs";
    private static final String KEY_LOGGED_IN = "is_logged_in";
    private static final String KEY_USER_ID = "user_id";

    private SharedPreferences sharedPreferences;

    public AuthManager(Context context) {
        sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public void saveUserId(String userId) {
        sharedPreferences.edit().putString(KEY_USER_ID, userId).apply();
    }

    public String getCurrentUserId() {
        return sharedPreferences.getString(KEY_USER_ID, null);
    }

    public void setPinForUser(String userId, String pin) {
        sharedPreferences.edit().putString("pin_" + userId, pin).apply();
    }

    public String getPinForCurrentUser() {
        String userId = getCurrentUserId();
        if (userId == null) return null;
        return sharedPreferences.getString("pin_" + userId, null);
    }

    public boolean hasPinForCurrentUser() {
        return getPinForCurrentUser() != null;
    }

    public void logout() {
        String userId = getCurrentUserId();
        if (userId != null) {
            sharedPreferences.edit().remove("pin_" + userId).apply();
        }
        sharedPreferences.edit().putBoolean(KEY_LOGGED_IN, false).apply();
    }

    public boolean isLoggedIn() {
        return sharedPreferences.getBoolean(KEY_LOGGED_IN, false);
    }

    public void setLoggedIn(boolean isLoggedIn) {
        sharedPreferences.edit().putBoolean(KEY_LOGGED_IN, isLoggedIn).apply();
    }
}