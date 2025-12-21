package com.project.smarthome.models.devices;

public class DeviceCreateRequest {
    private String name;
    private String type;
    private Integer room_id;
    private String state;
    public void HomeCreateRequest(String name) {
        this.name = name;
    }
    public DeviceCreateRequest(String name, String type, Integer room_id, String state) {
        this.name = name;
        this.type = type;
        this.room_id = room_id;
        this.state = state;
    }

    public String getName() { return name; }

    public void setName(String name) { this.name = name; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public Integer getRoomId() { return room_id; }
    public void setRoomId(Integer room_id) { this.room_id = room_id; }

    public String getState() { return state; }
    public void setState(String state) { this.state = state; }
}