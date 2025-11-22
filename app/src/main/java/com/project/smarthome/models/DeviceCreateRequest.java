package com.project.smarthome.models;

public class DeviceCreateRequest {
    private String name;
    private String type;
    private int room_id;
    private String state;

    public DeviceCreateRequest(String name, String type, int room_id, String state) {
        this.name = name;
        this.type = type;
        this.room_id = room_id;
        this.state = state;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public int getRoomId() {
        return room_id;
    }

    public String getState() {
        return state;
    }
}
