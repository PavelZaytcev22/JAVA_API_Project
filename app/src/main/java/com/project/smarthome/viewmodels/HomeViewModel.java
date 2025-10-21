package com.project.smarthome.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.project.smarthome.models.Device;
import com.project.smarthome.models.Room;
import com.project.smarthome.repositories.DeviceRepository;
import java.util.List;

public class HomeViewModel extends ViewModel {

    private final DeviceRepository deviceRepository;
    private final MutableLiveData<List<Device>> devices = new MutableLiveData<>();
    private final MutableLiveData<List<Room>> rooms = new MutableLiveData<>();
    private final MutableLiveData<Boolean> connectionStatus = new MutableLiveData<>(true);

    public HomeViewModel() {
        this.deviceRepository = new DeviceRepository();
    }

    public LiveData<List<Device>> getDevices() {
        return devices;
    }

    public LiveData<List<Room>> getRooms() {
        return rooms;
    }

    public LiveData<Boolean> getConnectionStatus() {
        return connectionStatus;
    }

    public void loadDevices() {
        // TODO: Заменить на реальный API call
        List<Device> demoDevices = createDemoDevices();
        devices.setValue(demoDevices);
    }

    public void loadRooms() {
        // TODO: Заменить на реальный API call
        List<Room> demoRooms = createDemoRooms();
        rooms.setValue(demoRooms);
    }

    public void toggleDevice(String deviceId, boolean isOn) {
        deviceRepository.toggleDevice(deviceId, isOn)
                .addOnSuccessListener(result -> {
                    // Успешно - обновляем локальное состояние
                    updateDeviceState(deviceId, isOn);
                })
                .addOnFailureListener(error -> {
                    // Ошибка - показываем уведомление
                    connectionStatus.setValue(false);
                });
    }

    public void turnAllDevices(boolean turnOn) {
        deviceRepository.turnAllDevices(turnOn)
                .addOnSuccessListener(result -> {
                    // Успешно - обновляем все устройства
                    updateAllDevicesState(turnOn);
                });
    }

    public void enableSecurityMode() {
        deviceRepository.enableSecurityMode()
                .addOnSuccessListener(result -> {
                    // Активируем соответствующие устройства
                });
    }

    private void updateDeviceState(String deviceId, boolean isOn) {
        List<Device> currentDevices = devices.getValue();
        if (currentDevices != null) {
            for (Device device : currentDevices) {
                if (device.getId().equals(deviceId)) {
                    device.getState().setOn(isOn);
                    break;
                }
            }
            devices.setValue(currentDevices);
        }
    }

    private void updateAllDevicesState(boolean turnOn) {
        List<Device> currentDevices = devices.getValue();
        if (currentDevices != null) {
            for (Device device : currentDevices) {
                if (device.getType().equals("lamp") || device.getType().equals("siren")) {
                    device.getState().setOn(turnOn);
                }
            }
            devices.setValue(currentDevices);
        }
    }

    private List<Device> createDemoDevices() {
        // Возвращаем демо-устройства (как в предыдущем примере)
        // ...
    }

    private List<Room> createDemoRooms() {
        List<Room> rooms = new ArrayList<>();
        rooms.add(new Room("1", "Гостиная"));
        rooms.add(new Room("2", "Спальня"));
        rooms.add(new Room("3", "Кухня"));
        rooms.add(new Room("4", "Прихожая"));
        return rooms;
    }
}