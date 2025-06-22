package com.example.practica.Activity;

import android.annotation.SuppressLint;
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

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.practica.Adapters.CartAdapter;
import com.example.practica.Classes.CoffeeOrder;
import com.example.practica.Classes.Order;
import com.example.practica.Items.RewardItem;
import com.example.practica.Managers.AuthManager;
import com.example.practica.Managers.CartManager;
import com.example.practica.R;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Collections;
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
    RecyclerView rvCartItems;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);



        authManager = new AuthManager(this);

        tvTotalPrice = findViewById(R.id.tvTotalPrice);
        rvCartItems = findViewById(R.id.rvCartItems);
        rvCartItems.setLayoutManager(new LinearLayoutManager(this));

        adapter = new CartAdapter(CartManager.getInstance().getItems(), this);
        rvCartItems.setLayoutManager(new LinearLayoutManager(this));
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
                Toast.makeText(this, getString(R.string.PleaseEnterDeliveryAddress), Toast.LENGTH_SHORT).show();
                return;
            }

            int selectedId = rgPayment.getCheckedRadioButtonId();
            if (selectedId == -1) {
                Toast.makeText(this, getString(R.string.PleaseSelectPaymentMethod), Toast.LENGTH_SHORT).show();
                return;
            }

            createOrder(address, selectedId, total);
            bottomSheetDialog.dismiss();
        });

        bottomSheetDialog.show();
    }

    private void createOrder(String address, int paymentMethodId, double total) {
        String userId = authManager.getCurrentUserId();
        SharedPreferences sharedPref = getSharedPreferences("my_app_data", Context.MODE_PRIVATE);
        String accessToken = sharedPref.getString("access_token", "");

        if (accessToken.isEmpty()) {
            runOnUiThread(() -> {
                Intent intent = new Intent(this, SignIn.class);
                startActivity(intent);
                finish();
            });
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
                itemJson.put("product_id", item.getProductId());
                itemJson.put("quantity", item.getQuantity());
                itemJson.put("price", item.getPrice());
                itemsArray.put(itemJson);

                Log.d("CartItem", "Product ID: " + item.getProductId() +
                        ", Name: " + item.getName() +
                        ", Quantity: " + item.getQuantity());
            }

            createMainOrder(orderJson, itemsArray);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        String orderId = "";
        authManager.addPointsForOrder(userId,orderId, new AuthManager.PointsCallback() {
            @Override
            public void onSuccess(int points) {
                runOnUiThread(() -> {

                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {

                });
            }
        });
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
                runOnUiThread(() -> {

                    Log.e("Network errror", e.getMessage().toString());
                });
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
                            Log.e("OrderError", "Error parsing" + e.getMessage().toString());
                        });
                    }
                } else {
                    String errorBody = response.body() != null ? response.body().string() : "Empty response body";
                    runOnUiThread(() -> {
                        Log.e("OrderError", "HTTP " + response.code() + ": " + errorBody);
                    });
                }
            }
        });
    }


    private void createOrderItems(String orderId, JSONArray itemsArray) {
        try {
            if (orderId == null || orderId.isEmpty()) {
                throw new IllegalArgumentException("Order ID is null or empty");
            }

            JSONArray requestArray = new JSONArray();

            for (int i = 0; i < itemsArray.length(); i++) {
                JSONObject item = itemsArray.getJSONObject(i);
                JSONObject pos = new JSONObject();

                pos.put("order_id", orderId);
                pos.put("product_id", item.getInt("product_id"));
                pos.put("quantity", item.optInt("quantity", 1));
                pos.put("price", item.getDouble("price"));

                requestArray.put(pos);
            }

            Log.d("OrderItemsRequest", requestArray.toString());

            OkHttpClient client = new OkHttpClient();
            RequestBody body = RequestBody.create(
                    requestArray.toString(),
                    MediaType.parse("application/json")
            );
            SharedPreferences sharedPref = getSharedPreferences("my_app_data", Context.MODE_PRIVATE);
            String accessToken = sharedPref.getString("access_token", "");

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
                public void onResponse(Call call, Response response) throws IOException {
                    String body = response.body() != null ? response.body().string() : "";
                    if (response.isSuccessful()) {
                        runOnUiThread(() -> {
                            CartManager.getInstance().clearCart();
                            startActivity(new Intent(CartActivity.this, OrderSuccessActivity.class));
                            finish();
                        });
                    }
                }

                @Override
                public void onFailure(Call call, IOException e) {
                    Log.e("OrderItemsError", "Request failed", e);
                }
            });

        } catch (Exception e) {
            Log.e("OrderItemsError", "Create items failed", e);
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
        adapter.notifyItemRangeChanged(position, adapter.getItemCount());
        updateTotal();
    }

    @Override
    public void onQuantityChanged() {
        updateTotal();
    }
    @Override
    protected void onResume() {
        super.onResume();
        adapter = new CartAdapter(CartManager.getInstance().getItems(), this);
        rvCartItems.setAdapter(adapter);
        updateTotal();
    }



    public void BackMainOnCart(View view){
        startActivity(new Intent(this, MainScreen.class));
    }
}