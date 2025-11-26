package com.project.smarthome.models;

public class RegisterResponse {
    private int id;
    private String username;
    private String email;
    private String full_name;

    // Getters and setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getFullName() { return full_name; }
    public void setFullName(String full_name) { this.full_name = full_name; }
}