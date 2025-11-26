package com.project.smarthome.models;

public class FamilyMember {
    private int id;
    private String username;
    private String full_name;
    private String role;

    public FamilyMember() {}

    public FamilyMember(int id, String username, String full_name, String role) {
        this.id = id;
        this.username = username;
        this.full_name = full_name;
        this.role = role;
    }

    // Getters and setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getFullName() { return full_name; }
    public void setFullName(String full_name) { this.full_name = full_name; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
}