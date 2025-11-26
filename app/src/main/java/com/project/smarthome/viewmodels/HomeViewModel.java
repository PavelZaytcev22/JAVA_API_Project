package com.project.smarthome.viewmodels;

import android.app.Application;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.project.smarthome.models.DeviceResponse;
import com.project.smarthome.models.HomeResponse;
import com.project.smarthome.models.RoomResponse;
import com.project.smarthome.repositories.DeviceRepository;

import java.util.ArrayList;
import java.util.List;

public class HomeViewModel extends AndroidViewModel {

    private final DeviceRepository repository;

    private final MutableLiveData<List<DeviceResponse>> devices = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<List<RoomResponse>> rooms = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<List<HomeResponse>> homes = new MutableLiveData<>(new ArrayList<>());

    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private final MutableLiveData<Boolean> connectionStatus = new MutableLiveData<>(true);

    public HomeViewModel(Application application) {
        super(application);
        repository = new DeviceRepository(application);
    }

    public LiveData<List<DeviceResponse>> getDevices() { return devices; }
    public LiveData<List<RoomResponse>> getRooms() { return rooms; }
    public LiveData<List<HomeResponse>> getHomes() { return homes; }
    public LiveData<Boolean> getIsLoading() { return isLoading; }
    public LiveData<String> getErrorMessage() { return errorMessage; }
    public LiveData<Boolean> getConnectionStatus() { return connectionStatus; }

    public void loadData() {
        if (!repository.isAuthenticated()) {
            errorMessage.setValue("User not authenticated");
            return;
        }

        isLoading.setValue(true);

        repository.getHomes()
                .thenAccept(homeList -> {
                    homes.postValue(homeList);

                    if (!homeList.isEmpty()) {
                        int homeId = homeList.get(0).getId();
                        repository.setCurrentHomeId(homeId);
                        loadRooms(homeId);
                    } else {
                        isLoading.postValue(false);
                    }
                })
                .exceptionally(th -> {
                    errorMessage.postValue("Failed to load homes: " + th.getMessage());
                    connectionStatus.postValue(false);
                    isLoading.postValue(false);
                    return null;
                });
    }

    private void loadRooms(int homeId) {
        repository.getRooms(homeId)
                .thenAccept(roomList -> {
                    rooms.postValue(roomList);

                    if (!roomList.isEmpty()) {
                        int roomId = roomList.get(0).getId();
                        repository.setCurrentRoomId(roomId);
                        loadDevices(roomId);
                    } else {
                        isLoading.postValue(false);
                    }
                })
                .exceptionally(th -> {
                    errorMessage.postValue("Failed to load rooms: " + th.getMessage());
                    connectionStatus.postValue(false);
                    isLoading.postValue(false);
                    return null;
                });
    }

    private void loadDevices(int roomId) {
        repository.getDevices(roomId)
                .thenAccept(deviceList -> {
                    devices.postValue(deviceList);
                    isLoading.postValue(false);
                    connectionStatus.postValue(true);
                })
                .exceptionally(th -> {
                    errorMessage.postValue("Failed to load devices: " + th.getMessage());
                    connectionStatus.postValue(false);
                    isLoading.postValue(false);
                    return null;
                });
    }

    public void toggleDevice(int deviceId) {
        repository.toggleDevice(deviceId)
                .thenAccept(updatedDevice -> {
                    List<DeviceResponse> deviceList = devices.getValue();
                    if (deviceList == null) return;

                    for (int i = 0; i < deviceList.size(); i++) {
                        if (deviceList.get(i).getId() == deviceId) {
                            deviceList.set(i, updatedDevice);
                            break;
                        }
                    }

                    devices.postValue(deviceList);
                })
                .exceptionally(th -> {
                    errorMessage.postValue("Failed to toggle device: " + th.getMessage());
                    connectionStatus.postValue(false);
                    return null;
                });
    }

    public void refreshData() {
        loadData();
    }
}
