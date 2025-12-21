package com.project.smarthome.models.homes.room;

import com.google.gson.annotations.SerializedName;

public class RoomResponse {

    private int id;
    private String name;

    @SerializedName("home_id")
    private int homeId;

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getHomeId() {
        return homeId;
    }
}
