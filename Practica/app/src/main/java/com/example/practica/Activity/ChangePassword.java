package com.example.practica.Activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;

import com.example.practica.R;

import org.json.JSONObject;

import java.io.IOException;

import okhttp3.*;

public class ChangePassword extends AppCompatActivity {

    private EditText etNewPassword, etConfirmPassword;
    private String email, otp;
    private final OkHttpClient client = new OkHttpClient();
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.change_password);

        email = getIntent().getStringExtra("email");
        otp = getIntent().getStringExtra("otp");

        etNewPassword = findViewById(R.id.newPassword);
        etConfirmPassword = findViewById(R.id.newPassword2);

        findViewById(R.id.changePasswordButton).setOnClickListener(v -> changePassword());
    }

    private void changePassword() {
        String newPassword = etNewPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();

        if (newPassword.isEmpty() || confirmPassword.isEmpty()) {
            Toast.makeText(this, getString(R.string.PleaseEnterAndConfirmNewPassword), Toast.LENGTH_SHORT).show();
            return;
        }

        if (!newPassword.equals(confirmPassword)) {
            Toast.makeText(this, getString(R.string.PasswordsDoNotMatch), Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("password", newPassword);

            RequestBody body = RequestBody.create(
                    jsonObject.toString(),
                    MediaType.parse("application/json")
            );
            SharedPreferences sharedPref = getSharedPreferences("my_app_data", Context.MODE_PRIVATE);
            String token = sharedPref.getString("access_token", null);

            Request request = new Request.Builder()
                    .url("https://xenkjiywsgjtgtiyfwxg.supabase.co/auth/v1/user")
                    .method("PUT", body)
                    .addHeader("apikey", getString(R.string.supabase_anon_key))
                    .addHeader("Authorization","Bearer " + token)
                    .addHeader("Content-Type", "application/json")
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    runOnUiThread(() ->{
                        Log.e("Network error", String.valueOf(e));
                    });


                }

                @Override
                public void onResponse(Call call, Response response) {
                    runOnUiThread(() -> {
                        if (response.isSuccessful()) {
                            startActivity(new Intent(ChangePassword.this, SignIn.class));
                            finishAffinity();

                        } else {
                            Log.e("",String.valueOf(response));
                        }
                    });
                }
            });
        } catch (Exception e) {
            Log.e("Error",e.getMessage().toString());
        }
    }
}