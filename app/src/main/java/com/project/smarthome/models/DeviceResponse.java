package com.project.smarthome.models;

public class DeviceResponse {
    private int id;
    private String name;
    private String type;
    private int room_id;
    private String state;
    private String last_update;
    private int home_id;

    public int getId() {
        return id;
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

    public String getLastUpdate() {
        return last_update;
    }

    public int getHomeId() {
        return home_id;
    }
}
