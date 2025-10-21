package com.project.smarthome.models;

public class Device {
    private String id;
    private String name;
    private String type; // "lamp", "motion_sensor", "temp_sensor", "siren"
    private boolean isOnline;
    private DeviceState state;

    // Конструкторы
    public Device() {}

    public Device(String id, String name, String type, boolean isOnline) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.isOnline = isOnline;
        this.state = new DeviceState();
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public boolean isOnline() { return isOnline; }
    public void setOnline(boolean online) { isOnline = online; }

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