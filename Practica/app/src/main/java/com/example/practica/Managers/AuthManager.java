package com.example.practica.Managers;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Base64;
import android.util.Log;

import com.example.practica.Classes.CoffeeOrder;
import com.example.practica.Classes.Order;
import com.example.practica.Items.CoffeeItem;
import com.example.practica.Items.RewardItem;
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
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class AuthManager {

    private static final String PREFS_NAME = "auth_prefs";
    private static final String KEY_LOGGED_IN = "is_logged_in";
    private static final String KEY_USER_ID = "user_id";
    public static final String DOMAIN_NAME = "https://xenkjiywsgjtgtiyfwxg.supabase.co/";
    public static String REST_PATH = "rest/v1/";
    public static String AUTH_PATH = "auth/v1/";
    public interface PointsCallback {
        void onSuccess(int points);
        void onError(String error);
    }

    public interface LoyaltyCallback {
        void onSuccess(int completedOrders, int cupsEarned);
        void onError(String error);
    }
    public interface OrderCallback {
        void onSuccess(List<Order> orders);
        void onError(String error);
    }
    public interface RewardHistoryCallback {
        void onSuccess(List<RewardItem> history);
        void onError(String error);
    }
    public interface ProductCallback {
        void onSuccess(List<CoffeeItem> products);
        void onError(String error);
    }

    public interface AuthCallback {
        void onSuccess(String message);
        void onError(String error);
    }
    private SharedPreferences sharedPreferences;
    private Context context;


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

    public AuthManager(Context context) {
        this.context = context;
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



    public void getOrders(Context context, String statusFilter, final OrderCallback callback) {
        String userId = getCurrentUserId();
        if (userId == null) {
            callback.onError("User not logged in");
            return;
        }

        OkHttpClient client = new OkHttpClient();
        SharedPreferences sharedPref = context.getSharedPreferences("my_app_data", Context.MODE_PRIVATE);
        String accessToken = sharedPref.getString("access_token", "");

        String url = DOMAIN_NAME + REST_PATH + "orders?user_id=eq." + userId
                + "&status_id=eq." + statusFilter
                + "&select=*,order_positions(*,products(name))";

        Request request = new Request.Builder()
                .url(url)
                .get()
                .addHeader("apikey", context.getString(R.string.supabase_anon_key))
                .addHeader("Authorization", "Bearer " + accessToken)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                callback.onError(e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) {
                    callback.onError(response.message());
                    return;
                }
                try {
                    String responseData = response.body().string();
                    JSONArray ordersArray = new JSONArray(responseData);
                    List<Order> orders = parseOrders(ordersArray);
                    callback.onSuccess(orders);
                } catch (Exception e) {
                    callback.onError("Error parsing orders: " + e.getMessage());
                }
            }
        });
    }

    private List<Order> parseOrders(JSONArray ordersArray) throws JSONException {
        List<Order> orders = new ArrayList<>();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());


        for (int i = 0; i < ordersArray.length(); i++) {
            JSONObject orderJson = ordersArray.getJSONObject(i);
            Order order = new Order();

            order.setId(orderJson.getString("id"));
            order.setStatusId(orderJson.getInt("status_id"));
            order.setCreatedAt(new Date());

            order.setAddress(getStringSafe(orderJson, "address", "Адрес не указан"));
            order.setTotalAmount(getDoubleSafe(orderJson, "ammount", 0));
            order.setPaymentMethod(getPaymentMethodText(getIntSafe(orderJson, "payment_method_id", 0)));

            String createdAt = orderJson.optString("created_at", null);
            if (createdAt != null) {
                try {
                    order.setCreatedAt(sdf.parse(createdAt));
                } catch (ParseException e) {
                    Log.e("DATE_PARSE", "Error parsing date: " + createdAt, e);
                }
            }


            if (orderJson.has("order_positions")) {
                JSONArray positionsArray = orderJson.getJSONArray("order_positions");
                List<CoffeeOrder> items = new ArrayList<>();

                for (int j = 0; j < positionsArray.length(); j++) {
                    JSONObject position = positionsArray.getJSONObject(j);
                    JSONObject product = position.getJSONObject("products");

                    CoffeeOrder item = new CoffeeOrder(
                            product.getString("name"),
                            position.getDouble("price"),
                            "",
                            position.getInt("quantity"),
                            position.getInt("product_id")
                    );
                    items.add(item);
                }
                order.setItems(items);
            }

            orders.add(order);
        }
        return orders;
    }
    private String getPaymentMethodText(int paymentMethodId) {
        switch (paymentMethodId) {
            case 1: return "Cash";
            case 2: return "Card";
            default: return "Unknown";
        }
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
    public void getCurrentUserPoints(String userId, PointsCallback callback) {
        SharedPreferences sharedPref = context.getSharedPreferences("my_app_data", Context.MODE_PRIVATE);
        String accessToken = sharedPref.getString("access_token", "");
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(DOMAIN_NAME + REST_PATH + "profiles?id=eq." + userId + "&select=бонусные_баллы")
                .get()
                .addHeader("apikey", context.getString(R.string.supabase_anon_key))
                .addHeader("Authorization", "Bearer " + accessToken)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                callback.onError(e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) {
                    callback.onError(response.message());
                    return;
                }
                try {
                    JSONArray jsonArray = new JSONArray(response.body().string());
                    if (jsonArray.length() > 0) {
                        int points = jsonArray.getJSONObject(0).optInt("бонусные_баллы", 0);
                        callback.onSuccess(points);
                    } else {
                        callback.onSuccess(0);
                    }
                } catch (Exception e) {
                    callback.onError(e.getMessage());
                }
            }
        });
    }

    public void updateUserPoints(String userId, int newTotalPoints, PointsCallback callback) {
        SharedPreferences sharedPref = context.getSharedPreferences("my_app_data", Context.MODE_PRIVATE);
        String accessToken = sharedPref.getString("access_token", "");

        JSONObject json = new JSONObject();
        try {
            json.put("бонусные_баллы", newTotalPoints);

            RequestBody body = RequestBody.create(
                    json.toString(),
                    MediaType.parse("application/json")
            );

            Request request = new Request.Builder()
                    .url(DOMAIN_NAME + REST_PATH + "profiles?id=eq." + userId)
                    .patch(body)
                    .addHeader("apikey", context.getString(R.string.supabase_anon_key))
                    .addHeader("Authorization", "Bearer " + accessToken)
                    .addHeader("Prefer", "return=representation")
                    .build();

            new OkHttpClient().newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    callback.onError(e.getMessage());
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (response.isSuccessful()) {
                        callback.onSuccess(newTotalPoints);
                    } else {
                        callback.onError(response.message());
                    }
                }
            });
        } catch (JSONException e) {
            callback.onError("Error creating JSON: " + e.getMessage());
        }
    }

    public void updateLoyaltyStatus(String userId, LoyaltyCallback callback) {
        getCompletedOrdersCount(userId, (ordersCount, error) -> {
            if (error != null) {
                callback.onError(error);
                return;
            }

            int cupsEarned = Math.min(ordersCount, 8);


            callback.onSuccess(ordersCount, cupsEarned);
        });
    }

    private void getCompletedOrdersCount(String userId, BiConsumer<Integer, String> callback) {
        if (context == null) {
            callback.accept(null, "Context is null");
            return;
        }

        SharedPreferences sharedPref = context.getSharedPreferences("my_app_data", Context.MODE_PRIVATE);
        String accessToken = sharedPref.getString("access_token", "");

        if (accessToken.isEmpty()) {
            callback.accept(null, "Access token is empty");
            return;
        }

        OkHttpClient client = new OkHttpClient();

        String url = DOMAIN_NAME + REST_PATH + "orders?" +
                "user_id=eq." + userId +
                "&status_id=eq.4" +
                "&select=*";

        Request request = new Request.Builder()
                .url(url)
                .get()
                .addHeader("apikey", context.getString(R.string.supabase_anon_key))
                .addHeader("Authorization", "Bearer " + accessToken)
                .addHeader("Prefer", "count=exact")
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                callback.accept(null, "Network error: " + e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseBody = response.body() != null ? response.body().string() : "{}";

                if (response.code() == 400) {
                    callback.accept(null, "Bad request. Check query parameters. Response: " + responseBody);
                    return;
                }

                if (!response.isSuccessful()) {
                    callback.accept(null, "Server error: " + response.code() + ". Response: " + responseBody);
                    return;
                }

                try {

                    String contentRange = response.header("Content-Range");
                    if (contentRange != null && contentRange.contains("/")) {
                        int count = Integer.parseInt(contentRange.split("/")[1]);
                        callback.accept(count, null);
                    } else {
                        JSONArray jsonArray = new JSONArray(responseBody);
                        callback.accept(jsonArray.length(), null);
                    }
                } catch (Exception e) {
                    callback.accept(null, "Parsing error: " + e.getMessage());
                }
            }
        });
    }
    public void getRewardHistory(String userId, RewardHistoryCallback callback) {
        SharedPreferences sharedPref = context.getSharedPreferences("my_app_data", Context.MODE_PRIVATE);
        String accessToken = sharedPref.getString("access_token", "");

        OkHttpClient client = new OkHttpClient();

        String url = DOMAIN_NAME + REST_PATH + "rpc/get_orders_by_user_and_status";

        JSONObject json = new JSONObject();
        try {
            json.put("p_user_id", userId);
            json.put("p_status_id", 1);
        } catch (JSONException e) {
            callback.onError("Error creating JSON: " + e.getMessage());
            return;
        }

        RequestBody body = RequestBody.create(
                json.toString(),
                MediaType.parse("application/json")
        );

        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .addHeader("apikey", context.getString(R.string.supabase_anon_key))
                .addHeader("Authorization", "Bearer " + accessToken)
                .addHeader("Content-Type", "application/json")
                .addHeader("Prefer", "params=single-object")
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                callback.onError("Network error: " + e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) {
                    String errorBody = response.body() != null ? response.body().string() : "empty body";

                    callback.onError("API error: " + response.code() + " - " + response.message() + "\nBody: " + errorBody);
                    return;
                }

                try {
                    String responseData = response.body().string();
                    JSONArray jsonArray = new JSONArray(responseData);
                    List<RewardItem> rewardHistory = parseRewardHistoryFromFunction(jsonArray);
                    callback.onSuccess(rewardHistory);
                } catch (Exception e) {
                    callback.onError("Parsing error: " + e.getMessage());
                }
            }
        });
    }
    private List<RewardItem> parseRewardHistoryFromFunction(JSONArray jsonArray) throws JSONException {
        List<RewardItem> rewardHistory = new ArrayList<>();
        SimpleDateFormat displayFormat = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());

        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject item = jsonArray.getJSONObject(i);
            JSONObject order = item.optJSONObject("orders");

            Date createdAt;
            try {
                createdAt = new SimpleDateFormat("yyyy-MM-dd").parse(item.getString("created_at"));
            } catch (ParseException e) {
                createdAt = new Date();
            }

            int points = item.optInt("points", 12);
            String productName = "Order";
            if (order != null && order.has("products")) {
                productName = order.getJSONObject("products").optString("name", "Order");
            }


            rewardHistory.add(new RewardItem(
                    item.getString("product_name"),
                    points,
                    createdAt,
                    displayFormat.format(createdAt)
            ));
        }

        Collections.sort(rewardHistory, (o1, o2) -> o2.getDate().compareTo(o1.getDate()));
        return rewardHistory;
    }


    public void getProducts(ProductCallback callback) {
        SharedPreferences sharedPref = context.getSharedPreferences("my_app_data", Context.MODE_PRIVATE);
        String accessToken = sharedPref.getString("access_token", "");

        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(DOMAIN_NAME + REST_PATH + "products?select=id,name,price,url_image_product,category_id,Категории_товаров(Название)")
                .get()
                .addHeader("apikey", context.getString(R.string.supabase_anon_key))
                .addHeader("Authorization", "Bearer " + accessToken)
                .addHeader("Content-Type", "application/json")
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                callback.onError(e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) {
                    callback.onError("HTTP error: " + response.code());
                    return;
                }
                try {
                    String jsonData = response.body().string();
                    JSONArray productsArray = new JSONArray(jsonData);
                    List<CoffeeItem> products = new ArrayList<>();
                    for (int i = 0; i < productsArray.length(); i++) {
                        JSONObject product = productsArray.getJSONObject(i);
                        String categoryName = "unknown";
                        if (product.has("Категории_товаров")) {
                            JSONObject categoryObj = product.getJSONObject("Категории_товаров");
                            categoryName = categoryObj.optString("Название", "unknown");
                        }

                        CoffeeItem coffeeItem = new CoffeeItem(
                                product.optString("name", "Unknown"),
                                product.optDouble("price", 0),
                                product.optString("url_image_product", ""),
                                product.optInt("id", -1)
                        );
                        coffeeItem.setCategory(categoryName);
                        products.add(coffeeItem);
                    }
                    callback.onSuccess(products);
                } catch (Exception e) {
                    callback.onError("Parse error: " + e.getMessage());
                }
            }
        });
    }
    public void calculateAndUpdateUserPoints(String userId, PointsCallback callback) {
        getRewardHistory(userId, new RewardHistoryCallback() {
            @Override
            public void onSuccess(List<RewardItem> history) {
                int totalPoints = 0;
                for (RewardItem item : history) {
                    totalPoints += item.getPoints();
                }

                updateUserPoints(userId, totalPoints, new PointsCallback() {
                    @Override
                    public void onSuccess(int points) {
                        callback.onSuccess(points);
                    }

                    @Override
                    public void onError(String error) {
                        callback.onError(error);
                    }
                });
            }

            @Override
            public void onError(String error) {
                callback.onError("Failed to get reward history: " + error);
            }
        });
    }
    public void addPointsForOrder(String userId, String orderId, PointsCallback callback) {
        getCurrentUserPoints(userId, new PointsCallback() {
            @Override
            public void onSuccess(int currentPoints) {
                int newPoints = currentPoints + 12;

                updateUserPoints(userId, newPoints, new PointsCallback() {
                    @Override
                    public void onSuccess(int points) {
                        recordPointsHistory(userId, orderId, 12, points, callback);
                    }

                    @Override
                    public void onError(String error) {
                        callback.onError("Failed to update points: " + error);
                    }
                });
            }

            @Override
            public void onError(String error) {
                callback.onError("Failed to get current points: " + error);
            }
        });
    }

    private void recordPointsHistory(String userId, String orderId, int pointsAdded, int totalPoints, PointsCallback callback) {
        SharedPreferences sharedPref = context.getSharedPreferences("my_app_data", Context.MODE_PRIVATE);
        String accessToken = sharedPref.getString("access_token", "");

        JSONObject json = new JSONObject();
        try {
            json.put("user_id", userId);
            json.put("order_id", orderId);
            json.put("points_added", pointsAdded);
            json.put("total_points", totalPoints);
            json.put("created_at", new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").format(new Date()));

            RequestBody body = RequestBody.create(
                    json.toString(),
                    MediaType.parse("application/json")
            );

            Request request = new Request.Builder()
                    .url(DOMAIN_NAME + REST_PATH + "points_history")
                    .post(body)
                    .addHeader("apikey", context.getString(R.string.supabase_anon_key))
                    .addHeader("Authorization", "Bearer " + accessToken)
                    .build();

            new OkHttpClient().newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    callback.onError(e.getMessage());
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (response.isSuccessful()) {
                        callback.onSuccess(totalPoints);
                    } else {
                        callback.onError(response.message());
                    }
                }
            });
        } catch (JSONException e) {
            callback.onError("Error creating JSON: " + e.getMessage());
        }
    }
    public boolean isTokenValid(Context context) {
        SharedPreferences sharedPref = context.getSharedPreferences("my_app_data", Context.MODE_PRIVATE);
        String accessToken = sharedPref.getString("access_token", null);

        if (accessToken == null || accessToken.isEmpty()) {
            return false;
        }

        try {
            String[] parts = accessToken.split("\\.");
            if (parts.length < 2) return false;

            String payload = new String(Base64.decode(parts[1], Base64.URL_SAFE));
            JSONObject json = new JSONObject(payload);
            long exp = json.getLong("exp") * 1000;
            return exp > System.currentTimeMillis();
        } catch (Exception e) {
            Log.e("AuthManager", "Error parsing JWT", e);
            return false;
        }
    }

    public void clearAuthData() {
        SharedPreferences sharedPref = context.getSharedPreferences("my_app_data", Context.MODE_PRIVATE);
        sharedPref.edit().remove("access_token").apply();

        logout();
    }
}



