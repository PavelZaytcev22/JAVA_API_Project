package com.project.smarthome.viewmodels;

import android.app.Application;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.project.smarthome.models.Device;
import com.project.smarthome.models.Home;
import com.project.smarthome.models.Room;
import com.project.smarthome.repositories.DeviceRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class HomeViewModel extends AndroidViewModel {
    private final DeviceRepository deviceRepository;
    private final MutableLiveData<List<Device>> devices = new MutableLiveData<>();
    private final MutableLiveData<List<Room>> rooms = new MutableLiveData<>();
    private final MutableLiveData<List<Home>> homes = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();

    public HomeViewModel(Application application) {
        super(application);
        this.deviceRepository = new DeviceRepository(application);
    }

    public LiveData<List<Device>> getDevices() { return devices; }
    public LiveData<List<Room>> getRooms() { return rooms; }
    public LiveData<List<Home>> getHomes() { return homes; }
    public LiveData<Boolean> getIsLoading() { return isLoading; }
    public LiveData<String> getErrorMessage() { return errorMessage; }

    public void loadHomes() {
        isLoading.setValue(true);

        deviceRepository.getHomes()
                .thenAccept(homesList -> {
                    homes.postValue(homesList);
                    if (!homesList.isEmpty()) {
                        loadRooms(homesList.get(0).getId());
                    } else {
                        isLoading.postValue(false);
                    }
                })
                .exceptionally(throwable -> {
                    errorMessage.postValue("Ошибка загрузки домов: " + throwable.getMessage());
                    isLoading.postValue(false);
                    return null;
                });
    }

    public void loadRooms(int homeId) {
        deviceRepository.getRooms(homeId)
                .thenAccept(roomsList -> {
                    rooms.postValue(roomsList);
                    if (!roomsList.isEmpty()) {
                        loadDevices(roomsList.get(0).getId());
                    } else {
                        isLoading.postValue(false);
                    }
                })
                .exceptionally(throwable -> {
                    errorMessage.postValue("Ошибка загрузки комнат: " + throwable.getMessage());
                    isLoading.postValue(false);
                    return null;
                });
    }

    public void loadDevices(int roomId) {
        deviceRepository.getDevices(roomId)
                .thenAccept(devicesList -> {
                    devices.postValue(devicesList);
                    isLoading.postValue(false);
                })
                .exceptionally(throwable -> {
                    errorMessage.postValue("Ошибка загрузки устройств: " + throwable.getMessage());
                    isLoading.postValue(false);
                    return null;
                });
    }

    public void toggleDevice(int deviceId, boolean isOn) {
        deviceRepository.toggleDevice(deviceId, isOn)
                .thenAccept(device -> {
                    // Обновляем устройство в списке
                    List<Device> currentDevices = devices.getValue();
                    if (currentDevices != null) {
                        for (int i = 0; i < currentDevices.size(); i++) {
                            if (currentDevices.get(i).getId() == deviceId) {
                                currentDevices.set(i, device);
                                break;
                            }
                        }
                        devices.postValue(currentDevices);
                    }
                })
                .exceptionally(throwable -> {
                    errorMessage.postValue("Ошибка переключения устройства: " + throwable.getMessage());
                    return null;
                });
    }

    public void refreshData() {
        loadHomes();
    }
}