package com.project.smarthome.models.homes;

import com.google.gson.annotations.SerializedName;
import com.project.smarthome.api.User;

public class HomeMember {

    @SerializedName("id")
    private int id;

    @SerializedName("user_id")
    private int userId;

    @SerializedName("home_id")
    private int homeId;

    @SerializedName("joined_at")
    private String joinedAt;

    @SerializedName("user")
    private User user;

    public int getId() { return id; }
    public int getUserId() { return userId; }
    public int getHomeId() { return homeId; }
    public String getJoinedAt() { return joinedAt; }
    public User getUser() { return user; }

    public void setId(int id) { this.id = id; }
    public void setUserId(int userId) { this.userId = userId; }
    public void setHomeId(int homeId) { this.homeId = homeId; }
    public void setJoinedAt(String joinedAt) { this.joinedAt = joinedAt; }
    public void setUser(User user) { this.user = user; }
}
