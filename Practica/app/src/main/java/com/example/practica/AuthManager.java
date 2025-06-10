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

    // Сохранить ID пользователя
    public void saveUserId(String userId) {
        sharedPreferences.edit().putString(KEY_USER_ID, userId).apply();
    }

    // Получить текущего пользователя
    public String getCurrentUserId() {
        return sharedPreferences.getString(KEY_USER_ID, null);
    }

    // Сохранить PIN для конкретного пользователя
    public void setPinForUser(String userId, String pin) {
        sharedPreferences.edit().putString("pin_" + userId, pin).apply();
    }

    // Получить PIN для текущего пользователя
    public String getPinForCurrentUser() {
        String userId = getCurrentUserId();
        if (userId == null) return null;
        return sharedPreferences.getString("pin_" + userId, null);
    }

    // Есть ли у текущего пользователя установленный PIN?
    public boolean hasPinForCurrentUser() {
        return getPinForCurrentUser() != null;
    }

    // Выход из аккаунта
    public void logout() {
        String userId = getCurrentUserId();
        if (userId != null) {
            sharedPreferences.edit().remove("pin_" + userId).apply();
        }
        sharedPreferences.edit().putBoolean(KEY_LOGGED_IN, false).apply();
    }

    // Пользователь вошёл?
    public boolean isLoggedIn() {
        return sharedPreferences.getBoolean(KEY_LOGGED_IN, false);
    }

    // Установить состояние входа
    public void setLoggedIn(boolean isLoggedIn) {
        sharedPreferences.edit().putBoolean(KEY_LOGGED_IN, isLoggedIn).apply();
    }
}