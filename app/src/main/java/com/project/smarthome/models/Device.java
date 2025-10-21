package com.project.smarthome.models;

public class Device {public class Device {
    private String id;
    private String name;
    private String type;
    private boolean is_online;
    private int room_id;
    private DeviceState state;

    public Device() {
        this.state = new DeviceState();
    }

    public Device(String id, String name, String type, boolean is_online, int room_id) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.is_online = is_online;
        this.room_id = room_id;
        this.state = new DeviceState();
    }

    // Getters and setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public boolean isOnline() { return is_online; }
    public void setOnline(boolean online) { is_online = online; }

    public int getRoomId() { return room_id; }
    public void setRoomId(int room_id) { this.room_id = room_id; }

    public DeviceState getState() { return state; }
    public void setState(DeviceState state) { this.state = state; }
}

// Класс для хранения состояния устройства
class DeviceState {
    private boolean on;
    private int brightness; // 0-100%
    private int temperature; // для датчиков температуры
    private boolean motion; // для датчиков движения

    public DeviceState() {
        this.on = false;
        this.brightness = 100;
        this.temperature = 22;
        this.motion = false;
    }

    // Getters and Setters
    public boolean isOn() { return on; }
    public void setOn(boolean on) { this.on = on; }

    public int getBrightness() { return brightness; }
    public void setBrightness(int brightness) { this.brightness = brightness; }

    public int getTemperature() { return temperature; }
    public void setTemperature(int temperature) { this.temperature = temperature; }

    public boolean isMotion() { return motion; }
    public void setMotion(boolean motion) { this.motion = motion; }
}