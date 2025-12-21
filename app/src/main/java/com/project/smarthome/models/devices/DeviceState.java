package com.project.smarthome.models.devices;

import com.google.gson.annotations.SerializedName;

public class DeviceState {
    @SerializedName("is_on")
    private boolean isOn;

    @SerializedName("brightness")
    private int brightness;

    @SerializedName("temperature")
    private int temperature;

    @SerializedName("motion")
    private boolean motion;

    // Конструктор
    public DeviceState() {
        this.isOn = false;
        this.brightness = 100;
        this.temperature = 22;
        this.motion = false;
    }

    // Getters and setters
    public boolean isOn() { return isOn; }
    public void setOn(boolean on) { isOn = on; }

    public int getBrightness() { return brightness; }
    public void setBrightness(int brightness) { this.brightness = brightness; }

    public int getTemperature() { return temperature; }
    public void setTemperature(int temperature) { this.temperature = temperature; }

    // Для motion - оба варианта для совместимости
    public boolean isMotion() { return motion; }
    public void setMotion(boolean motion) { this.motion = motion; }

    // Альтернативные методы с понятными названиями
    public boolean isMotionDetected() { return motion; }
    public void setMotionDetected(boolean motionDetected) { this.motion = motionDetected; }
}