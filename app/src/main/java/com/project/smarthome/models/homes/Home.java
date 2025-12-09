package com.project.smarthome.models.homes;

import com.google.gson.annotations.SerializedName;
import com.project.smarthome.models.homes.HomeMember;
import java.util.List;

public class Home {

    @SerializedName("id")
    private int id;

    @SerializedName("name")
    private String name;

    @SerializedName("owner_id")
    private int ownerId;

    @SerializedName("created_at")
    private String createdAt;

    @SerializedName("members")
    private List<HomeMember> members;

    public Home() {}

    public Home(int id, String name, int ownerId, String createdAt, List<HomeMember> members) {
        this.id = id;
        this.name = name;
        this.ownerId = ownerId;
        this.createdAt = createdAt;
        this.members = members;
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public int getOwnerId() { return ownerId; }
    public String getCreatedAt() { return createdAt; }
    public List<HomeMember> getMembers() { return members; }

    public void setId(int id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setOwnerId(int ownerId) { this.ownerId = ownerId; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
    public void setMembers(List<HomeMember> members) { this.members = members; }
}
