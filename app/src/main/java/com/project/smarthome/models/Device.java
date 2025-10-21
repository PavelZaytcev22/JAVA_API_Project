package com.project.smarthome.models;

public class Device {
    private int id;
    private String name;
    private String type;
    private boolean is_online;
    private int room_id;
    private DeviceState state;

    public Device() {
        this.state = new DeviceState();
    }

    public Device(int id, String name, String type, boolean is_online, int room_id) {
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

