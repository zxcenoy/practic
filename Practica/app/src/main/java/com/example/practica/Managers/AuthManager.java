package com.example.practica.Managers;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.example.practica.Classes.CoffeeOrder;
import com.example.practica.Classes.Order;
import com.example.practica.R;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class AuthManager {

    private static final String PREFS_NAME = "auth_prefs";
    private static final String KEY_LOGGED_IN = "is_logged_in";
    private static final String KEY_USER_ID = "user_id";
    public static final String DOMAIN_NAME = "https://xenkjiywsgjtgtiyfwxg.supabase.co/";
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

            Log.d("TokenStorage", "Token sucsessfuly save");
        } catch (Exception e) {
            Log.e("TokenStorage", "Error get or save token", e);
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

    public interface OrderCallback {
        void onSuccess(List<Order> orders);

        void onError(String error);
    }

    public void getOrders(Context context, String statusFilter, final OrderCallback callback) {
        String userId = getCurrentUserId();
        if (userId == null) {
            callback.onError("User not logged in");
            return;
        }

        OkHttpClient client = new OkHttpClient();
        SharedPreferences sharedPref = context.getSharedPreferences("my_app_data", Context.MODE_PRIVATE);
        String accessToken = sharedPref.getString("access_token", "");

        Log.d("ORDERS_REQUEST", "URL: " + DOMAIN_NAME + REST_PATH +
                "orders?user_id=eq." + userId + "&status_id=eq." + statusFilter);

        Request request = new Request.Builder()
                .url(DOMAIN_NAME + REST_PATH +
                        "orders?user_id=eq." + userId +
                        "&status_id=eq." + statusFilter +
                        "&select=*,items:order_positions(*)")
                .get()
                .addHeader("apikey",context.getString(R.string.supabase_anon_key))
                .addHeader("Authorization", "Bearer " + accessToken)
                .addHeader("Content-Type", "application/json")
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("ORDERS_ERROR", "Network error", e);
                callback.onError(e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) {
                    Log.e("ORDERS_ERROR", "Server error: " + response.code() + " - " + response.message());
                    callback.onError(response.message());
                    return;
                }

                try {
                    String responseData = response.body().string();
                    Log.d("ORDERS_RESPONSE", "Raw data: " + responseData);
                    JSONArray ordersArray = new JSONArray(responseData);
                    List<Order> orders = parseOrders(ordersArray);
                    callback.onSuccess(orders);
                } catch (Exception e) {
                    Log.e("ORDERS_ERROR", "Parsing error", e);
                    callback.onError("Error parsing orders");
                }
            }
        });
    }

    private List<Order> parseOrders(JSONArray ordersArray) throws JSONException, ParseException {
        List<Order> orders = new ArrayList<>();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());

        for (int i = 0; i < ordersArray.length(); i++) {
            try {
                JSONObject orderJson = ordersArray.getJSONObject(i);
                Order order = new Order();

                order.setId(getStringSafe(orderJson, "id", ""));
                order.setUserId(getStringSafe(orderJson, "user_id", ""));
                order.setAddress(getStringSafe(orderJson, "address", "Address null"));
                order.setPaymentMethod(getStringSafe(orderJson, "payment_method", "Null"));
                order.setTotalAmount(getDoubleSafe(orderJson, "ammount", 0.0));
                order.setStatus(getStringSafe(orderJson, "status", "1"));
                order.setStatusId(getIntSafe(orderJson, "status_id", 1));
                order.setProductId(getIntSafe(orderJson, "product_id",0));

                String createdAt = orderJson.optString("created_at", null);
                if (createdAt != null) {
                    try {
                        order.setCreatedAt(sdf.parse(createdAt));
                    } catch (ParseException e) {
                        order.setCreatedAt(new Date());
                        Log.e("DATE_PARSE", "Date parsing errror: " + createdAt, e);
                    }
                } else {
                    order.setCreatedAt(new Date());
                }

                if (orderJson.has("items")) {
                    try {
                        JSONArray itemsArray = orderJson.getJSONArray("items");
                        List<CoffeeOrder> items = new ArrayList<>();
                        for (int j = 0; j < itemsArray.length(); j++) {
                            JSONObject itemJson = itemsArray.getJSONObject(j);
                            CoffeeOrder item = new CoffeeOrder(
                                    getStringSafe(itemJson, "name", ""),
                                    getDoubleSafe(itemJson, "ammount", 0.0),
                                    getStringSafe(itemJson, "description", ""),
                                    getIntSafe(itemJson, "quantity", 1)
                            );
                            items.add(item);
                        }
                        order.setItems(items);
                    } catch (JSONException e) {
                        Log.e("ITEMS_PARSE", "Error parsing items", e);
                        order.setItems(new ArrayList<>());
                    }
                } else {
                    order.setItems(new ArrayList<>());
                }

                orders.add(order);
            } catch (JSONException e) {
                Log.e("ORDER_PARSE", "Error order parsing", e);
            }
        }
        return orders;
    }

    private String getStringSafe(JSONObject json, String key, String defaultValue) {
        return json.has(key) ? json.optString(key, defaultValue) : defaultValue;
    }

    private double getDoubleSafe(JSONObject json, String key, double defaultValue) {
        return json.has(key) ? json.optDouble(key, defaultValue) : defaultValue;
    }

    private int getIntSafe(JSONObject json, String key, int defaultValue) {
        return json.has(key) ? json.optInt(key, defaultValue) : defaultValue;
    }
}

