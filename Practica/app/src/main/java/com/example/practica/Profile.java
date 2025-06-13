    package com.example.practica;

    import androidx.appcompat.app.AppCompatActivity;
    import androidx.appcompat.widget.AppCompatImageButton;

    import android.content.Context;
    import android.content.Intent;
    import android.content.SharedPreferences;
    import android.net.Uri;
    import android.os.Bundle;
    import android.util.Log;
    import android.view.View;
    import android.widget.ImageView;
    import android.widget.TextView;
    import android.widget.Toast;

    import com.squareup.picasso.Picasso;

    import org.json.JSONArray;
    import org.json.JSONException;
    import org.json.JSONObject;

    import java.io.IOException;
    import java.io.InputStream;

    import okhttp3.Call;
    import okhttp3.Callback;
    import okhttp3.HttpUrl;
    import okhttp3.MediaType;
    import okhttp3.MultipartBody;
    import okhttp3.OkHttpClient;
    import okhttp3.Request;
    import okhttp3.RequestBody;
    import okhttp3.Response;

    public class Profile extends AppCompatActivity {
        private AuthManager authManager;
        private TextView name;
        private static final int CHANGE_NAME_REQUEST = 1;
        private static final int CHANGE_EMAIL_REQUEST = 2;
        private static final int CHANGE_ADDRESS_REQUEST = 3;
        private static final int PICK_IMAGE_REQUEST = 101;



        private TextView email;
        private ImageView avatar;


        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.profile);

            authManager = new AuthManager(this);
            name = findViewById(R.id.name);
            email = findViewById(R.id.email);
            SharedPreferences prefs = getSharedPreferences("my_app_data", MODE_PRIVATE);
            String userName = prefs.getString("user_name", "");
            if (!userName.isEmpty()) {
                name.setText(userName);
            }
            name.setText(prefs.getString("user_name", ""));
            email.setText(prefs.getString("user_email", ""));
            avatar = findViewById(R.id.avatar);
            avatar.setOnClickListener(v -> openImageChooser());

            TextView addressView = findViewById(R.id.adress);
            addressView.setText(prefs.getString("user_address", ""));


            AppCompatImageButton logoutButton = findViewById(R.id.LogOutButton);
            logoutButton.setOnClickListener(v -> {
                authManager.logout();
                Toast.makeText(Profile.this, "Logging out...", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(Profile.this, SignIn.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            });

            String cachedAvatar = prefs.getString("avatar_url", "");
            if (!cachedAvatar.isEmpty()) {
                Picasso.get()
                        .load(cachedAvatar)
                        .placeholder(R.drawable.profile)
                        .into(avatar);
            }
            fetchUserProfile();

            AppCompatImageButton editNameButton = findViewById(R.id.editNameButton);
            AppCompatImageButton editPasswordButton = findViewById(R.id.editPasswordButton);
            AppCompatImageButton editEmailButton = findViewById(R.id.editEmailButton);
            AppCompatImageButton editAdressButton = findViewById(R.id.editAddressButton);

            editNameButton.setOnClickListener(v -> {
                Intent intent = new Intent(Profile.this, ChangeName.class);
                startActivityForResult(intent, CHANGE_NAME_REQUEST);
            });

            editPasswordButton.setOnClickListener(v -> {
                startActivity(new Intent(Profile.this, ChangePassword.class));
            });

            editEmailButton.setOnClickListener(v -> {
                Intent intent = new Intent(Profile.this, ChangeEmail.class);
                startActivityForResult(intent, CHANGE_EMAIL_REQUEST);
            });

            editAdressButton.setOnClickListener(v -> {
                Intent intent = new Intent(Profile.this, ChangeAddress.class);
                startActivityForResult(intent, CHANGE_ADDRESS_REQUEST);
            });
            fetchUserProfile();

        }

        private void openImageChooser() {
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
        }

        @Override
        protected void onActivityResult(int requestCode, int resultCode, Intent data) {
            super.onActivityResult(requestCode, resultCode, data);
            if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
                Uri imageUri = data.getData();
                avatar.setImageURI(imageUri);
                uploadImageToSupabase(imageUri);

            }

            if (requestCode == CHANGE_NAME_REQUEST && resultCode == RESULT_OK) {
                String newName = data.getStringExtra("new_name");
                if (newName != null) {
                    name.setText(newName);
                }
            }
            else if (requestCode == CHANGE_EMAIL_REQUEST && resultCode == RESULT_OK) {
                String newEmail = data.getStringExtra("new_email");
                if (newEmail != null) {
                    email.setText(newEmail);
                    SharedPreferences.Editor editor = getSharedPreferences("my_app_data", MODE_PRIVATE).edit();
                    editor.putString("user_email", newEmail);
                    editor.apply();
                    fetchUserProfile();
                }
            }
            else if (requestCode == CHANGE_ADDRESS_REQUEST && resultCode == RESULT_OK) {
                String newAddress = data.getStringExtra("new_address");
                if (newAddress != null) {
                    TextView addressView = findViewById(R.id.adress);
                    addressView.setText(newAddress);

                    SharedPreferences.Editor editor = getSharedPreferences("my_app_data", MODE_PRIVATE).edit();
                    editor.putString("user_address", newAddress);
                    editor.apply();

                    //fetchUserProfile();
                }
            }
        }
        private void uploadImageToSupabase(Uri imageUri) {
            String userId = authManager.getCurrentUserId();
            if (userId == null) {
                Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show();
                return;
            }
            try {
                InputStream inputStream = getContentResolver().openInputStream(imageUri);
                byte[] imageBytes = new byte[inputStream.available()];
                inputStream.read(imageBytes);

                RequestBody requestBody = new MultipartBody.Builder()
                        .setType(MultipartBody.FORM)
                        .addFormDataPart("file", "avatar.jpg",
                                RequestBody.create(imageBytes, MediaType.parse("image/jpeg")))
                        .build();

                SharedPreferences prefs = getSharedPreferences("my_app_data", MODE_PRIVATE);
                String accessToken = prefs.getString("access_token", null);

                Request request = new Request.Builder()
                        .url("https://xenkjiywsgjtgtiyfwxg.supabase.co/storage/v1/object/avatars/" + userId + "/avatar.jpg")
                        .put(requestBody)
                        .addHeader("apikey", getString(R.string.supabase_anon_key))
                        .addHeader("Authorization", "Bearer " + accessToken)
                        .build();

                new OkHttpClient().newCall(request).enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        runOnUiThread(() ->
                                Toast.makeText(Profile.this, "Upload failed: " + e.getMessage(), Toast.LENGTH_LONG).show());
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        runOnUiThread(() -> {
                            if (response.isSuccessful()) {
                                updateProfileWithAvatarUrl(userId);
                            } else {
                                Toast.makeText(Profile.this, "Upload failed: " + response.code(), Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                });
            } catch (Exception e) {
                Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
        private void updateProfileWithAvatarUrl(String userId) {
            String avatarUrl = "https://xenkjiywsgjtgtiyfwxg.supabase.co/storage/v1/object/public/avatars/" + userId + "/avatar.jpg";

            JSONObject jsonBody = new JSONObject();
            try {
                jsonBody.put("avatar_url", avatarUrl);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            SharedPreferences prefs = getSharedPreferences("my_app_data", MODE_PRIVATE);
            String accessToken = prefs.getString("access_token", null);

            Request request = new Request.Builder()
                    .url("https://xenkjiywsgjtgtiyfwxg.supabase.co/rest/v1/profiles?id=eq." + userId)
                    .patch(RequestBody.create(jsonBody.toString(), MediaType.get("application/json")))
                    .addHeader("apikey", getString(R.string.supabase_anon_key))
                    .addHeader("Authorization", "Bearer " + accessToken)
                    .addHeader("Content-Type", "application/json")
                    .build();

            new OkHttpClient().newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    runOnUiThread(() ->
                            Toast.makeText(Profile.this, "Profile update failed", Toast.LENGTH_SHORT).show());
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    runOnUiThread(() -> {
                        if (response.isSuccessful()) {
                            Toast.makeText(Profile.this, "Avatar updated!", Toast.LENGTH_SHORT).show();
                            // Сохраняем URL аватара локально
                            SharedPreferences.Editor editor = prefs.edit();
                            editor.putString("avatar_url", avatarUrl);
                            editor.putString("user_avatar_updated", "true");
                            editor.apply();
                        } else {
                            Toast.makeText(Profile.this, "Failed to update profile", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            });
        }
        private void fetchUserProfile() {
            OkHttpClient client = new OkHttpClient();
            SharedPreferences prefs = getSharedPreferences("my_app_data", MODE_PRIVATE);
            String accessToken = prefs.getString("access_token", null);
            String userId = prefs.getString("user_id", null);

            if (userId == null || accessToken == null) {
                Toast.makeText(this, "Authentication required", Toast.LENGTH_SHORT).show();
                return;
            }


            // Запрос для получения данных из profiles
            HttpUrl profilesUrl = HttpUrl.parse("https://xenkjiywsgjtgtiyfwxg.supabase.co/rest/v1/profiles")
                    .newBuilder()
                    .addQueryParameter("id", "eq." + userId)
                    .addQueryParameter("select", "full_name,address")
                    .build();

            HttpUrl authUrl = HttpUrl.parse("https://xenkjiywsgjtgtiyfwxg.supabase.co/rest/v1/users")
                    .newBuilder()
                    .addQueryParameter("id", "eq." + userId)
                    .addQueryParameter("select", "email")
                    .build();

            Request profilesRequest = new Request.Builder()
                    .url(profilesUrl)
                    .get()
                    .addHeader("apikey", getString(R.string.supabase_anon_key))
                    .addHeader("Authorization", "Bearer " + accessToken)
                    .build();

            Request authRequest = new Request.Builder()
                    .url(authUrl)
                    .get()
                    .addHeader("apikey", getString(R.string.supabase_anon_key))
                    .addHeader("Authorization", "Bearer " + accessToken)
                    .build();

            client.newCall(profilesRequest).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    runOnUiThread(() -> {
                        Log.e("Profile", "Error fetching profile data", e);
                        Toast.makeText(Profile.this, "Failed to load profile data", Toast.LENGTH_SHORT).show();
                    });
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (response.isSuccessful()) {
                        try {
                            String responseBody = response.body().string();
                            JSONArray profiles = new JSONArray(responseBody);

                            if (profiles.length() > 0) {
                                JSONObject profile = profiles.getJSONObject(0);
                                String fullName = profile.optString("full_name", "");
                                String avatarUrl = profile.optString("avatar_url", "");
                                String address = profile.optString("address", "");
                                String adressemail = profile.optString("user_email","");

                                runOnUiThread(() -> {
                                    if (!fullName.isEmpty()) {
                                        name.setText(fullName);
                                        SharedPreferences.Editor editor = prefs.edit();
                                        editor.putString("user_name", fullName);

                                        editor.apply();
                                    }
                                    if (!address.isEmpty()) {
                                        TextView addressView = findViewById(R.id.adress);
                                        addressView.setText(address);
                                        SharedPreferences.Editor editor = prefs.edit();
                                        editor.putString("user_address", address);
                                        editor.apply();
                                    }
                                    if (!adressemail.isEmpty()){
                                        email.setText(adressemail);
                                        SharedPreferences.Editor editor1 = prefs.edit();
                                        editor1.putString("user_email",adressemail);
                                        editor1.apply();
                                    }
                                    if (!avatarUrl.isEmpty()) {
                                        if (!prefs.getString("user_avatar_updated", "false").equals("true")) {
                                            Picasso.get()
                                                    .load(avatarUrl)
                                                    .placeholder(R.drawable.profile)
                                                    .into(avatar);
                                        }
                                    }
                                });
                            }
                        } catch (Exception e) {
                            runOnUiThread(() -> {
                                Log.e("Profile", "Error parsing profile data", e);
                            });
                        }
                    }
                }
            });

            client.newCall(authRequest).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    runOnUiThread(() -> {
                        Log.e("Profile", "Error fetching auth data", e);
                    });
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (response.isSuccessful()) {
                        try {
                            String responseBody = response.body().string();
                            JSONArray users = new JSONArray(responseBody);

                            if (users.length() > 0) {
                                JSONObject user = users.getJSONObject(0);
                                String userEmail = user.optString("email", "");

                                runOnUiThread(() -> {
                                    // Обновляем email
                                    if (!userEmail.isEmpty()) {
                                        email.setText(userEmail);
                                        SharedPreferences.Editor editor = prefs.edit();
                                        editor.putString("user_email", userEmail);
                                        editor.apply();
                                    }
                                });
                            }
                        } catch (Exception e) {
                            runOnUiThread(() -> {
                                Log.e("Profile", "Error parsing auth data", e);
                            });
                        }
                    }
                }
            });
        }
    }





