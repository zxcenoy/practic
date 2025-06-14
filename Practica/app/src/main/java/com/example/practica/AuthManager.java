package com.example.practica;

import static com.example.practica.ChangeEmail.JSON;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class AuthManager {

    private static final String PREFS_NAME = "auth_prefs";
    private static final String KEY_LOGGED_IN = "is_logged_in";
    private static final String KEY_USER_ID = "user_id";
    private static final String DOMAIN_NAME = "https://xenkjiywsgjtgtiyfwxg.supabase.co/";
    public static String REST_PATH = "rest/v1/";
    public static String AUTH_PATH = "auth/v1/";

    public interface AuthCallback {
        void onSuccess(String message);
        void onError(String error);
    }


    public void saveAccessTokenFromResponse(String jsonResponse, Context context) {
        try {
            Gson gson = new Gson();
            JsonObject jsonObject = gson.fromJson(jsonResponse, JsonObject.class);

            String accessToken = jsonObject.get("access_token").getAsString();

            SharedPreferences sharedPref = context.getSharedPreferences("my_app_data", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putString("access_token", accessToken);
            editor.apply();

            Log.d("TokenStorage", "Токен успешно сохранён");
        } catch (Exception e) {
            Log.e("TokenStorage", "Ошибка при извлечении или сохранении токена", e);
        }

    }

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
        sharedPreferences.edit()
                .remove(KEY_USER_ID)
                .putBoolean(KEY_LOGGED_IN, false)
                .apply();
    }

    public boolean isLoggedIn() {
        return sharedPreferences.getBoolean(KEY_LOGGED_IN, false);
    }

    public void setLoggedIn(boolean isLoggedIn) {
        sharedPreferences.edit().putBoolean(KEY_LOGGED_IN, isLoggedIn).apply();
    }
}