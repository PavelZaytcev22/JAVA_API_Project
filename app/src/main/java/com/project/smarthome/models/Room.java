package com.project.smarthome.models;

public class Room {
    private String id;
    private String name;
    private int deviceCount;

    public Room(String id, String name) {
        this.id = id;
        this.name = name;
        this.deviceCount = 0;
    }

    // Getters and setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public int getDeviceCount() { return deviceCount; }
    public void setDeviceCount(int deviceCount) { this.deviceCount = deviceCount; }
}