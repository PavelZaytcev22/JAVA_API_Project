package com.project.smarthome;

import android.os.Bundle;
import android.view.View;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.project.smarthome.api.ApiClient;
import com.project.smarthome.api.ApiService;
import com.project.smarthome.models.*;
import com.project.smarthome.utils.SessionManager;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DeviceAddActivity extends AppCompatActivity {

    private EditText editName, editState;
    private Spinner spinnerType, spinnerRoom;
    private Button btnCreate;
    private ProgressBar progress;

    private ApiService apiService;
    private SessionManager sessionManager;

    private int homeId;
    private List<RoomResponse> roomList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_device);

        editName = findViewById(R.id.editDeviceName);
        editState = findViewById(R.id.editDeviceState);
        spinnerType = findViewById(R.id.spinnerDeviceType);
        spinnerRoom = findViewById(R.id.spinnerRoom);
        btnCreate = findViewById(R.id.btnCreateDevice);
        progress = findViewById(R.id.progressBarDevice);

        sessionManager = new SessionManager(this);
        apiService = ApiClient.getApiService(this);

        homeId = getIntent().getIntExtra("home_id", -1);
        if (homeId == -1) {
            Toast.makeText(this, "Ошибка: дом не выбран", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        loadRooms();

        btnCreate.setOnClickListener(v -> createDevice());
    }

    private void loadRooms() {
        progress.setVisibility(View.VISIBLE);

        String token = "Bearer " + sessionManager.getToken();

        apiService.getRooms(token, homeId).enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<List<RoomResponse>> call,
                                   @NonNull Response<List<RoomResponse>> response) {

                progress.setVisibility(View.GONE);

                if (!response.isSuccessful() || response.body() == null) {
                    Toast.makeText(DeviceAddActivity.this,
                            "Не удалось загрузить комнаты", Toast.LENGTH_SHORT).show();
                    return;
                }

                roomList = response.body();

                ArrayAdapter<String> adapter = new ArrayAdapter<>(
                        DeviceAddActivity.this,
                        android.R.layout.simple_spinner_item,
                        roomList.stream().map(RoomResponse::getName).toArray(String[]::new)
                );
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerRoom.setAdapter(adapter);
            }

            @Override
            public void onFailure(@NonNull Call<List<RoomResponse>> call, @NonNull Throwable t) {
                progress.setVisibility(View.GONE);
                Toast.makeText(DeviceAddActivity.this,
                        "Ошибка сети: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void createDevice() {
        String name = editName.getText().toString().trim();
        String type = spinnerType.getSelectedItem().toString();
        String state = editState.getText().toString().trim();

        if (name.isEmpty()) {
            Toast.makeText(this, "Введите название устройства", Toast.LENGTH_SHORT).show();
            return;
        }

        int roomId = roomList.get(spinnerRoom.getSelectedItemPosition()).getId();

        DeviceCreateRequest request = new DeviceCreateRequest(
                name, type, roomId, state
        );

        progress.setVisibility(View.VISIBLE);
        btnCreate.setEnabled(false);

        String token = "Bearer " + sessionManager.getToken();

        apiService.createDevice(token, homeId, request).enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<DeviceResponse> call,
                                   @NonNull Response<DeviceResponse> response) {

                progress.setVisibility(View.GONE);
                btnCreate.setEnabled(true);

                if (response.isSuccessful()) {
                    Toast.makeText(DeviceAddActivity.this,
                            "Устройство создано", Toast.LENGTH_SHORT).show();
                    setResult(RESULT_OK);
                    finish();
                } else {
                    Toast.makeText(DeviceAddActivity.this,
                            "Ошибка создания", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<DeviceResponse> call, @NonNull Throwable t) {
                progress.setVisibility(View.GONE);
                btnCreate.setEnabled(true);
                Toast.makeText(DeviceAddActivity.this,
                        "Ошибка сети: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
