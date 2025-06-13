package com.example.practica;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import org.json.JSONObject;

import java.io.IOException;

import okhttp3.*;


public class OTPVerify extends AppCompatActivity {

    private EditText[] otpFields = new EditText[6];
    private String email;
    private final OkHttpClient client = new OkHttpClient();
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.otp_code);

        email = getIntent().getStringExtra("email");

        otpFields[0] = findViewById(R.id.otpField1);
        otpFields[1] = findViewById(R.id.otpField2);
        otpFields[2] = findViewById(R.id.otpField3);
        otpFields[3] = findViewById(R.id.otpField4);
        otpFields[4] = findViewById(R.id.otpField5);
        otpFields[5] = findViewById(R.id.otpField6);
        setupOtpFields();


        findViewById(R.id.otp).setOnClickListener(v -> verifyOtpCode());
    }
    private void setupOtpFields() {
        for (int i = 0; i < otpFields.length; i++) {
            final int currentIndex = i;

            otpFields[i].addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (s.length() == 1 && currentIndex < otpFields.length - 1) {
                        otpFields[currentIndex + 1].requestFocus();
                    }
                }

                @Override
                public void afterTextChanged(Editable s) {}
            });

            otpFields[i].setOnKeyListener((v, keyCode, event) -> {
                if (keyCode == KeyEvent.KEYCODE_DEL &&
                        event.getAction() == KeyEvent.ACTION_DOWN &&
                        otpFields[currentIndex].getText().length() == 0 &&
                        currentIndex > 0) {

                    otpFields[currentIndex - 1].requestFocus();
                    otpFields[currentIndex - 1].setText("");
                    return true;
                }
                return false;
            });
        }
    }

    private void verifyOtpCode() {
        StringBuilder code = new StringBuilder();
        for (EditText field : otpFields) {
            code.append(field.getText().toString().trim());
        }

        String otpCode = code.toString();
        if (otpCode.length() != 6) {
            Toast.makeText(this, "Please enter a valid 6-digit code", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            JSONObject jsonBody = new JSONObject();
            jsonBody.put("email", email);
            jsonBody.put("token", otpCode);
            jsonBody.put("type", "recovery");

            Request request = new Request.Builder()
                    .url("https://xenkjiywsgjtgtiyfwxg.supabase.co/auth/v1/verify")
                    .post(RequestBody.create(jsonBody.toString(), JSON))
                    .addHeader("apikey", getString(R.string.supabase_anon_key))
                    .addHeader("Content-Type", "application/json")
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    runOnUiThread(() ->
                            Toast.makeText(OTPVerify.this, "Network error", Toast.LENGTH_SHORT).show());
                }

                @Override
                public void onResponse(Call call, Response response) {
                    runOnUiThread(() -> {
                        if (response.isSuccessful()) {
                           try(ResponseBody responseBody = response.body()) {
                               String responseBodyString = responseBody.string();

                               try {
                                   JsonElement jsonElement = new JsonParser().parse(responseBodyString);

                                   Gson gson = new Gson();
                                   String prettyJson = gson.toJson(jsonElement);

                                   AuthManager auth = new AuthManager(getApplicationContext());
                                   auth.saveAccessTokenFromResponse(prettyJson, getApplicationContext());

                               } catch (Exception e) {
                                   Log.e("JSON_PARSE_ERROR", "Ошибка при парсинге JSON", e);
                               }
                           }
                           catch (Exception e){
                               Log.e("JSON_PARSE_ERROR", "Ошибка при парсинге JSON", e);

                           }
                                                  Intent intent = new Intent(OTPVerify.this, ChangePassword.class);
                            intent.putExtra("email", email);
                            intent.putExtra("otp", otpCode);
                            startActivity(intent);
                            finish();
                        } else {
                            Toast.makeText(OTPVerify.this, "Invalid OTP code", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            });
        } catch (Exception e) {
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}