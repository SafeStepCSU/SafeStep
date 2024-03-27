package com.example.myapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.ParcelUuid;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.chaquo.python.PyObject;
import com.chaquo.python.Python;
import com.chaquo.python.android.AndroidPlatform;
import com.example.myapplication.ui.profile.ProfileActivity;

import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    // GATT Service and Characteristic, predefined "environmental_sensing" and "temperature"
    private static final UUID UUID_ENV_SENSE_SERVICE = UUID.fromString("0000181A-0000-1000-8000-00805f9b34fb");
    private static final UUID UUID_TEMP_CHAR = UUID.fromString("00002A6E-0000-1000-8000-00805f9b34fb");


    private BluetoothAdapter bluetoothAdapter;
    private BluetoothLeScanner bluetoothLeScanner;
    private BluetoothGatt bluetoothGatt;
    private boolean scanning;
    private Handler handler = new Handler();

    // Duration for scanning (in milliseconds)
    private static final long SCAN_PERIOD = 60000; // 60 seconds


    OkHttpClient client;
    String url = "http://10.0.2.2:5000/data";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        client = new OkHttpClient();

        Button button = findViewById(R.id.button);
        Button button2 = findViewById(R.id.button2);
        Button button3 = findViewById(R.id.button3);
        Button button4 = findViewById(R.id.button4);
        Button btnProfile = findViewById(R.id.btnProfile);

        // Start Python
        if (! Python.isStarted()) {
            Python.start(new AndroidPlatform(this));
        }

        // chaquopy - Python SDK so that we can call Python functions within java
        Python py = Python.getInstance();
        PyObject myModule = py.getModule("test");
        PyObject myFnCallVale = myModule.get("simple_sort");
        int[] numList = {1, 3, 2, 5, 9};
        System.out.println("The Function Call's Return Value: " + myFnCallVale.call(numList).toString());

        // Bluetooth Stuff
        BluetoothManager bluetoothManager = getSystemService(BluetoothManager.class);
        bluetoothAdapter = bluetoothManager.getAdapter();
        bluetoothLeScanner = bluetoothAdapter.getBluetoothLeScanner();


        button.setOnClickListener(v -> {
            Log.d("MainActivity", "Button clicked");
            testProtectedGetRequest();
        });

        button2.setOnClickListener(v -> {
            Log.d("MainActivity", "Button2 clicked");
            testProtectedPostRequest();
        });

        button3.setOnClickListener(v -> {
            Log.d("MainActivity", "Button3 clicked");
            testBluetooth();
        });

        button4.setOnClickListener(v -> {
            Log.d("MainActivity", "Button4 clicked");
            getBluetoothDevices();
        });

        btnProfile.setOnClickListener(v -> {
            Log.d("MainActivity", "Profile button clicked");
            showProfile();
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
                        // it's important to call response.body().string() only once since it consumes and closes the response body, probably should add a check for null too...
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
                    response.close(); // Make sure to close the response to avoid leaks
                }
            }
        });
    }

    private void showProfile() {
        // switch to the profile activity
        Intent intent = new Intent(this, ProfileActivity.class);
        startActivity(intent);
    }

    private void getBluetoothDevices() {
        if (ActivityCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{android.Manifest.permission.BLUETOOTH_CONNECT}, 1);
            return;
        }

        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
        if (!pairedDevices.isEmpty()) {
            // List paired devices
            for (BluetoothDevice device : pairedDevices) {
                Log.d("Paired Device", "Name: " + device.getName() + ", Address: " + device.getAddress());
                // Consider showing these devices in a UI element
            }
        } else {
            Toast.makeText(this, "No paired Bluetooth devices found", Toast.LENGTH_SHORT).show();
        }
    }

    private String getToken() {
        SharedPreferences sharedPreferences = getSharedPreferences("MySharedPref", MODE_PRIVATE);
        return sharedPreferences.getString("token", "");
    }


    private void scanLeDevice() {
        if (!scanning) {
            //Stops scanning after a predefined scan period.
            handler.postDelayed(() -> {
                scanning = false;
                if (ActivityCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(MainActivity.this, "Bluetooth scan permission not granted", Toast.LENGTH_SHORT).show();
                    return;
                }
                bluetoothLeScanner.stopScan(scanCallback);
                Log.d("Bluetooth", "Stopped scanning for Bluetooth devices after " + SCAN_PERIOD + " milliseconds");
            }, SCAN_PERIOD);

            scanning = true;

            // Scan for devices advertising the environmental sensing service
            // This was for testing.. its not being used right now but you can add the filter scan in the startScan method, its currently null (line 283)
            ScanFilter scanFilter = new ScanFilter.Builder()
                    .setServiceUuid(new ParcelUuid(UUID_ENV_SENSE_SERVICE))
                    .build();

            List<ScanFilter> filters = new ArrayList<>();
            filters.add(scanFilter);

            // Scan settings - low latency mode
            ScanSettings settings = new ScanSettings.Builder()
                    .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                    .build();
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Bluetooth scan permission not granted", Toast.LENGTH_SHORT).show();
                return;
            }
            Log.d("Bluetooth", "Started scanning for Bluetooth devices.....");

            // startScan - (filters, settings, callback)
            bluetoothLeScanner.startScan(null, settings, scanCallback);

            //bluetoothLeScanner.startScan(scanCallback);
        } else {
            scanning = false;
            Log.d("Bluetooth", "Stopped scanning for Bluetooth devices");
            bluetoothLeScanner.stopScan(scanCallback);
        }
    }

    // Device scan callback.
    private final ScanCallback scanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);
            BluetoothDevice device = result.getDevice();
            if (ActivityCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(MainActivity.this, "Bluetooth connect permission not granted", Toast.LENGTH_SHORT).show();
                return;
            }
            Log.d("Bluetooth", "Found device: " + device.getName() + ", " + device.getAddress());
            Toast.makeText(MainActivity.this, "Found device: " + device.getName() + ", " + device.getAddress(), Toast.LENGTH_SHORT).show();
            if (device.getName() != null) {
                Log.d("Bluetooth", "Connecting to device: " + device.getName() + ", " + device.getAddress());
                connectToDevice(device);
            }
        }

        @Override
        public void onScanFailed(int errorCode) {
            super.onScanFailed(errorCode);
            Log.d("Bluetooth", "Scan failed with error code: " + errorCode);
        }
    };

    private void connectToDevice(BluetoothDevice device) {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(MainActivity.this, "Bluetooth connect permission not granted", Toast.LENGTH_SHORT).show();
            return;
        }

        Log.d("Bluetooth", "Connecting to GATT server");
        bluetoothGatt = device.connectGatt(this, false, gattCallback);
    }

    // Bluetooth GATT callback
    private BluetoothGattCallback gattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                Log.d("Bluetooth", "Connected to GATT server.");
                if (ActivityCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
                bluetoothGatt.discoverServices();
                Log.d("Bluetooth", "Discovering services");
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                Log.d("Bluetooth", "Disconnected from GATT server.");
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                BluetoothGattService envSensingService = bluetoothGatt.getService(UUID_ENV_SENSE_SERVICE);
                if (envSensingService != null) {
                    BluetoothGattCharacteristic tempCharacteristic = envSensingService.getCharacteristic(UUID_TEMP_CHAR);
                    if (tempCharacteristic != null) {
                        if (ActivityCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                            return;
                        }
                        bluetoothGatt.setCharacteristicNotification(tempCharacteristic, true);
                        BluetoothGattDescriptor descriptor = tempCharacteristic.getDescriptor(UUID.fromString("00002902-0000-1000-8000-00805f9b34fb"));
                        descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                        bluetoothGatt.writeDescriptor(descriptor);
                        Log.d("Bluetooth", "Subscribed to temperature characteristic");
                    }
                }
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            if (characteristic.getUuid().equals(UUID_TEMP_CHAR)) {
                byte[] tempData = characteristic.getValue();
                // Process the received temperature data
                Log.d("Bluetooth", "Received temperature data: " + new String(tempData));
            }
        }
    };


    private void testBluetooth() {
        if (!bluetoothAdapter.isEnabled()) {
            Toast.makeText(this, "Bluetooth is disabled", Toast.LENGTH_SHORT).show();
            // TODO: Request user to enable Bluetooth
            return;
        }

        // Check for permissions (Android 6.0+ runtime permissions)
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.BLUETOOTH_SCAN}, 1);
        }

        // Start scanning
        Log.d("BLUETOOTH", "Scanning for Bluetooth devices");
        scanLeDevice();
    }
}