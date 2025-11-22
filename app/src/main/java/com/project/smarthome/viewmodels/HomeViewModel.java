package com.project.smarthome.viewmodels;

import android.app.Application;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.project.smarthome.models.Device;
import com.project.smarthome.models.Home;
import com.project.smarthome.models.RoomCreateRequest;
import com.project.smarthome.repositories.DeviceRepository;
import java.util.ArrayList;
import java.util.List;

public class HomeViewModel extends AndroidViewModel {
    private final DeviceRepository deviceRepository;
    private final MutableLiveData<List<Device>> devices = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<List<RoomCreateRequest>> rooms = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<List<Home>> homes = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private final MutableLiveData<Boolean> connectionStatus = new MutableLiveData<>(true);

    public HomeViewModel(Application application) {
        super(application);
        this.deviceRepository = new DeviceRepository(application);
    }

    public LiveData<List<Device>> getDevices() { return devices; }
    public LiveData<List<RoomCreateRequest>> getRooms() { return rooms; }
    public LiveData<List<Home>> getHomes() { return homes; }
    public LiveData<Boolean> getIsLoading() { return isLoading; }
    public LiveData<String> getErrorMessage() { return errorMessage; }
    public LiveData<Boolean> getConnectionStatus() { return connectionStatus; }

    public void loadData() {
        if (!deviceRepository.isAuthenticated()) {
            errorMessage.setValue("User not authenticated");
            return;
        }

        isLoading.setValue(true);

        deviceRepository.getHomes()
                .thenAccept(homesList -> {
                    homes.postValue(homesList);
                    if (!homesList.isEmpty()) {
                        int homeId = homesList.get(0).getId();
                        deviceRepository.setCurrentHomeId(homeId);
                        loadRooms(homeId);
                    } else {
                        isLoading.postValue(false);
                        loadDemoData();
                    }
                })
                .exceptionally(throwable -> {
                    errorMessage.postValue("Error loading homes: " + throwable.getMessage());
                    isLoading.postValue(false);
                    connectionStatus.postValue(false);
                    return null;
                });
    }

    private void loadRooms(int homeId) {
        deviceRepository.getRooms(homeId)
                .thenAccept(roomsList -> {
                    rooms.postValue(roomsList);
                    if (!roomsList.isEmpty()) {
                        int roomId = roomsList.get(0).getId();
                        deviceRepository.setCurrentRoomId(roomId);
                        loadDevices(roomId);
                    } else {
                        isLoading.postValue(false);
                    }
                })
                .exceptionally(throwable -> {
                    errorMessage.postValue("Error loading rooms: " + throwable.getMessage());
                    isLoading.postValue(false);
                    connectionStatus.postValue(false);
                    return null;
                });
    }

    private void loadDevices(int roomId) {
        deviceRepository.getDevices(roomId)
                .thenAccept(devicesList -> {
                    devices.postValue(devicesList);
                    isLoading.postValue(false);
                    connectionStatus.postValue(true);
                })
                .exceptionally(throwable -> {
                    errorMessage.postValue("Error loading devices: " + throwable.getMessage());
                    isLoading.postValue(false);
                    connectionStatus.postValue(false);
                    return null;
                });
    }

    public void toggleDevice(int deviceId, boolean isOn) {
        deviceRepository.toggleDevice(deviceId, isOn)
                .thenAccept(updatedDevice -> {
                    // Update the device in the list
                    List<Device> currentDevices = devices.getValue();
                    if (currentDevices != null) {
                        for (int i = 0; i < currentDevices.size(); i++) {
                            if (currentDevices.get(i).getId() == deviceId) {
                                currentDevices.set(i, updatedDevice);
                                break;
                            }
                        }
                        devices.postValue(currentDevices);
                    }
                })
                .exceptionally(throwable -> {
                    errorMessage.postValue("Error toggling device: " + throwable.getMessage());
                    connectionStatus.postValue(false);
                    return null;
                });
    }

    public void turnAllDevices(boolean turnOn) {
        deviceRepository.turnAllDevices(turnOn)
                .thenAccept(result -> {
                    // Обновляем все устройства в списке
                    List<Device> currentDevices = devices.getValue();
                    if (currentDevices != null) {
                        for (Device device : currentDevices) {
                            if (device.getType().equals("lamp") || device.getType().equals("siren") || device.getType().equals("plug")) {
                                device.getState().setOn(turnOn);
                            }
                        }
                        devices.postValue(currentDevices);
                    }
                })
                .exceptionally(throwable -> {
                    errorMessage.postValue("Error turning all devices: " + throwable.getMessage());
                    connectionStatus.postValue(false);
                    return null;
                });
    }

    public void enableSecurityMode() {
        deviceRepository.enableSecurityMode()
                .thenAccept(result -> {
                    // Активируем соответствующие устройства
                    List<Device> currentDevices = devices.getValue();
                    if (currentDevices != null) {
                        for (Device device : currentDevices) {
                            if (device.getType().equals("siren") || device.getType().equals("motion_sensor")) {
                                device.getState().setOn(true);
                            }
                        }
                        devices.postValue(currentDevices);
                    }
                })
                .exceptionally(throwable -> {
                    errorMessage.postValue("Error enabling security mode: " + throwable.getMessage());
                    connectionStatus.postValue(false);
                    return null;
                });
    }

    public void refreshData() {
        loadData();
    }

    // Fallback to demo data if API fails
    private void loadDemoData() {
        List<RoomCreateRequest> demoRooms = new ArrayList<>();
        demoRooms.add(new RoomCreateRequest(1, "Гостиная", 1));
        demoRooms.add(new RoomCreateRequest(2, "Спальня", 1));
        rooms.setValue(demoRooms);

        List<Device> demoDevices = new ArrayList<>();
        demoDevices.add(new Device(1, "Лампа гостиная", "lamp", true, 1));
        demoDevices.add(new Device(2, "Датчик движения", "motion_sensor", true, 1));
        demoDevices.add(new Device(3, "Термостат", "temp_sensor", true, 1));
        demoDevices.add(new Device(4, "Сирена", "siren", true, 1));
        devices.setValue(demoDevices);

        isLoading.setValue(false);
        connectionStatus.setValue(true);
    }
}