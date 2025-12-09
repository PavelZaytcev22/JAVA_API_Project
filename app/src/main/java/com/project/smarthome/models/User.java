package com.project.smarthome.models;
import com.google.gson.annotations.SerializedName;
public class User {
    @SerializedName("id")
    public int id;

    @SerializedName("username")
    public String username;

    @SerializedName("email")
    public String email;
}


