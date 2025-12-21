package com.project.smarthome.models.families;

import com.google.gson.annotations.SerializedName;

public class FamilyMember {

    @SerializedName("id")
    private int id;

    @SerializedName("username")
    private String username;

    @SerializedName("full_name")
    private String fullName;

    @SerializedName("role")
    private String role;

    public FamilyMember() {}

    public FamilyMember(int id, String username, String fullName, String role) {
        this.id = id;
        this.username = username;
        this.fullName = fullName;
        this.role = role;
    }

    // Getters and setters

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
}
