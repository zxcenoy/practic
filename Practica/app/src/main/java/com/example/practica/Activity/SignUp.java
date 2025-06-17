package com.example.practica.Activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageButton;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.practica.R;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class SignUp extends AppCompatActivity {

    private TextInputEditText etUsername, etEmail, etPassword1, etPassword2;
    private TextInputLayout layoutUsername, layoutEmail, layoutPassword1, layoutPassword2;
    private AppCompatImageButton nextButton4;

    private static final String BASE_URL = "https://xenkjiywsgjtgtiyfwxg.supabase.co/auth/v1/signup";
    private static final String API_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InhlbmtqaXl3c2dqdGd0aXlmd3hnIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NDcxMDkwMzEsImV4cCI6MjA2MjY4NTAzMX0.DkEOCkk34vyLZiJq7ivhU0XUIT8l7Z7pu7pP21TF2XU";
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sign_up);

        etUsername = findViewById(R.id.UserNameSignUp);
        etEmail = findViewById(R.id.etEmail);
        etPassword1 = findViewById(R.id.PasswordSignUp1);
        etPassword2 = findViewById(R.id.PasswordSignUP2);
        nextButton4 = findViewById(R.id.nextButton4);

        layoutUsername = findViewById(R.id.UserNameSignUP);
        layoutEmail = findViewById(R.id.emailInputLayoutSignUP);
        layoutPassword1 = findViewById(R.id.passwordInputLayout);
        layoutPassword2 = findViewById(R.id.passwordInputLayout2);

        setupTextWatchers();

        nextButton4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validateFields()) {
                    registerUser();
                }
            }
        });

    }

    private void setupTextWatchers() {
        etUsername.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                validateUsername();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        etEmail.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                validateEmail();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        etPassword1.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                validatePasswords();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        etPassword2.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                validatePasswords();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private boolean validateFields() {
        return validateUsername() && validateEmail() && validatePasswords();
    }

    private boolean validateUsername() {
        String text = etUsername.getText().toString().trim();
        if (text.isEmpty()) {
            layoutUsername.setError("Username is required");
            return false;
        } else {
            layoutUsername.setError(null);
            return true;
        }
    }

    private boolean validateEmail() {
        String text = etEmail.getText().toString().trim();
        if (text.isEmpty()) {
            layoutEmail.setError("Email is required");
            return false;
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(text).matches()) {
            layoutEmail.setError("Enter a valid email");
            return false;
        } else {
            layoutEmail.setError(null);
            return true;
        }
    }

    private boolean validatePasswords() {
        String pass1 = etPassword1.getText().toString().trim();
        String pass2 = etPassword2.getText().toString().trim();

        if (pass1.isEmpty()) {
            layoutPassword1.setError("Password is required");
            layoutPassword2.setError(null);
            return false;
        } else if (pass2.isEmpty()) {
            layoutPassword2.setError("Confirm password is required");
            layoutPassword1.setError(null);
            return false;
        } else if (!pass1.equals(pass2)) {
            layoutPassword1.setError("Passwords do not match");
            layoutPassword2.setError("Passwords do not match");
            return false;
        } else {
            layoutPassword1.setError(null);
            layoutPassword2.setError(null);
            return true;
        }
    }

    private void registerUser() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword1.getText().toString().trim();

        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("email", email);
            jsonBody.put("password", password);
        } catch (Exception e) {
            Toast.makeText(this, "Error creating request", Toast.LENGTH_SHORT).show();
            return;
        }

        RequestBody body = RequestBody.create(jsonBody.toString(), JSON);

        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url(BASE_URL)
                .post(body)
                .addHeader("apikey", API_KEY)
                .addHeader("Content-Type", "application/json")
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() ->{
                    Log.e("Network error: " , e.getMessage().toString());
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    runOnUiThread(() -> {
                        Intent intent = new Intent(SignUp.this, SignIn.class);
                        startActivity(intent);
                    });
                } else {
                    runOnUiThread(() -> {
                        Log.e("",String.valueOf(response));
                    });
                }
            }
        });
    }
    public void BackToSignIn(View view){
        Intent intent = new Intent(this,SignIn.class);
        startActivity(intent);
    }
    public void SignInClick(View view){
        Intent intent = new Intent(this, SignIn.class);
        startActivity(intent);
    }
}