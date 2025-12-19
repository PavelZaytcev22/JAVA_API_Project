package com.project.smarthome.models.homes;

import androidx.annotation.NonNull;

import com.google.gson.annotations.SerializedName;

public class HomeResponse {

    private int id;
    private String name;

    @SerializedName("owner_id")
    private int ownerId;

    public HomeResponse() {}

    public HomeResponse(int id, String name, int ownerId) {
        this.id = id;
        this.name = name;
        this.ownerId = ownerId;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(int ownerId) {
        this.ownerId = ownerId;
    }

    @NonNull
    @Override
    public String toString() {
        return "HomeResponse{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", ownerId=" + ownerId +
                '}';
    }
}
