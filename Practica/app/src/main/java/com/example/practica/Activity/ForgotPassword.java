package com.example.practica.Activity;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.example.practica.R;

import org.json.JSONObject;

import java.io.IOException;

import okhttp3.*;

public class ForgotPassword extends AppCompatActivity {

    private EditText etEmail;
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");
    private final OkHttpClient client = new OkHttpClient();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.forgot_password_enter_email);

        etEmail = findViewById(R.id.etEmail);
        findViewById(R.id.resetPasswordButton).setOnClickListener(v -> sendRecoveryEmail());
    }

    private void sendRecoveryEmail() {
        String email = etEmail.getText().toString().trim();

        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, getString(R.string.PleaseEnterAvalidEmail), Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            JSONObject jsonBody = new JSONObject();
            jsonBody.put("email", email);

            Request request = new Request.Builder()
                    .url("https://xenkjiywsgjtgtiyfwxg.supabase.co/auth/v1/recover")
                    .post(RequestBody.create(jsonBody.toString(), JSON))
                    .addHeader("apikey", getString(R.string.supabase_anon_key))
                    .addHeader("Content-Type", "application/json")
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    runOnUiThread(() -> {
                        Log.e("Network error:", e.getMessage().toString());
                    });
                }

                @Override
                public void onResponse(Call call, Response response) {
                    runOnUiThread(() -> {
                        if (response.isSuccessful()) {
                            Toast.makeText(ForgotPassword.this, getString(R.string.OTPSend), Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(ForgotPassword.this, OTPVerify.class);
                            intent.putExtra("email", email);
                            startActivity(intent);
                        } else {
                            Toast.makeText(ForgotPassword.this, getString(R.string.OTPDontSend), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            });
        } catch (Exception e) {
            Log.e("ERROR SEND",e.getMessage().toString());
        }
    }
    public void BackToSignUp(View view){
        Intent intent = new Intent(this,SignIn.class);
        startActivity(intent);
    }
}