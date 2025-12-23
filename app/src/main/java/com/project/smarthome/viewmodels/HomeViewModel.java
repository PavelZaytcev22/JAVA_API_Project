package com.project.smarthome.viewmodels;

import android.app.Application;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.project.smarthome.models.devices.Device;
import com.project.smarthome.models.homes.Home;
import com.project.smarthome.models.homes.room.Room;
import com.project.smarthome.repositories.DeviceRepository;

import java.util.ArrayList;
import java.util.List;

public class HomeViewModel extends AndroidViewModel {

    private final DeviceRepository repository;

    private final MutableLiveData<List<Device>> devices = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<List<Room>> rooms = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<List<Home>> homes = new MutableLiveData<>(new ArrayList<>());

    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private final MutableLiveData<Boolean> connectionStatus = new MutableLiveData<>(true);

    // Храним текущие ID
    private int currentHomeId = -1;
    private int currentRoomId = -1;

    public HomeViewModel(Application application) {
        super(application);
        repository = new DeviceRepository(application);
    }

    public LiveData<List<Device>> getDevices() { return devices; }
    public LiveData<List<Room>> getRooms() { return rooms; }
    public LiveData<List<Home>> getHomes() { return homes; }
    public LiveData<Boolean> getIsLoading() { return isLoading; }
    public LiveData<String> getErrorMessage() { return errorMessage; }
    public LiveData<Boolean> getConnectionStatus() { return connectionStatus; }

    public int getCurrentHomeId() { return currentHomeId; }
    public int getCurrentRoomId() { return currentRoomId; }

    public void loadData() {
        if (!repository.isAuthenticated()) {
            errorMessage.setValue("Пользователь не авторизован");
            return;
        }
        isLoading.setValue(true);

//        repository.getHomes()
//                .thenAccept(homeList -> {
//                    homes.postValue(homeList);
//
//                    if (!homeList.isEmpty()) {
//                        currentHomeId = homeList.get(0).getId();
//                        loadRooms(currentHomeId);
//                    } else {
//                        isLoading.postValue(false);
//                        errorMessage.postValue("Дома не найдены");
//                    }
//                })
//                .exceptionally(th -> {
//                    errorMessage.postValue("Не удалось загрузить дома: " + th.getMessage());
//                    connectionStatus.postValue(false);
//                    isLoading.postValue(false);
//                    return null;
//                });
    }

    public void loadRooms(int homeId) {
        currentHomeId = homeId;

//        repository.getRooms(homeId)
//                .thenAccept(roomList -> {
//                    rooms.postValue(roomList);
//
//                    if (!roomList.isEmpty()) {
//                        currentRoomId = roomList.get(0).getId();
//                        loadDevices(homeId);
//                    } else {
//                        isLoading.postValue(false);
//                        devices.postValue(new ArrayList<>()); // Очищаем список устройств
//                    }
//                })
//                .exceptionally(th -> {
//                    errorMessage.postValue("Не удалось загрузить комнаты: " + th.getMessage());
//                    connectionStatus.postValue(false);
//                    isLoading.postValue(false);
//                    return null;
//                });
    }

    public void loadDevices(int homeId) {
        repository.getDevices(homeId)
                .thenAccept(deviceList -> {
                    devices.postValue(deviceList);
                    isLoading.postValue(false);
                    connectionStatus.postValue(true);
                })
                .exceptionally(th -> {
                    errorMessage.postValue("Не удалось загрузить устройства: " + th.getMessage());
                    connectionStatus.postValue(false);
                    isLoading.postValue(false);
                    return null;
                });
    }

    public void controlDevice(int deviceId, String newState) {
        repository.controlDevice(deviceId, newState)
                .thenAccept(result -> {
                    // Обновляем устройство в списке
                    List<Device> deviceList = devices.getValue();
                    if (deviceList != null) {
                        for (Device device : deviceList) {
                            if (device.getId() == deviceId) {
                                device.setState(newState);
                                break;
                            }
                        }
                        devices.postValue(deviceList);
                    }
                })
                .exceptionally(th -> {
                    errorMessage.postValue("Не удалось управлять устройством: " + th.getMessage());
                    connectionStatus.postValue(false);
                    return null;
                });
    }

    public void refreshData() {
        if (currentHomeId != -1) {
            loadDevices(currentHomeId);
        } else {
            loadData();
        }
    }

    public void setCurrentHomeId(int homeId) {
        this.currentHomeId = homeId;
        loadRooms(homeId);
    }

    public void setCurrentRoomId(int roomId) {
        this.currentRoomId = roomId;
        // Для комнат фильтруем устройства
        List<Device> allDevices = devices.getValue();
        if (allDevices != null) {
            List<Device> filteredDevices = new ArrayList<>();
            for (Device device : allDevices) {
                if (device.getRoomId() != null && device.getRoomId() == roomId) {
                    filteredDevices.add(device);
                }
            }
            devices.postValue(filteredDevices);
        }
    }
}