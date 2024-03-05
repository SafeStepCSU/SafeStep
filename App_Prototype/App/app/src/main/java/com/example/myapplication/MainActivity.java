package com.example.myapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    OkHttpClient client;
    String url = "http://10.0.2.2:5000/data";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        client = new OkHttpClient();

        Button button = findViewById(R.id.button);
        Button button2 = findViewById(R.id.button2);

        button.setOnClickListener(v -> {
            Log.d("MainActivity", "Button clicked");
            testProtectedGetRequest();
        });

        button2.setOnClickListener(v -> {
            Log.d("MainActivity", "Button2 clicked");
            testProtectedPostRequest();
        });
    }

    public void disable(View view) {
        view.setEnabled(false);
        Log.d("success", "Button disabled");
    }

    private void testProtectedGetRequest() {
        String token = getToken();
        Log.d("MainActivity", "Token: " + token);

        Log.d("MainActivity", "Sending request to " + url);
        // create a request
        Request request = new Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer " + token) // authorization header
                .build();

        // send the request
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                e.printStackTrace();
                Toast.makeText(MainActivity.this, "Request failed", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                try {
                    if (response.isSuccessful()) {
                        String responseString = response.body().string();
                        Log.d("MainActivity", "Response: " + responseString);
                        runOnUiThread(() -> Toast.makeText(MainActivity.this, responseString, Toast.LENGTH_SHORT).show());
                    } else {
                        Log.d("MainActivity", "Response: " + response.code());
                        runOnUiThread(() -> Toast.makeText(MainActivity.this, "Request failed with code: " + response.code(), Toast.LENGTH_SHORT).show());
                    }
                } finally {
                    response.close(); // Make sure to close the response to avoid leaks
                }
            }
        });
    }

    private void testProtectedPostRequest() {
        String token = getToken();
        MediaType JSON = MediaType.parse("application/json; charset=utf-8"); // Set JSON media type
        JSONObject jsonObject = new JSONObject(); // Create JSON object
        try {
            // add sensor and temperature to the JSON object
            jsonObject.put("sensor1", 75);
            jsonObject.put("temperature", 25);
        } catch (Exception e) {
            e.printStackTrace();
        }

        RequestBody body = RequestBody.create(jsonObject.toString(), JSON); // Create request body
        Request request = new Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer " + token) // authorization header
                .post(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                e.printStackTrace();
                runOnUiThread(() -> Toast.makeText(MainActivity.this, "Request failed", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                try {
                    if (response.isSuccessful()) {
                        String responseString = response.body().string();
                        Log.d("MainActivity", "Response: " + responseString);
                        runOnUiThread(() -> Toast.makeText(MainActivity.this, responseString, Toast.LENGTH_SHORT).show());
                    } else {
                        Log.d("MainActivity", "Response: " + response.code());
                        runOnUiThread(() -> Toast.makeText(MainActivity.this, "Request failed with code: " + response.code(), Toast.LENGTH_SHORT).show());
                    }
                } finally {
                    response.close();
                }
            }
        });

    }

    private String getToken() {
        SharedPreferences sharedPreferences = getSharedPreferences("MySharedPref", MODE_PRIVATE);
        return sharedPreferences.getString("token", "");
    }

}