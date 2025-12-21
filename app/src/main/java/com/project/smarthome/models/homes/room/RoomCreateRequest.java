package com.project.smarthome.models.homes.room;

public class RoomCreateRequest {

    private String name;

    public RoomCreateRequest(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
