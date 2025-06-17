package com.example.practica;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialog;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class CartActivity extends AppCompatActivity implements CartAdapter.OnCartItemListener {
    private CartAdapter adapter;
    private TextView tvTotalPrice;
    private AuthManager authManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        authManager = new AuthManager(this);

        tvTotalPrice = findViewById(R.id.tvTotalPrice);
        RecyclerView rvCartItems = findViewById(R.id.rvCartItems);
        rvCartItems.setLayoutManager(new LinearLayoutManager(this));

        adapter = new CartAdapter(CartManager.getInstance().getItems(), this);
        rvCartItems.setAdapter(adapter);

        Button btnCheckout = findViewById(R.id.btnCheckout);
        btnCheckout.setOnClickListener(v -> showCheckoutBottomSheet());

        updateTotal();
    }

    private void showCheckoutBottomSheet() {
        View bottomSheetView = getLayoutInflater().inflate(R.layout.checkout_bottom_sheet, null);
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this);
        bottomSheetDialog.setContentView(bottomSheetView);

        EditText etAddress = bottomSheetView.findViewById(R.id.etAddress);
        RadioGroup rgPayment = bottomSheetView.findViewById(R.id.rgPayment);
        TextView tvSubtotal = bottomSheetView.findViewById(R.id.tvSubtotal);
        TextView tvDeliveryFee = bottomSheetView.findViewById(R.id.tvDeliveryFee);
        TextView tvTotal = bottomSheetView.findViewById(R.id.tvTotal);
        Button btnPayNow = bottomSheetView.findViewById(R.id.btnPayNow);

        double subtotal = CartManager.getInstance().calculateTotal();
        double deliveryFee = 2.00;
        double total = subtotal + deliveryFee;

        tvSubtotal.setText(String.format("$%.2f", subtotal));
        tvDeliveryFee.setText(String.format("$%.2f", deliveryFee));
        tvTotal.setText(String.format("$%.2f", total));

        btnPayNow.setOnClickListener(v -> {
            String address = etAddress.getText().toString().trim();
            if (address.isEmpty()) {
                Toast.makeText(this, "Please enter delivery address", Toast.LENGTH_SHORT).show();
                return;
            }

            int selectedId = rgPayment.getCheckedRadioButtonId();
            if (selectedId == -1) {
                Toast.makeText(this, "Please select payment method", Toast.LENGTH_SHORT).show();
                return;
            }

            createOrder(address, selectedId, total);
            bottomSheetDialog.dismiss();
        });

        bottomSheetDialog.show();
    }

    private void createOrder(String address, int paymentMethodId, double total) {
        String userId = authManager.getCurrentUserId();
        if (userId == null) {
            Toast.makeText(this, "Пользователь не авторизован", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            JSONObject orderJson = new JSONObject();
            orderJson.put("user_id", userId);
            orderJson.put("address", address);

            int paymentCode = paymentMethodId == R.id.rbCash ? 1 : 2;
            orderJson.put("payment_method_id", paymentCode);
            orderJson.put("ammount", total);
            orderJson.put("status_id", 1);
            orderJson.put("created_at", new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date()));

            JSONArray itemsArray = new JSONArray();
            for (CoffeeOrder item : CartManager.getInstance().getItems()) {
                JSONObject itemJson = new JSONObject();
                itemJson.put("product_id", item.getId());
                itemJson.put("quantity", item.getQuantity());
                itemJson.put("price", item.getPrice());
                itemsArray.put(itemJson);
            }

            createMainOrder(orderJson, itemsArray);
        } catch (JSONException e) {
            Toast.makeText(this, "Ошибка формирования заказа", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    private void createMainOrder(JSONObject orderJson, JSONArray itemsArray) {
        OkHttpClient client = new OkHttpClient();
        RequestBody body = RequestBody.create(orderJson.toString(), MediaType.parse("application/json"));

        SharedPreferences sharedPref = getSharedPreferences("my_app_data", Context.MODE_PRIVATE);
        String accessToken = sharedPref.getString("access_token", "");

        Request request = new Request.Builder()
                .url(AuthManager.DOMAIN_NAME + AuthManager.REST_PATH + "orders")
                .post(body)
                .addHeader("Authorization", "Bearer " + accessToken)
                .addHeader("apikey", getString(R.string.supabase_anon_key))
                .addHeader("Content-Type", "application/json")
                .addHeader("Prefer", "return=representation")
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() ->
                        Toast.makeText(CartActivity.this, "Ошибка сети: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    try {
                        String responseData = response.body().string();
                        JSONArray resultArray = new JSONArray(responseData);
                        if (resultArray.length() > 0) {
                            JSONObject createdOrder = resultArray.getJSONObject(0);
                            String orderId = createdOrder.getString("id");
                            createOrderItems(orderId, itemsArray);

                        }
                    } catch (Exception e) {
                        runOnUiThread(() -> {
                            Toast.makeText(CartActivity.this, "Ошибка обработки заказа", Toast.LENGTH_SHORT).show();
                            Log.e("OrderError", "Ошибка парсинга ответа", e);
                        });
                    }
                } else {
                    String errorBody = response.body() != null ? response.body().string() : "Empty response body";
                    runOnUiThread(() -> {
                        Toast.makeText(CartActivity.this, "Ошибка создания заказа: " + response.code() + " - " + errorBody, Toast.LENGTH_LONG).show();
                        Log.e("OrderError", "HTTP " + response.code() + ": " + errorBody);
                    });
                }
            }
        });
    }


    private void createOrderItems(String orderId, JSONArray itemsArray) {
        OkHttpClient client = new OkHttpClient();
        SharedPreferences sharedPref = getSharedPreferences("my_app_data", Context.MODE_PRIVATE);
        String accessToken = sharedPref.getString("access_token", "");

        if (accessToken.isEmpty()) {
            runOnUiThread(() -> {
                Toast.makeText(this, "Требуется авторизация", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(this, SignIn.class);
                startActivity(intent);
                finish();
            });
            return;
        }

        try {
            JSONArray cleanedItemsArray = new JSONArray();
            List<String> invalidItems = new ArrayList<>();

            for (int i = 0; i < itemsArray.length(); i++) {
                try {
                    JSONObject originalItem = itemsArray.getJSONObject(i);
                    JSONObject cleanedItem = new JSONObject();

                    Object productIdObj = originalItem.get("product_id");
                    int productId;

                    if (productIdObj instanceof Boolean) {
                        productId = (Boolean)productIdObj ? 1 : 0;
                        Log.w("OrderItems", "Получен boolean вместо product_id, преобразовано в: " + productId);
                    } else if (productIdObj instanceof Integer) {
                        productId = (Integer)productIdObj;
                    } else if (productIdObj instanceof String) {
                        try {
                            productId = Integer.parseInt((String)productIdObj);
                        } catch (NumberFormatException e) {
                            throw new JSONException("Неверный формат product_id: " + productIdObj);
                        }
                    } else {
                        throw new JSONException("Неизвестный тип product_id: " + productIdObj.getClass().getSimpleName());
                    }

                    if (productId >= 0) {
                        cleanedItem.put("order_id", orderId);
                        cleanedItem.put("product_id", productId);
                        cleanedItem.put("quantity", originalItem.getInt("quantity"));
                        cleanedItem.put("price", originalItem.getDouble("price"));
                        cleanedItemsArray.put(cleanedItem);
                    } else {
                        invalidItems.add("Неверный ID товара: " + productIdObj);
                    }

                } catch (JSONException e) {
                    invalidItems.add("Ошибка в позиции " + i + ": " + e.getMessage());
                    Log.e("OrderItems", "Ошибка обработки элемента заказа", e);
                }
            }

            if (!invalidItems.isEmpty()) {
                runOnUiThread(() -> {
                    String message = "Проблемы с " + invalidItems.size() + " позициями:\n" +
                            TextUtils.join("\n", invalidItems.subList(0, Math.min(3, invalidItems.size())));
                    Toast.makeText(this, message, Toast.LENGTH_LONG).show();
                });
            }

            if (cleanedItemsArray.length() == 0) {
                runOnUiThread(() -> {
                    Toast.makeText(this, "Нет валидных позиций для сохранения", Toast.LENGTH_LONG).show();
                });
                return;
            }

            Log.d("OrderItems", "Отправляемые данные: " + cleanedItemsArray.toString(2));

            RequestBody body = RequestBody.create(
                    cleanedItemsArray.toString(),
                    MediaType.parse("application/json")
            );

            Request request = new Request.Builder()
                    .url(AuthManager.DOMAIN_NAME + AuthManager.REST_PATH + "order_positions")
                    .post(body)
                    .addHeader("Authorization", "Bearer " + accessToken)
                    .addHeader("apikey", getString(R.string.supabase_anon_key))
                    .addHeader("Content-Type", "application/json")
                    .addHeader("Prefer", "return=representation")
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    runOnUiThread(() -> {
                        Toast.makeText(CartActivity.this,
                                "Ошибка сети: " + e.getMessage(),
                                Toast.LENGTH_LONG).show();
                    });
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    String responseBody = response.body() != null ? response.body().string() : "{}";

                    runOnUiThread(() -> {
                        if (response.isSuccessful()) {
                            CartManager.getInstance().clearCart();
                            startActivity(new Intent(CartActivity.this, OrderSuccessActivity.class));
                            finish();
                        } else {
                            String errorMsg = "Ошибка сервера: " + response.code() + " - " + responseBody;
                            Toast.makeText(CartActivity.this, errorMsg, Toast.LENGTH_LONG).show();
                            Log.e("OrderItemsError", errorMsg);
                        }
                    });
                }
            });

        } catch (Exception e) {
            runOnUiThread(() -> {
                Toast.makeText(this, "Критическая ошибка: " + e.getMessage(), Toast.LENGTH_LONG).show();
                Log.e("OrderItemsError", "Fatal error in createOrderItems", e);
            });
        }
    }

    private void updateTotal() {
        double total = 0;
        for (CoffeeOrder item : CartManager.getInstance().getItems()) {
            total += item.getPrice() * item.getQuantity();
        }
        tvTotalPrice.setText(String.format("$%.2f", total));
    }

    @Override
    public void onItemRemoved(int position) {
        CartManager.getInstance().removeItem(position);
        adapter.notifyItemRemoved(position);
        updateTotal();
    }

    @Override
    public void onQuantityChanged() {
        updateTotal();
    }
}