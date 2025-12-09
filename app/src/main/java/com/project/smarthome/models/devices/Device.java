package com.project.smarthome.models.devices;

import java.util.Map;

public class Device {
    private int id;
    private String name;
    private String type;
    private Integer room_id; // Может быть null поэтому Integer
    private int home_id;
    private String state;
    private Map<String, Object> properties;

    public Device() {}

    public Device(int id, String name, String type, Integer room_id, int home_id, String state) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.room_id = room_id;
        this.home_id = home_id;
        this.state = state;
    }

    // Getters and setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public Integer getRoomId() { return room_id; }
    public void setRoomId(Integer room_id) { this.room_id = room_id; }

    public int getHomeId() { return home_id; }
    public void setHomeId(int home_id) { this.home_id = home_id; }

    public String getState() { return state; }
    public void setState(String state) { this.state = state; }

    public Map<String, Object> getProperties() { return properties; }
    public void setProperties(Map<String, Object> properties) { this.properties = properties; }
}